-- Migration: Remove source_id_positive constraint
-- This allows large source IDs that may appear negative when stored as BIGINT

-- Drop the constraint if it exists
ALTER TABLE public.synced_books 
DROP CONSTRAINT IF EXISTS source_id_positive;

-- Verify the constraint is removed
SELECT conname, contype, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'public.synced_books'::regclass 
AND conname = 'source_id_positive';
