-- Migration: Add usage-based achievement badges
-- This migration adds achievement badges that users can earn through app usage

-- Insert achievement badges for readers
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, is_available, image_url)
VALUES
    -- Reading achievement badges
    ('novice_reader', 'Novice Reader', 'Read your first 10 chapters', '📖', 'reader', 'common', 'ACHIEVEMENT', TRUE, NULL),
    ('avid_reader', 'Avid Reader', 'Read 100 chapters', '📚', 'reader', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    ('bookworm', 'Bookworm', 'Read 500 chapters', '🐛', 'reader', 'epic', 'ACHIEVEMENT', TRUE, NULL),
    ('master_reader', 'Master Reader', 'Read 1000 chapters', '🎓', 'reader', 'legendary', 'ACHIEVEMENT', TRUE, NULL),
    
    -- Book completion badges
    ('first_finish', 'First Finish', 'Complete your first book', '🏁', 'reader', 'common', 'ACHIEVEMENT', TRUE, NULL),
    ('book_collector', 'Book Collector', 'Complete 10 books', '📕', 'reader', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    ('library_master', 'Library Master', 'Complete 50 books', '📚', 'reader', 'epic', 'ACHIEVEMENT', TRUE, NULL),
    ('legendary_collector', 'Legendary Collector', 'Complete 100 books', '👑', 'reader', 'legendary', 'ACHIEVEMENT', TRUE, NULL),
    
    -- Review/Critic badges
    ('first_critic', 'First Critic', 'Write your first review', '✍️', 'reviewer', 'common', 'ACHIEVEMENT', TRUE, NULL),
    ('thoughtful_critic', 'Thoughtful Critic', 'Write 10 reviews', '💭', 'reviewer', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    ('master_critic', 'Master Critic', 'Write 50 reviews', '🎭', 'reviewer', 'epic', 'ACHIEVEMENT', TRUE, NULL),
    ('legendary_critic', 'Legendary Critic', 'Write 100 reviews', '🏆', 'reviewer', 'legendary', 'ACHIEVEMENT', TRUE, NULL),
    
    -- Reading streak badges
    ('week_warrior', 'Week Warrior', 'Read for 7 consecutive days', '🔥', 'reader', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    ('month_master', 'Month Master', 'Read for 30 consecutive days', '⚡', 'reader', 'epic', 'ACHIEVEMENT', TRUE, NULL),
    ('year_legend', 'Year Legend', 'Read for 365 consecutive days', '🌟', 'reader', 'legendary', 'ACHIEVEMENT', TRUE, NULL),
    
    -- Time-based reading badges
    ('night_owl', 'Night Owl', 'Read 100 chapters between 10 PM and 6 AM', '🦉', 'special', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    ('early_bird', 'Early Bird', 'Read 100 chapters between 5 AM and 9 AM', '🐦', 'special', 'rare', 'ACHIEVEMENT', TRUE, NULL),
    
    -- Speed reading badges
    ('speed_reader', 'Speed Reader', 'Read 50 chapters in a single day', '⚡', 'reader', 'epic', 'ACHIEVEMENT', TRUE, NULL),
    ('marathon_reader', 'Marathon Reader', 'Read for 12 hours in a single day', '🏃', 'reader', 'legendary', 'ACHIEVEMENT', TRUE, NULL)
ON CONFLICT (id) DO NOTHING;

-- Create function to check and award achievement badges
CREATE OR REPLACE FUNCTION check_and_award_achievement_badge(
    p_user_id UUID,
    p_badge_id TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_already_has_badge BOOLEAN;
BEGIN
    -- Check if user already has the badge
    SELECT EXISTS(
        SELECT 1 FROM public.user_badges
        WHERE user_id = p_user_id AND badge_id = p_badge_id
    ) INTO v_already_has_badge;
    
    -- If user doesn't have the badge, award it
    IF NOT v_already_has_badge THEN
        INSERT INTO public.user_badges (user_id, badge_id, earned_at)
        VALUES (p_user_id, p_badge_id, NOW())
        ON CONFLICT (user_id, badge_id) DO NOTHING;
        
        RETURN TRUE;
    END IF;
    
    RETURN FALSE;
END;
$$;

-- Grant execute permission
GRANT EXECUTE ON FUNCTION check_and_award_achievement_badge(UUID, TEXT) TO authenticated;

COMMENT ON FUNCTION check_and_award_achievement_badge IS 'Checks if user qualifies for and awards an achievement badge';
