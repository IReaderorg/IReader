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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
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
