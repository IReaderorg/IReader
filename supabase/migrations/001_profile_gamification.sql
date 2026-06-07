-- ============================================================================
-- Migration 001: Profile & Community Gamification (No-Money Edition)
-- ============================================================================
-- Additive migration. Safe to run on top of schema.sql. Idempotent where
-- possible (IF NOT EXISTS / OR REPLACE / ON CONFLICT DO NOTHING).
--
-- Principles enforced here:
--   * No money / no NFT in any new flow. Spirit Stones are earned-only.
--   * Rewards are unforgeable: xp / spirit_stones / user_achievements are
--     written ONLY by SECURITY DEFINER functions (which bypass RLS as owner);
--     direct client writes to those columns/tables are revoked.
--   * Reading totals stay in public.leaderboard (canonical); the economy
--     (xp, stones, level, check-in) lives on public.users.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Extend public.users (profile + economy + discord + engine stats)
-- ----------------------------------------------------------------------------
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS display_name          TEXT,
    ADD COLUMN IF NOT EXISTS bio                   TEXT    DEFAULT '',
    ADD COLUMN IF NOT EXISTS avatar_url            TEXT,
    ADD COLUMN IF NOT EXISTS cover_image_url       TEXT,
    ADD COLUMN IF NOT EXISTS cover_theme           TEXT    DEFAULT 'default',
    ADD COLUMN IF NOT EXISTS level                 INT     DEFAULT 1,
    ADD COLUMN IF NOT EXISTS xp                    BIGINT  DEFAULT 0,
    ADD COLUMN IF NOT EXISTS level_title           TEXT    DEFAULT 'Novice Reader',
    ADD COLUMN IF NOT EXISTS spirit_stones         BIGINT  DEFAULT 0,
    ADD COLUMN IF NOT EXISTS active_title_id       TEXT,
    ADD COLUMN IF NOT EXISTS checkin_streak        INT     DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_checkin_date     DATE,
    ADD COLUMN IF NOT EXISTS is_public_profile     BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS show_reading_activity BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS show_favorite_books   BOOLEAN DEFAULT TRUE,
    -- engine stats not present in leaderboard
    ADD COLUMN IF NOT EXISTS longest_streak        INT     DEFAULT 0,
    ADD COLUMN IF NOT EXISTS avg_wpm               INT     DEFAULT 0,
    ADD COLUMN IF NOT EXISTS genres_explored       INT     DEFAULT 0,
    -- discord
    ADD COLUMN IF NOT EXISTS discord_id            TEXT,
    ADD COLUMN IF NOT EXISTS discord_username      TEXT,
    ADD COLUMN IF NOT EXISTS discord_avatar        TEXT;

-- ----------------------------------------------------------------------------
-- 2. Reprice badges in Spirit Stones; allow COSMETIC type (drop money reliance)
-- ----------------------------------------------------------------------------
ALTER TABLE public.badges
    ADD COLUMN IF NOT EXISTS cost_spirit_stones INT;

ALTER TABLE public.badges DROP CONSTRAINT IF EXISTS badge_type_valid;
ALTER TABLE public.badges
    ADD CONSTRAINT badge_type_valid
    CHECK (type IN ('PURCHASABLE', 'NFT_EXCLUSIVE', 'ACHIEVEMENT', 'COSMETIC'));

-- ----------------------------------------------------------------------------
-- 3. book_reviews: helpful voting
-- ----------------------------------------------------------------------------
ALTER TABLE public.book_reviews
    ADD COLUMN IF NOT EXISTS helpful_count INT DEFAULT 0;

-- ----------------------------------------------------------------------------
-- 4. New tables
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_follows (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id  UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (follower_id, following_id),
    CONSTRAINT no_self_follow CHECK (follower_id <> following_id)
);

CREATE TABLE IF NOT EXISTS public.user_titles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title_id    TEXT NOT NULL,
    title_name  TEXT NOT NULL,
    rarity      TEXT NOT NULL DEFAULT 'COMMON',
    is_active   BOOLEAN DEFAULT FALSE,
    acquired_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, title_id)
);

