-- Migration: Add book_url column to synced_books table
-- This fixes the missing book_url column error

-- Add the book_url column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'synced_books' 
        AND column_name = 'book_url'
    ) THEN
        ALTER TABLE public.synced_books 
        ADD COLUMN book_url TEXT NOT NULL DEFAULT '';
        
        -- Add constraint
        ALTER TABLE public.synced_books 
        ADD CONSTRAINT book_url_not_empty CHECK (LENGTH(book_url) > 0);
        
        -- Add comment
        COMMENT ON COLUMN public.synced_books.book_url IS 'Book URL/link on the source';
        
        RAISE NOTICE 'Added book_url column to synced_books table';
    ELSE
        RAISE NOTICE 'book_url column already exists';
    END IF;
END $$;
