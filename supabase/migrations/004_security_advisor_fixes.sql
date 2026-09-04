-- ============================================================================
-- Migration 004: Supabase Security Advisor Fixes (for Existing Database)
-- 
-- Resolves all critical and warning security advisor findings:
-- 1. [CRITICAL] RLS Disabled in Public: Table public.schema_version
-- 2. [CRITICAL] Security Definer Views: 
--      - public.user_reading_summary
--      - public.recent_activity
--      - public.leaderboard_with_rank
--      - public.book_reviews_with_badges
--      - public.chapter_reviews_with_badges
--      - public.book_ratings_summary (split schemas)
--      - public.chapter_ratings_summary (split schemas)
-- 3. [SECURITY] Function Search Path Mutable:
--      Hardens all SECURITY DEFINER functions with explicit search_path = public
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Enable Row Level Security on all public tables missing RLS
-- ----------------------------------------------------------------------------
DO $$
DECLARE
    t RECORD;
BEGIN
    -- Enable RLS on schema_version specifically if it exists
    IF EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'public' AND tablename = 'schema_version'
    ) THEN
        ALTER TABLE public.schema_version ENABLE ROW LEVEL SECURITY;
        
        -- Create read policy for schema_version if missing
        IF NOT EXISTS (
            SELECT 1 FROM pg_policies 
            WHERE schemaname = 'public' 
              AND tablename = 'schema_version' 
              AND policyname = 'Allow read access to schema_version'
        ) THEN
            CREATE POLICY "Allow read access to schema_version" 
                ON public.schema_version FOR SELECT 
                USING (true);
        END IF;
    END IF;

    -- Also scan and enable RLS on ANY other table in public that has RLS disabled
    FOR t IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
          AND rowsecurity = false
    LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t.tablename);
        RAISE NOTICE 'Enabled RLS on public.%', t.tablename;
    END LOOP;
END $$;


-- ----------------------------------------------------------------------------
-- 2. Set security_invoker = true on all views in public schema
-- 
-- In PostgreSQL 15+, views created without WITH (security_invoker = true) default
-- to running with the view owner's privileges (SECURITY DEFINER). This bypasses
-- Row Level Security (RLS) on underlying tables and can expose private rows.
-- ----------------------------------------------------------------------------
DO $$
DECLARE
    v RECORD;
BEGIN
    FOR v IN 
        SELECT c.relname AS view_name
        FROM pg_class c
        JOIN pg_namespace n ON c.relnamespace = n.oid
        WHERE n.nspname = 'public' 
          AND c.relkind = 'v'
    LOOP
        BEGIN
            EXECUTE format('ALTER VIEW public.%I SET (security_invoker = true)', v.view_name);
            RAISE NOTICE 'Applied security_invoker = true on public.%', v.view_name;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not set security_invoker on public.%: %', v.view_name, SQLERRM;
        END;
    END LOOP;
END $$;


-- ----------------------------------------------------------------------------
-- 3. Harden search_path on all SECURITY DEFINER functions in public
-- 
-- Functions with SECURITY DEFINER that do not have a fixed search_path are vulnerable
-- to search-path hijacking attacks. Setting search_path = public satisfies the
-- Supabase Security Advisor check.
-- ----------------------------------------------------------------------------
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN 
        SELECT p.oid::regprocedure AS func_signature
        FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
        WHERE n.nspname = 'public'
          AND p.prosecdef = true
    LOOP
        BEGIN
            EXECUTE format('ALTER FUNCTION %s SET search_path = public', r.func_signature);
            RAISE NOTICE 'Hardened search_path on %', r.func_signature;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not set search_path on %: %', r.func_signature, SQLERRM;
        END;
    END LOOP;
END $$;


-- ----------------------------------------------------------------------------
-- 4. Record migration in schema_version
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'schema_version') THEN
        INSERT INTO public.schema_version (version, description)
        VALUES (4, 'Fix Supabase Security Advisor warnings (RLS on schema_version, security_invoker on views, hardened search_path)')
        ON CONFLICT (version) DO UPDATE 
        SET description = EXCLUDED.description, applied_at = NOW();
    END IF;
END $$;
