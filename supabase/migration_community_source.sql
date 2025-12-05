-- Community Source Database Migration
-- This migration adds tables for the Community Source feature
-- Run this in your Supabase SQL Editor to enable community translations

-- ============================================================================
-- COMMUNITY BOOKS TABLE
-- Stores books contributed by the community
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.community_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    cover TEXT DEFAULT '',
    genres TEXT[] DEFAULT '{}',
    status TEXT DEFAULT 'Ongoing',
    original_language TEXT DEFAULT 'en',
    available_languages TEXT[] DEFAULT '{}',
    contributor_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    contributor_name TEXT DEFAULT '',
    view_count BIGINT DEFAULT 0,
    chapter_count INTEGER DEFAULT 0,
    last_updated BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    is_nsfw BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT status_valid CHECK (status IN ('Ongoing', 'Completed', 'Hiatus', 'Dropped')),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0),
    CONSTRAINT chapter_count_non_negative CHECK (chapter_count >= 0)
);

-- Indexes for community_books
CREATE INDEX IF NOT EXISTS idx_community_books_title ON public.community_books(title);
CREATE INDEX IF NOT EXISTS idx_community_books_author ON public.community_books(author);
CREATE INDEX IF NOT EXISTS idx_community_books_contributor ON public.community_books(contributor_id);
CREATE INDEX IF NOT EXISTS idx_community_books_status ON public.community_books(status);
CREATE INDEX IF NOT EXISTS idx_community_books_view_count ON public.community_books(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_created_at ON public.community_books(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_last_updated ON public.community_books(last_updated DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_languages ON public.community_books USING GIN(available_languages);
CREATE INDEX IF NOT EXISTS idx_community_books_genres ON public.community_books USING GIN(genres);

-- Comments
COMMENT ON TABLE public.community_books IS 'Books contributed by the community for translation sharing';
COMMENT ON COLUMN public.community_books.id IS 'Unique book identifier';
COMMENT ON COLUMN public.community_books.title IS 'Book title';
COMMENT ON COLUMN public.community_books.author IS 'Book author';
COMMENT ON COLUMN public.community_books.description IS 'Book description/synopsis';
COMMENT ON COLUMN public.community_books.cover IS 'Cover image URL';
COMMENT ON COLUMN public.community_books.genres IS 'Array of genre tags';
COMMENT ON COLUMN public.community_books.status IS 'Publication status (Ongoing, Completed, Hiatus, Dropped)';
COMMENT ON COLUMN public.community_books.original_language IS 'Original language code (e.g., en, ja, ko)';
COMMENT ON COLUMN public.community_books.available_languages IS 'Array of available translation languages';
COMMENT ON COLUMN public.community_books.contributor_id IS 'User who contributed this book';
COMMENT ON COLUMN public.community_books.contributor_name IS 'Display name of contributor';
COMMENT ON COLUMN public.community_books.view_count IS 'Total view count';
COMMENT ON COLUMN public.community_books.chapter_count IS 'Number of chapters available';
COMMENT ON COLUMN public.community_books.last_updated IS 'Last update timestamp (milliseconds)';
COMMENT ON COLUMN public.community_books.created_at IS 'Creation timestamp (milliseconds)';
COMMENT ON COLUMN public.community_books.is_nsfw IS 'Whether book contains adult content';
COMMENT ON COLUMN public.community_books.is_approved IS 'Whether book is approved for public viewing';

-- ============================================================================
-- COMMUNITY CHAPTERS TABLE
-- Stores translated chapters contributed by the community
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.community_chapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES public.community_books(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    number REAL DEFAULT -1,
    content TEXT NOT NULL,
    language TEXT NOT NULL,
    translator_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    translator_name TEXT DEFAULT '',
    original_chapter_key TEXT DEFAULT '',
    rating REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    updated_at BIGINT DEFAULT 0,
    is_approved BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT chapter_content_not_empty CHECK (LENGTH(content) > 0),
    CONSTRAINT language_not_empty CHECK (LENGTH(language) > 0),
    CONSTRAINT rating_range CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT rating_count_non_negative CHECK (rating_count >= 0),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0)
);

-- Indexes for community_chapters
CREATE INDEX IF NOT EXISTS idx_community_chapters_book_id ON public.community_chapters(book_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_language ON public.community_chapters(language);
CREATE INDEX IF NOT EXISTS idx_community_chapters_translator ON public.community_chapters(translator_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_number ON public.community_chapters(number);
CREATE INDEX IF NOT EXISTS idx_community_chapters_rating ON public.community_chapters(rating DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_created_at ON public.community_chapters(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_book_lang ON public.community_chapters(book_id, language);

-- Comments
COMMENT ON TABLE public.community_chapters IS 'Translated chapters contributed by the community';
COMMENT ON COLUMN public.community_chapters.id IS 'Unique chapter identifier';
COMMENT ON COLUMN public.community_chapters.book_id IS 'Reference to parent book';
COMMENT ON COLUMN public.community_chapters.name IS 'Chapter name/title';
COMMENT ON COLUMN public.community_chapters.number IS 'Chapter number for ordering';
COMMENT ON COLUMN public.community_chapters.content IS 'Translated chapter content';
COMMENT ON COLUMN public.community_chapters.language IS 'Translation language code';
COMMENT ON COLUMN public.community_chapters.translator_id IS 'User who translated this chapter';
COMMENT ON COLUMN public.community_chapters.translator_name IS 'Display name of translator';
COMMENT ON COLUMN public.community_chapters.original_chapter_key IS 'Reference to original chapter (if applicable)';
COMMENT ON COLUMN public.community_chapters.rating IS 'Average translation quality rating (0-5)';
COMMENT ON COLUMN public.community_chapters.rating_count IS 'Number of ratings received';
COMMENT ON COLUMN public.community_chapters.view_count IS 'Total view count';
COMMENT ON COLUMN public.community_chapters.created_at IS 'Creation timestamp (milliseconds)';
COMMENT ON COLUMN public.community_chapters.updated_at IS 'Last update timestamp (milliseconds)';
COMMENT ON COLUMN public.community_chapters.is_approved IS 'Whether chapter is approved for public viewing';

-- ============================================================================
-- CHAPTER REPORTS TABLE
-- Stores reports for problematic chapters
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.chapter_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    reporter_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    created_at BIGINT DEFAULT 0,
    reviewed_at BIGINT,
    reviewed_by UUID REFERENCES public.users(id),
    
    -- Constraints
    CONSTRAINT reason_not_empty CHECK (LENGTH(reason) > 0),
    CONSTRAINT report_status_valid CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED'))
);

-- Indexes for chapter_reports
CREATE INDEX IF NOT EXISTS idx_chapter_reports_chapter ON public.chapter_reports(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reports_status ON public.chapter_reports(status);
CREATE INDEX IF NOT EXISTS idx_chapter_reports_created_at ON public.chapter_reports(created_at DESC);

-- Comments
COMMENT ON TABLE public.chapter_reports IS 'Reports for problematic community chapters';

-- ============================================================================
-- CHAPTER RATINGS TABLE
-- Stores individual ratings for chapters
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.chapter_ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    created_at BIGINT DEFAULT 0,
    
    -- Constraints
    CONSTRAINT unique_user_chapter_rating UNIQUE(user_id, chapter_id),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5)
);

-- Indexes for chapter_ratings
CREATE INDEX IF NOT EXISTS idx_chapter_ratings_chapter ON public.chapter_ratings(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_ratings_user ON public.chapter_ratings(user_id);

-- Comments
COMMENT ON TABLE public.chapter_ratings IS 'Individual user ratings for community chapters';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

-- Enable RLS on all community tables
ALTER TABLE public.community_books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_chapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_ratings ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- Community Books Policies
-- ----------------------------------------------------------------------------

-- Everyone can view approved community books
CREATE POLICY "Everyone can view approved community books"
    ON public.community_books FOR SELECT
    USING (is_approved = TRUE);

-- Authenticated users can insert their own books
CREATE POLICY "Users can insert their own books"
    ON public.community_books FOR INSERT
    WITH CHECK (auth.uid() = contributor_id);

-- Users can update their own books
CREATE POLICY "Users can update their own books"
    ON public.community_books FOR UPDATE
    USING (auth.uid() = contributor_id);

-- Users can delete their own books
CREATE POLICY "Users can delete their own books"
    ON public.community_books FOR DELETE
    USING (auth.uid() = contributor_id);

-- ----------------------------------------------------------------------------
-- Community Chapters Policies
-- ----------------------------------------------------------------------------

-- Everyone can view approved chapters
CREATE POLICY "Everyone can view approved chapters"
    ON public.community_chapters FOR SELECT
    USING (is_approved = TRUE);

-- Authenticated users can insert chapters
CREATE POLICY "Users can insert chapters"
    ON public.community_chapters FOR INSERT
    WITH CHECK (auth.uid() = translator_id);

-- Users can update their own chapters
CREATE POLICY "Users can update their own chapters"
    ON public.community_chapters FOR UPDATE
    USING (auth.uid() = translator_id);

-- Users can delete their own chapters
CREATE POLICY "Users can delete their own chapters"
    ON public.community_chapters FOR DELETE
    USING (auth.uid() = translator_id);

-- ----------------------------------------------------------------------------
-- Chapter Reports Policies
-- ----------------------------------------------------------------------------

-- Users can view their own reports
CREATE POLICY "Users can view their own reports"
    ON public.chapter_reports FOR SELECT
    USING (auth.uid() = reporter_id);

-- Authenticated users can submit reports
CREATE POLICY "Users can submit reports"
    ON public.chapter_reports FOR INSERT
    WITH CHECK (auth.uid() = reporter_id);

-- ----------------------------------------------------------------------------
-- Chapter Ratings Policies
-- ----------------------------------------------------------------------------

-- Everyone can view ratings
CREATE POLICY "Everyone can view ratings"
    ON public.chapter_ratings FOR SELECT
    USING (true);

-- Authenticated users can rate chapters
CREATE POLICY "Users can rate chapters"
    ON public.chapter_ratings FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own ratings
CREATE POLICY "Users can update their own ratings"
    ON public.chapter_ratings FOR UPDATE
    USING (auth.uid() = user_id);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Function: Rate a chapter
-- Updates the chapter's average rating when a new rating is submitted
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION rate_chapter(
    p_chapter_id UUID,
    p_rating INTEGER
)
RETURNS BOOLEAN AS $$
DECLARE
    v_user_id UUID;
    v_existing_rating INTEGER;
    v_new_avg REAL;
    v_new_count INTEGER;
BEGIN
    v_user_id := auth.uid();
    
    IF v_user_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Check for existing rating
    SELECT rating INTO v_existing_rating
    FROM public.chapter_ratings
    WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    
    IF v_existing_rating IS NOT NULL THEN
        -- Update existing rating
        UPDATE public.chapter_ratings
        SET rating = p_rating
        WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    ELSE
        -- Insert new rating
        INSERT INTO public.chapter_ratings (chapter_id, user_id, rating, created_at)
        VALUES (p_chapter_id, v_user_id, p_rating, EXTRACT(EPOCH FROM NOW()) * 1000);
    END IF;
    
    -- Calculate new average
    SELECT AVG(rating)::REAL, COUNT(*)::INTEGER
    INTO v_new_avg, v_new_count
    FROM public.chapter_ratings
    WHERE chapter_id = p_chapter_id;
    
    -- Update chapter with new average
    UPDATE public.community_chapters
    SET rating = v_new_avg, rating_count = v_new_count
    WHERE id = p_chapter_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION rate_chapter(UUID, INTEGER) IS 'Rate a community chapter and update average rating';

-- ----------------------------------------------------------------------------
-- Function: Increment view count
-- Increments the view count for a book or chapter
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION increment_book_view(p_book_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE public.community_books
    SET view_count = view_count + 1
    WHERE id = p_book_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION increment_chapter_view(p_chapter_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE public.community_chapters
    SET view_count = view_count + 1
    WHERE id = p_chapter_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Update book chapter count and languages
-- Automatically updates book metadata when chapters change
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_book_metadata()
RETURNS TRIGGER AS $$
DECLARE
    v_chapter_count INTEGER;
    v_languages TEXT[];
BEGIN
    -- Get chapter count
    SELECT COUNT(*) INTO v_chapter_count
    FROM public.community_chapters
    WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    
    -- Get unique languages
    SELECT ARRAY_AGG(DISTINCT language) INTO v_languages
    FROM public.community_chapters
    WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    
    -- Update book
    UPDATE public.community_books
    SET 
        chapter_count = v_chapter_count,
        available_languages = COALESCE(v_languages, '{}'),
        last_updated = EXTRACT(EPOCH FROM NOW()) * 1000
    WHERE id = COALESCE(NEW.book_id, OLD.book_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Trigger to update book metadata on chapter changes
DROP TRIGGER IF EXISTS trigger_update_book_metadata ON public.community_chapters;
CREATE TRIGGER trigger_update_book_metadata
    AFTER INSERT OR UPDATE OR DELETE ON public.community_chapters
    FOR EACH ROW
    EXECUTE FUNCTION update_book_metadata();

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Uncomment to insert sample data for testing
/*
INSERT INTO public.community_books (title, author, description, genres, status, original_language, contributor_name, created_at, last_updated)
VALUES 
    ('Sample Novel', 'Test Author', 'A sample novel for testing the community source.', ARRAY['Fantasy', 'Adventure'], 'Ongoing', 'en', 'System', EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
    ('Another Story', 'Another Author', 'Another sample story.', ARRAY['Romance', 'Drama'], 'Completed', 'ja', 'System', EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000);
*/

-- ============================================================================
-- GRANTS
-- ============================================================================

-- Grant usage on schema
GRANT USAGE ON SCHEMA public TO anon, authenticated;

-- Grant access to tables
GRANT SELECT ON public.community_books TO anon, authenticated;
GRANT SELECT ON public.community_chapters TO anon, authenticated;
GRANT SELECT ON public.chapter_ratings TO anon, authenticated;

GRANT INSERT, UPDATE, DELETE ON public.community_books TO authenticated;
GRANT INSERT, UPDATE, DELETE ON public.community_chapters TO authenticated;
GRANT INSERT, UPDATE ON public.chapter_ratings TO authenticated;
GRANT INSERT ON public.chapter_reports TO authenticated;

-- Grant execute on functions
GRANT EXECUTE ON FUNCTION rate_chapter(UUID, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION increment_book_view(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION increment_chapter_view(UUID) TO anon, authenticated;
