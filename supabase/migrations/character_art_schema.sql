-- ============================================
-- Character Art Gallery Schema for Supabase
-- ============================================
-- Images stored in Cloudflare R2, metadata in Supabase
-- Run this in your Supabase SQL Editor

-- ============================================
-- Main character_art table
-- ============================================
CREATE TABLE IF NOT EXISTS character_art (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_name TEXT NOT NULL,
    book_title TEXT NOT NULL,
    book_author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    image_url TEXT NOT NULL,
    thumbnail_url TEXT DEFAULT '',
    submitter_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
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
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- Likes table (many-to-many)
-- ============================================
CREATE TABLE IF NOT EXISTS character_art_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES character_art(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(art_id, user_id)
);

-- ============================================
-- Reports table
-- ============================================
CREATE TABLE IF NOT EXISTS character_art_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES character_art(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- Indexes for performance
-- ============================================
CREATE INDEX IF NOT EXISTS idx_character_art_status ON character_art(status);
CREATE INDEX IF NOT EXISTS idx_character_art_submitter ON character_art(submitter_id);
CREATE INDEX IF NOT EXISTS idx_character_art_book ON character_art(book_title);
CREATE INDEX IF NOT EXISTS idx_character_art_character ON character_art(character_name);
CREATE INDEX IF NOT EXISTS idx_character_art_featured ON character_art(is_featured) WHERE is_featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_character_art_likes ON character_art(likes_count DESC);
CREATE INDEX IF NOT EXISTS idx_character_art_submitted ON character_art(submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_character_art_tags ON character_art USING GIN(tags);

CREATE INDEX IF NOT EXISTS idx_art_likes_art ON character_art_likes(art_id);
CREATE INDEX IF NOT EXISTS idx_art_likes_user ON character_art_likes(user_id);

-- ============================================
-- Functions for like count management
-- ============================================
CREATE OR REPLACE FUNCTION increment_art_likes(art_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE character_art 
    SET likes_count = likes_count + 1,
        updated_at = NOW()
    WHERE id = art_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION decrement_art_likes(art_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE character_art 
    SET likes_count = GREATEST(0, likes_count - 1),
        updated_at = NOW()
    WHERE id = art_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- Auto-update timestamp trigger
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER character_art_updated_at
    BEFORE UPDATE ON character_art
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- ============================================
-- Row Level Security (RLS)
-- ============================================
ALTER TABLE character_art ENABLE ROW LEVEL SECURITY;
ALTER TABLE character_art_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE character_art_reports ENABLE ROW LEVEL SECURITY;

-- Anyone can view approved art
CREATE POLICY "Anyone can view approved art"
    ON character_art FOR SELECT
    USING (status = 'APPROVED');

-- Users can view their own submissions (any status)
CREATE POLICY "Users can view own submissions"
    ON character_art FOR SELECT
    USING (auth.uid() = submitter_id);

-- Authenticated users can submit art
CREATE POLICY "Authenticated users can submit art"
    ON character_art FOR INSERT
    WITH CHECK (auth.uid() = submitter_id);

-- Users can delete their own pending art
CREATE POLICY "Users can delete own pending art"
    ON character_art FOR DELETE
    USING (auth.uid() = submitter_id AND status = 'PENDING');

-- Likes policies
CREATE POLICY "Anyone can view likes"
    ON character_art_likes FOR SELECT
    USING (TRUE);

CREATE POLICY "Authenticated users can like"
    ON character_art_likes FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can unlike their own likes"
    ON character_art_likes FOR DELETE
    USING (auth.uid() = user_id);

-- Reports policies
CREATE POLICY "Authenticated users can report"
    ON character_art_reports FOR INSERT
    WITH CHECK (auth.uid() = reporter_id);

-- ============================================
-- Admin policies (optional - requires admin role)
-- ============================================
-- Uncomment if you have admin role set up

-- CREATE POLICY "Admins can view all art"
--     ON character_art FOR SELECT
--     USING (
--         EXISTS (
--             SELECT 1 FROM profiles 
--             WHERE id = auth.uid() AND is_admin = TRUE
--         )
--     );

-- CREATE POLICY "Admins can update art status"
--     ON character_art FOR UPDATE
--     USING (
--         EXISTS (
--             SELECT 1 FROM profiles 
--             WHERE id = auth.uid() AND is_admin = TRUE
--         )
--     );

-- CREATE POLICY "Admins can delete any art"
--     ON character_art FOR DELETE
--     USING (
--         EXISTS (
--             SELECT 1 FROM profiles 
--             WHERE id = auth.uid() AND is_admin = TRUE
--         )
--     );

-- ============================================
-- Sample data (optional - for testing)
-- ============================================
-- INSERT INTO character_art (character_name, book_title, book_author, image_url, submitter_id, ai_model, status, is_featured, tags)
-- VALUES 
--     ('Harry Potter', 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 'https://example.com/harry.jpg', 'your-user-id', 'Midjourney', 'APPROVED', TRUE, ARRAY['FANTASY', 'REALISTIC']),
--     ('Frodo Baggins', 'The Lord of the Rings', 'J.R.R. Tolkien', 'https://example.com/frodo.jpg', 'your-user-id', 'DALL-E', 'APPROVED', FALSE, ARRAY['FANTASY']);

-- ============================================
-- Useful queries
-- ============================================

-- Get approved art with like status for a user:
-- SELECT ca.*, 
--        EXISTS(SELECT 1 FROM character_art_likes WHERE art_id = ca.id AND user_id = 'user-uuid') as is_liked
-- FROM character_art ca
-- WHERE ca.status = 'APPROVED'
-- ORDER BY ca.submitted_at DESC;

-- Get pending art count:
-- SELECT COUNT(*) FROM character_art WHERE status = 'PENDING';

-- Get top liked art:
-- SELECT * FROM character_art 
-- WHERE status = 'APPROVED' 
-- ORDER BY likes_count DESC 
-- LIMIT 10;
