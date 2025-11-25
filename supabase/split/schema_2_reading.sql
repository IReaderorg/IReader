-- ============================================================================
-- PROJECT 2: READING DATABASE SCHEMA
-- ============================================================================
-- This schema contains ONLY reading progress tracking
-- Tables: reading_progress
-- Estimated storage: ~500MB for 2.5M+ reading sessions
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Reading Progress Table
-- Stores current reading position for each book
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,  -- Stored as TEXT (from Project 1)
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_user_book UNIQUE(user_id, book_id),
    CONSTRAINT scroll_position_range CHECK (last_scroll_position >= 0 AND last_scroll_position <= 1),
    CONSTRAINT book_id_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT chapter_slug_not_empty CHECK (LENGTH(last_chapter_slug) > 0),
    CONSTRAINT user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_book ON public.reading_progress(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_book_id ON public.reading_progress(book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_updated_at ON public.reading_progress(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_reading_progress_created_at ON public.reading_progress(created_at DESC);

-- Comments
COMMENT ON TABLE public.reading_progress IS 'Current reading position for each book (Project 2 - Reading)';
COMMENT ON COLUMN public.reading_progress.user_id IS 'User ID from Project 1 (stored as TEXT)';
COMMENT ON COLUMN public.reading_progress.book_id IS 'Composite book identifier (sourceId-bookId)';
COMMENT ON COLUMN public.reading_progress.last_chapter_slug IS 'Key/slug of the last read chapter';
COMMENT ON COLUMN public.reading_progress.last_scroll_position IS 'Scroll position in chapter (0.0 to 1.0)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;

-- Users can view their own reading progress
CREATE POLICY "Users can view their own reading progress"
    ON public.reading_progress FOR SELECT
    USING (user_id = auth.uid()::TEXT);

-- Users can insert their own reading progress
CREATE POLICY "Users can insert their own reading progress"
    ON public.reading_progress FOR INSERT
    WITH CHECK (user_id = auth.uid()::TEXT);

-- Users can update their own reading progress
CREATE POLICY "Users can update their own reading progress"
    ON public.reading_progress FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- Users can delete their own reading progress
CREATE POLICY "Users can delete their own reading progress"
    ON public.reading_progress FOR DELETE
    USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- Update timestamp function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS update_reading_progress_updated_at ON public.reading_progress;
CREATE TRIGGER update_reading_progress_updated_at
    BEFORE UPDATE ON public.reading_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 2 (READING) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Table created:';
    RAISE NOTICE '- reading_progress: Current reading positions';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Track reading position per book';
    RAISE NOTICE '- Scroll position tracking (0.0 to 1.0)';
    RAISE NOTICE '- Automatic timestamp updates';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~2.5M reading sessions in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id is stored as TEXT (from Project 1)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 2 URL and Anon Key';
    RAISE NOTICE '2. Create Project 3 for library (schema_3_library.sql)';
END $$;
