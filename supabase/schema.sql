-- IReader Sync Database Schema
-- This file contains all tables, policies, triggers, and functions for the IReader sync feature
-- Run this in your Supabase SQL Editor to set up the complete database

-- ============================================================================
-- EXTENSIONS
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Users Table
-- Stores user profile information and authentication data
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT auth.uid(),
    email TEXT NOT NULL UNIQUE,
    username TEXT,
    eth_wallet_address TEXT,
    is_supporter BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT username_length CHECK (username IS NULL OR LENGTH(username) >= 3),
    CONSTRAINT eth_wallet_format CHECK (eth_wallet_address IS NULL OR eth_wallet_address ~* '^0x[a-fA-F0-9]{40}$')
);

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_eth_wallet ON public.users(eth_wallet_address) WHERE eth_wallet_address IS NOT NULL;

-- Comments
COMMENT ON TABLE public.users IS 'User profiles and authentication data';
COMMENT ON COLUMN public.users.id IS 'User ID from Supabase Auth';
COMMENT ON COLUMN public.users.email IS 'User email address';
COMMENT ON COLUMN public.users.username IS 'Display username (optional)';
COMMENT ON COLUMN public.users.eth_wallet_address IS 'Ethereum wallet address for API key authentication';
COMMENT ON COLUMN public.users.is_supporter IS 'Whether user is a supporter/premium member';

-- ----------------------------------------------------------------------------
-- Reading Progress Table
-- Stores current reading position for each book
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_user_book UNIQUE(user_id, book_id),
    CONSTRAINT scroll_position_range CHECK (last_scroll_position >= 0 AND last_scroll_position <= 1),
    CONSTRAINT book_id_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT chapter_slug_not_empty CHECK (LENGTH(last_chapter_slug) > 0)
);

-- Indexes for reading_progress table
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_book ON public.reading_progress(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_updated_at ON public.reading_progress(updated_at DESC);

-- Comments
COMMENT ON TABLE public.reading_progress IS 'Current reading position for each book';
COMMENT ON COLUMN public.reading_progress.book_id IS 'Composite book identifier (sourceId-bookId)';
COMMENT ON COLUMN public.reading_progress.last_chapter_slug IS 'Key/slug of the last read chapter';
COMMENT ON COLUMN public.reading_progress.last_scroll_position IS 'Scroll position in chapter (0.0 to 1.0)';

-- ----------------------------------------------------------------------------
-- Synced Books Table
-- Stores favorite books with essential metadata
-- All fields are required for proper functionality
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.synced_books (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    source_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    book_url TEXT NOT NULL,
    last_read BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    PRIMARY KEY (user_id, book_id),
    CONSTRAINT book_id_synced_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT book_url_not_empty CHECK (LENGTH(book_url) > 0),
    -- Removed source_id_positive constraint to allow large/negative values
    CONSTRAINT last_read_non_negative CHECK (last_read >= 0)
);

-- Indexes for synced_books table
CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_last_read ON public.synced_books(user_id, last_read DESC);
CREATE INDEX IF NOT EXISTS idx_synced_books_title ON public.synced_books(title);

-- Comments
COMMENT ON TABLE public.synced_books IS 'Favorite books with essential metadata (all fields required)';
COMMENT ON COLUMN public.synced_books.user_id IS 'User who owns the book';
COMMENT ON COLUMN public.synced_books.book_id IS 'Composite book identifier (sourceId-bookId)';
COMMENT ON COLUMN public.synced_books.source_id IS 'Source/catalog ID where book is from';
COMMENT ON COLUMN public.synced_books.title IS 'Book title';
COMMENT ON COLUMN public.synced_books.book_url IS 'Book URL/link on the source';
COMMENT ON COLUMN public.synced_books.last_read IS 'Last read timestamp (milliseconds)';

-- ----------------------------------------------------------------------------
-- Book Reviews Table
-- Reviews are based on normalized book title - shared across all sources
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.book_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_title TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_user_book_review UNIQUE(user_id, book_title),
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 2000)
);

