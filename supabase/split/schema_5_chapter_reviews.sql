-- ============================================================================
-- PROJECT 5: CHAPTER REVIEWS DATABASE SCHEMA
-- ============================================================================
-- This schema contains ONLY chapter reviews
-- Tables: chapter_reviews
-- Estimated storage: ~500MB for 500K+ chapter reviews
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Chapter Reviews Table
-- Reviews are based on normalized book title + chapter name
-- NOTE: Users can submit multiple reviews for the same chapter
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chapter_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,  -- Stored as TEXT (from Project 1)
    username TEXT NOT NULL,  -- Denormalized for display
    book_title TEXT NOT NULL,
    chapter_name TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT book_title_chapter_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(chapter_name) > 0),
    CONSTRAINT chapter_rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chapter_review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 1000),
    CONSTRAINT chapter_user_id_not_empty CHECK (LENGTH(user_id) > 0),
    CONSTRAINT chapter_username_not_empty CHECK (LENGTH(username) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_user_id ON public.chapter_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_title ON public.chapter_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_chapter ON public.chapter_reviews(book_title, chapter_name);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_rating ON public.chapter_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_created_at ON public.chapter_reviews(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_chapter_rating ON public.chapter_reviews(book_title, chapter_name, rating DESC);

-- Full-text search index for review text
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_text_search 
ON public.chapter_reviews USING gin(to_tsvector('english', review_text));

-- Comments
COMMENT ON TABLE public.chapter_reviews IS 'Chapter reviews based on normalized book + chapter names (Project 5 - Chapter Reviews)';
COMMENT ON COLUMN public.chapter_reviews.user_id IS 'User ID from Project 1 (stored as TEXT)';
COMMENT ON COLUMN public.chapter_reviews.username IS 'Username from Project 1 (denormalized)';
COMMENT ON COLUMN public.chapter_reviews.book_title IS 'Normalized book title (lowercase, trimmed)';
COMMENT ON COLUMN public.chapter_reviews.chapter_name IS 'Normalized chapter name (lowercase, trimmed)';
COMMENT ON COLUMN public.chapter_reviews.rating IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN public.chapter_reviews.review_text IS 'User review text (max 1000 characters)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.chapter_reviews ENABLE ROW LEVEL SECURITY;

-- Everyone can view chapter reviews (public reading)
CREATE POLICY "Everyone can view chapter reviews"
    ON public.chapter_reviews FOR SELECT
    USING (true);

-- Authenticated users can insert chapter reviews
CREATE POLICY "Authenticated users can insert chapter reviews"
    ON public.chapter_reviews FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Users can update their own chapter reviews
CREATE POLICY "Users can update their own chapter reviews"
    ON public.chapter_reviews FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- Users can delete their own chapter reviews
CREATE POLICY "Users can delete their own chapter reviews"
    ON public.chapter_reviews FOR DELETE
    USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- VIEWS
-- ============================================================================

-- Average rating per chapter
CREATE OR REPLACE VIEW public.chapter_ratings_summary AS
SELECT 
    book_title,
    chapter_name,
    COUNT(*) as review_count,
    AVG(rating)::NUMERIC(3,2) as average_rating,
    COUNT(CASE WHEN rating = 5 THEN 1 END) as five_star_count,
    COUNT(CASE WHEN rating = 4 THEN 1 END) as four_star_count,
    COUNT(CASE WHEN rating = 3 THEN 1 END) as three_star_count,
    COUNT(CASE WHEN rating = 2 THEN 1 END) as two_star_count,
    COUNT(CASE WHEN rating = 1 THEN 1 END) as one_star_count,
    MAX(created_at) as latest_review_at
FROM public.chapter_reviews
GROUP BY book_title, chapter_name;

COMMENT ON VIEW public.chapter_ratings_summary IS 'Summary of ratings per chapter';

-- Grant access to the view
GRANT SELECT ON public.chapter_ratings_summary TO authenticated;
GRANT SELECT ON public.chapter_ratings_summary TO anon;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 5 (CHAPTER REVIEWS) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Table created:';
    RAISE NOTICE '- chapter_reviews: Chapter reviews and ratings';
    RAISE NOTICE '';
    RAISE NOTICE 'Views created:';
    RAISE NOTICE '- chapter_ratings_summary: Average ratings per chapter';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Public reading of reviews';
    RAISE NOTICE '- Full-text search support';
    RAISE NOTICE '- Rating statistics per chapter';
    RAISE NOTICE '- Users can submit multiple reviews';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~500K chapter reviews in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id and username stored as TEXT (from Project 1)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 5 URL and Anon Key';
    RAISE NOTICE '2. Create Project 6 for badges (schema_6_badges.sql)';
END $$;
