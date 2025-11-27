-- Migration: Add donation leaderboard functions
-- These functions calculate donation rankings based on purchased badges

-- ============================================================================
-- DONATION LEADERBOARD FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Function: Get donation leaderboard
-- Returns users ranked by total donation amount (sum of badge prices)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_donation_leaderboard(
    p_limit INTEGER DEFAULT 100,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    user_id TEXT,
    username TEXT,
    total_donation_amount DECIMAL,
    badge_count INTEGER,
    highest_badge_rarity TEXT,
    avatar_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.id::TEXT as user_id,
        COALESCE(u.username, split_part(u.email, '@', 1)) as username,
        COALESCE(SUM(b.price), 0) as total_donation_amount,
        COUNT(ub.badge_id)::INTEGER as badge_count,
        (
            SELECT b2.rarity
            FROM public.user_badges ub2
            JOIN public.badges b2 ON ub2.badge_id = b2.id
            WHERE ub2.user_id = u.id
            AND b2.type = 'PURCHASABLE'
            AND b2.price IS NOT NULL
            ORDER BY 
                CASE b2.rarity
                    WHEN 'legendary' THEN 4
                    WHEN 'epic' THEN 3
                    WHEN 'rare' THEN 2
                    WHEN 'common' THEN 1
                    ELSE 0
                END DESC
            LIMIT 1
        ) as highest_badge_rarity,
        NULL::TEXT as avatar_url
    FROM public.users u
    LEFT JOIN public.user_badges ub ON u.id = ub.user_id
    LEFT JOIN public.badges b ON ub.badge_id = b.id 
        AND b.type = 'PURCHASABLE' 
        AND b.price IS NOT NULL
    GROUP BY u.id, u.username, u.email
    HAVING COALESCE(SUM(b.price), 0) > 0
    ORDER BY total_donation_amount DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_donation_leaderboard(INTEGER, INTEGER) IS 
    'Returns donation leaderboard ranked by total badge purchase amount';


-- ----------------------------------------------------------------------------
-- Function: Get user's donation rank
-- Returns a specific user's donation stats and rank
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_donation_rank(p_user_id TEXT)
RETURNS TABLE (
    user_id TEXT,
    username TEXT,
    total_donation_amount DECIMAL,
    badge_count INTEGER,
    highest_badge_rarity TEXT,
    avatar_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.id::TEXT as user_id,
        COALESCE(u.username, split_part(u.email, '@', 1)) as username,
        COALESCE(SUM(b.price), 0) as total_donation_amount,
        COUNT(ub.badge_id)::INTEGER as badge_count,
        (
            SELECT b2.rarity
            FROM public.user_badges ub2
            JOIN public.badges b2 ON ub2.badge_id = b2.id
            WHERE ub2.user_id = u.id
            AND b2.type = 'PURCHASABLE'
            AND b2.price IS NOT NULL
            ORDER BY 
                CASE b2.rarity
                    WHEN 'legendary' THEN 4
                    WHEN 'epic' THEN 3
                    WHEN 'rare' THEN 2
                    WHEN 'common' THEN 1
                    ELSE 0
                END DESC
            LIMIT 1
        ) as highest_badge_rarity,
        NULL::TEXT as avatar_url
    FROM public.users u
    LEFT JOIN public.user_badges ub ON u.id = ub.user_id
    LEFT JOIN public.badges b ON ub.badge_id = b.id 
        AND b.type = 'PURCHASABLE' 
        AND b.price IS NOT NULL
    WHERE u.id::TEXT = p_user_id
    GROUP BY u.id, u.username, u.email;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_donation_rank(TEXT) IS 
    'Returns donation stats for a specific user';

-- ----------------------------------------------------------------------------
-- Function: Get user's donation badges
-- Returns all purchasable badges owned by a user with their prices
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_donation_badges(p_user_id TEXT)
RETURNS TABLE (
    badge_id TEXT,
    badge_name TEXT,
    badge_icon TEXT,
    badge_rarity TEXT,
    badge_price DECIMAL,
    earned_at TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.id as badge_id,
        b.name as badge_name,
        b.icon as badge_icon,
        b.rarity as badge_rarity,
        b.price as badge_price,
        ub.earned_at::TEXT as earned_at
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id::TEXT = p_user_id
    AND b.type = 'PURCHASABLE'
    AND b.price IS NOT NULL
    ORDER BY b.price DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_donation_badges(TEXT) IS 
    'Returns all purchasable badges owned by a user';

-- ============================================================================
-- GRANT PERMISSIONS
-- ============================================================================

-- Grant execute permissions to authenticated users
GRANT EXECUTE ON FUNCTION get_donation_leaderboard(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_donation_leaderboard(INTEGER, INTEGER) TO anon;
GRANT EXECUTE ON FUNCTION get_user_donation_rank(TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_donation_rank(TEXT) TO anon;
GRANT EXECUTE ON FUNCTION get_user_donation_badges(TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_donation_badges(TEXT) TO anon;