-- Editable achievement catalog (add rows = add achievements, no app update)
CREATE TABLE IF NOT EXISTS public.achievement_definitions (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    description     TEXT NOT NULL,
    icon            TEXT NOT NULL DEFAULT '🏅',  -- emoji fallback if image_url null
    image_url       TEXT,
    category        TEXT NOT NULL,
    tier            TEXT NOT NULL DEFAULT 'BRONZE',
    metric          TEXT NOT NULL,
    threshold       BIGINT NOT NULL,
    reward_xp       INT NOT NULL DEFAULT 0,
    reward_stones   INT NOT NULL DEFAULT 0,
    reward_title_id TEXT,
    reward_badge_id TEXT,
    is_secret       BOOLEAN DEFAULT FALSE,
    is_active       BOOLEAN DEFAULT TRUE,
    sort_order      INT DEFAULT 0,
    CONSTRAINT achv_tier_valid CHECK (tier IN ('BRONZE','SILVER','GOLD','PLATINUM','LEGENDARY'))
);

CREATE TABLE IF NOT EXISTS public.user_achievements (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    achievement_id TEXT NOT NULL REFERENCES public.achievement_definitions(id) ON DELETE CASCADE,
    progress       BIGINT  DEFAULT 0,
    is_completed   BOOLEAN DEFAULT FALSE,
    earned_at      TIMESTAMPTZ,
    UNIQUE (user_id, achievement_id)
);

CREATE TABLE IF NOT EXISTS public.spirit_stone_transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    amount      BIGINT NOT NULL,            -- + earned, - spent on cosmetics
    type        TEXT NOT NULL,              -- CHECKIN, ACHIEVEMENT, STREAK, COSMETIC_SPEND...
    description TEXT DEFAULT '',
    reference_id TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.power_stone_votes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_id    TEXT NOT NULL,
    vote_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, book_id, vote_date)    -- one free vote per book per day
);

CREATE TABLE IF NOT EXISTS public.daily_checkins (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    checkin_date  DATE NOT NULL,
    streak_day    INT DEFAULT 1,
    reward_amount INT DEFAULT 10,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, checkin_date)
);

CREATE TABLE IF NOT EXISTS public.profile_comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    commenter_id    UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    comment_text    TEXT NOT NULL,
    likes_count     INT DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT comment_len CHECK (LENGTH(comment_text) BETWEEN 1 AND 500)
);

CREATE TABLE IF NOT EXISTS public.reading_activity (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL,   -- READING, REVIEW, VOTE, ACHIEVEMENT
    book_id       TEXT,
    book_title    TEXT,
    chapter_number INT,
    description   TEXT DEFAULT '',
    is_public     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMPTZ DEFAULT NOW()
);

-- Admin-authored (NO bot). See Discord §5 of the plan.
CREATE TABLE IF NOT EXISTS public.community_announcements (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               TEXT,
    body                TEXT,
    author_id           UUID REFERENCES public.users(id) ON DELETE SET NULL,
    discord_message_url TEXT,
    posted_at           TIMESTAMPTZ DEFAULT NOW()
);

-- Weekly rank snapshot → ▲▼ movement chevrons
CREATE TABLE IF NOT EXISTS public.leaderboard_snapshots (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    metric        TEXT NOT NULL,
    period        TEXT NOT NULL,
    rank          INT NOT NULL,
    snapshot_date DATE NOT NULL,
    UNIQUE (user_id, metric, period, snapshot_date)
);

-- Daily reader-count snapshot → "📈 Rising This Week"
CREATE TABLE IF NOT EXISTS public.reader_count_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id       TEXT NOT NULL,
    reader_count  BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    UNIQUE (book_id, snapshot_date)
);

