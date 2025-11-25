-- ============================================================================
-- PROJECT 4: BOOK REVIEWS DATABASE SCHEMA
-- ============================================================================
-- This schema contains ONLY book reviews
-- Tables: book_reviews
-- Estimated storage: ~500MB for 250K+ book reviews
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Book Reviews Table
-- Reviews are based on normalized book title - shared across all sources
-- NOTE: Users can submit multiple reviews for the same book
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.book_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,  -- Stored as TEXT (from Project 1)
    username TEXT NOT NULL,  -- Denormalized for display
    book_title TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 2000),
    CONSTRAINT user_id_not_empty CHECK (LENGTH(user_id) > 0),
    CONSTRAINT username_not_empty CHECK (LENGTH(username) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_book_reviews_user_id ON public.book_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_book_title ON public.book_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_book_reviews_rating ON public.book_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_created_at ON public.book_reviews(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_book_rating ON public.book_reviews(book_title, rating DESC);

-- Full-text search index for review text
CREATE INDEX IF NOT EXISTS idx_book_reviews_text_search 
ON public.book_reviews USING gin(to_tsvector('english', review_text));

-- Comments
COMMENT ON TABLE public.book_reviews IS 'Book reviews based on normalized title (Project 4 - Book Reviews)';
COMMENT ON COLUMN public.book_reviews.user_id IS 'User ID from Project 1 (stored as TEXT)';
COMMENT ON COLUMN public.book_reviews.username IS 'Username from Project 1 (denormalized)';
COMMENT ON COLUMN public.book_reviews.book_title IS 'Normalized book title (lowercase, trimmed)';
COMMENT ON COLUMN public.book_reviews.rating IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN public.book_reviews.review_text IS 'User review text (max 2000 characters)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.book_reviews ENABLE ROW LEVEL SECURITY;

-- Everyone can view book reviews (public reading)
CREATE POLICY "Everyone can view book reviews"
    ON public.book_reviews FOR SELECT
    USING (true);

-- Authenticated users can insert book reviews
CREATE POLICY "Authenticated users can insert book reviews"
    ON public.book_reviews FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Users can update their own book reviews
CREATE POLICY "Users can update their own book reviews"
    ON public.book_reviews FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- Users can delete their own book reviews
CREATE POLICY "Users can delete their own book reviews"
    ON public.book_reviews FOR DELETE
    USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- VIEWS
-- ============================================================================

-- Average rating per book
CREATE OR REPLACE VIEW public.book_ratings_summary AS
SELECT 
    book_title,
    COUNT(*) as review_count,
    AVG(rating)::NUMERIC(3,2) as average_rating,
    COUNT(CASE WHEN rating = 5 THEN 1 END) as five_star_count,
    COUNT(CASE WHEN rating = 4 THEN 1 END) as four_star_count,
    COUNT(CASE WHEN rating = 3 THEN 1 END) as three_star_count,
    COUNT(CASE WHEN rating = 2 THEN 1 END) as two_star_count,
    COUNT(CASE WHEN rating = 1 THEN 1 END) as one_star_count,
    MAX(created_at) as latest_review_at
FROM public.book_reviews
GROUP BY book_title;

COMMENT ON VIEW public.book_ratings_summary IS 'Summary of ratings per book';

-- Grant access to the view
GRANT SELECT ON public.book_ratings_summary TO authenticated;
GRANT SELECT ON public.book_ratings_summary TO anon;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 4 (BOOK REVIEWS) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Table created:';
    RAISE NOTICE '- book_reviews: Book reviews and ratings';
    RAISE NOTICE '';
    RAISE NOTICE 'Views created:';
    RAISE NOTICE '- book_ratings_summary: Average ratings per book';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Public reading of reviews';
    RAISE NOTICE '- Full-text search support';
    RAISE NOTICE '- Rating statistics';
    RAISE NOTICE '- Users can submit multiple reviews';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~250K book reviews in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id and username stored as TEXT (from Project 1)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 4 URL and Anon Key';
    RAISE NOTICE '2. Create Project 5 for chapter reviews (schema_5_chapter_reviews.sql)';
END $$;
