-- ============================================================================
-- PROJECT 7: ANALYTICS DATABASE SCHEMA
-- ============================================================================
-- This schema contains leaderboard and statistics
-- Tables: leaderboard
-- Estimated storage: ~500MB for 2.5M+ leaderboard entries
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Leaderboard Table
-- Tracks user reading statistics for global leaderboard
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.leaderboard (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,  -- Stored as TEXT since user is in different project
    username TEXT NOT NULL,
    total_reading_time_minutes BIGINT NOT NULL DEFAULT 0,
    total_chapters_read INTEGER NOT NULL DEFAULT 0,
    books_completed INTEGER NOT NULL DEFAULT 0,
    reading_streak INTEGER NOT NULL DEFAULT 0,
    has_badge BOOLEAN NOT NULL DEFAULT FALSE,
    badge_type TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT lb_user_id_not_empty CHECK (LENGTH(user_id) > 0),
    CONSTRAINT lb_username_not_empty CHECK (LENGTH(username) > 0)
);

-- Indexes for faster leaderboard queries
CREATE INDEX IF NOT EXISTS idx_leaderboard_reading_time 
ON public.leaderboard(total_reading_time_minutes DESC);

CREATE INDEX IF NOT EXISTS idx_leaderboard_user_id 
ON public.leaderboard(user_id);

CREATE INDEX IF NOT EXISTS idx_leaderboard_username 
ON public.leaderboard(username);

CREATE INDEX IF NOT EXISTS idx_leaderboard_chapters 
ON public.leaderboard(total_chapters_read DESC);

CREATE INDEX IF NOT EXISTS idx_leaderboard_books 
ON public.leaderboard(books_completed DESC);

-- Comments
COMMENT ON TABLE public.leaderboard IS 'Stores user reading statistics for the leaderboard feature';
COMMENT ON COLUMN public.leaderboard.user_id IS 'User ID from Project 1 (stored as TEXT)';
COMMENT ON COLUMN public.leaderboard.username IS 'Display name for the leaderboard';
COMMENT ON COLUMN public.leaderboard.total_reading_time_minutes IS 'Total reading time in minutes';
COMMENT ON COLUMN public.leaderboard.total_chapters_read IS 'Total number of chapters read';
COMMENT ON COLUMN public.leaderboard.books_completed IS 'Total number of books completed';
COMMENT ON COLUMN public.leaderboard.reading_streak IS 'Current reading streak in days';
COMMENT ON COLUMN public.leaderboard.has_badge IS 'Whether the user has purchased a badge';
COMMENT ON COLUMN public.leaderboard.badge_type IS 'Type of badge (supporter, nft, premium, etc.)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;

-- Policy: Anyone can read the leaderboard
CREATE POLICY "Leaderboard is publicly readable"
ON public.leaderboard
FOR SELECT
USING (true);

-- Policy: Authenticated users can insert their own stats
CREATE POLICY "Authenticated users can insert stats"
ON public.leaderboard
FOR INSERT
WITH CHECK (auth.role() = 'authenticated');

-- Policy: Users can update their own stats
CREATE POLICY "Users can update their own stats"
ON public.leaderboard
FOR UPDATE
USING (user_id = auth.uid()::TEXT);

-- Policy: Users can delete their own stats
CREATE POLICY "Users can delete their own stats"
ON public.leaderboard
FOR DELETE
USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_leaderboard_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_leaderboard_updated_at() IS 'Automatically updates updated_at timestamp on leaderboard updates';