-- ----------------------------------------------------------------------------
-- 5. Indexes
-- ----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_user_follows_follower   ON public.user_follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_user_follows_following  ON public.user_follows(following_id);
CREATE INDEX IF NOT EXISTS idx_user_titles_user        ON public.user_titles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_user  ON public.user_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_sst_user                ON public.spirit_stone_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_psv_book                ON public.power_stone_votes(book_id);
CREATE INDEX IF NOT EXISTS idx_psv_user                ON public.power_stone_votes(user_id);
CREATE INDEX IF NOT EXISTS idx_checkins_user           ON public.daily_checkins(user_id);
CREATE INDEX IF NOT EXISTS idx_profile_comments_target ON public.profile_comments(profile_user_id);
CREATE INDEX IF NOT EXISTS idx_reading_activity_user   ON public.reading_activity(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rch_book_date           ON public.reader_count_history(book_id, snapshot_date);

-- ----------------------------------------------------------------------------
-- 6. Row Level Security
-- ----------------------------------------------------------------------------
ALTER TABLE public.user_follows             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_titles              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.achievement_definitions  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_achievements        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.spirit_stone_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.power_stone_votes        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_checkins           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profile_comments         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reading_activity         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_announcements  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaderboard_snapshots    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reader_count_history     ENABLE ROW LEVEL SECURITY;

-- Follows: public read, owner writes own follow rows
DROP POLICY IF EXISTS follows_read   ON public.user_follows;
DROP POLICY IF EXISTS follows_insert ON public.user_follows;
DROP POLICY IF EXISTS follows_delete ON public.user_follows;
CREATE POLICY follows_read   ON public.user_follows FOR SELECT USING (true);
CREATE POLICY follows_insert ON public.user_follows FOR INSERT WITH CHECK (auth.uid() = follower_id);
CREATE POLICY follows_delete ON public.user_follows FOR DELETE USING (auth.uid() = follower_id);

-- Titles: public read (to show on profiles); only owner may toggle is_active.
-- Ownership rows are inserted by SECURITY DEFINER functions (no client INSERT).
DROP POLICY IF EXISTS titles_read   ON public.user_titles;
DROP POLICY IF EXISTS titles_update ON public.user_titles;
CREATE POLICY titles_read   ON public.user_titles FOR SELECT USING (true);
CREATE POLICY titles_update ON public.user_titles FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Achievement catalog: public read; admins manage rows
DROP POLICY IF EXISTS achv_def_read  ON public.achievement_definitions;
DROP POLICY IF EXISTS achv_def_admin ON public.achievement_definitions;
CREATE POLICY achv_def_read  ON public.achievement_definitions FOR SELECT USING (true);
CREATE POLICY achv_def_admin ON public.achievement_definitions FOR ALL
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- User achievements: owner-read ONLY. No write policies → clients cannot grant
-- themselves anything; SECURITY DEFINER functions are the only writers.
DROP POLICY IF EXISTS user_achv_read ON public.user_achievements;
CREATE POLICY user_achv_read ON public.user_achievements FOR SELECT USING (auth.uid() = user_id);

-- Stone ledger: owner-read ONLY (written by functions).
DROP POLICY IF EXISTS sst_read ON public.spirit_stone_transactions;
CREATE POLICY sst_read ON public.spirit_stone_transactions FOR SELECT USING (auth.uid() = user_id);

-- Votes: public read (for counts), owner insert via vote_book; block dup via UNIQUE
DROP POLICY IF EXISTS votes_read   ON public.power_stone_votes;
CREATE POLICY votes_read ON public.power_stone_votes FOR SELECT USING (true);
-- (no INSERT policy: votes go through vote_book SECURITY DEFINER)

-- Check-ins: owner-read (written by checkin_daily)
DROP POLICY IF EXISTS checkins_read ON public.daily_checkins;
CREATE POLICY checkins_read ON public.daily_checkins FOR SELECT USING (auth.uid() = user_id);

-- Profile comments: public read, commenter writes/deletes own, wall owner can delete
DROP POLICY IF EXISTS pc_read   ON public.profile_comments;
DROP POLICY IF EXISTS pc_insert ON public.profile_comments;
DROP POLICY IF EXISTS pc_delete ON public.profile_comments;
CREATE POLICY pc_read   ON public.profile_comments FOR SELECT USING (true);
CREATE POLICY pc_insert ON public.profile_comments FOR INSERT WITH CHECK (auth.uid() = commenter_id);
CREATE POLICY pc_delete ON public.profile_comments FOR DELETE
    USING (auth.uid() = commenter_id OR auth.uid() = profile_user_id);

-- Reading activity: public read of public rows (or own), owner insert
DROP POLICY IF EXISTS ra_read   ON public.reading_activity;
DROP POLICY IF EXISTS ra_insert ON public.reading_activity;
CREATE POLICY ra_read   ON public.reading_activity FOR SELECT USING (is_public OR auth.uid() = user_id);
CREATE POLICY ra_insert ON public.reading_activity FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Announcements: public read, admins write
DROP POLICY IF EXISTS ann_read  ON public.community_announcements;
DROP POLICY IF EXISTS ann_admin ON public.community_announcements;
CREATE POLICY ann_read  ON public.community_announcements FOR SELECT USING (true);
CREATE POLICY ann_admin ON public.community_announcements FOR ALL
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Snapshots: public read (written by pg_cron jobs running as owner)
DROP POLICY IF EXISTS lb_snap_read ON public.leaderboard_snapshots;
DROP POLICY IF EXISTS rch_read     ON public.reader_count_history;
CREATE POLICY lb_snap_read ON public.leaderboard_snapshots FOR SELECT USING (true);
CREATE POLICY rch_read     ON public.reader_count_history FOR SELECT USING (true);

-- ----------------------------------------------------------------------------
-- 7. Lock down economy columns on public.users (defense in depth)
--    The existing "Users can update their own data" policy lets a client UPDATE
--    its own row. Column privileges are checked separately from RLS, so revoke
--    UPDATE on reward columns: only SECURITY DEFINER functions may change them.
--    NOTE: public.leaderboard remains client-trusted (as in the current app) —
--    sync_reading_stats merges monotonically, but the pre-existing leaderboard
--    UPDATE policy is left intact to avoid breaking existing sync. The hardened
--    surface is the new reward economy (xp / spirit_stones / achievements).
-- ----------------------------------------------------------------------------
REVOKE UPDATE (xp, level, level_title, spirit_stones, checkin_streak,
               last_checkin_date, longest_streak, avg_wpm, genres_explored)
    ON public.users FROM authenticated, anon;

-- ----------------------------------------------------------------------------
-- 8. Functions
-- ----------------------------------------------------------------------------

-- Level curve: cumulative XP for level L is 50*L*(L-1). Invert for level.
CREATE OR REPLACE FUNCTION public.calculate_level(p_xp BIGINT)
RETURNS INT
LANGUAGE sql IMMUTABLE AS $$
    SELECT GREATEST(1, FLOOR((1 + SQRT(1 + 4 * (GREATEST(p_xp,0)::numeric / 50))) / 2))::INT;
$$;

CREATE OR REPLACE FUNCTION public.level_title_for(p_level INT)
RETURNS TEXT
LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE
        WHEN p_level >= 50 THEN 'Grandmaster Reader'
        WHEN p_level >= 30 THEN 'Sage Reader'
        WHEN p_level >= 20 THEN 'Master Reader'
        WHEN p_level >= 12 THEN 'Avid Reader'
        WHEN p_level >= 6  THEN 'Adept Reader'
        WHEN p_level >= 3  THEN 'Apprentice Reader'
        ELSE 'Novice Reader'
    END;
$$;

-- Idempotent evaluator. SECURITY DEFINER → bypasses RLS as table owner.
-- Returns newly-unlocked achievements (for the celebration UI).
CREATE OR REPLACE FUNCTION public.evaluate_achievements(p_user UUID)
RETURNS TABLE (
    achievement_id TEXT,
    name           TEXT,
    icon           TEXT,
    image_url      TEXT,
    tier           TEXT,
    reward_xp      INT,
    reward_stones  INT
)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
#variable_conflict use_column
DECLARE
    d           RECORD;
    v_value     BIGINT;
    v_minutes   BIGINT := 0;
    v_chapters  BIGINT := 0;
    v_books     BIGINT := 0;
    v_streak    BIGINT := 0;
    v_longest   BIGINT := 0;
    v_avg_wpm   BIGINT := 0;
    v_genres    BIGINT := 0;
    v_discord   BIGINT := 0;
    v_checkins  BIGINT := 0;
    v_reviews   BIGINT := 0;
    v_helpful   BIGINT := 0;
    v_votes     BIGINT := 0;
    v_followers BIGINT := 0;
    v_new_level INT;
BEGIN
    -- Gather canonical metrics once
    SELECT COALESCE(total_reading_time_minutes,0), COALESCE(total_chapters_read,0),
           COALESCE(books_completed,0), COALESCE(reading_streak,0)
      INTO v_minutes, v_chapters, v_books, v_streak
      FROM public.leaderboard WHERE user_id = p_user;

    SELECT COALESCE(longest_streak,0), COALESCE(avg_wpm,0), COALESCE(genres_explored,0),
           CASE WHEN discord_id IS NOT NULL THEN 1 ELSE 0 END
      INTO v_longest, v_avg_wpm, v_genres, v_discord
      FROM public.users WHERE id = p_user;

    SELECT COUNT(*) INTO v_checkins  FROM public.daily_checkins      WHERE user_id = p_user;
    SELECT COUNT(*) INTO v_reviews   FROM public.book_reviews        WHERE user_id = p_user;
    SELECT COALESCE(SUM(helpful_count),0) INTO v_helpful FROM public.book_reviews WHERE user_id = p_user;
    SELECT COUNT(*) INTO v_votes     FROM public.power_stone_votes   WHERE user_id = p_user;
    SELECT COUNT(*) INTO v_followers FROM public.user_follows        WHERE following_id = p_user;

    FOR d IN SELECT * FROM public.achievement_definitions WHERE is_active LOOP
        v_value := CASE d.metric
            WHEN 'READING_MINUTES'  THEN v_minutes
            WHEN 'CHAPTERS_READ'    THEN v_chapters
            WHEN 'BOOKS_COMPLETED'  THEN v_books
            WHEN 'STREAK_DAYS'      THEN v_streak
            WHEN 'LONGEST_STREAK'   THEN v_longest
            WHEN 'CHECKINS_TOTAL'   THEN v_checkins
            WHEN 'REVIEWS_WRITTEN'  THEN v_reviews
            WHEN 'HELPFUL_RECEIVED' THEN v_helpful
            WHEN 'VOTES_CAST'       THEN v_votes
            WHEN 'GENRES_EXPLORED'  THEN v_genres
            WHEN 'AVG_WPM'          THEN v_avg_wpm
            WHEN 'FOLLOWERS'        THEN v_followers
            WHEN 'DISCORD_LINKED'   THEN v_discord
            ELSE 0
        END;

        -- Track progress (capped at threshold)
        INSERT INTO public.user_achievements (user_id, achievement_id, progress)
        VALUES (p_user, d.id, LEAST(v_value, d.threshold))
        ON CONFLICT (user_id, achievement_id)
        DO UPDATE SET progress = LEAST(v_value, d.threshold)
        WHERE public.user_achievements.is_completed = FALSE;

        -- Grant once when threshold reached
        IF v_value >= d.threshold AND NOT EXISTS (
            SELECT 1 FROM public.user_achievements
            WHERE user_id = p_user AND achievement_id = d.id AND is_completed = TRUE
        ) THEN
            UPDATE public.user_achievements
               SET is_completed = TRUE, earned_at = NOW(), progress = d.threshold
             WHERE user_id = p_user AND achievement_id = d.id;

            UPDATE public.users
               SET xp = xp + d.reward_xp,
                   spirit_stones = spirit_stones + d.reward_stones
             WHERE id = p_user;

            IF d.reward_stones <> 0 THEN
                INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description, reference_id)
                VALUES (p_user, d.reward_stones, 'ACHIEVEMENT', d.name, d.id);
            END IF;

            IF d.reward_title_id IS NOT NULL THEN
                INSERT INTO public.user_titles (user_id, title_id, title_name, rarity)
                VALUES (p_user, d.reward_title_id, d.name, d.tier)
                ON CONFLICT (user_id, title_id) DO NOTHING;
            END IF;

            IF d.reward_badge_id IS NOT NULL THEN
                INSERT INTO public.user_badges (user_id, badge_id)
                VALUES (p_user, d.reward_badge_id)
                ON CONFLICT (user_id, badge_id) DO NOTHING;
            END IF;

            INSERT INTO public.reading_activity (user_id, activity_type, description)
            VALUES (p_user, 'ACHIEVEMENT', d.name);

            achievement_id := d.id; name := d.name; icon := d.icon;
            image_url := d.image_url; tier := d.tier;
            reward_xp := d.reward_xp; reward_stones := d.reward_stones;
            RETURN NEXT;
        END IF;
    END LOOP;

    -- Recompute level/title from final xp
    SELECT public.calculate_level(xp) INTO v_new_level FROM public.users WHERE id = p_user;
    UPDATE public.users
       SET level = v_new_level, level_title = public.level_title_for(v_new_level)
     WHERE id = p_user;
