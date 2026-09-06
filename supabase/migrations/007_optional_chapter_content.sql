-- ============================================================================
-- Migration 007: Optional Chapter Text Content Storage (Self-Hosted Support)
-- ============================================================================
-- Additive, idempotent migration. Safe to run on top of migrations 001-006.
--
-- Purpose:
-- Enables storing full downloaded chapter bodies/texts in Supabase for users
-- self-hosting their database (e.g., TrueNAS, VPS, Docker, or unlimited storage).
-- For users on free cloud tiers (500MB limits), keeping chapter content disabled
-- in app settings avoids filling storage.
--
-- Key Changes:
-- 1. Adds 'content' TEXT column to public.synced_chapters.
-- 2. Updates public.synced_chapters_view to expose 'content' from the sync_manifest.
-- 3. Records version 7 in public.schema_version.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Add content column to public.synced_chapters (Idempotent)
-- ----------------------------------------------------------------------------
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'synced_chapters' 
          AND column_name = 'content'
    ) THEN
        ALTER TABLE public.synced_chapters ADD COLUMN content TEXT DEFAULT '';
        COMMENT ON COLUMN public.synced_chapters.content IS 'Full serialized chapter page/text content for self-hosted backup';
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 2. Update public.synced_chapters_view to include content
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.synced_chapters_view 
WITH (security_invoker = true) AS
SELECT 
    sm.user_id,
    ch->>'globalId' AS chapter_id,
    ch->>'bookGlobalId' AS book_id,
    ch->>'key' AS chapter_key,
    ch->>'name' AS name,
    COALESCE((ch->>'number')::numeric, 0) AS chapter_number,
    COALESCE((ch->>'sourceOrder')::bigint, 0) AS source_order,
    COALESCE((ch->>'read')::boolean, false) AS read,
    COALESCE((ch->>'bookmark')::boolean, false) AS bookmark,
    COALESCE((ch->>'lastPageRead')::bigint, 0) AS last_page_read,
    COALESCE((ch->>'dateUpload')::bigint, 0) AS date_upload,
    COALESCE((ch->>'dateFetch')::bigint, 0) AS date_fetch,
    COALESCE(ch->>'translator', '') AS translator,
    COALESCE(ch->>'content', '') AS content,
    sm.updated_at
FROM public.sync_manifest sm,
LATERAL jsonb_array_elements(sm.manifest->'chapters') AS ch;

-- ----------------------------------------------------------------------------
-- 3. Record migration in schema_version (if table exists)
-- ----------------------------------------------------------------------------
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'schema_version') THEN
        INSERT INTO public.schema_version (version, description)
        VALUES (7, 'Add optional chapter text content column to synced_chapters and synced_chapters_view')
        ON CONFLICT (version) DO UPDATE 
        SET description = EXCLUDED.description, applied_at = NOW();
    END IF;
END $$;
