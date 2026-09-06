-- ============================================================================
-- Migration 006: Chapter Sync & Manifest Performance Enhancements
-- ============================================================================
-- Additive, idempotent migration. Safe to run on top of schema.sql and migrations 001-005.
--
-- Key Improvements:
-- 1. Adds public.synced_chapters (Relational Table) for storing chapter metadata
--    (name, number, order, read status, bookmark, lastPageRead, scanlator) without content.
-- 2. Creates public.synced_chapters_view to dynamically query chapters unpacked
--    from the full-fidelity public.sync_manifest JSONB document store.
-- 3. Adds GIN index on public.sync_manifest for fast JSONB querying.
-- 4. Enables RLS with permissive policies for personal Supabase cloud sync.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Synced Chapters (Relational Table - Metadata Only, Zero Chapter Content)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.synced_chapters (
    user_id        TEXT NOT NULL,
    chapter_id     TEXT NOT NULL, -- Global ID (sourceId|chapterUrl)
    book_id        TEXT NOT NULL, -- Book Global ID (sourceId|bookUrl)
    chapter_key    TEXT NOT NULL,
    name           TEXT NOT NULL,
    chapter_number REAL DEFAULT 0,
    source_order   BIGINT DEFAULT 0,
    read           BOOLEAN DEFAULT false,
    bookmark       BOOLEAN DEFAULT false,
    last_page_read BIGINT DEFAULT 0,
    date_upload    BIGINT DEFAULT 0,
    date_fetch     BIGINT DEFAULT 0,
    translator     TEXT DEFAULT '',
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, chapter_id)
);

-- Indexes for fast filtering and joins
CREATE INDEX IF NOT EXISTS idx_synced_chapters_user_id ON public.synced_chapters(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_book_id ON public.synced_chapters(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_read ON public.synced_chapters(user_id, read);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_bookmark ON public.synced_chapters(user_id, bookmark);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_order ON public.synced_chapters(user_id, book_id, source_order ASC);

-- Row Level Security
ALTER TABLE public.synced_chapters ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE schemaname = 'public' 
          AND tablename = 'synced_chapters' 
          AND policyname = 'Allow public synced_chapters access'
    ) THEN
        CREATE POLICY "Allow public synced_chapters access" 
            ON public.synced_chapters FOR ALL 
            USING (true) 
            WITH CHECK (true);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 2. Manifest Performance Index (JSONB GIN)
-- ----------------------------------------------------------------------------
-- Optimizes queries filtering or projecting jsonb keys inside sync_manifest
CREATE INDEX IF NOT EXISTS idx_sync_manifest_gin 
    ON public.sync_manifest USING GIN (manifest jsonb_path_ops);

-- ----------------------------------------------------------------------------
-- 3. Dynamic Chapter View from Sync Manifest
-- ----------------------------------------------------------------------------
-- Allows querying individual chapters directly from the JSON document store
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
    sm.updated_at
FROM public.sync_manifest sm,
LATERAL jsonb_array_elements(sm.manifest->'chapters') AS ch;

-- ----------------------------------------------------------------------------
-- 4. Record migration in schema_version (if table exists)
-- ----------------------------------------------------------------------------
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'schema_version') THEN
        INSERT INTO public.schema_version (version, description)
        VALUES (6, 'Add synced_chapters table, synced_chapters_view from manifest, and JSONB GIN index on sync_manifest')
        ON CONFLICT (version) DO UPDATE 
        SET description = EXCLUDED.description, applied_at = NOW();
    END IF;
END $$;
