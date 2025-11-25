-- Migration: Add get_popular_books function
-- This function efficiently retrieves the most popular books based on reader count

-- Drop function if exists
DROP FUNCTION IF EXISTS get_popular_books(INTEGER);

-- Create function to get popular books
CREATE OR REPLACE FUNCTION get_popular_books(p_limit INTEGER DEFAULT 50)
RETURNS TABLE (
    book_id TEXT,
    title TEXT,
    book_url TEXT,
    source_id BIGINT,
    reader_count INTEGER,
    last_read BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sb.book_id,
        sb.title,
        sb.book_url,
        sb.source_id,
        COUNT(DISTINCT sb.user_id)::INTEGER as reader_count,
        MAX(sb.last_read) as last_read
    FROM public.synced_books sb
    GROUP BY sb.book_id, sb.title, sb.book_url, sb.source_id
    ORDER BY reader_count DESC, last_read DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission to authenticated users
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO anon;

COMMENT ON FUNCTION get_popular_books(INTEGER) IS 'Returns the most popular books based on how many users are reading them';
