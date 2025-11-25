-- ============================================================================
-- PROJECT 1: AUTH DATABASE SCHEMA
-- ============================================================================
-- This schema contains ONLY user authentication and profiles
-- Tables: users
-- Estimated storage: ~500MB for 5M+ user profiles
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
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT username_length CHECK (username IS NULL OR LENGTH(username) >= 3),
    CONSTRAINT eth_wallet_format CHECK (eth_wallet_address IS NULL OR eth_wallet_address ~* '^0x[a-fA-F0-9]{40}$')
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON public.users(username);
CREATE INDEX IF NOT EXISTS idx_users_eth_wallet ON public.users(eth_wallet_address) WHERE eth_wallet_address IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_is_admin ON public.users(is_admin) WHERE is_admin = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_is_supporter ON public.users(is_supporter) WHERE is_supporter = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_created_at ON public.users(created_at DESC);

-- Comments
COMMENT ON TABLE public.users IS 'User profiles and authentication data (Project 1 - Auth)';
COMMENT ON COLUMN public.users.id IS 'User ID from Supabase Auth';
COMMENT ON COLUMN public.users.email IS 'User email address';
COMMENT ON COLUMN public.users.username IS 'Display username (auto-generated if not provided)';
COMMENT ON COLUMN public.users.eth_wallet_address IS 'Ethereum wallet address for NFT verification';
COMMENT ON COLUMN public.users.is_supporter IS 'Whether user is a supporter/premium member';
COMMENT ON COLUMN public.users.is_admin IS 'Whether user has admin privileges';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

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

-- Public can view usernames (for reviews, leaderboard, etc.)
CREATE POLICY "Public can view basic user info"
    ON public.users FOR SELECT
    USING (true);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- Update timestamp function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates updated_at timestamp';

-- Generate default username
CREATE OR REPLACE FUNCTION generate_default_username()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.username IS NULL OR LENGTH(TRIM(NEW.username)) = 0 THEN
        IF NEW.email IS NOT NULL THEN
            NEW.username := 'Reader_' || SUBSTRING(NEW.email FROM 1 FOR POSITION('@' IN NEW.email) - 1);
        ELSE
            NEW.username := 'Reader_' || SUBSTRING(NEW.id::TEXT FROM 1 FOR 8);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_default_username() IS 'Auto-generates username for users without one';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS update_users_updated_at ON public.users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS generate_username_on_insert ON public.users;
CREATE TRIGGER generate_username_on_insert
    BEFORE INSERT ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION generate_default_username();

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 1 (AUTH) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Table created:';
    RAISE NOTICE '- users: User profiles and authentication';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- Auto-generated usernames';
    RAISE NOTICE '- Email validation';
    RAISE NOTICE '- Ethereum wallet support';
    RAISE NOTICE '- Admin and supporter flags';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~5M user profiles in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Set up authentication in Supabase Dashboard';
    RAISE NOTICE '2. Note your Project 1 URL and Anon Key';
    RAISE NOTICE '3. Create Project 2 for reading progress (schema_2_reading.sql)';
END $$;
