-- Migration: Add views for reviews with user badges
-- These views make it easy to fetch reviews with username and primary badge info

-- Drop views if they exist
DROP VIEW IF EXISTS book_reviews_with_badges;
DROP VIEW IF EXISTS chapter_reviews_with_badges;

-- ============================================================================
-- Book Reviews with Badges View
-- ============================================================================

CREATE OR REPLACE VIEW book_reviews_with_badges AS
SELECT 
    br.id,
    br.user_id,
    br.book_title,
    br.rating,
    br.review_text,
    br.created_at,
    u.username,
    ub.badge_id,
    b.name as badge_name,
    b.icon as badge_icon,
    b.image_url as badge_image_url
FROM public.book_reviews br
LEFT JOIN public.users u ON br.user_id = u.id
LEFT JOIN public.user_badges ub ON br.user_id = ub.user_id AND ub.is_primary = true
LEFT JOIN public.badges b ON ub.badge_id = b.id
ORDER BY br.created_at DESC;

-- Grant access to the view
GRANT SELECT ON book_reviews_with_badges TO authenticated;
GRANT SELECT ON book_reviews_with_badges TO anon;

COMMENT ON VIEW book_reviews_with_badges IS 'Book reviews with username and primary badge information';

-- ============================================================================
-- Chapter Reviews with Badges View
-- ============================================================================

CREATE OR REPLACE VIEW chapter_reviews_with_badges AS
SELECT 
    cr.id,
    cr.user_id,
    cr.book_title,
    cr.chapter_name,
    cr.rating,
    cr.review_text,
    cr.created_at,
    u.username,
    ub.badge_id,
    b.name as badge_name,
    b.icon as badge_icon,
    b.image_url as badge_image_url
FROM public.chapter_reviews cr
LEFT JOIN public.users u ON cr.user_id = u.id
LEFT JOIN public.user_badges ub ON cr.user_id = ub.user_id AND ub.is_primary = true
LEFT JOIN public.badges b ON ub.badge_id = b.id
ORDER BY cr.created_at DESC;

-- Grant access to the view
GRANT SELECT ON chapter_reviews_with_badges TO authenticated;
GRANT SELECT ON chapter_reviews_with_badges TO anon;

COMMENT ON VIEW chapter_reviews_with_badges IS 'Chapter reviews with username and primary badge information';

-- ============================================================================
-- Helper Functions (Optional - for more complex queries)
-- ============================================================================

-- Function to get book reviews with pagination
CREATE OR REPLACE FUNCTION get_book_reviews_with_badges(
    p_limit INTEGER DEFAULT 50,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    user_id UUID,
    book_title TEXT,
    rating INTEGER,
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    username TEXT,
    badge_id TEXT,
    badge_name TEXT,
    badge_icon TEXT,
    badge_image_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        br.id,
        br.user_id,
        br.book_title,
        br.rating,
        br.review_text,
        br.created_at,
        u.username,
        ub.badge_id,
        b.name as badge_name,
        b.icon as badge_icon,
        b.image_url as badge_image_url
    FROM public.book_reviews br
    LEFT JOIN public.users u ON br.user_id = u.id
    LEFT JOIN public.user_badges ub ON br.user_id = ub.user_id AND ub.is_primary = true
    LEFT JOIN public.badges b ON ub.badge_id = b.id
    ORDER BY br.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission
GRANT EXECUTE ON FUNCTION get_book_reviews_with_badges(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_book_reviews_with_badges(INTEGER, INTEGER) TO anon;

COMMENT ON FUNCTION get_book_reviews_with_badges(INTEGER, INTEGER) IS 'Get book reviews with username and badge info, with pagination';

-- Function to get chapter reviews with pagination
CREATE OR REPLACE FUNCTION get_chapter_reviews_with_badges(
    p_limit INTEGER DEFAULT 50,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    user_id UUID,
    book_title TEXT,
    chapter_name TEXT,
    rating INTEGER,
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    username TEXT,
    badge_id TEXT,
    badge_name TEXT,
    badge_icon TEXT,
    badge_image_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cr.id,
        cr.user_id,
        cr.book_title,
        cr.chapter_name,
        cr.rating,
        cr.review_text,
        cr.created_at,
        u.username,
        ub.badge_id,
        b.name as badge_name,
        b.icon as badge_icon,
        b.image_url as badge_image_url
    FROM public.chapter_reviews cr
    LEFT JOIN public.users u ON cr.user_id = u.id
    LEFT JOIN public.user_badges ub ON cr.user_id = ub.user_id AND ub.is_primary = true
    LEFT JOIN public.badges b ON ub.badge_id = b.id
    ORDER BY cr.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission
GRANT EXECUTE ON FUNCTION get_chapter_reviews_with_badges(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_chapter_reviews_with_badges(INTEGER, INTEGER) TO anon;

COMMENT ON FUNCTION get_chapter_reviews_with_badges(INTEGER, INTEGER) IS 'Get chapter reviews with username and badge info, with pagination';

-- ============================================================================
-- Verification
-- ============================================================================

-- Test the views
-- SELECT * FROM book_reviews_with_badges LIMIT 5;
-- SELECT * FROM chapter_reviews_with_badges LIMIT 5;

-- Test the functions
-- SELECT * FROM get_book_reviews_with_badges(10, 0);
-- SELECT * FROM get_chapter_reviews_with_badges(10, 0);
