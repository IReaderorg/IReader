-- Remove unique constraints to allow multiple reviews per user

-- Drop unique constraint on book_reviews
ALTER TABLE public.book_reviews 
DROP CONSTRAINT IF EXISTS unique_user_book_review;

-- Drop unique constraint on chapter_reviews  
ALTER TABLE public.chapter_reviews
DROP CONSTRAINT IF EXISTS unique_user_chapter_review;

-- Verify constraints are removed
SELECT 
    conname AS constraint_name,
    conrelid::regclass AS table_name
FROM pg_constraint
WHERE conname LIKE '%unique_user%';

-- Should return empty result if constraints are removed successfully
