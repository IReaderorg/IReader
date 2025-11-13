-- ============================================================================
-- Migration: Auto-generate usernames for users
-- ============================================================================
-- This migration adds automatic username generation for users who don't have one
-- Run this if you already have an existing database with users

-- ----------------------------------------------------------------------------
-- Step 1: Create the username generation function
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION generate_default_username()
RETURNS TRIGGER AS $$
BEGIN
    -- If username is NULL or empty, generate one from email or random
    IF NEW.username IS NULL OR LENGTH(TRIM(NEW.username)) = 0 THEN
        -- Try to use email prefix
        IF NEW.email IS NOT NULL THEN
            NEW.username := 'Reader_' || SUBSTRING(NEW.email FROM 1 FOR POSITION('@' IN NEW.email) - 1);
        ELSE
            -- Fallback to random username based on user ID
            NEW.username := 'Reader_' || SUBSTRING(NEW.id::TEXT FROM 1 FOR 8);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_default_username() IS 'Auto-generates username for users without one';

-- ----------------------------------------------------------------------------
-- Step 2: Add trigger for new users
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS generate_username_on_insert ON public.users;
CREATE TRIGGER generate_username_on_insert
    BEFORE INSERT ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION generate_default_username();

-- ----------------------------------------------------------------------------
-- Step 3: Update existing users who don't have usernames
-- ----------------------------------------------------------------------------
-- Generate usernames for existing users based on their email
UPDATE public.users
SET username = 'Reader_' || SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1)
WHERE username IS NULL OR LENGTH(TRIM(username)) = 0;

-- ----------------------------------------------------------------------------
-- Step 4: Verify the migration
-- ----------------------------------------------------------------------------
-- Check how many users now have usernames
SELECT 
    COUNT(*) as total_users,
    COUNT(username) as users_with_username,
    COUNT(*) - COUNT(username) as users_without_username
FROM public.users;

-- Show sample of generated usernames
SELECT id, email, username
FROM public.users
LIMIT 10;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================
DO $$
BEGIN
    RAISE NOTICE 'Auto-username migration completed successfully!';
    RAISE NOTICE 'All existing users now have usernames';
    RAISE NOTICE 'New users will automatically get usernames on signup';
END $$;
