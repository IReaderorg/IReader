# 🌩️ Supabase Cloud Sync & Database Setup Guide

This guide walks you through setting up your own **free, private Supabase database** to synchronize your novels, reading progress, and Spirit Stones across all your devices (Android, Desktop JVM, iOS).

---

## 📌 Why Use Personal Supabase Sync?

* **100% Free Forever**: Supabase's free tier provides 500MB of database storage (enough for millions of synced books and chapters).
* **Private & Secure**: Your library data and reading history are stored on your personal cloud database, never on developer or community servers.
* **Full-Fidelity Synchronization (`sync_manifest`)**: Syncs books, categories, cover art, chapter progress, scroll percentages, and reading stats.
* **Cross-Device Gamification**: Seamlessly earn Spirit Stones, keep your check-in streak, and sync balances across all your devices.
* **Quick Configuration Sharing**: Export a lightweight JSON config on one device and import it on your phone or desktop in 2 clicks.

---

## 📋 Table of Contents
1. [Step 1: Create a Free Supabase Project](#step-1-create-a-free-supabase-project)
2. [Step 2: Copy Your Project Credentials](#step-2-copy-your-project-credentials)
3. [Step 3: Run the Database Setup SQL Script](#step-3-run-the-database-setup-sql-script)
4. [Step 4: Configure IReader App](#step-4-configure-ireader-app)
5. [Step 5: Sync to Multiple Devices (Export & Import)](#step-5-sync-to-multiple-devices-export--import)
6. [Step 6: Daily Check-in & Spirit Stones](#step-6-daily-check-in--spirit-stones)
7. [Troubleshooting & Common Errors](#troubleshooting--common-errors)

---

## Step 1: Create a Free Supabase Project

1. Open your browser and navigate to [https://supabase.com](https://supabase.com).
2. Click **Start your project** (or sign in with GitHub / Email).
3. In the Supabase Dashboard, click **New project**.
4. Choose an organization (or create a personal one).
5. Fill in the project details:
   - **Name**: `IReader Sync` (or any name you like).
   - **Database Password**: Choose a strong password and save it in your password manager.
   - **Region**: Choose the region geographically closest to you for fastest sync speeds.
   - **Pricing Plan**: Select **Free tier** ($0/month).
6. Click **Create new project**. Supabase will take 1–2 minutes to provision your database.

---

## Step 2: Copy Your Project Credentials

Once the project is ready:

1. In the left navigation sidebar, click the **Settings** gear icon (⚙️) at the bottom.
2. Select **API** (or **Data API**).
3. Locate the following two values:
   * **Project URL**: Format looks like `https://abcdefghijklm.supabase.co`
   * **Project API Keys**: Copy the **`anon` / `public`** key (starts with `ey...`).
4. Keep these handy—you will enter them into IReader.

---

## Step 3: Run the Database Setup SQL Script

To prepare your database tables and RPC functions for book syncing and Spirit Stones check-in:

1. In the Supabase dashboard sidebar, click **SQL Editor** (icon with `>_`).
2. Click **New query** (or the `+` button).
3. Copy and paste the complete SQL script below into the editor:

```sql
-- ==========================================================
-- IReader Unified Sync & Gamification Setup Script
-- ==========================================================

-- 1. Full Sync Manifest (High-Fidelity Document Store)
CREATE TABLE IF NOT EXISTS public.sync_manifest (
    user_id    TEXT NOT NULL PRIMARY KEY,
    manifest   JSONB NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT 0
);
ALTER TABLE public.sync_manifest ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public sync_manifest access" ON public.sync_manifest;
CREATE POLICY "Allow public sync_manifest access" ON public.sync_manifest FOR ALL USING (true) WITH CHECK (true);

-- 2. Synced Books (Relational View with rich metadata)
CREATE TABLE IF NOT EXISTS public.synced_books (
    user_id     TEXT NOT NULL,
    book_id     TEXT NOT NULL,
    source_id   BIGINT NOT NULL,
    title       TEXT NOT NULL,
    book_url    TEXT NOT NULL,
    last_read   BIGINT NOT NULL DEFAULT 0,
    cover_url   TEXT DEFAULT '',
    source_name TEXT DEFAULT '',
    author      TEXT DEFAULT '',
    description TEXT DEFAULT '',
    genres      TEXT DEFAULT '',
    status      BIGINT DEFAULT 0,
    favorite    BOOLEAN DEFAULT true,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, book_id)
);

ALTER TABLE public.synced_books DROP CONSTRAINT IF EXISTS synced_books_user_id_fkey;

ALTER TABLE public.synced_books
    ADD COLUMN IF NOT EXISTS author      TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS description TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS genres      TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS status      BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS favorite    BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS cover_url   TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS source_name TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public synced_books access" ON public.synced_books;
CREATE POLICY "Allow public synced_books access" ON public.synced_books FOR ALL USING (true) WITH CHECK (true);

-- 3. Reading Progress (Relational View)
CREATE TABLE IF NOT EXISTS public.reading_progress (
    user_id              TEXT NOT NULL,
    book_id              TEXT NOT NULL,
    last_chapter_slug    TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    updated_at           BIGINT DEFAULT 0,
    PRIMARY KEY (user_id, book_id)
);

ALTER TABLE public.reading_progress DROP CONSTRAINT IF EXISTS reading_progress_user_id_fkey;
ALTER TABLE public.reading_progress DROP CONSTRAINT IF EXISTS scroll_position_range;

CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public reading_progress access" ON public.reading_progress;
CREATE POLICY "Allow public reading_progress access" ON public.reading_progress FOR ALL USING (true) WITH CHECK (true);

-- 4. Users & Gamification Economy
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT auth.uid(),
    email TEXT,
    username TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can read own profile" ON public.users;
CREATE POLICY "Users can read own profile" ON public.users FOR SELECT USING (auth.uid() = id);
DROP POLICY IF EXISTS "Users can update own profile" ON public.users;
CREATE POLICY "Users can update own profile" ON public.users FOR UPDATE USING (auth.uid() = id);

ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS display_name      TEXT,
    ADD COLUMN IF NOT EXISTS bio               TEXT    DEFAULT '',
    ADD COLUMN IF NOT EXISTS avatar_url        TEXT,
    ADD COLUMN IF NOT EXISTS cover_image_url   TEXT,
    ADD COLUMN IF NOT EXISTS level             INT     DEFAULT 1,
    ADD COLUMN IF NOT EXISTS xp                BIGINT  DEFAULT 0,
    ADD COLUMN IF NOT EXISTS level_title       TEXT    DEFAULT 'Novice Reader',
    ADD COLUMN IF NOT EXISTS spirit_stones     BIGINT  DEFAULT 0,
    ADD COLUMN IF NOT EXISTS active_title_id   TEXT,
    ADD COLUMN IF NOT EXISTS checkin_streak    INT     DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_checkin_date DATE;

-- 5. Daily Check-ins Table
CREATE TABLE IF NOT EXISTS public.daily_checkins (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    checkin_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    streak_day    INT NOT NULL DEFAULT 1,
    reward_amount INT NOT NULL DEFAULT 10,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, checkin_date)
);
ALTER TABLE public.daily_checkins ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS daily_checkins_read ON public.daily_checkins;
CREATE POLICY daily_checkins_read ON public.daily_checkins FOR SELECT USING (auth.uid() = user_id);

-- 6. Spirit Stone Transactions Table
CREATE TABLE IF NOT EXISTS public.spirit_stone_transactions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    amount       BIGINT NOT NULL,
    type         TEXT NOT NULL,
    description  TEXT DEFAULT '',
    reference_id TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.spirit_stone_transactions ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS sst_read ON public.spirit_stone_transactions;
CREATE POLICY sst_read ON public.spirit_stone_transactions FOR SELECT USING (auth.uid() = user_id);

-- 7. User Titles Table
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
ALTER TABLE public.user_titles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS user_titles_all ON public.user_titles;
CREATE POLICY user_titles_all ON public.user_titles FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 8. Daily Check-in RPC Function (Clean drop prevents return-type conflict)
DROP FUNCTION IF EXISTS public.checkin_daily();

CREATE OR REPLACE FUNCTION public.checkin_daily()
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user UUID := auth.uid();
    v_today DATE := CURRENT_DATE;
    v_last DATE;
    v_streak INT;
    v_reward INT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;

    INSERT INTO public.users (id, email, username)
    SELECT v_user, auth.users.email, COALESCE(auth.users.raw_user_meta_data->>'username', 'Reader')
    FROM auth.users WHERE id = v_user
    ON CONFLICT (id) DO NOTHING;

    SELECT last_checkin_date, COALESCE(checkin_streak, 0) INTO v_last, v_streak
      FROM public.users WHERE id = v_user;

    IF v_last = v_today THEN
        RETURN json_build_object('already', true, 'streak_day', v_streak, 'reward', 0);
    END IF;

    IF v_last = v_today - 1 THEN
        v_streak := v_streak + 1;
    ELSE
        v_streak := 1;
    END IF;

    v_reward := CASE
        WHEN v_streak % 30 = 0 THEN 200
        WHEN v_streak % 7 = 0 THEN 50
        ELSE 10
    END;

    INSERT INTO public.daily_checkins (user_id, checkin_date, streak_day, reward_amount)
    VALUES (v_user, v_today, v_streak, v_reward)
    ON CONFLICT (user_id, checkin_date) DO NOTHING;

    UPDATE public.users
       SET spirit_stones = COALESCE(spirit_stones, 0) + v_reward,
           checkin_streak = v_streak,
           last_checkin_date = v_today
     WHERE id = v_user;

    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description)
    VALUES (v_user, v_reward, 'CHECKIN', 'Daily check-in (day ' || v_streak || ')');

    RETURN json_build_object('already', false, 'streak_day', v_streak, 'reward', v_reward);
END;
$$;
GRANT EXECUTE ON FUNCTION public.checkin_daily() TO authenticated;

-- 9. Spend Stones RPC Function
DROP FUNCTION IF EXISTS public.spend_stones(TEXT, TEXT, INT);
DROP FUNCTION IF EXISTS public.spend_stones(INT, TEXT);
DROP FUNCTION IF EXISTS public.spend_stones;

CREATE OR REPLACE FUNCTION public.spend_stones(
    p_item_type TEXT,
    p_item_id   TEXT,
    p_cost      INT
)
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user UUID := auth.uid();
    v_balance BIGINT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;
    IF p_cost < 0 THEN RAISE EXCEPTION 'Invalid cost'; END IF;

    SELECT COALESCE(spirit_stones, 0) INTO v_balance FROM public.users WHERE id = v_user FOR UPDATE;
    IF v_balance < p_cost THEN
        RETURN json_build_object('ok', false, 'reason', 'INSUFFICIENT_STONES', 'balance', v_balance);
    END IF;

    UPDATE public.users SET spirit_stones = spirit_stones - p_cost WHERE id = v_user;
    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description, reference_id)
    VALUES (v_user, -p_cost, 'SPEND', 'Purchased ' || p_item_type || ': ' || p_item_id, p_item_id);

    IF p_item_type = 'TITLE' THEN
        INSERT INTO public.user_titles (user_id, title_id, title_name)
        VALUES (v_user, p_item_id, p_item_id)
        ON CONFLICT (user_id, title_id) DO NOTHING;
    END IF;

    RETURN json_build_object('ok', true, 'balance', v_balance - p_cost);
END;
$$;
GRANT EXECUTE ON FUNCTION public.spend_stones(TEXT, TEXT, INT) TO authenticated;
```

4. Click **Run** (or press `Ctrl+Enter` / `Cmd+Enter`).
5. Verify you see `Success. No rows returned.` in the results pane.

> [!TIP]
> You can also copy this script directly inside the app at any time by navigating to **Settings → Supabase Configuration** and tapping **"Copy Setup SQL"**.

---

## Step 4: Configure IReader App

1. Open **IReader**.
2. Go to **More** / **Settings** → **Sync** → **Supabase Configuration**.
3. Under **Personal Supabase (Single Project - Recommended)**:
   - Paste your **Project URL** into the URL field.
   - Paste your **anon / public Key** into the API Key field.
4. Tap **Save Configuration**.
5. Tap **Test Connection**.
   - You should see: `✓ Connection successful! Personal Supabase is ready for sync.`

---

## Step 5: Sync to Multiple Devices (Export & Import)

You don't need to type long URLs and keys on every device:

1. On your configured device, go to **Settings → Supabase Configuration**.
2. Tap **"Share Config"** (or Export). A JSON configuration will be copied to your clipboard.
3. Send this snippet securely to your other device (via Signal, Telegram Saved Messages, Notes, etc.).
4. On your second device, open **Settings → Supabase Configuration**.
5. Tap **"Import Config"**, paste the JSON snippet, and tap **Import**.
6. The URL and Key will be populated automatically! Tap **Test Connection** to confirm.

---

## Step 6: Daily Check-in & Spirit Stones

Once configured:
* **Library Sync**: In **Settings → Unified Sync**, tap **"Sync Now"** (or enable Auto-Sync) to backup your books and reading progress to your private cloud.
* **Daily Check-in**: Open **Profile / Webnovel Profile**, then tap **"Daily Check-in"**.
  * Your streak will increment (+1 day).
  * Spirit Stones will be awarded (10 base, 50 at 7-day streak, 200 at 30-day streak).
  * Your balance will sync across all connected devices.

---

## Troubleshooting & Common Errors

### 1. `ERROR: 42P13: cannot change return type of existing function`
* **Cause**: PostgreSQL does not allow changing function return types with `CREATE OR REPLACE`.
* **Fix**: Ensure you run `DROP FUNCTION IF EXISTS public.checkin_daily();` before recreating the function (this is already included in Step 3's script).

### 2. `Connection failed: ✗ ... (Have you run the Setup SQL script?)`
* **Cause**: The app pinged `sync_manifest`, but the table has not been created yet.
* **Fix**: Open the Supabase SQL Editor and run the script from Step 3.

### 3. `permission denied for table ...` or RLS Violation
* **Cause**: Row Level Security (RLS) is blocking anonymous single-project access.
* **Fix**: Ensure the RLS policies in Step 3 (`Allow public sync_manifest access`, etc.) were executed. They allow the `anon` key of your personal Supabase project to insert and query your library.

### 4. `column "author" of relation "synced_books" does not exist`
* **Cause**: The `synced_books` table existed prior to commit `6cc33417` and lacks the new rich metadata columns.
* **Fix**: Running the script in Step 3 executes `ALTER TABLE public.synced_books ADD COLUMN IF NOT EXISTS author...` to upgrade your existing table without deleting your books.
