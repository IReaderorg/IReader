-- ============================================================================
-- MIGRATION: Sync Badge Icon URLs from docs/icons.json
-- ============================================================================
-- This migration removes all existing badges and re-inserts them
-- based on docs/icons.json
-- ============================================================================

-- First, remove all user_badges references (due to foreign key constraint)
DELETE FROM public.user_badges;

-- Remove all existing badges
DELETE FROM public.badges;

-- Insert all badges from docs/icons.json

-- Donor badges (PURCHASABLE - shown in badge store)
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available, price) VALUES
    ('donor_bronze', 'Bronze Supporter', 'Donated $5 or more', '🥉', 'donor', 'common', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bronze-supporter.png', TRUE, 5.00),
    ('donor_silver', 'Silver Supporter', 'Donated $10 or more', '🥈', 'donor', 'rare', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/silver-min.png', TRUE, 10.00),
    ('donor_gold', 'Gold Supporter', 'Donated $25 or more', '🥇', 'donor', 'epic', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/gold-min.png', TRUE, 25.00),
    ('donor_platinum', 'Platinum Supporter', 'Donated $50 or more', '💎', 'donor', 'legendary', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/platinum-min.png', TRUE, 50.00),
    ('donor_coffee', 'Coffee Supporter', 'Bought us a coffee', '☕', 'donor', 'common', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/coffee-supporter.png', TRUE, 3.00),
    ('donor_early', 'Early Supporter', 'Supported during early development', '🌱', 'donor', 'rare', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/early-supporter.png', TRUE, 15.00),
    ('donor_lifetime', 'Lifetime Supporter', 'Made a lifetime contribution', '♾️', 'donor', 'legendary', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/lifetime-supporter.png', TRUE, 100.00),
    ('donor_ultimate', 'Ultimate Benefactor', 'Made an extraordinary contribution', '👑', 'donor', 'legendary', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/ultimate-benefactor.png', TRUE, 200.00),
    ('patreon_generous', 'Generous Patreon', 'Generous Patreon supporter', '🎁', 'donor', 'epic', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/generous-pathreon.png', TRUE, 30.00),
    ('patreon_legendary', 'Legendary Patreon', 'Legendary Patreon supporter', '🏆', 'donor', 'legendary', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/legendary-patreon.png', TRUE, 75.00);

-- Contributor badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('contributor_translator', 'Translator', 'Contributed translations', '🌐', 'contributor', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/translator-min.png', TRUE),
    ('contributor_developer', 'Developer', 'Contributed code', '💻', 'contributor', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/developer-min.png', TRUE),
    ('contributor_designer', 'Designer', 'Contributed designs', '🎨', 'contributor', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/designer-min.png', TRUE),
    ('contributor_code', 'Code Contributor', 'Contributed code to the project', '🔧', 'contributor', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/code-contributor.png', TRUE);

-- Reader badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('novice_reader', 'Novice Reader', 'Read your first 10 chapters', '📖', 'reader', 'common', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/novice-reader-min.png', TRUE),
    ('avid_reader', 'Avid Reader', 'Read 100 chapters', '📚', 'reader', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/avid-reader-min.png', TRUE),
    ('bookworm', 'Bookworm', 'Read 500 chapters', '🐛', 'reader', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bookworm-min.png', TRUE),
    ('master_reader', 'Master Reader', 'Read 1000 chapters', '🎓', 'reader', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/master-reader-min.png', TRUE),
    ('marathon_reader', 'Marathon Reader', 'Read for an extended period without breaks', '🏃', 'reader', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/marathond-reader.png', TRUE),
    ('scholar', 'Scholar', 'Demonstrated exceptional reading knowledge', '🎓', 'reader', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/scholar.png', TRUE),
    ('week_warrior', 'Week Warrior', 'Read for 7 consecutive days', '🔥', 'reader', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/week-warrior-min.png', TRUE),
    ('month_master', 'Month Master', 'Read for 30 consecutive days', '⚡', 'reader', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/month-master-min.png', TRUE),
    ('year_legend', 'Year Legend', 'Read for 365 consecutive days', '🌟', 'reader', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/year-legend-min.png', TRUE),
    ('speed_reader', 'Speed Reader', 'Read 50 chapters in a single day', '⚡', 'reader', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/speed-reader-min.png', TRUE),
    ('first_finish', 'First Finish', 'Finished your first book', '🏁', 'reader', 'common', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/first-finish.png', TRUE);


-- Reviewer badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('first_critic', 'First Critic', 'Write your first review', '✍️', 'reviewer', 'common', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/first-critic-min.png', TRUE),
    ('thoughtful_critic', 'Thoughtful Critic', 'Write 10 reviews', '🤔', 'reviewer', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/thoughtful-critic-min.png', TRUE),
    ('master_critic', 'Master Critic', 'Write 50 reviews', '🎭', 'reviewer', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/master-critic-min.png', TRUE),
    ('critic', 'Critic', 'Write reviews regularly', '📝', 'reviewer', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/critic.png', TRUE),
    ('expert_reviewer', 'Expert Reviewer', 'Write high-quality detailed reviews', '⭐', 'reviewer', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/expert-reviewer.png', TRUE),
    ('legendary_critic', 'Legendary Critic', 'Achieved legendary status as a reviewer', '🌟', 'reviewer', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/legendary-critic.png', TRUE);

-- Special badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('night_owl', 'Night Owl', 'Read 100 chapters between 10 PM and 6 AM', '🦉', 'special', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/night-owl-min.png', TRUE),
    ('early_bird', 'Early Bird', 'Read 100 chapters between 5 AM and 9 AM', '🐦', 'special', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/early-bird-min.png', TRUE),
    ('special_early_adopter', 'Early Adopter', 'Joined during beta', '🚀', 'special', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/early-adopter-min.png', TRUE),
    ('special_bug_hunter', 'Bug Hunter', 'Reported critical bugs', '🐛', 'special', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bug-hunter-min.png', TRUE),
    ('special_founding_member', 'Founding Member', 'One of the original founding members', '🏛️', 'special', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/founding-member.png', TRUE),
    ('book_collector', 'Book Collector', 'Added many books to your library', '📚', 'special', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/book-collector.png', TRUE),
    ('legendary_collector', 'Legendary Collector', 'Amassed an extraordinary book collection', '📖', 'special', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/legendary-collector.png', TRUE),
    ('library_master', 'Library Master', 'Mastered the art of library organization', '🏛️', 'special', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/library-master.png', TRUE),
    ('book_patron', 'Book Patron', 'A true patron of literature', '📜', 'special', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/book-patron.png', TRUE);
