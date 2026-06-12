-- Migration: Add cover_url column to synced_books
-- This enables popular books to display cover images from synced library data.

-- For Project 3 (Library) - split schema uses TEXT user_id
ALTER TABLE public.synced_books ADD COLUMN IF NOT EXISTS cover_url TEXT DEFAULT '';

-- For monolithic schema (schema.sql) - already covered by the same ALTER TABLE
-- The column addition is idempotent (IF NOT EXISTS)

-- Update the RPC function to return cover_url
CREATE OR REPLACE FUNCTION get_popular_books(p_limit INTEGER DEFAULT 50)
RETURNS TABLE (book_id TEXT, title TEXT, book_url TEXT, source_id BIGINT, reader_count INTEGER, last_read BIGINT, cover_url TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT sb.book_id, sb.title, sb.book_url, sb.source_id,
           COUNT(DISTINCT sb.user_id)::INTEGER as reader_count, MAX(sb.last_read) as last_read,
           COALESCE(MAX(sb.cover_url), '') as cover_url
    FROM public.synced_books sb
    GROUP BY sb.book_id, sb.title, sb.book_url, sb.source_id
    ORDER BY reader_count DESC, last_read DESC LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO anon;
