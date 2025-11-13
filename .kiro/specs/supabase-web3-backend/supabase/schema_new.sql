-- Supabase Database Schema for IReader with Email Authentication
-- This schema replaces Web3 wallet authentication with email/password authentication

-- ============================================================================
-- USERS TABLE
-- ============================================================================
-- Stores user profiles with email authentication

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    username TEXT,
    eth_wallet_address TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    is_supporter BOOLEAN DEFAULT FALSE
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_eth_wallet ON users(eth_wallet_address);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can read all user profiles
CREATE POLICY "Users can read all users"
    ON users FOR SELECT
    USING (true);

-- RLS Policy: Users can insert their own profile
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- RLS Policy: Users can update only their own profile
CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid() = id);

-- ============================================================================
-- READING_PROGRESS TABLE
-- ============================================================================
-- Stores reading progress for each user and book combination

CREATE TABLE IF NOT EXISTS reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position REAL NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, book_id)
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_reading_progress_user ON reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_book ON reading_progress(book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_updated ON reading_progress(updated_at);

-- Enable Row Level Security
ALTER TABLE reading_progress ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can only access their own reading progress
CREATE POLICY "Users can manage own progress"
    ON reading_progress
    FOR ALL
    USING (auth.uid() = user_id);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update updated_at on reading_progress updates
DROP TRIGGER IF EXISTS update_reading_progress_updated_at ON reading_progress;
CREATE TRIGGER update_reading_progress_updated_at
    BEFORE UPDATE ON reading_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to automatically create user profile on signup
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, username, eth_wallet_address, is_supporter)
    VALUES (NEW.id, NEW.email, NULL, NULL, FALSE);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create user profile on auth.users insert
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION handle_new_user();

-- ============================================================================
-- REALTIME PUBLICATION
-- ============================================================================
-- Enable realtime for reading_progress table

ALTER PUBLICATION supabase_realtime ADD TABLE reading_progress;

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON TABLE users IS 'User profiles with email authentication';
COMMENT ON COLUMN users.id IS 'User ID from auth.users';
COMMENT ON COLUMN users.email IS 'User email address';
COMMENT ON COLUMN users.username IS 'Optional display name chosen by user';
COMMENT ON COLUMN users.eth_wallet_address IS 'Optional Ethereum wallet address used as API key';
COMMENT ON COLUMN users.is_supporter IS 'Flag indicating if user is a supporter/premium member';

COMMENT ON TABLE reading_progress IS 'Reading progress tracking for books across devices';
COMMENT ON COLUMN reading_progress.book_id IS 'Normalized book identifier (e.g., "lord-of-the-mysteries")';
COMMENT ON COLUMN reading_progress.last_chapter_slug IS 'Identifier for the last read chapter';
COMMENT ON COLUMN reading_progress.last_scroll_position IS 'Scroll position within the chapter (0.0 to 1.0)';
