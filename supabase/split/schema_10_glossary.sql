-- ============================================================================
-- PROJECT 10: GLOSSARY DATABASE SCHEMA
-- ============================================================================
-- This schema contains glossary sync system
-- Tables: glossary_entries, community_glossaries
-- Estimated storage: ~500MB for glossary entries
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- Glossary Entries Table (User's personal glossary)
CREATE TABLE IF NOT EXISTS public.glossary_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    book_key TEXT NOT NULL,
    book_title TEXT NOT NULL,
    source_term TEXT NOT NULL,
    target_term TEXT NOT NULL,
    term_type TEXT NOT NULL DEFAULT 'custom',
    notes TEXT,
    source_language TEXT NOT NULL DEFAULT 'auto',
    target_language TEXT NOT NULL DEFAULT 'en',
    is_public BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, book_key, source_term),
    CONSTRAINT ge_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_glossary_entries_user_id ON public.glossary_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_book_key ON public.glossary_entries(book_key);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_public ON public.glossary_entries(is_public) WHERE is_public = true;

COMMENT ON TABLE public.glossary_entries IS 'User glossary entries for translation sync';

-- Community Glossaries Table (Shared/verified entries)
CREATE TABLE IF NOT EXISTS public.community_glossaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_key TEXT NOT NULL,
    book_title TEXT NOT NULL,
    source_term TEXT NOT NULL,
    target_term TEXT NOT NULL,
    term_type TEXT NOT NULL DEFAULT 'custom',
    notes TEXT,
    source_language TEXT NOT NULL DEFAULT 'auto',
    target_language TEXT NOT NULL DEFAULT 'en',
    contributor_id TEXT,
    upvotes INTEGER NOT NULL DEFAULT 0,
    downvotes INTEGER NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(book_key, source_term, target_language)
);

CREATE INDEX IF NOT EXISTS idx_community_glossaries_book_key ON public.community_glossaries(book_key);
CREATE INDEX IF NOT EXISTS idx_community_glossaries_verified ON public.community_glossaries(is_verified) WHERE is_verified = true;

COMMENT ON TABLE public.community_glossaries IS 'Community shared glossaries (curated/verified entries)';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.glossary_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_glossaries ENABLE ROW LEVEL SECURITY;

-- Glossary entries policies
CREATE POLICY "Users can read own glossary entries" ON public.glossary_entries FOR SELECT USING (user_id = auth.uid()::TEXT);
CREATE POLICY "Anyone can read public glossary entries" ON public.glossary_entries FOR SELECT USING (is_public = true);
CREATE POLICY "Users can insert own glossary entries" ON public.glossary_entries FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update own glossary entries" ON public.glossary_entries FOR UPDATE USING (user_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete own glossary entries" ON public.glossary_entries FOR DELETE USING (user_id = auth.uid()::TEXT);

-- Community glossaries policies
CREATE POLICY "Anyone can read community glossaries" ON public.community_glossaries FOR SELECT USING (true);
CREATE POLICY "Authenticated users can contribute glossaries" ON public.community_glossaries FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

CREATE OR REPLACE FUNCTION update_glossary_updated_at()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS glossary_entries_updated_at ON public.glossary_entries;
CREATE TRIGGER glossary_entries_updated_at BEFORE UPDATE ON public.glossary_entries FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at();

DROP TRIGGER IF EXISTS community_glossaries_updated_at ON public.community_glossaries;
CREATE TRIGGER community_glossaries_updated_at BEFORE UPDATE ON public.community_glossaries FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at();

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $
BEGIN
    RAISE NOTICE '✅ PROJECT 10 (GLOSSARY) Schema created successfully!';
    RAISE NOTICE 'Tables: glossary_entries, community_glossaries';
END $;
