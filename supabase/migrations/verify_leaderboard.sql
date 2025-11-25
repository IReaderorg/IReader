-- ============================================================================
-- Leaderboard Verification Script
-- Run this in Supabase SQL Editor to verify the leaderboard setup
-- ============================================================================

-- 1. Check if leaderboard table exists
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name = 'leaderboard';
-- Expected: 1 row with table_name = 'leaderboard'

-- 2. Check table structure
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'leaderboard'
ORDER BY ordinal_position;
-- Expected: 10 columns (id, user_id, username, etc.)

-- 3. Check if RLS is enabled
SELECT 
    tablename,
    rowsecurity
FROM pg_tables 
WHERE schemaname = 'public' 
  AND tablename = 'leaderboard';
-- Expected: rowsecurity = true

-- 4. Check RLS policies
SELECT 
    policyname,
    cmd,
    qual,
    with_check
FROM pg_policies 
WHERE schemaname = 'public'
  AND tablename = 'leaderboard'
ORDER BY policyname;
-- Expected: 4 policies (SELECT, INSERT, UPDATE, DELETE)

-- 5. Check indexes
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'leaderboard'
ORDER BY indexname;
-- Expected: 3-4 indexes

-- 6. Check current data
SELECT 
    COUNT(*) as total_entries,
    MAX(total_reading_time_minutes) as max_reading_time,
    MIN(total_reading_time_minutes) as min_reading_time,
    AVG(total_reading_time_minutes) as avg_reading_time
FROM public.leaderboard;
-- Shows current leaderboard statistics

-- 7. View top 10 users
SELECT 
    ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as rank,
    username,
    total_reading_time_minutes,
    total_chapters_read,
    books_completed,
    has_badge,
    badge_type,
    updated_at
FROM public.leaderboard
ORDER BY total_reading_time_minutes DESC
LIMIT 10;
-- Shows top 10 users on leaderboard

-- 8. Check if realtime is enabled
SELECT 
    schemaname,
    tablename
FROM pg_publication_tables
WHERE pubname = 'supabase_realtime'
  AND tablename = 'leaderboard';
-- Expected: 1 row if realtime is enabled

-- 9. Test insert permission (will fail if not authenticated)
-- DO NOT RUN THIS - Just for reference
-- INSERT INTO public.leaderboard (user_id, username, total_reading_time_minutes)
-- VALUES (auth.uid(), 'TestUser', 100);

-- 10. Check for your user
-- Replace 'your-email@example.com' with your actual email
SELECT 
    u.id as user_id,
    u.email,
    l.username,
    l.total_reading_time_minutes,
    l.total_chapters_read,
    l.books_completed,
    l.updated_at,
    ROW_NUMBER() OVER (ORDER BY l.total_reading_time_minutes DESC) as rank
FROM auth.users u
LEFT JOIN public.leaderboard l ON u.id = l.user_id
WHERE u.email = 'your-email@example.com';
-- Replace email and run to see if your entry exists

-- ============================================================================
-- TROUBLESHOOTING QUERIES
-- ============================================================================

-- If table doesn't exist, create it:
/*
CREATE TABLE IF NOT EXISTS public.leaderboard (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT NOT NULL,
    total_reading_time_minutes BIGINT NOT NULL DEFAULT 0,
    total_chapters_read INTEGER NOT NULL DEFAULT 0,
    books_completed INTEGER NOT NULL DEFAULT 0,
    reading_streak INTEGER NOT NULL DEFAULT 0,
    has_badge BOOLEAN NOT NULL DEFAULT FALSE,
    badge_type TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id)
);
*/

-- If RLS is not enabled:
/*
ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;
*/

-- If policies are missing:
/*
CREATE POLICY "Leaderboard is publicly readable" 
ON public.leaderboard FOR SELECT USING (true);

CREATE POLICY "Users can insert their own stats" 
ON public.leaderboard FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own stats" 
ON public.leaderboard FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own stats" 
ON public.leaderboard FOR DELETE USING (auth.uid() = user_id);
*/

-- If indexes are missing:
/*
CREATE INDEX IF NOT EXISTS idx_leaderboard_reading_time 
ON public.leaderboard(total_reading_time_minutes DESC);

CREATE INDEX IF NOT EXISTS idx_leaderboard_user_id 
ON public.leaderboard(user_id);
*/

-- If realtime is not enabled:
/*
ALTER PUBLICATION supabase_realtime ADD TABLE public.leaderboard;
*/

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'leaderboard'
    ) THEN
        RAISE NOTICE '✅ Leaderboard table exists!';
        RAISE NOTICE 'Run queries 1-10 above to verify complete setup.';
    ELSE
        RAISE NOTICE '❌ Leaderboard table does NOT exist!';
        RAISE NOTICE 'Run the CREATE TABLE statement in the troubleshooting section.';
    END IF;
END $$;
