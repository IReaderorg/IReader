-- ============================================================================
-- Migration 002: Seed achievement_definitions + supporting badges
-- ============================================================================
-- Tier rewards:  BRONZE 25xp/10st · SILVER 75/25 · GOLD 200/50 ·
--                PLATINUM 500/100 · LEGENDARY 1200/250
-- icon = emoji fallback; image_url stays NULL until artwork is uploaded
-- (see plans/image-prompts.md). reward_badge_id must reference a real badge.
-- ============================================================================

-- Badge granted by the Discord-linked achievement (must exist for FK on grant)
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, is_available)
VALUES ('verified_reader', 'Verified Reader', 'Linked a Discord account to IReader',
        '🎮', 'special', 'rare', 'ACHIEVEMENT', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.achievement_definitions
    (id, name, description, icon, category, tier, metric, threshold,
     reward_xp, reward_stones, reward_title_id, reward_badge_id, sort_order)
VALUES
-- ⏱ Reading time (minutes)
('reading_time_1','First Hour','Read for 1 hour total','⏱','READING_TIME','BRONZE','READING_MINUTES',60,25,10,NULL,NULL,10),
('reading_time_2','Ten Hours','Read for 10 hours total','⏱','READING_TIME','BRONZE','READING_MINUTES',600,25,10,NULL,NULL,11),
('reading_time_3','Fifty Hours','Read for 50 hours total','⏱','READING_TIME','SILVER','READING_MINUTES',3000,75,25,NULL,NULL,12),
('reading_time_4','Bookworm','Read for 100 hours total','⏱','READING_TIME','GOLD','READING_MINUTES',6000,200,50,NULL,NULL,13),
('reading_time_5','Reading Marathon','Read for 250 hours total','⏱','READING_TIME','PLATINUM','READING_MINUTES',15000,500,100,NULL,NULL,14),
('reading_time_6','Time Lord','Read for 1000 hours total','⏱','READING_TIME','LEGENDARY','READING_MINUTES',60000,1200,250,'time_lord',NULL,15),
-- 📖 Chapters
('chapters_1','First Chapters','Read 10 chapters','📖','CHAPTERS','BRONZE','CHAPTERS_READ',10,25,10,NULL,NULL,20),
('chapters_2','Chapter Reader','Read 100 chapters','📖','CHAPTERS','BRONZE','CHAPTERS_READ',100,25,10,NULL,NULL,21),
('chapters_3','Chapter Hunter','Read 500 chapters','📖','CHAPTERS','SILVER','CHAPTERS_READ',500,75,25,NULL,NULL,22),
('chapters_4','Chapter Master','Read 1000 chapters','📖','CHAPTERS','GOLD','CHAPTERS_READ',1000,200,50,NULL,NULL,23),
('chapters_5','Chapter Sage','Read 5000 chapters','📖','CHAPTERS','PLATINUM','CHAPTERS_READ',5000,500,100,NULL,NULL,24),
('chapters_6','Chapter Legend','Read 10000 chapters','📖','CHAPTERS','LEGENDARY','CHAPTERS_READ',10000,1200,250,'chapter_legend',NULL,25),
-- 📚 Books completed
('books_1','First Book','Complete your first book','📚','BOOKS','BRONZE','BOOKS_COMPLETED',1,25,10,NULL,NULL,30),
('books_2','Book Collector','Complete 5 books','📚','BOOKS','BRONZE','BOOKS_COMPLETED',5,25,10,NULL,NULL,31),
('books_3','Shelf Builder','Complete 10 books','📚','BOOKS','SILVER','BOOKS_COMPLETED',10,75,25,NULL,NULL,32),
('books_4','Library Builder','Complete 25 books','📚','BOOKS','GOLD','BOOKS_COMPLETED',25,200,50,NULL,NULL,33),
('books_5','Bibliophile','Complete 50 books','📚','BOOKS','PLATINUM','BOOKS_COMPLETED',50,500,100,NULL,NULL,34),
('books_6','Librarian Legend','Complete 100 books','📚','BOOKS','LEGENDARY','BOOKS_COMPLETED',100,1200,250,'librarian_legend',NULL,35),
-- 🔥 Streak (current reading streak days)
('streak_1','Getting Started','3-day reading streak','🔥','STREAK','BRONZE','STREAK_DAYS',3,25,10,NULL,NULL,40),
('streak_2','Week Warrior','7-day reading streak','🔥','STREAK','BRONZE','STREAK_DAYS',7,25,10,NULL,NULL,41),
('streak_3','Fortnight','14-day reading streak','🔥','STREAK','SILVER','STREAK_DAYS',14,75,25,NULL,NULL,42),
('streak_4','Month Master','30-day reading streak','🔥','STREAK','GOLD','STREAK_DAYS',30,200,50,NULL,NULL,43),
('streak_5','Centurion','100-day reading streak','🔥','STREAK','PLATINUM','STREAK_DAYS',100,500,100,NULL,NULL,44),
('streak_6','Eternal Flame','365-day reading streak','🔥','STREAK','LEGENDARY','STREAK_DAYS',365,1200,250,'eternal_flame',NULL,45),
-- 📅 Check-ins (cumulative)
('checkin_1','Regular','Check in 7 times','📅','CHECKIN','BRONZE','CHECKINS_TOTAL',7,25,10,NULL,NULL,50),
('checkin_2','Devoted','Check in 30 times','📅','CHECKIN','SILVER','CHECKINS_TOTAL',30,75,25,NULL,NULL,51),
('checkin_3','Faithful','Check in 100 times','📅','CHECKIN','GOLD','CHECKINS_TOTAL',100,200,50,NULL,NULL,52),
('checkin_4','Unwavering','Check in 365 times','📅','CHECKIN','PLATINUM','CHECKINS_TOTAL',365,500,100,NULL,NULL,53),
-- ⭐ Reviews written
('reviews_1','First Review','Write your first review','⭐','REVIEW','BRONZE','REVIEWS_WRITTEN',1,25,10,NULL,NULL,60),
('reviews_2','Reviewer','Write 10 reviews','⭐','REVIEW','SILVER','REVIEWS_WRITTEN',10,75,25,NULL,NULL,61),
('reviews_3','Critic','Write 50 reviews','⭐','REVIEW','GOLD','REVIEWS_WRITTEN',50,200,50,'critic',NULL,62),
-- 👍 Helpful votes received
('helpful_1','Helpful','Receive 10 helpful votes','👍','REVIEW','BRONZE','HELPFUL_RECEIVED',10,25,10,NULL,NULL,70),
('helpful_2','Trusted Voice','Receive 100 helpful votes','👍','REVIEW','SILVER','HELPFUL_RECEIVED',100,75,25,NULL,NULL,71),
('helpful_3','Tastemaker','Receive 500 helpful votes','👍','REVIEW','GOLD','HELPFUL_RECEIVED',500,200,50,NULL,NULL,72),
-- 🗳 Power-stone votes cast
('votes_1','Supporter','Cast 10 votes','🗳','VOTE','BRONZE','VOTES_CAST',10,25,10,NULL,NULL,80),
('votes_2','Campaigner','Cast 100 votes','🗳','VOTE','SILVER','VOTES_CAST',100,75,25,NULL,NULL,81),
('votes_3','Kingmaker','Cast 1000 votes','🗳','VOTE','GOLD','VOTES_CAST',1000,200,50,NULL,NULL,82),
-- 🧭 Genres explored
('genres_1','Curious','Explore 3 genres','🧭','GENRE','BRONZE','GENRES_EXPLORED',3,25,10,NULL,NULL,90),
('genres_2','Wanderer','Explore 5 genres','🧭','GENRE','SILVER','GENRES_EXPLORED',5,75,25,NULL,NULL,91),
('genres_3','Genre Explorer','Explore 10 genres','🧭','GENRE','GOLD','GENRES_EXPLORED',10,200,50,'genre_explorer',NULL,92),
-- ⚡ Reading speed
('speed_1','Quick Reader','Average 300+ WPM','⚡','SPEED','SILVER','AVG_WPM',300,75,25,NULL,NULL,100),
('speed_2','Speed Reader','Average 500+ WPM','⚡','SPEED','GOLD','AVG_WPM',500,200,50,'speed_reader',NULL,101),
-- 👥 Social
('social_1','First Friend','Gain your first follower','👥','SOCIAL','BRONZE','FOLLOWERS',1,25,10,NULL,NULL,110),
('social_2','Popular','Gain 10 followers','👥','SOCIAL','SILVER','FOLLOWERS',10,75,25,NULL,NULL,111),
('social_3','Influencer','Gain 50 followers','👥','SOCIAL','GOLD','FOLLOWERS',50,200,50,NULL,NULL,112),
-- 🎮 Discord
('discord_link','Verified Reader','Link your Discord account','🎮','SPECIAL','GOLD','DISCORD_LINKED',1,200,50,NULL,'verified_reader',120)
ON CONFLICT (id) DO NOTHING;

DO $$ BEGIN RAISE NOTICE '✅ Migration 002 (achievement seed) applied.'; END $$;
