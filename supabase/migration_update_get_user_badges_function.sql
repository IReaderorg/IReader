-- Migration: Update get_user_badges function to include image_url and type
-- This migration updates the get_user_badges function to return badge image URLs and types

-- Drop the old function
DROP FUNCTION IF EXISTS get_user_badges(UUID);

-- Create the updated function with image_url and type fields
CREATE OR REPLACE FUNCTION get_user_badges(p_user_id UUID)
RETURNS TABLE (
    badge_id TEXT,
    badge_name TEXT,
    badge_description TEXT,
    badge_icon TEXT,
    badge_category TEXT,
    badge_rarity TEXT,
    badge_image_url TEXT,
    badge_type TEXT,
    is_primary BOOLEAN,
    is_featured BOOLEAN,
    earned_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.id,
        b.name,
        b.description,
        b.icon,
        b.category,
        b.rarity,
        b.image_url,
        b.type,
        ub.is_primary,
        ub.is_featured,
        ub.earned_at,
        ub.metadata
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id = p_user_id
    ORDER BY ub.earned_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_badges(UUID) IS 'Returns all badges earned by a user with image_url, type, and display flags';

-- Grant execute permission to authenticated users
GRANT EXECUTE ON FUNCTION get_user_badges(UUID) TO authenticated;
