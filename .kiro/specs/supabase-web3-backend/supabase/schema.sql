-- Supabase Database Schema for IReader Web3 Backend
-- This file contains the complete database schema including tables, indexes, and RLS policies

-- ============================================================================
-- USERS TABLE
-- ============================================================================
-- Stores user profiles with wallet addresses as primary identifiers

CREATE TABLE IF NOT EXISTS users (
    wallet_address TEXT PRIMARY KEY,
    username TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    is_supporter BOOLEAN DEFAULT FALSE
);

-- Create index for faster username lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can read all user profiles
CREATE POLICY "Users can read all users"
    ON users FOR SELECT
    USING (true);

-- RLS Policy: Users can insert their own profile
-- Note: With Web3 auth, we'll use JWT claims to store wallet_address
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.jwt() ->> 'wallet_address' = wallet_address);

-- RLS Policy: Users can update only their own profile
CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.jwt() ->> 'wallet_address' = wallet_address);

-- ============================================================================
-- READING_PROGRESS TABLE
-- ============================================================================
-- Stores reading progress for each user and book combination

CREATE TABLE IF NOT EXISTS reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_wallet_address TEXT NOT NULL REFERENCES users(wallet_address) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position REAL NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_wallet_address, book_id)
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_reading_progress_user ON reading_progress(user_wallet_address);
CREATE INDEX IF NOT EXISTS idx_reading_progress_book ON reading_progress(book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_updated ON reading_progress(updated_at);

-- Enable Row Level Security
ALTER TABLE reading_progress ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can only access their own reading progress
-- Note: With Web3 auth, we'll use JWT claims to store wallet_address
CREATE POLICY "Users can manage own progress"
    ON reading_progress
    FOR ALL
    USING (auth.jwt() ->> 'wallet_address' = user_wallet_address);

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

-- ============================================================================
-- REALTIME PUBLICATION
-- ============================================================================
-- Enable realtime for reading_progress table

ALTER PUBLICATION supabase_realtime ADD TABLE reading_progress;

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON TABLE users IS 'User profiles with Web3 wallet authentication';
COMMENT ON COLUMN users.wallet_address IS 'Ethereum wallet address (primary key and user identifier)';
COMMENT ON COLUMN users.username IS 'Optional display name chosen by user';
COMMENT ON COLUMN users.is_supporter IS 'Flag indicating if user is a supporter/premium member';

COMMENT ON TABLE reading_progress IS 'Reading progress tracking for books across devices';
COMMENT ON COLUMN reading_progress.book_id IS 'Normalized book identifier (e.g., "lord-of-the-mysteries")';
COMMENT ON COLUMN reading_progress.last_chapter_slug IS 'Identifier for the last read chapter';
COMMENT ON COLUMN reading_progress.last_scroll_position IS 'Scroll position within the chapter (0.0 to 1.0)';
