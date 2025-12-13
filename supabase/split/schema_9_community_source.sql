-- ============================================================================
-- PROJECT 9: COMMUNITY SOURCE DATABASE SCHEMA
-- ============================================================================
-- This schema contains community books and translations
-- Tables: community_books, community_chapters, chapter_reports, chapter_ratings
-- Estimated storage: ~500MB for community content
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- Community Books Table
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
    contributor_id TEXT,
    contributor_name TEXT DEFAULT '',
    view_count BIGINT DEFAULT 0,
    chapter_count INTEGER DEFAULT 0,
    last_updated BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    is_nsfw BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT status_valid CHECK (status IN ('Ongoing', 'Completed', 'Hiatus', 'Dropped')),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0),
    CONSTRAINT chapter_count_non_negative CHECK (chapter_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_community_books_title ON public.community_books(title);
CREATE INDEX IF NOT EXISTS idx_community_books_author ON public.community_books(author);
CREATE INDEX IF NOT EXISTS idx_community_books_contributor ON public.community_books(contributor_id);
CREATE INDEX IF NOT EXISTS idx_community_books_status ON public.community_books(status);
CREATE INDEX IF NOT EXISTS idx_community_books_view_count ON public.community_books(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_created_at ON public.community_books(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_last_updated ON public.community_books(last_updated DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_languages ON public.community_books USING GIN(available_languages);
CREATE INDEX IF NOT EXISTS idx_community_books_genres ON public.community_books USING GIN(genres);

COMMENT ON TABLE public.community_books IS 'Books contributed by the community';

-- Community Chapters Table
CREATE TABLE IF NOT EXISTS public.community_chapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES public.community_books(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    number REAL DEFAULT -1,
    content TEXT NOT NULL,
    language TEXT NOT NULL,
    translator_id TEXT,
    translator_name TEXT DEFAULT '',
    original_chapter_key TEXT DEFAULT '',
    rating REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    updated_at BIGINT DEFAULT 0,
    is_approved BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT chapter_content_not_empty CHECK (LENGTH(content) > 0),
    CONSTRAINT language_not_empty CHECK (LENGTH(language) > 0),
    CONSTRAINT rating_range CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT rating_count_non_negative CHECK (rating_count >= 0),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_community_chapters_book_id ON public.community_chapters(book_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_language ON public.community_chapters(language);
CREATE INDEX IF NOT EXISTS idx_community_chapters_translator ON public.community_chapters(translator_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_number ON public.community_chapters(number);
CREATE INDEX IF NOT EXISTS idx_community_chapters_rating ON public.community_chapters(rating DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_created_at ON public.community_chapters(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_book_lang ON public.community_chapters(book_id, language);

COMMENT ON TABLE public.community_chapters IS 'Translated chapters contributed by the community';

-- Chapter Reports Table
CREATE TABLE IF NOT EXISTS public.chapter_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    reporter_id TEXT,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    created_at BIGINT DEFAULT 0,
    reviewed_at BIGINT,
    reviewed_by TEXT,
    
    CONSTRAINT reason_not_empty CHECK (LENGTH(reason) > 0),
    CONSTRAINT report_status_valid CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED'))
);

CREATE INDEX IF NOT EXISTS idx_chapter_reports_chapter ON public.chapter_reports(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reports_status ON public.chapter_reports(status);

COMMENT ON TABLE public.chapter_reports IS 'Reports for problematic community chapters';

-- Chapter Ratings Table
CREATE TABLE IF NOT EXISTS public.chapter_ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    rating INTEGER NOT NULL,
    created_at BIGINT DEFAULT 0,
    
    CONSTRAINT unique_user_chapter_rating UNIQUE(user_id, chapter_id),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT cr_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_chapter_ratings_chapter ON public.chapter_ratings(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_ratings_user ON public.chapter_ratings(user_id);

COMMENT ON TABLE public.chapter_ratings IS 'Individual user ratings for community chapters';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.community_books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_chapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_ratings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view approved community books" ON public.community_books FOR SELECT USING (is_approved = TRUE);
CREATE POLICY "Users can insert their own books" ON public.community_books FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update their own books" ON public.community_books FOR UPDATE USING (contributor_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own books" ON public.community_books FOR DELETE USING (contributor_id = auth.uid()::TEXT);

CREATE POLICY "Everyone can view approved chapters" ON public.community_chapters FOR SELECT USING (is_approved = TRUE);
CREATE POLICY "Users can insert chapters" ON public.community_chapters FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update their own chapters" ON public.community_chapters FOR UPDATE USING (translator_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own chapters" ON public.community_chapters FOR DELETE USING (translator_id = auth.uid()::TEXT);

CREATE POLICY "Users can view their own reports" ON public.chapter_reports FOR SELECT USING (reporter_id = auth.uid()::TEXT);
CREATE POLICY "Users can submit reports" ON public.chapter_reports FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Everyone can view ratings" ON public.chapter_ratings FOR SELECT USING (true);
CREATE POLICY "Users can rate chapters" ON public.chapter_ratings FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update their own ratings" ON public.chapter_ratings FOR UPDATE USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

CREATE OR REPLACE FUNCTION rate_chapter(p_chapter_id UUID, p_rating INTEGER)
RETURNS BOOLEAN AS $
DECLARE v_user_id TEXT; v_existing_rating INTEGER; v_new_avg REAL; v_new_count INTEGER;
BEGIN
    v_user_id := auth.uid()::TEXT;
    IF v_user_id IS NULL THEN RETURN FALSE; END IF;
    
    SELECT rating INTO v_existing_rating FROM public.chapter_ratings WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    
    IF v_existing_rating IS NOT NULL THEN
        UPDATE public.chapter_ratings SET rating = p_rating WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    ELSE
        INSERT INTO public.chapter_ratings (chapter_id, user_id, rating, created_at)
        VALUES (p_chapter_id, v_user_id, p_rating, EXTRACT(EPOCH FROM NOW()) * 1000);
    END IF;
    
    SELECT AVG(rating)::REAL, COUNT(*)::INTEGER INTO v_new_avg, v_new_count FROM public.chapter_ratings WHERE chapter_id = p_chapter_id;
    UPDATE public.community_chapters SET rating = v_new_avg, rating_count = v_new_count WHERE id = p_chapter_id;
    RETURN TRUE;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION increment_book_view(p_book_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.community_books SET view_count = view_count + 1 WHERE id = p_book_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION increment_chapter_view(p_chapter_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.community_chapters SET view_count = view_count + 1 WHERE id = p_chapter_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION update_book_metadata()
RETURNS TRIGGER AS $
DECLARE v_chapter_count INTEGER; v_languages TEXT[];
BEGIN
    SELECT COUNT(*) INTO v_chapter_count FROM public.community_chapters WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    SELECT ARRAY_AGG(DISTINCT language) INTO v_languages FROM public.community_chapters WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    UPDATE public.community_books SET chapter_count = v_chapter_count, available_languages = COALESCE(v_languages, '{}'),
           last_updated = EXTRACT(EPOCH FROM NOW()) * 1000 WHERE id = COALESCE(NEW.book_id, OLD.book_id);
    RETURN COALESCE(NEW, OLD);
END;
$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_book_metadata ON public.community_chapters;
CREATE TRIGGER trigger_update_book_metadata AFTER INSERT OR UPDATE OR DELETE ON public.community_chapters FOR EACH ROW EXECUTE FUNCTION update_book_metadata();

GRANT EXECUTE ON FUNCTION rate_chapter(UUID, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION increment_book_view(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION increment_chapter_view(UUID) TO anon, authenticated;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $
BEGIN
    RAISE NOTICE '✅ PROJECT 9 (COMMUNITY SOURCE) Schema created successfully!';
    RAISE NOTICE 'Tables: community_books, community_chapters, chapter_reports, chapter_ratings';
END $;