END;
$$;

-- Client calls this after a reading session / on app open. Monotonic-merge
-- (rejects decreases) then evaluates achievements. Returns newly-unlocked.
CREATE OR REPLACE FUNCTION public.sync_reading_stats(
    p_minutes  BIGINT,
    p_chapters BIGINT,
    p_books    BIGINT,
    p_streak   BIGINT,
    p_longest  BIGINT,
    p_avg_wpm  BIGINT,
    p_genres   BIGINT
)
RETURNS TABLE (
    achievement_id TEXT, name TEXT, icon TEXT, image_url TEXT,
    tier TEXT, reward_xp INT, reward_stones INT
)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user     UUID := auth.uid();
    v_username TEXT;
BEGIN
    IF v_user IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    SELECT COALESCE(username, 'Reader') INTO v_username FROM public.users WHERE id = v_user;

    -- Canonical reading totals live in leaderboard; merge monotonically.
    INSERT INTO public.leaderboard (user_id, username, total_reading_time_minutes,
                                    total_chapters_read, books_completed, reading_streak, updated_at)
    VALUES (v_user, v_username, GREATEST(p_minutes,0), GREATEST(p_chapters,0),
            GREATEST(p_books,0), GREATEST(p_streak,0), NOW())
    ON CONFLICT (user_id) DO UPDATE SET
        total_reading_time_minutes = GREATEST(public.leaderboard.total_reading_time_minutes, EXCLUDED.total_reading_time_minutes),
        total_chapters_read        = GREATEST(public.leaderboard.total_chapters_read,        EXCLUDED.total_chapters_read),
        books_completed            = GREATEST(public.leaderboard.books_completed,            EXCLUDED.books_completed),
        reading_streak             = EXCLUDED.reading_streak,  -- current streak may drop
        updated_at                 = NOW();

    -- Engine-only stats on users (monotonic where it makes sense)
    UPDATE public.users SET
        longest_streak  = GREATEST(longest_streak, p_longest, p_streak),
        genres_explored = GREATEST(genres_explored, p_genres),
        avg_wpm         = p_avg_wpm
      WHERE id = v_user;

    RETURN QUERY SELECT * FROM public.evaluate_achievements(v_user);