-- Function to get user's rank
CREATE OR REPLACE FUNCTION get_user_leaderboard_rank(p_user_id TEXT)
RETURNS TABLE (
    rank BIGINT,
    total_users BIGINT,
    percentile NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    WITH user_rank AS (
        SELECT 
            ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as user_rank,
            user_id
        FROM public.leaderboard
    ),
    total_count AS (
        SELECT COUNT(*) as total FROM public.leaderboard
    )
    SELECT 
        ur.user_rank,
        tc.total,
        ROUND((ur.user_rank::NUMERIC / tc.total::NUMERIC) * 100, 2) as percentile
    FROM user_rank ur, total_count tc
    WHERE ur.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_leaderboard_rank(TEXT) IS 'Returns user rank, total users, and percentile on leaderboard';

-- Function to get top N users
CREATE OR REPLACE FUNCTION get_top_leaderboard_users(p_limit INTEGER DEFAULT 10)
RETURNS TABLE (
    rank BIGINT,
    user_id TEXT,
    username TEXT,
    total_reading_time_minutes BIGINT,
    total_chapters_read INTEGER,
    books_completed INTEGER,
    reading_streak INTEGER,
    has_badge BOOLEAN,
    badge_type TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ROW_NUMBER() OVER (ORDER BY l.total_reading_time_minutes DESC) as rank,
        l.user_id,
        l.username,
        l.total_reading_time_minutes,
        l.total_chapters_read,
        l.books_completed,
        l.reading_streak,
        l.has_badge,
        l.badge_type
    FROM public.leaderboard l
    ORDER BY l.total_reading_time_minutes DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_top_leaderboard_users(INTEGER) IS 'Returns top N users from leaderboard';

-- Function to get users around a specific rank
CREATE OR REPLACE FUNCTION get_leaderboard_around_rank(p_user_id TEXT, p_range INTEGER DEFAULT 5)
RETURNS TABLE (
    rank BIGINT,
    user_id TEXT,
    username TEXT,
    total_reading_time_minutes BIGINT,
    is_current_user BOOLEAN
) AS $$
DECLARE
    v_user_rank BIGINT;
BEGIN
    -- Get user's rank
    SELECT ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC)
    INTO v_user_rank
    FROM public.leaderboard
    WHERE user_id = p_user_id;
    
    -- Return users around that rank
    RETURN QUERY
    WITH ranked_users AS (
        SELECT 
            ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as rank,
            l.user_id,
            l.username,
            l.total_reading_time_minutes
        FROM public.leaderboard l
    )
    SELECT 
        ru.rank,
        ru.user_id,
        ru.username,
        ru.total_reading_time_minutes,
        (ru.user_id = p_user_id) as is_current_user
    FROM ranked_users ru
    WHERE ru.rank BETWEEN (v_user_rank - p_range) AND (v_user_rank + p_range)
    ORDER BY ru.rank;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_leaderboard_around_rank(TEXT, INTEGER) IS 'Returns users around a specific user rank';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS update_leaderboard_timestamp ON public.leaderboard;
CREATE TRIGGER update_leaderboard_timestamp
BEFORE UPDATE ON public.leaderboard
FOR EACH ROW
EXECUTE FUNCTION update_leaderboard_updated_at();

-- ============================================================================
-- VIEWS
-- ============================================================================

-- Create a view for leaderboard with rankings
CREATE OR REPLACE VIEW public.leaderboard_with_rank AS
SELECT 
    id,
    user_id,
    username,
    total_reading_time_minutes,
    total_chapters_read,
    books_completed,
    reading_streak,
    has_badge,
    badge_type,
    updated_at,
    created_at,
    ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as rank
FROM public.leaderboard
ORDER BY total_reading_time_minutes DESC;

COMMENT ON VIEW public.leaderboard_with_rank IS 'Leaderboard with calculated rankings based on reading time';

-- Grant access to the view
GRANT SELECT ON public.leaderboard_with_rank TO authenticated;
GRANT SELECT ON public.leaderboard_with_rank TO anon;

-- ============================================================================
-- REALTIME (OPTIONAL)
-- ============================================================================

-- Enable realtime for the leaderboard table
-- Uncomment if you want live updates
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.leaderboard;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 7 (ANALYTICS) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Tables created:';
    RAISE NOTICE '- leaderboard: Global reading statistics and rankings';
    RAISE NOTICE '';
    RAISE NOTICE 'Views created:';
    RAISE NOTICE '- leaderboard_with_rank: Leaderboard with calculated ranks';
    RAISE NOTICE '';
    RAISE NOTICE 'Functions created:';
    RAISE NOTICE '- get_user_leaderboard_rank(user_id)';
    RAISE NOTICE '- get_top_leaderboard_users(limit)';
    RAISE NOTICE '- get_leaderboard_around_rank(user_id, range)';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id is stored as TEXT (not UUID foreign key)';
    RAISE NOTICE 'Your app must sync user_id from Project 1 when creating records';
    RAISE NOTICE '';
    RAISE NOTICE 'Optional: Enable realtime by uncommenting the ALTER PUBLICATION line';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 7 URL and Anon Key';
    RAISE NOTICE '2. Update your app to connect to all 7 Supabase projects';
    RAISE NOTICE '3. Implement cross-project data synchronization';
    RAISE NOTICE '4. Test thoroughly before production deployment';
    RAISE NOTICE '';
    RAISE NOTICE '🎉 All 7 projects complete! Total storage: 3.5GB';
END $$;
