-- ============================================================================
-- PROJECT 11: CHARACTER ART DATABASE SCHEMA
-- ============================================================================
-- This schema contains character art gallery system
-- Tables: character_art, character_art_likes, character_art_reports
-- Estimated storage: ~500MB for art metadata
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- Character Art Table
CREATE TABLE IF NOT EXISTS public.character_art (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_name TEXT NOT NULL,
    book_title TEXT NOT NULL,
    book_author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    image_url TEXT NOT NULL,
    thumbnail_url TEXT DEFAULT '',
    submitter_id TEXT NOT NULL,
    submitter_username TEXT DEFAULT '',
    ai_model TEXT DEFAULT '',
    prompt TEXT DEFAULT '',
    likes_count INTEGER DEFAULT 0,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    submitted_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_featured BOOLEAN DEFAULT FALSE,
    tags TEXT[] DEFAULT '{}',
    rejection_reason TEXT DEFAULT '',
    width INTEGER DEFAULT 0,
    height INTEGER DEFAULT 0,
    auto_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT ca_submitter_id_not_empty CHECK (LENGTH(submitter_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_character_art_status ON public.character_art(status);
CREATE INDEX IF NOT EXISTS idx_character_art_submitter ON public.character_art(submitter_id);
CREATE INDEX IF NOT EXISTS idx_character_art_book ON public.character_art(book_title);
CREATE INDEX IF NOT EXISTS idx_character_art_character ON public.character_art(character_name);
CREATE INDEX IF NOT EXISTS idx_character_art_featured ON public.character_art(is_featured) WHERE is_featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_character_art_likes ON public.character_art(likes_count DESC);
CREATE INDEX IF NOT EXISTS idx_character_art_submitted ON public.character_art(submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_character_art_tags ON public.character_art USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_character_art_pending_submitted ON public.character_art(submitted_at) WHERE status = 'PENDING';

COMMENT ON TABLE public.character_art IS 'Character art gallery with community submissions';
COMMENT ON COLUMN public.character_art.auto_approved IS 'True if this art was automatically approved after being pending for 7+ days';

-- Character Art Likes Table
CREATE TABLE IF NOT EXISTS public.character_art_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES public.character_art(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(art_id, user_id),
    CONSTRAINT cal_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_art_likes_art ON public.character_art_likes(art_id);
CREATE INDEX IF NOT EXISTS idx_art_likes_user ON public.character_art_likes(user_id);

COMMENT ON TABLE public.character_art_likes IS 'Likes for character art';

-- Character Art Reports Table
CREATE TABLE IF NOT EXISTS public.character_art_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES public.character_art(id) ON DELETE CASCADE,
    reporter_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT car_reporter_id_not_empty CHECK (LENGTH(reporter_id) > 0)
);

COMMENT ON TABLE public.character_art_reports IS 'Reports for character art';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.character_art ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.character_art_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.character_art_reports ENABLE ROW LEVEL SECURITY;

-- Character art policies
CREATE POLICY "Anyone can view approved art" ON public.character_art FOR SELECT USING (status = 'APPROVED');
CREATE POLICY "Users can view own submissions" ON public.character_art FOR SELECT USING (submitter_id = auth.uid()::TEXT);
CREATE POLICY "Authenticated users can submit art" ON public.character_art FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can delete own pending art" ON public.character_art FOR DELETE USING (submitter_id = auth.uid()::TEXT AND status = 'PENDING');

-- Character art likes policies
CREATE POLICY "Anyone can view likes" ON public.character_art_likes FOR SELECT USING (TRUE);
CREATE POLICY "Authenticated users can like" ON public.character_art_likes FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can unlike their own likes" ON public.character_art_likes FOR DELETE USING (user_id = auth.uid()::TEXT);

-- Character art reports policies
CREATE POLICY "Authenticated users can report" ON public.character_art_reports FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION increment_art_likes(art_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.character_art SET likes_count = likes_count + 1, updated_at = NOW() WHERE id = art_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION decrement_art_likes(art_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.character_art SET likes_count = GREATEST(0, likes_count - 1), updated_at = NOW() WHERE id = art_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION auto_approve_old_pending_art(days_threshold INTEGER DEFAULT 7)
RETURNS INTEGER LANGUAGE plpgsql SECURITY DEFINER AS $
DECLARE approved_count INTEGER;
BEGIN
    WITH updated AS (
        UPDATE public.character_art SET status = 'APPROVED', auto_approved = TRUE
        WHERE status = 'PENDING' AND submitted_at < (EXTRACT(EPOCH FROM NOW()) * 1000 - (days_threshold * 24 * 60 * 60 * 1000))
        RETURNING id
    )
    SELECT COUNT(*) INTO approved_count FROM updated;
    RETURN approved_count;
END;
$;

GRANT EXECUTE ON FUNCTION auto_approve_old_pending_art(INTEGER) TO authenticated;
COMMENT ON FUNCTION auto_approve_old_pending_art IS 'Auto-approves pending character art older than specified days. Returns count of approved items.';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS character_art_updated_at ON public.character_art;
CREATE TRIGGER character_art_updated_at BEFORE UPDATE ON public.character_art FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $
BEGIN
    RAISE NOTICE '✅ PROJECT 11 (CHARACTER ART) Schema created successfully!';
    RAISE NOTICE 'Tables: character_art, character_art_likes, character_art_reports';
    RAISE NOTICE 'Features: Auto-approve after 7 days, likes, reports';
END $;
