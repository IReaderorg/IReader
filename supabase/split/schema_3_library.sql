-- ============================================================================
-- PROJECT 3: LIBRARY DATABASE SCHEMA
-- ============================================================================
-- This schema contains ONLY synced books library
-- Tables: synced_books
-- Estimated storage: ~500MB for 5M+ synced books
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Synced Books Table
-- Stores favorite books with essential metadata
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.synced_books (
    user_id TEXT NOT NULL,  -- Stored as TEXT (from Project 1)
    book_id TEXT NOT NULL,
    source_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    book_url TEXT NOT NULL,
    last_read BIGINT NOT NULL DEFAULT 0,
    cover_url TEXT DEFAULT '',
    source_name TEXT DEFAULT '',
    author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    genres TEXT DEFAULT '',
    status BIGINT DEFAULT 0,
    favorite BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    PRIMARY KEY (user_id, book_id),
    CONSTRAINT book_id_synced_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT book_url_not_empty CHECK (LENGTH(book_url) > 0),
    CONSTRAINT last_read_non_negative CHECK (last_read >= 0),
    CONSTRAINT user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_book_id ON public.synced_books(book_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_source_id ON public.synced_books(source_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_last_read ON public.synced_books(user_id, last_read DESC);
CREATE INDEX IF NOT EXISTS idx_synced_books_title ON public.synced_books(title);
CREATE INDEX IF NOT EXISTS idx_synced_books_created_at ON public.synced_books(created_at DESC);

-- Comments
COMMENT ON TABLE public.synced_books IS 'Favorite books library (Project 3 - Library)';
COMMENT ON COLUMN public.synced_books.user_id IS 'User ID from Project 1 (stored as TEXT)';
COMMENT ON COLUMN public.synced_books.book_id IS 'Composite book identifier (sourceId-bookId)';
COMMENT ON COLUMN public.synced_books.source_id IS 'Source/catalog ID where book is from';
COMMENT ON COLUMN public.synced_books.title IS 'Book title';
COMMENT ON COLUMN public.synced_books.book_url IS 'Book URL/link on the source';
COMMENT ON COLUMN public.synced_books.last_read IS 'Last read timestamp (milliseconds)';
COMMENT ON COLUMN public.synced_books.cover_url IS 'Cover image URL for the book';
COMMENT ON COLUMN public.synced_books.source_name IS 'Human-readable source name';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;

-- Users can view their own synced books
CREATE POLICY "Users can view their own synced books"
    ON public.synced_books FOR SELECT
    USING (user_id = auth.uid()::TEXT);

-- Users can insert their own synced books
CREATE POLICY "Users can insert their own synced books"
    ON public.synced_books FOR INSERT
    WITH CHECK (user_id = auth.uid()::TEXT);

-- Users can update their own synced books
CREATE POLICY "Users can update their own synced books"
    ON public.synced_books FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- Users can delete their own synced books
CREATE POLICY "Users can delete their own synced books"
    ON public.synced_books FOR DELETE
    USING (user_id = auth.uid()::TEXT);

-- ----------------------------------------------------------------------------
-- Sync Manifest Table (Document Store)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.sync_manifest (
    user_id TEXT NOT NULL PRIMARY KEY,
    manifest JSONB NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE public.sync_manifest ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own sync manifest"
    ON public.sync_manifest FOR ALL
    USING (user_id = auth.uid()::TEXT OR auth.uid() IS NULL)
    WITH CHECK (user_id = auth.uid()::TEXT OR auth.uid() IS NULL);

CREATE INDEX IF NOT EXISTS idx_sync_manifest_gin 
    ON public.sync_manifest USING GIN (manifest jsonb_path_ops);

-- ----------------------------------------------------------------------------
-- Synced Chapters Table (Relational Store)
-- Option A (Default): Metadata Only (zero chapter content stored, free-tier friendly)
-- Option B: With Chapter Content (for self-hosted PostgreSQL/Supabase instances)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.synced_chapters (
    user_id        TEXT NOT NULL,
    chapter_id     TEXT NOT NULL,
    book_id        TEXT NOT NULL,
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
    -- Option B: Stores full novel chapter body/text for offline self-hosted backup.
    -- If using Option A (lightweight), this column consumes 0 bytes when empty.
    content        TEXT DEFAULT '',
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, chapter_id)
);

CREATE INDEX IF NOT EXISTS idx_synced_chapters_user_id ON public.synced_chapters(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_book_id ON public.synced_chapters(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_read ON public.synced_chapters(user_id, read);
CREATE INDEX IF NOT EXISTS idx_synced_chapters_bookmark ON public.synced_chapters(user_id, bookmark);

ALTER TABLE public.synced_chapters ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own synced chapters"
    ON public.synced_chapters FOR ALL
    USING (user_id = auth.uid()::TEXT OR auth.uid() IS NULL)
    WITH CHECK (user_id = auth.uid()::TEXT OR auth.uid() IS NULL);

-- ----------------------------------------------------------------------------
-- Synced Chapters Dynamic View (Unpacked from JSONB Manifest)
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

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 3 (LIBRARY) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Table created:';
    RAISE NOTICE '- synced_books: Favorite books library';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Store favorite books';
    RAISE NOTICE '- Track last read timestamp';
    RAISE NOTICE '- Book URL and metadata';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~5M synced books in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id is stored as TEXT (from Project 1)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 3 URL and Anon Key';
    RAISE NOTICE '2. Create Project 4 for book reviews (schema_4_book_reviews.sql)';
END $$;