END;
$$;

-- Daily check-in: awards streak-scaled stones, then evaluates achievements.
CREATE OR REPLACE FUNCTION public.checkin_daily()
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user   UUID := auth.uid();
    v_today  DATE := CURRENT_DATE;
    v_last   DATE;
    v_streak INT;
    v_reward INT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;

    SELECT last_checkin_date, COALESCE(checkin_streak,0) INTO v_last, v_streak
      FROM public.users WHERE id = v_user;

    IF v_last = v_today THEN
        RETURN json_build_object('already', true, 'streak_day', v_streak);
    END IF;

    IF v_last = v_today - 1 THEN
        v_streak := v_streak + 1;
    ELSE
        v_streak := 1;
    END IF;

    -- Reward curve with milestone bonuses
    v_reward := CASE
        WHEN v_streak % 30 = 0 THEN 200
        WHEN v_streak % 7  = 0 THEN 50
        ELSE 10
    END;

    INSERT INTO public.daily_checkins (user_id, checkin_date, streak_day, reward_amount)
    VALUES (v_user, v_today, v_streak, v_reward)
    ON CONFLICT (user_id, checkin_date) DO NOTHING;

    UPDATE public.users
       SET spirit_stones = spirit_stones + v_reward,
           checkin_streak = v_streak,
           last_checkin_date = v_today
     WHERE id = v_user;

    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description)
    VALUES (v_user, v_reward, 'CHECKIN', 'Daily check-in (day ' || v_streak || ')');

    PERFORM public.evaluate_achievements(v_user);

    RETURN json_build_object('already', false, 'streak_day', v_streak, 'reward', v_reward);
