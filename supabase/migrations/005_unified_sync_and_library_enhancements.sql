-- ============================================================================
-- Migration 005: Unified Sync Manifest & Library Enhancements
-- Based on commit 6cc33417da01acac87a7755e1fc8d8fdce52fbc6
-- ============================================================================
-- Additive, idempotent migration. Safe to run on top of schema.sql, 001, 002, 003, 004.
--
-- Key Improvements:
-- 1. Adds public.sync_manifest (Document Store) for full-fidelity sync of books,
--    reading progress, categories, and settings.
-- 2. Enhances public.synced_books (Relational View) with rich metadata:
--    author, description, genres, status, favorite, updated_at.
-- 3. Decouples user_id foreign keys so personal Supabase projects can sync
--    both with authenticated accounts and anonymous client IDs (user_<hash>).
-- 4. Enables RLS with permissive policies for personal Supabase cloud sync.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Full Sync Manifest (High-Fidelity Document Store)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.sync_manifest (
    user_id    TEXT NOT NULL PRIMARY KEY,
    manifest   JSONB NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE public.sync_manifest ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE schemaname = 'public' 
          AND tablename = 'sync_manifest' 
          AND policyname = 'Allow public sync_manifest access'
    ) THEN
        CREATE POLICY "Allow public sync_manifest access" 
            ON public.sync_manifest FOR ALL 
            USING (true) 
            WITH CHECK (true);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 2. Enhanced Synced Books (Relational View)
-- ----------------------------------------------------------------------------
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

-- If table existed previously with FK to users, remove FK to support personal anon IDs
ALTER TABLE public.synced_books DROP CONSTRAINT IF EXISTS synced_books_user_id_fkey;

-- Migrate user_id to TEXT if it was UUID in an older schema
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' 
          AND table_name = 'synced_books' 
          AND column_name = 'user_id' 
          AND data_type = 'uuid'
    ) THEN
        ALTER TABLE public.synced_books ALTER COLUMN user_id TYPE TEXT;
    END IF;
END $$;

-- Ensure all new columns exist if table was previously created without them
ALTER TABLE public.synced_books
    ADD COLUMN IF NOT EXISTS author      TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS description TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS genres      TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS status      BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS favorite    BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS cover_url   TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS source_name TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW();

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_book_id ON public.synced_books(book_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_last_read ON public.synced_books(user_id, last_read DESC);
CREATE INDEX IF NOT EXISTS idx_synced_books_title ON public.synced_books(title);

ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE schemaname = 'public' 
          AND tablename = 'synced_books' 
          AND policyname = 'Allow public synced_books access'
    ) THEN
        CREATE POLICY "Allow public synced_books access" 
            ON public.synced_books FOR ALL 
            USING (true) 
            WITH CHECK (true);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 3. Enhanced Reading Progress (Relational View)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.reading_progress (
    user_id              TEXT NOT NULL,
    book_id              TEXT NOT NULL,
    last_chapter_slug    TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    updated_at           BIGINT DEFAULT 0,
    PRIMARY KEY (user_id, book_id)
);

-- If table existed previously with FK to users, remove FK
ALTER TABLE public.reading_progress DROP CONSTRAINT IF EXISTS reading_progress_user_id_fkey;

-- Drop strict scroll check constraint if present so floating point offsets don't fail
ALTER TABLE public.reading_progress DROP CONSTRAINT IF EXISTS scroll_position_range;

-- Migrate user_id to TEXT if it was UUID in an older schema
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' 
          AND table_name = 'reading_progress' 
          AND column_name = 'user_id' 
          AND data_type = 'uuid'
    ) THEN
        ALTER TABLE public.reading_progress ALTER COLUMN user_id TYPE TEXT;
    END IF;
END $$;

-- Ensure composite uniqueness for upsert on (user_id, book_id)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.reading_progress'::regclass
          AND contype IN ('p', 'u')
    ) THEN
        ALTER TABLE public.reading_progress ADD CONSTRAINT unique_user_book UNIQUE (user_id, book_id);
    END IF;
EXCEPTION WHEN OTHERS THEN
    -- Constraint or primary key already present
    NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_book_id ON public.reading_progress(book_id);

ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE schemaname = 'public' 
          AND tablename = 'reading_progress' 
          AND policyname = 'Allow public reading_progress access'
    ) THEN
        CREATE POLICY "Allow public reading_progress access" 
            ON public.reading_progress FOR ALL 
            USING (true) 
            WITH CHECK (true);
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 4. Record migration in schema_version (if table exists)
-- ----------------------------------------------------------------------------
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'schema_version') THEN
        INSERT INTO public.schema_version (version, description)
        VALUES (5, 'Unified sync manifest, rich book metadata (author, description, genres, status, favorite), and personal Supabase sync support')
        ON CONFLICT (version) DO UPDATE 
        SET description = EXCLUDED.description, applied_at = NOW();
    END IF;
END $$;
