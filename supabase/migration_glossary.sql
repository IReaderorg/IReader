-- Migration: Add glossary tables for community glossary sync
-- This allows users to sync their glossaries across devices and share with the community

-- Global glossary entries table
CREATE TABLE IF NOT EXISTS public.glossary_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
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
    UNIQUE(user_id, book_key, source_term)
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_glossary_entries_user_id ON public.glossary_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_book_key ON public.glossary_entries(book_key);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_public ON public.glossary_entries(is_public) WHERE is_public = true;

-- Community shared glossaries (curated/verified entries)
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
    contributor_id UUID REFERENCES auth.users(id),
    upvotes INTEGER NOT NULL DEFAULT 0,
    downvotes INTEGER NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(book_key, source_term, target_language)
);

-- Index for community glossaries
CREATE INDEX IF NOT EXISTS idx_community_glossaries_book_key ON public.community_glossaries(book_key);
CREATE INDEX IF NOT EXISTS idx_community_glossaries_verified ON public.community_glossaries(is_verified) WHERE is_verified = true;

-- RLS Policies for glossary_entries
ALTER TABLE public.glossary_entries ENABLE ROW LEVEL SECURITY;

-- Users can read their own entries
CREATE POLICY "Users can read own glossary entries"
    ON public.glossary_entries FOR SELECT
    USING (auth.uid() = user_id);

-- Users can read public entries
CREATE POLICY "Anyone can read public glossary entries"
    ON public.glossary_entries FOR SELECT
    USING (is_public = true);

-- Users can insert their own entries
CREATE POLICY "Users can insert own glossary entries"
    ON public.glossary_entries FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own entries
CREATE POLICY "Users can update own glossary entries"
    ON public.glossary_entries FOR UPDATE
    USING (auth.uid() = user_id);

-- Users can delete their own entries
CREATE POLICY "Users can delete own glossary entries"
    ON public.glossary_entries FOR DELETE
    USING (auth.uid() = user_id);

-- RLS Policies for community_glossaries
ALTER TABLE public.community_glossaries ENABLE ROW LEVEL SECURITY;

-- Anyone can read community glossaries
CREATE POLICY "Anyone can read community glossaries"
    ON public.community_glossaries FOR SELECT
    USING (true);

-- Only authenticated users can contribute
CREATE POLICY "Authenticated users can contribute glossaries"
    ON public.community_glossaries FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_glossary_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER glossary_entries_updated_at
    BEFORE UPDATE ON public.glossary_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_glossary_updated_at();

CREATE TRIGGER community_glossaries_updated_at
    BEFORE UPDATE ON public.community_glossaries
    FOR EACH ROW
    EXECUTE FUNCTION update_glossary_updated_at();