END;
$$;

-- Free daily power-stone vote (no currency cost). Idempotent per book/day.
CREATE OR REPLACE FUNCTION public.vote_book(p_book_id TEXT)
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user  UUID := auth.uid();
    v_count INT;
    v_voted BOOLEAN;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;

    INSERT INTO public.power_stone_votes (user_id, book_id)
    VALUES (v_user, p_book_id)
    ON CONFLICT (user_id, book_id, vote_date) DO NOTHING;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    v_voted := v_count > 0;
    IF v_voted THEN
        INSERT INTO public.reading_activity (user_id, activity_type, book_id, description)
        VALUES (v_user, 'VOTE', p_book_id, 'Voted for a book');
        PERFORM public.evaluate_achievements(v_user);
    END IF;

    RETURN json_build_object('voted', v_voted);
END;
$$;

-- Spend earned Spirit Stones on a cosmetic (badge / title). Cosmetic-only.
CREATE OR REPLACE FUNCTION public.spend_stones(
    p_item_type TEXT,   -- 'BADGE' | 'TITLE'
    p_item_id   TEXT,
    p_cost      INT
)
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user    UUID := auth.uid();
    v_balance BIGINT;
    v_name    TEXT;
    v_rarity  TEXT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;
    IF p_cost < 0 THEN RAISE EXCEPTION 'Invalid cost'; END IF;

    SELECT spirit_stones INTO v_balance FROM public.users WHERE id = v_user FOR UPDATE;
    IF v_balance < p_cost THEN
        RETURN json_build_object('ok', false, 'reason', 'INSUFFICIENT_STONES');
    END IF;

    IF p_item_type = 'BADGE' THEN
        SELECT name, rarity INTO v_name, v_rarity FROM public.badges
          WHERE id = p_item_id AND type = 'COSMETIC';
        IF v_name IS NULL THEN RETURN json_build_object('ok', false, 'reason', 'NOT_FOUND'); END IF;
        INSERT INTO public.user_badges (user_id, badge_id)
        VALUES (v_user, p_item_id) ON CONFLICT (user_id, badge_id) DO NOTHING;
    ELSIF p_item_type = 'TITLE' THEN
        INSERT INTO public.user_titles (user_id, title_id, title_name, rarity)
        VALUES (v_user, p_item_id, p_item_id, 'COMMON') ON CONFLICT (user_id, title_id) DO NOTHING;
    ELSE
        RETURN json_build_object('ok', false, 'reason', 'BAD_TYPE');
    END IF;

    UPDATE public.users SET spirit_stones = spirit_stones - p_cost WHERE id = v_user;
    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description, reference_id)
    VALUES (v_user, -p_cost, 'COSMETIC_SPEND', p_item_type || ':' || p_item_id, p_item_id);

    RETURN json_build_object('ok', true, 'balance', v_balance - p_cost);
