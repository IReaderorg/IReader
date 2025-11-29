-- ============================================================================
-- MIGRATION: Update Badge Icon URLs
-- ============================================================================
-- This migration updates the image_url column for all badges with new
-- icon URLs from the badge-repo repository.
-- ============================================================================

-- Update badge icon URLs
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bronze-min.png' WHERE id = 'donor_bronze';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/silver-min.png' WHERE id = 'donor_silver';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/gold-min.png' WHERE id = 'donor_gold';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/platinum-min.png' WHERE id = 'donor_platinum';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/translator-min.png' WHERE id = 'contributor_translator';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/developer-min.png' WHERE id = 'contributor_developer';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/designer-min.png' WHERE id = 'contributor_designer';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/novice-reader-min.png' WHERE id = 'novice_reader';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/avid-reader-min.png' WHERE id = 'avid_reader';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bookworm-min.png' WHERE id = 'bookworm';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/master-reader-min.png' WHERE id = 'master_reader';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/first-critic-min.png' WHERE id = 'first_critic';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/thoughtful-critic-min.png' WHERE id = 'thoughtful_critic';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/master-critic-min.png' WHERE id = 'master_critic';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/week-warrior-min.png' WHERE id = 'week_warrior';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/month-master-min.png' WHERE id = 'month_master';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/year-legend-min.png' WHERE id = 'year_legend';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/night-owl-min.png' WHERE id = 'night_owl';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/early-bird-min.png' WHERE id = 'early_bird';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/speed-reader-min.png' WHERE id = 'speed_reader';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/early-adopter-min.png' WHERE id = 'special_early_adopter';
UPDATE public.badges SET image_url = 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/bug-hunter-min.png' WHERE id = 'special_bug_hunter';

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ Badge icon URLs migration completed!';
    RAISE NOTICE '';
    RAISE NOTICE 'Updated badges:';
    RAISE NOTICE '- 4 donor badges (bronze, silver, gold, platinum)';
    RAISE NOTICE '- 3 contributor badges (translator, developer, designer)';
    RAISE NOTICE '- 4 reader progress badges (novice, avid, bookworm, master)';
    RAISE NOTICE '- 3 reviewer badges (first, thoughtful, master critic)';
    RAISE NOTICE '- 3 streak badges (week, month, year)';
    RAISE NOTICE '- 2 time-based badges (night owl, early bird)';
    RAISE NOTICE '- 1 speed badge (speed reader)';
    RAISE NOTICE '- 2 special badges (early adopter, bug hunter)';
    RAISE NOTICE '';
    RAISE NOTICE 'Total: 22 badges updated with new icon URLs';
END $$;
