-- Create leaderboard table for tracking user reading statistics
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
    
    -- Ensure one entry per user
    UNIQUE(user_id)
);

-- Create index for faster leaderboard queries (ordered by reading time)
CREATE INDEX IF NOT EXISTS idx_leaderboard_reading_time 
ON public.leaderboard(total_reading_time_minutes DESC);

-- Create index for user lookups
CREATE INDEX IF NOT EXISTS idx_leaderboard_user_id 
ON public.leaderboard(user_id);

-- Create index for username searches
CREATE INDEX IF NOT EXISTS idx_leaderboard_username 
ON public.leaderboard(username);

-- Enable Row Level Security
ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;

-- Policy: Anyone can read the leaderboard
CREATE POLICY "Leaderboard is publicly readable"
ON public.leaderboard
FOR SELECT
USING (true);

-- Policy: Users can insert their own stats
CREATE POLICY "Users can insert their own stats"
ON public.leaderboard
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: Users can update their own stats
CREATE POLICY "Users can update their own stats"
ON public.leaderboard
FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Policy: Users can delete their own stats
CREATE POLICY "Users can delete their own stats"
ON public.leaderboard
FOR DELETE
USING (auth.uid() = user_id);

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_leaderboard_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to call the function before update
CREATE TRIGGER update_leaderboard_timestamp
BEFORE UPDATE ON public.leaderboard
FOR EACH ROW
EXECUTE FUNCTION update_leaderboard_updated_at();

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

-- Grant access to the view
GRANT SELECT ON public.leaderboard_with_rank TO authenticated;
GRANT SELECT ON public.leaderboard_with_rank TO anon;

-- Enable realtime for the leaderboard table
ALTER PUBLICATION supabase_realtime ADD TABLE public.leaderboard;

-- Add comments for documentation
COMMENT ON TABLE public.leaderboard IS 'Stores user reading statistics for the leaderboard feature';
COMMENT ON COLUMN public.leaderboard.user_id IS 'Reference to the user in auth.users';
COMMENT ON COLUMN public.leaderboard.username IS 'Display name for the leaderboard';
COMMENT ON COLUMN public.leaderboard.total_reading_time_minutes IS 'Total reading time in minutes';
COMMENT ON COLUMN public.leaderboard.total_chapters_read IS 'Total number of chapters read';
COMMENT ON COLUMN public.leaderboard.books_completed IS 'Total number of books completed';
COMMENT ON COLUMN public.leaderboard.reading_streak IS 'Current reading streak in days';
COMMENT ON COLUMN public.leaderboard.has_badge IS 'Whether the user has purchased a badge';
COMMENT ON COLUMN public.leaderboard.badge_type IS 'Type of badge (supporter, nft, premium, etc.)';