-- Indexes for book_reviews table
CREATE INDEX IF NOT EXISTS idx_book_reviews_user_id ON public.book_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_book_title ON public.book_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_book_reviews_rating ON public.book_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_created_at ON public.book_reviews(created_at DESC);

-- Comments
COMMENT ON TABLE public.book_reviews IS 'Book reviews based on normalized title (shared across sources)';
COMMENT ON COLUMN public.book_reviews.book_title IS 'Normalized book title (lowercase, trimmed)';
COMMENT ON COLUMN public.book_reviews.rating IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN public.book_reviews.review_text IS 'User review text (max 2000 characters)';

-- ----------------------------------------------------------------------------
-- Chapter Reviews Table
-- Reviews are based on normalized book title + chapter name - shared across all sources
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chapter_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_title TEXT NOT NULL,
    chapter_name TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_user_chapter_review UNIQUE(user_id, book_title, chapter_name),
    CONSTRAINT book_title_chapter_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(chapter_name) > 0),
    CONSTRAINT chapter_rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chapter_review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 1000)
);

-- Indexes for chapter_reviews table
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_user_id ON public.chapter_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_title ON public.chapter_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_chapter ON public.chapter_reviews(book_title, chapter_name);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_rating ON public.chapter_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_created_at ON public.chapter_reviews(created_at DESC);

-- Comments
COMMENT ON TABLE public.chapter_reviews IS 'Chapter reviews based on normalized book + chapter names (shared across sources)';
COMMENT ON COLUMN public.chapter_reviews.book_title IS 'Normalized book title (lowercase, trimmed)';
COMMENT ON COLUMN public.chapter_reviews.chapter_name IS 'Normalized chapter name (lowercase, trimmed)';
COMMENT ON COLUMN public.chapter_reviews.rating IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN public.chapter_reviews.review_text IS 'User review text (max 1000 characters)';

-- ----------------------------------------------------------------------------
-- Badges Table
-- Defines available badges that can be earned
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.badges (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    icon TEXT NOT NULL,
    category TEXT NOT NULL,
    rarity TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT badge_id_not_empty CHECK (LENGTH(id) > 0),
    CONSTRAINT badge_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT badge_category_valid CHECK (category IN ('donor', 'contributor', 'reader', 'reviewer', 'special')),
    CONSTRAINT badge_rarity_valid CHECK (rarity IN ('common', 'rare', 'epic', 'legendary'))
);

-- Indexes for badges table
CREATE INDEX IF NOT EXISTS idx_badges_category ON public.badges(category);
CREATE INDEX IF NOT EXISTS idx_badges_rarity ON public.badges(rarity);

-- Comments
COMMENT ON TABLE public.badges IS 'Available badges that users can earn';
COMMENT ON COLUMN public.badges.id IS 'Unique badge identifier (e.g., "donor_bronze")';
COMMENT ON COLUMN public.badges.name IS 'Display name of the badge';
COMMENT ON COLUMN public.badges.description IS 'Description of how to earn the badge';
COMMENT ON COLUMN public.badges.icon IS 'Icon/emoji for the badge';
COMMENT ON COLUMN public.badges.category IS 'Badge category (donor, contributor, reader, reviewer, special)';
COMMENT ON COLUMN public.badges.rarity IS 'Badge rarity (common, rare, epic, legendary)';

-- ----------------------------------------------------------------------------
-- User Badges Table
-- Tracks which badges each user has earned
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_badges (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    badge_id TEXT NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    earned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    metadata JSONB,
    
    -- Constraints
    PRIMARY KEY (user_id, badge_id)
);