END;
$$;

-- ----------------------------------------------------------------------------
-- 9. Triggers — auto-evaluate when off-path metrics change
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.trg_eval_review()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    PERFORM public.evaluate_achievements(NEW.user_id);
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS after_review_insert ON public.book_reviews;
CREATE TRIGGER after_review_insert AFTER INSERT ON public.book_reviews
    FOR EACH ROW EXECUTE FUNCTION public.trg_eval_review();

CREATE OR REPLACE FUNCTION public.trg_eval_follow()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    PERFORM public.evaluate_achievements(NEW.following_id);  -- FOLLOWERS metric
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS after_follow_insert ON public.user_follows;
CREATE TRIGGER after_follow_insert AFTER INSERT ON public.user_follows
    FOR EACH ROW EXECUTE FUNCTION public.trg_eval_follow();

-- ----------------------------------------------------------------------------
-- 10. Grants for RPC
-- ----------------------------------------------------------------------------
GRANT EXECUTE ON FUNCTION public.calculate_level(BIGINT)            TO authenticated, anon;
GRANT EXECUTE ON FUNCTION public.sync_reading_stats(BIGINT,BIGINT,BIGINT,BIGINT,BIGINT,BIGINT,BIGINT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.evaluate_achievements(UUID)        TO authenticated;
GRANT EXECUTE ON FUNCTION public.checkin_daily()                   TO authenticated;
GRANT EXECUTE ON FUNCTION public.vote_book(TEXT)                   TO authenticated;
GRANT EXECUTE ON FUNCTION public.spend_stones(TEXT,TEXT,INT)       TO authenticated;

-- ----------------------------------------------------------------------------
-- 11. pg_cron snapshot jobs (only if the extension is available)
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_cron') THEN
        CREATE EXTENSION IF NOT EXISTS pg_cron;

        -- Daily reader-count snapshot at 00:10 UTC
        PERFORM cron.schedule('rch_daily', '10 0 * * *', $job$
            INSERT INTO public.reader_count_history (book_id, reader_count, snapshot_date)
            SELECT id::text, view_count, CURRENT_DATE FROM public.community_books
            ON CONFLICT (book_id, snapshot_date) DO UPDATE SET reader_count = EXCLUDED.reader_count;
        $job$);

        -- Weekly all-time reading-minutes rank snapshot, Monday 00:05 UTC
        PERFORM cron.schedule('lb_weekly', '5 0 * * 1', $job$
            INSERT INTO public.leaderboard_snapshots (user_id, metric, period, rank, snapshot_date)
            SELECT user_id, 'READING_MINUTES', 'ALL_TIME',
                   RANK() OVER (ORDER BY total_reading_time_minutes DESC), CURRENT_DATE
            FROM public.leaderboard
            ON CONFLICT (user_id, metric, period, snapshot_date) DO UPDATE SET rank = EXCLUDED.rank;
        $job$);
    ELSE
        RAISE NOTICE 'pg_cron not available — schedule rch_daily / lb_weekly manually or via Supabase scheduled functions.';
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 12. Version stamp
-- ----------------------------------------------------------------------------
INSERT INTO public.schema_version (version, description)
VALUES (3, 'Profile & community gamification: achievements engine, stones, follows, votes, check-ins, snapshots')
ON CONFLICT (version) DO NOTHING;

DO $$ BEGIN RAISE NOTICE '✅ Migration 001 (profile gamification) applied.'; END $$;
