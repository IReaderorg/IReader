-- ============================================================================
-- Migration: Add username generation for existing database
-- ============================================================================
-- This migration is for databases that already have users, book_reviews, 
-- and chapter_reviews tables. It adds automatic username generation.

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
DO $$
DECLARE
    total_count INTEGER;
    with_username_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_count FROM public.users;
    SELECT COUNT(*) INTO with_username_count FROM public.users WHERE username IS NOT NULL AND LENGTH(TRIM(username)) > 0;
    
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'Username Migration Results:';
    RAISE NOTICE 'Total users: %', total_count;
    RAISE NOTICE 'Users with username: %', with_username_count;
    RAISE NOTICE '===========================================';
    
    IF with_username_count = total_count THEN
        RAISE NOTICE '✓ SUCCESS: All users now have usernames!';
    ELSE
        RAISE WARNING '⚠ WARNING: % users still without username', (total_count - with_username_count);
    END IF;
END $$;

-- Show sample of users with their new usernames
SELECT 
    SUBSTRING(id::TEXT FROM 1 FOR 8) as user_id,
    email,
    username,
    created_at
FROM public.users
ORDER BY created_at DESC
LIMIT 5;

-- ============================================================================
-- IMPORTANT NOTES
-- ============================================================================
-- 
-- After running this migration:
-- 
-- 1. All existing users will have usernames
-- 2. New users will automatically get usernames on signup
-- 3. Reviews will show usernames via JOIN query:
--    SELECT *, users!inner(username) FROM book_reviews
-- 
-- The review tables (book_reviews, chapter_reviews) DO NOT need username columns.
-- Usernames are fetched via JOIN with the users table.
-- 
-- This is the correct database design because:
-- - No data duplication
-- - If user changes username, all reviews automatically show new name
-- - More efficient storage
-- 
-- ============================================================================