-- Indexes for user_badges table
CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON public.user_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_badge_id ON public.user_badges(badge_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_earned_at ON public.user_badges(earned_at DESC);

-- Comments
COMMENT ON TABLE public.user_badges IS 'Badges earned by users';
COMMENT ON COLUMN public.user_badges.user_id IS 'User who earned the badge';
COMMENT ON COLUMN public.user_badges.badge_id IS 'Badge that was earned';
COMMENT ON COLUMN public.user_badges.earned_at IS 'When the badge was earned';
COMMENT ON COLUMN public.user_badges.metadata IS 'Additional data (e.g., donation amount, task details)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.book_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_badges ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- Users Table Policies
-- ----------------------------------------------------------------------------

-- Users can view their own data
CREATE POLICY "Users can view their own data"
    ON public.users FOR SELECT
    USING (auth.uid() = id);

-- Users can update their own data
CREATE POLICY "Users can update their own data"
    ON public.users FOR UPDATE
    USING (auth.uid() = id);

-- Users can insert their own data (on signup)
CREATE POLICY "Users can insert their own data"
    ON public.users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Users cannot delete their own data (use Supabase Auth for account deletion)
-- No DELETE policy - prevents accidental data loss

-- ----------------------------------------------------------------------------
-- Reading Progress Table Policies
-- ----------------------------------------------------------------------------

-- Users can view their own reading progress
CREATE POLICY "Users can view their own reading progress"
    ON public.reading_progress FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own reading progress
CREATE POLICY "Users can insert their own reading progress"
    ON public.reading_progress FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own reading progress
CREATE POLICY "Users can update their own reading progress"
    ON public.reading_progress FOR UPDATE
    USING (auth.uid() = user_id);

-- Users can delete their own reading progress
CREATE POLICY "Users can delete their own reading progress"
    ON public.reading_progress FOR DELETE
    USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Synced Books Table Policies
-- ----------------------------------------------------------------------------

-- Users can view their own synced books
CREATE POLICY "Users can view their own synced books"
    ON public.synced_books FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own synced books
CREATE POLICY "Users can insert their own synced books"
    ON public.synced_books FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own synced books
CREATE POLICY "Users can update their own synced books"
    ON public.synced_books FOR UPDATE
    USING (auth.uid() = user_id);

-- Users can delete their own synced books
CREATE POLICY "Users can delete their own synced books"
    ON public.synced_books FOR DELETE
    USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Book Reviews Table Policies
-- ----------------------------------------------------------------------------

-- Users can view all book reviews (public reading)
CREATE POLICY "Users can view all book reviews"
    ON public.book_reviews FOR SELECT
    USING (true);

-- Users can insert their own book reviews
CREATE POLICY "Users can insert their own book reviews"
    ON public.book_reviews FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own book reviews
CREATE POLICY "Users can update their own book reviews"
    ON public.book_reviews FOR UPDATE
    USING (auth.uid() = user_id);

-- Users can delete their own book reviews
CREATE POLICY "Users can delete their own book reviews"
    ON public.book_reviews FOR DELETE
    USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Chapter Reviews Table Policies
-- ----------------------------------------------------------------------------

-- Users can view all chapter reviews (public reading)
CREATE POLICY "Users can view all chapter reviews"
    ON public.chapter_reviews FOR SELECT
    USING (true);

-- Users can insert their own chapter reviews
CREATE POLICY "Users can insert their own chapter reviews"
    ON public.chapter_reviews FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own chapter reviews
CREATE POLICY "Users can update their own chapter reviews"
    ON public.chapter_reviews FOR UPDATE
    USING (auth.uid() = user_id);

-- Users can delete their own chapter reviews
CREATE POLICY "Users can delete their own chapter reviews"
    ON public.chapter_reviews FOR DELETE
    USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Badges Table Policies
-- ----------------------------------------------------------------------------

-- Everyone can view available badges
CREATE POLICY "Everyone can view badges"
    ON public.badges FOR SELECT
    USING (true);

-- Only admins can manage badges (handled via service role)
-- No INSERT/UPDATE/DELETE policies for regular users

-- ----------------------------------------------------------------------------
-- User Badges Table Policies
-- ----------------------------------------------------------------------------

-- Everyone can view all user badges (public display)
CREATE POLICY "Everyone can view user badges"
    ON public.user_badges FOR SELECT
    USING (true);

-- Only admins can award badges (handled via service role)
-- No INSERT/UPDATE/DELETE policies for regular users

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Function: Update updated_at timestamp
-- Automatically updates the updated_at column when a row is modified
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates updated_at timestamp on row modification';

-- ----------------------------------------------------------------------------
-- Function: Award badge to user
-- Awards a badge to a user with optional metadata
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION award_badge(
    p_user_id UUID,
    p_badge_id TEXT,
    p_metadata JSONB DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO public.user_badges (user_id, badge_id, metadata)
    VALUES (p_user_id, p_badge_id, p_metadata)
    ON CONFLICT (user_id, badge_id) DO NOTHING;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION award_badge(UUID, TEXT, JSONB) IS 'Awards a badge to a user';

-- ----------------------------------------------------------------------------
-- Function: Get user badges
-- Returns all badges earned by a user
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_badges(p_user_id UUID)
RETURNS TABLE (
    badge_id TEXT,
    badge_name TEXT,
    badge_description TEXT,
    badge_icon TEXT,
    badge_category TEXT,
    badge_rarity TEXT,
    earned_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.id,
        b.name,
        b.description,
        b.icon,
        b.category,
        b.rarity,
        ub.earned_at,
        ub.metadata
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id = p_user_id
    ORDER BY ub.earned_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_badges(UUID) IS 'Returns all badges earned by a user';

-- ----------------------------------------------------------------------------
-- Function: Get user statistics
-- Returns reading statistics for a user
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_statistics(p_user_id UUID)
RETURNS TABLE (
    total_books BIGINT,
    favorite_books BIGINT,
    total_chapters BIGINT,
    read_chapters BIGINT,
    bookmarked_chapters BIGINT,
    books_in_progress BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(DISTINCT sb.book_id) AS total_books,
        0::BIGINT AS favorite_books,
        0::BIGINT AS total_chapters,
        0::BIGINT AS read_chapters,
        0::BIGINT AS bookmarked_chapters,
        COUNT(DISTINCT rp.book_id) AS books_in_progress
    FROM public.users u
    LEFT JOIN public.synced_books sb ON u.id = sb.user_id
    LEFT JOIN public.reading_progress rp ON u.id = rp.user_id
    WHERE u.id = p_user_id
    GROUP BY u.id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_statistics(UUID) IS 'Returns reading statistics for a user';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Create triggers for automatic updated_at timestamp updates

-- Users table trigger
DROP TRIGGER IF EXISTS update_users_updated_at ON public.users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Reading progress table trigger
DROP TRIGGER IF EXISTS update_reading_progress_updated_at ON public.reading_progress;
CREATE TRIGGER update_reading_progress_updated_at
    BEFORE UPDATE ON public.reading_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();



-- ============================================================================
-- VIEWS (Optional - for analytics and reporting)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- View: User Reading Summary
-- Provides a summary of each user's reading activity
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW user_reading_summary AS
SELECT
    u.id AS user_id,
    u.email,
    u.username,
    u.is_supporter,
    COUNT(DISTINCT sb.book_id) AS total_books,
    0 AS favorite_books,
    0 AS total_chapters,
    0 AS read_chapters,
    COUNT(DISTINCT rp.id) AS books_in_progress,
    MAX(rp.updated_at) AS last_reading_activity,
    u.created_at AS user_since
FROM public.users u
LEFT JOIN public.synced_books sb ON u.id = sb.user_id
LEFT JOIN public.reading_progress rp ON u.id = rp.user_id
GROUP BY u.id, u.email, u.username, u.is_supporter, u.created_at;

COMMENT ON VIEW user_reading_summary IS 'Summary of each user''s reading activity';

-- ----------------------------------------------------------------------------
-- View: Recent Activity
-- Shows recent sync activity across all users (for admin monitoring)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW recent_activity AS
SELECT
    'reading_progress' AS activity_type,
    rp.user_id,
    u.email,
    rp.book_id,
    NULL AS title,
    rp.updated_at
FROM public.reading_progress rp
JOIN public.users u ON rp.user_id = u.id
UNION ALL
SELECT
    'synced_book' AS activity_type,
    sb.user_id,
    u.email,
    sb.book_id,
    sb.title,
    to_timestamp(sb.last_read / 1000.0) AS updated_at
FROM public.synced_books sb
JOIN public.users u ON sb.user_id = u.id
ORDER BY updated_at DESC
LIMIT 100;

COMMENT ON VIEW recent_activity IS 'Recent sync activity across all users (last 100 activities)';

-- ============================================================================
-- GRANTS (Optional - for service role access)
-- ============================================================================

-- Grant necessary permissions to authenticated users
-- These are handled by RLS policies, but explicit grants can be added if needed

-- Example: Grant usage on sequences (if any custom sequences are created)
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO authenticated;

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Uncomment to insert sample data for testing
/*
-- Sample user (requires auth.uid() to match)
INSERT INTO public.users (id, email, username, is_supporter)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'test@example.com', 'testuser', FALSE)
ON CONFLICT (id) DO NOTHING;

-- Sample reading progress
INSERT INTO public.reading_progress (user_id, book_id, last_chapter_slug, last_scroll_position)
VALUES 
    ('00000000-0000-0000-0000-000000000001', '1-123', 'chapter-5', 0.75)
ON CONFLICT (user_id, book_id) DO NOTHING;

-- Sample synced book
INSERT INTO public.synced_books (user_id, book_id, source_id, title, book_url, last_read)
VALUES 
    ('00000000-0000-0000-0000-000000000001', '1-123', 1, 'Sample Book', 'https://example.com/book/123', 0)
ON CONFLICT (user_id, book_id) DO NOTHING;
*/

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Run these queries to verify the schema was created correctly

-- Check all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('users', 'reading_progress', 'synced_books')
ORDER BY table_name;

-- Check RLS is enabled
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
  AND tablename IN ('users', 'reading_progress', 'synced_books')
ORDER BY tablename;

-- Check policies exist
SELECT tablename, policyname, cmd 
FROM pg_policies 
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

-- Check triggers exist
SELECT trigger_name, event_object_table, action_timing, event_manipulation
FROM information_schema.triggers
WHERE trigger_schema = 'public'
  AND event_object_table IN ('users', 'reading_progress', 'synced_books')
ORDER BY event_object_table, trigger_name;

-- ============================================================================
-- MAINTENANCE QUERIES
-- ============================================================================

-- Clean up old data (run periodically if needed)
/*
-- Delete reading progress older than 1 year with no recent updates
DELETE FROM public.reading_progress
WHERE updated_at < NOW() - INTERVAL '1 year';

-- Delete synced books that haven't been accessed in over 2 years
DELETE FROM public.synced_books
WHERE last_read < EXTRACT(EPOCH FROM (NOW() - INTERVAL '2 years')) * 1000;
*/

-- ============================================================================
-- SCHEMA VERSION
-- ============================================================================

-- Track schema version for migrations
CREATE TABLE IF NOT EXISTS public.schema_version (
    version INTEGER PRIMARY KEY,
    applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    description TEXT
);

INSERT INTO public.schema_version (version, description)
VALUES (1, 'Initial schema for IReader sync feature')
ON CONFLICT (version) DO NOTHING;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================

-- Success message
-- ============================================================================
-- INITIAL BADGE DATA
-- ============================================================================

-- Insert default badges
INSERT INTO public.badges (id, name, description, icon, category, rarity) VALUES
-- Donor Badges
('donor_bronze', 'Bronze Supporter', 'Donated $5 or more', '🥉', 'donor', 'common'),
('donor_silver', 'Silver Supporter', 'Donated $10 or more', '🥈', 'donor', 'rare'),
('donor_gold', 'Gold Supporter', 'Donated $25 or more', '🥇', 'donor', 'epic'),
('donor_platinum', 'Platinum Supporter', 'Donated $50 or more', '💎', 'donor', 'legendary'),

-- Contributor Badges
('contributor_translator', 'Translator', 'Contributed translations', '🌐', 'contributor', 'rare'),
('contributor_developer', 'Developer', 'Contributed code', '💻', 'contributor', 'epic'),
('contributor_designer', 'Designer', 'Contributed designs', '🎨', 'contributor', 'rare'),

-- Reader Badges
('reader_novice', 'Novice Reader', 'Read 10 chapters', '📖', 'reader', 'common'),
('reader_bookworm', 'Bookworm', 'Read 100 chapters', '📚', 'reader', 'rare'),
('reader_scholar', 'Scholar', 'Read 500 chapters', '🎓', 'reader', 'epic'),
('reader_master', 'Reading Master', 'Read 1000 chapters', '👑', 'reader', 'legendary'),

-- Reviewer Badges
('reviewer_critic', 'Critic', 'Wrote 10 reviews', '✍️', 'reviewer', 'common'),
('reviewer_expert', 'Expert Reviewer', 'Wrote 50 reviews', '⭐', 'reviewer', 'rare'),
('reviewer_master', 'Master Critic', 'Wrote 100 reviews', '🌟', 'reviewer', 'epic'),

-- Special Badges
('special_early_adopter', 'Early Adopter', 'Joined during beta', '🚀', 'special', 'legendary'),
('special_bug_hunter', 'Bug Hunter', 'Reported critical bugs', '🐛', 'special', 'epic'),
('special_community_hero', 'Community Hero', 'Outstanding community contribution', '🦸', 'special', 'legendary')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE 'IReader Sync schema created successfully!';
    RAISE NOTICE 'Tables: users, reading_progress, synced_books, book_reviews, chapter_reviews, badges, user_badges';
    RAISE NOTICE 'RLS: Enabled on all tables';
    RAISE NOTICE 'Policies: Created for all CRUD operations';
    RAISE NOTICE 'Triggers: Created for automatic timestamp updates';
    RAISE NOTICE 'Views: user_reading_summary, recent_activity';
    RAISE NOTICE 'Functions: update_updated_at_column, get_user_statistics, award_badge, get_user_badges';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Optimized storage: Essential book data synced';
    RAISE NOTICE '- Book reviews: Shared across all sources (by title)';
    RAISE NOTICE '- Chapter reviews: Shared across all sources (by title + chapter)';
    RAISE NOTICE '- Badge system: Reward donors and contributors';
    RAISE NOTICE '- Public reviews: All users can read reviews';
    RAISE NOTICE '- All fields required: user_id, book_id, source_id, title, book_url, last_read';
    RAISE NOTICE '';
    RAISE NOTICE 'Badge Categories:';
    RAISE NOTICE '- Donor: Bronze, Silver, Gold, Platinum';
    RAISE NOTICE '- Contributor: Translator, Developer, Designer';
    RAISE NOTICE '- Reader: Novice, Bookworm, Scholar, Master';
    RAISE NOTICE '- Reviewer: Critic, Expert, Master';
    RAISE NOTICE '- Special: Early Adopter, Bug Hunter, Community Hero';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Configure authentication in Supabase Dashboard';
    RAISE NOTICE '2. Test with the IReader app';
    RAISE NOTICE '3. Award badges using award_badge() function';
    RAISE NOTICE '4. Monitor using the views and statistics function';
END $$;
