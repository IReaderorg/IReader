-- ==========================================================
-- IReader Complete Database Schema
-- ==========================================================
-- This file contains all tables, policies, triggers, and functions
-- Run this in your Supabase SQL Editor to set up the complete database
--
-- INCLUDED FEATURES:
-- ✓ Base schema (users, reading_progress, synced_books, reviews)
-- ✓ Badge system (badges, user_badges, payment_proofs, nft_wallets)
-- ✓ Leaderboard system with realtime support
-- ✓ Achievement badges (reading, reviews, streaks, etc.)
-- ✓ Admin role for badge verification
-- ✓ Username auto-generation
-- ✓ Quotes system with community submissions
-- ✓ Community source (books, chapters, translations)
-- ✓ Glossary sync system
-- ✓ Character art gallery
-- ✓ Donation leaderboard
-- ==========================================================

-- ============================================================================
-- EXTENSIONS
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Users Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT auth.uid(),
    email TEXT NOT NULL UNIQUE,
    username TEXT,
    eth_wallet_address TEXT,
    is_supporter BOOLEAN DEFAULT FALSE,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT username_length CHECK (username IS NULL OR LENGTH(username) >= 3),
    CONSTRAINT eth_wallet_format CHECK (eth_wallet_address IS NULL OR eth_wallet_address ~* '^0x[a-fA-F0-9]{40}$')
);

CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_eth_wallet ON public.users(eth_wallet_address) WHERE eth_wallet_address IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_is_admin ON public.users(is_admin) WHERE is_admin = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_email_btree ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username_btree ON public.users(username);

COMMENT ON TABLE public.users IS 'User profiles and authentication data';
COMMENT ON COLUMN public.users.is_admin IS 'Whether user has admin privileges for badge verification';

-- ----------------------------------------------------------------------------
-- Reading Progress Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_user_book UNIQUE(user_id, book_id),
    CONSTRAINT scroll_position_range CHECK (last_scroll_position >= 0 AND last_scroll_position <= 1),
    CONSTRAINT book_id_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT chapter_slug_not_empty CHECK (LENGTH(last_chapter_slug) > 0)
);

CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_book ON public.reading_progress(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_reading_progress_updated_at ON public.reading_progress(updated_at DESC);

COMMENT ON TABLE public.reading_progress IS 'Current reading position for each book';

-- ----------------------------------------------------------------------------
-- Synced Books Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.synced_books (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    source_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    book_url TEXT NOT NULL,
    last_read BIGINT NOT NULL DEFAULT 0,
    
    PRIMARY KEY (user_id, book_id),
    CONSTRAINT book_id_synced_not_empty CHECK (LENGTH(book_id) > 0),
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT book_url_not_empty CHECK (LENGTH(book_url) > 0),
    CONSTRAINT last_read_non_negative CHECK (last_read >= 0)
);

CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
CREATE INDEX IF NOT EXISTS idx_synced_books_last_read ON public.synced_books(user_id, last_read DESC);
CREATE INDEX IF NOT EXISTS idx_synced_books_title ON public.synced_books(title);

COMMENT ON TABLE public.synced_books IS 'Favorite books with essential metadata';

-- ----------------------------------------------------------------------------
-- Book Reviews Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.book_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_title TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 2000)
);

CREATE INDEX IF NOT EXISTS idx_book_reviews_user_id ON public.book_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_book_reviews_book_title ON public.book_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_book_reviews_rating ON public.book_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_book_reviews_created_at ON public.book_reviews(created_at DESC);

COMMENT ON TABLE public.book_reviews IS 'Book reviews based on normalized title (shared across sources)';

-- ----------------------------------------------------------------------------
-- Chapter Reviews Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chapter_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    book_title TEXT NOT NULL,
    chapter_name TEXT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT book_title_chapter_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(chapter_name) > 0),
    CONSTRAINT chapter_rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chapter_review_text_length CHECK (LENGTH(review_text) > 0 AND LENGTH(review_text) <= 1000)
);

CREATE INDEX IF NOT EXISTS idx_chapter_reviews_user_id ON public.chapter_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_title ON public.chapter_reviews(book_title);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_book_chapter ON public.chapter_reviews(book_title, chapter_name);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_rating ON public.chapter_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_chapter_reviews_created_at ON public.chapter_reviews(created_at DESC);

COMMENT ON TABLE public.chapter_reviews IS 'Chapter reviews based on normalized book + chapter names';

-- ----------------------------------------------------------------------------
-- Badges Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.badges (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    icon TEXT NOT NULL,
    category TEXT NOT NULL,
    rarity TEXT NOT NULL,
    price DECIMAL(10, 2),
    type TEXT NOT NULL DEFAULT 'ACHIEVEMENT',
    image_url TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT badge_id_not_empty CHECK (LENGTH(id) > 0),
    CONSTRAINT badge_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT badge_category_valid CHECK (category IN ('donor', 'contributor', 'reader', 'reviewer', 'special', 'purchasable', 'nft')),
    CONSTRAINT badge_rarity_valid CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')),
    CONSTRAINT badge_type_valid CHECK (type IN ('PURCHASABLE', 'NFT_EXCLUSIVE', 'ACHIEVEMENT')),
    CONSTRAINT badge_price_positive CHECK (price IS NULL OR price > 0)
);

CREATE INDEX IF NOT EXISTS idx_badges_category ON public.badges(category);
CREATE INDEX IF NOT EXISTS idx_badges_rarity ON public.badges(rarity);
CREATE INDEX IF NOT EXISTS idx_badges_type ON public.badges(type);
CREATE INDEX IF NOT EXISTS idx_badges_available ON public.badges(is_available) WHERE is_available = TRUE;

COMMENT ON TABLE public.badges IS 'Available badges that users can earn or purchase';

-- ----------------------------------------------------------------------------
-- User Badges Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_badges (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    badge_id TEXT NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    earned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_primary BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    metadata JSONB,
    
    PRIMARY KEY (user_id, badge_id)
);

CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON public.user_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_badge_id ON public.user_badges(badge_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_earned_at ON public.user_badges(earned_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_badges_primary ON public.user_badges(user_id, is_primary) WHERE is_primary = TRUE;
CREATE INDEX IF NOT EXISTS idx_user_badges_featured ON public.user_badges(user_id, is_featured) WHERE is_featured = TRUE;

COMMENT ON TABLE public.user_badges IS 'Badges earned or purchased by users';

-- ----------------------------------------------------------------------------
-- Payment Proofs Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.payment_proofs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    badge_id TEXT NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    transaction_id TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    proof_image_url TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID REFERENCES public.users(id),
    
    CONSTRAINT payment_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT transaction_id_not_empty CHECK (LENGTH(transaction_id) > 0),
    CONSTRAINT payment_method_not_empty CHECK (LENGTH(payment_method) > 0)
);

CREATE INDEX IF NOT EXISTS idx_payment_proofs_user_id ON public.payment_proofs(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_status ON public.payment_proofs(status);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_submitted_at ON public.payment_proofs(submitted_at DESC);

COMMENT ON TABLE public.payment_proofs IS 'Payment proof submissions for badge purchases';

-- ----------------------------------------------------------------------------
-- NFT Wallets Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.nft_wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES public.users(id) ON DELETE CASCADE,
    wallet_address TEXT NOT NULL,
    last_verified TIMESTAMP WITH TIME ZONE,
    owns_nft BOOLEAN DEFAULT FALSE,
    nft_token_id TEXT,
    verification_cache_expires TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT wallet_address_format CHECK (wallet_address ~* '^0x[a-fA-F0-9]{40}$')
);

CREATE INDEX IF NOT EXISTS idx_nft_wallets_user_id ON public.nft_wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_address ON public.nft_wallets(wallet_address);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_cache_expires ON public.nft_wallets(verification_cache_expires);

COMMENT ON TABLE public.nft_wallets IS 'Ethereum wallet addresses and NFT ownership verification';

-- ----------------------------------------------------------------------------
-- Leaderboard Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.leaderboard (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT NOT NULL,
    total_reading_time_minutes BIGINT NOT NULL DEFAULT 0,
    total_chapters_read INTEGER NOT NULL DEFAULT 0,
    books_completed INTEGER NOT NULL DEFAULT 0,
    reading_streak INTEGER NOT NULL DEFAULT 0,
    has_badge BOOLEAN NOT NULL DEFAULT FALSE,
    badge_type TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id)
);

CREATE INDEX IF NOT EXISTS idx_leaderboard_reading_time ON public.leaderboard(total_reading_time_minutes DESC);
CREATE INDEX IF NOT EXISTS idx_leaderboard_user_id ON public.leaderboard(user_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_username ON public.leaderboard(username);

COMMENT ON TABLE public.leaderboard IS 'User reading statistics for the leaderboard feature';


-- ----------------------------------------------------------------------------
-- Community Quotes Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    quote_text TEXT NOT NULL,
    book_title TEXT NOT NULL,
    author TEXT,
    chapter_title TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID REFERENCES public.users(id),
    likes_count INTEGER DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT quote_text_length CHECK (LENGTH(quote_text) >= 10 AND LENGTH(quote_text) <= 1000),
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT quote_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_community_quotes_user_id ON public.community_quotes(user_id);
CREATE INDEX IF NOT EXISTS idx_community_quotes_status ON public.community_quotes(status);
CREATE INDEX IF NOT EXISTS idx_community_quotes_approved ON public.community_quotes(status, submitted_at DESC) WHERE status = 'APPROVED';
CREATE INDEX IF NOT EXISTS idx_community_quotes_featured ON public.community_quotes(featured, likes_count DESC) WHERE featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_community_quotes_book ON public.community_quotes(book_title);

COMMENT ON TABLE public.community_quotes IS 'Community-submitted quotes from books, requires admin approval';

-- ----------------------------------------------------------------------------
-- Quote Likes Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.quote_likes (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    liked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    PRIMARY KEY (user_id, quote_id)
);

CREATE INDEX IF NOT EXISTS idx_quote_likes_quote_id ON public.quote_likes(quote_id);
CREATE INDEX IF NOT EXISTS idx_quote_likes_user_id ON public.quote_likes(user_id);

COMMENT ON TABLE public.quote_likes IS 'Tracks user likes on quotes';

-- ----------------------------------------------------------------------------
-- Daily Quote History Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.daily_quote_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    shown_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_daily_quote_date ON public.daily_quote_history(shown_date DESC);

COMMENT ON TABLE public.daily_quote_history IS 'History of daily quotes shown';

-- ----------------------------------------------------------------------------
-- Community Books Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    cover TEXT DEFAULT '',
    genres TEXT[] DEFAULT '{}',
    status TEXT DEFAULT 'Ongoing',
    original_language TEXT DEFAULT 'en',
    available_languages TEXT[] DEFAULT '{}',
    contributor_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    contributor_name TEXT DEFAULT '',
    view_count BIGINT DEFAULT 0,
    chapter_count INTEGER DEFAULT 0,
    last_updated BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    is_nsfw BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT title_not_empty CHECK (LENGTH(title) > 0),
    CONSTRAINT status_valid CHECK (status IN ('Ongoing', 'Completed', 'Hiatus', 'Dropped')),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0),
    CONSTRAINT chapter_count_non_negative CHECK (chapter_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_community_books_title ON public.community_books(title);
CREATE INDEX IF NOT EXISTS idx_community_books_author ON public.community_books(author);
CREATE INDEX IF NOT EXISTS idx_community_books_contributor ON public.community_books(contributor_id);
CREATE INDEX IF NOT EXISTS idx_community_books_status ON public.community_books(status);
CREATE INDEX IF NOT EXISTS idx_community_books_view_count ON public.community_books(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_created_at ON public.community_books(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_last_updated ON public.community_books(last_updated DESC);
CREATE INDEX IF NOT EXISTS idx_community_books_languages ON public.community_books USING GIN(available_languages);
CREATE INDEX IF NOT EXISTS idx_community_books_genres ON public.community_books USING GIN(genres);

COMMENT ON TABLE public.community_books IS 'Books contributed by the community for translation sharing';

-- ----------------------------------------------------------------------------
-- Community Chapters Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_chapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES public.community_books(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    number REAL DEFAULT -1,
    content TEXT NOT NULL,
    language TEXT NOT NULL,
    translator_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    translator_name TEXT DEFAULT '',
    original_chapter_key TEXT DEFAULT '',
    rating REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at BIGINT DEFAULT 0,
    updated_at BIGINT DEFAULT 0,
    is_approved BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT chapter_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT chapter_content_not_empty CHECK (LENGTH(content) > 0),
    CONSTRAINT language_not_empty CHECK (LENGTH(language) > 0),
    CONSTRAINT rating_range CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT rating_count_non_negative CHECK (rating_count >= 0),
    CONSTRAINT view_count_non_negative CHECK (view_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_community_chapters_book_id ON public.community_chapters(book_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_language ON public.community_chapters(language);
CREATE INDEX IF NOT EXISTS idx_community_chapters_translator ON public.community_chapters(translator_id);
CREATE INDEX IF NOT EXISTS idx_community_chapters_number ON public.community_chapters(number);
CREATE INDEX IF NOT EXISTS idx_community_chapters_rating ON public.community_chapters(rating DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_created_at ON public.community_chapters(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_community_chapters_book_lang ON public.community_chapters(book_id, language);

COMMENT ON TABLE public.community_chapters IS 'Translated chapters contributed by the community';

-- ----------------------------------------------------------------------------
-- Chapter Reports Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chapter_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    reporter_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    created_at BIGINT DEFAULT 0,
    reviewed_at BIGINT,
    reviewed_by UUID REFERENCES public.users(id),
    
    CONSTRAINT reason_not_empty CHECK (LENGTH(reason) > 0),
    CONSTRAINT report_status_valid CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED'))
);

CREATE INDEX IF NOT EXISTS idx_chapter_reports_chapter ON public.chapter_reports(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_reports_status ON public.chapter_reports(status);
CREATE INDEX IF NOT EXISTS idx_chapter_reports_created_at ON public.chapter_reports(created_at DESC);

COMMENT ON TABLE public.chapter_reports IS 'Reports for problematic community chapters';

-- ----------------------------------------------------------------------------
-- Chapter Ratings Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chapter_ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES public.community_chapters(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    created_at BIGINT DEFAULT 0,
    
    CONSTRAINT unique_user_chapter_rating UNIQUE(user_id, chapter_id),
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX IF NOT EXISTS idx_chapter_ratings_chapter ON public.chapter_ratings(chapter_id);
CREATE INDEX IF NOT EXISTS idx_chapter_ratings_user ON public.chapter_ratings(user_id);

COMMENT ON TABLE public.chapter_ratings IS 'Individual user ratings for community chapters';

-- ----------------------------------------------------------------------------
-- Glossary Entries Table
-- ----------------------------------------------------------------------------
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

CREATE INDEX IF NOT EXISTS idx_glossary_entries_user_id ON public.glossary_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_book_key ON public.glossary_entries(book_key);
CREATE INDEX IF NOT EXISTS idx_glossary_entries_public ON public.glossary_entries(is_public) WHERE is_public = true;

COMMENT ON TABLE public.glossary_entries IS 'User glossary entries for translation sync';

-- ----------------------------------------------------------------------------
-- Community Glossaries Table
-- ----------------------------------------------------------------------------
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

CREATE INDEX IF NOT EXISTS idx_community_glossaries_book_key ON public.community_glossaries(book_key);
CREATE INDEX IF NOT EXISTS idx_community_glossaries_verified ON public.community_glossaries(is_verified) WHERE is_verified = true;

COMMENT ON TABLE public.community_glossaries IS 'Community shared glossaries (curated/verified entries)';

-- ----------------------------------------------------------------------------
-- Character Art Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.character_art (
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
    auto_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
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

-- ----------------------------------------------------------------------------
-- Character Art Likes Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.character_art_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES public.character_art(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(art_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_art_likes_art ON public.character_art_likes(art_id);
CREATE INDEX IF NOT EXISTS idx_art_likes_user ON public.character_art_likes(user_id);

COMMENT ON TABLE public.character_art_likes IS 'Likes for character art';

-- ----------------------------------------------------------------------------
-- Character Art Reports Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.character_art_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    art_id UUID NOT NULL REFERENCES public.character_art(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE public.character_art_reports IS 'Reports for character art';

-- ----------------------------------------------------------------------------
-- Schema Version Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.schema_version (
    version INTEGER PRIMARY KEY,
    applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    description TEXT
);

INSERT INTO public.schema_version (version, description)
VALUES (2, 'Complete consolidated schema with all features')
ON CONFLICT (version) DO NOTHING;


-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.book_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_proofs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nft_wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_quotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quote_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_quote_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_chapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapter_ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.glossary_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_glossaries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.character_art ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.character_art_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.character_art_reports ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- Users Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own data" ON public.users FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update their own data" ON public.users FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert their own data" ON public.users FOR INSERT WITH CHECK (auth.uid() = id);

-- ----------------------------------------------------------------------------
-- Reading Progress Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own reading progress" ON public.reading_progress FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own reading progress" ON public.reading_progress FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own reading progress" ON public.reading_progress FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own reading progress" ON public.reading_progress FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Synced Books Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own synced books" ON public.synced_books FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own synced books" ON public.synced_books FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own synced books" ON public.synced_books FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own synced books" ON public.synced_books FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Book Reviews Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view all book reviews" ON public.book_reviews FOR SELECT USING (true);
CREATE POLICY "Users can insert their own book reviews" ON public.book_reviews FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own book reviews" ON public.book_reviews FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own book reviews" ON public.book_reviews FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Chapter Reviews Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view all chapter reviews" ON public.chapter_reviews FOR SELECT USING (true);
CREATE POLICY "Users can insert their own chapter reviews" ON public.chapter_reviews FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own chapter reviews" ON public.chapter_reviews FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own chapter reviews" ON public.chapter_reviews FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Badges Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view badges" ON public.badges FOR SELECT USING (true);

-- ----------------------------------------------------------------------------
-- User Badges Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view user badges" ON public.user_badges FOR SELECT USING (true);
CREATE POLICY "Users can update their own badge settings" ON public.user_badges FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Payment Proofs Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own payment proofs" ON public.payment_proofs FOR SELECT
    USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Users can insert their own payment proofs" ON public.payment_proofs FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Admins can update payment proofs" ON public.payment_proofs FOR UPDATE
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- ----------------------------------------------------------------------------
-- NFT Wallets Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own NFT wallet" ON public.nft_wallets FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own NFT wallet" ON public.nft_wallets FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own NFT wallet" ON public.nft_wallets FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Leaderboard Table Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Leaderboard is publicly readable" ON public.leaderboard FOR SELECT USING (true);
CREATE POLICY "Users can insert their own stats" ON public.leaderboard FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own stats" ON public.leaderboard FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete their own stats" ON public.leaderboard FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Community Quotes Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view approved quotes" ON public.community_quotes FOR SELECT
    USING (status = 'APPROVED' OR auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Users can submit quotes" ON public.community_quotes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update pending quotes" ON public.community_quotes FOR UPDATE
    USING ((auth.uid() = user_id AND status = 'PENDING') OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Users can delete pending quotes" ON public.community_quotes FOR DELETE USING (auth.uid() = user_id AND status = 'PENDING');

-- ----------------------------------------------------------------------------
-- Quote Likes Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view quote likes" ON public.quote_likes FOR SELECT USING (true);
CREATE POLICY "Users can like quotes" ON public.quote_likes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can unlike quotes" ON public.quote_likes FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Daily Quote History Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view daily quote history" ON public.daily_quote_history FOR SELECT USING (true);

-- ----------------------------------------------------------------------------
-- Community Books Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view approved community books" ON public.community_books FOR SELECT USING (is_approved = TRUE);
CREATE POLICY "Users can insert their own books" ON public.community_books FOR INSERT WITH CHECK (auth.uid() = contributor_id);
CREATE POLICY "Users can update their own books" ON public.community_books FOR UPDATE USING (auth.uid() = contributor_id);
CREATE POLICY "Users can delete their own books" ON public.community_books FOR DELETE USING (auth.uid() = contributor_id);

-- ----------------------------------------------------------------------------
-- Community Chapters Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view approved chapters" ON public.community_chapters FOR SELECT USING (is_approved = TRUE);
CREATE POLICY "Users can insert chapters" ON public.community_chapters FOR INSERT WITH CHECK (auth.uid() = translator_id);
CREATE POLICY "Users can update their own chapters" ON public.community_chapters FOR UPDATE USING (auth.uid() = translator_id);
CREATE POLICY "Users can delete their own chapters" ON public.community_chapters FOR DELETE USING (auth.uid() = translator_id);

-- ----------------------------------------------------------------------------
-- Chapter Reports Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can view their own reports" ON public.chapter_reports FOR SELECT USING (auth.uid() = reporter_id);
CREATE POLICY "Users can submit reports" ON public.chapter_reports FOR INSERT WITH CHECK (auth.uid() = reporter_id);

-- ----------------------------------------------------------------------------
-- Chapter Ratings Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Everyone can view ratings" ON public.chapter_ratings FOR SELECT USING (true);
CREATE POLICY "Users can rate chapters" ON public.chapter_ratings FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own ratings" ON public.chapter_ratings FOR UPDATE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Glossary Entries Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Users can read own glossary entries" ON public.glossary_entries FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Anyone can read public glossary entries" ON public.glossary_entries FOR SELECT USING (is_public = true);
CREATE POLICY "Users can insert own glossary entries" ON public.glossary_entries FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own glossary entries" ON public.glossary_entries FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own glossary entries" ON public.glossary_entries FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Community Glossaries Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Anyone can read community glossaries" ON public.community_glossaries FOR SELECT USING (true);
CREATE POLICY "Authenticated users can contribute glossaries" ON public.community_glossaries FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- ----------------------------------------------------------------------------
-- Character Art Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Anyone can view approved art" ON public.character_art FOR SELECT USING (status = 'APPROVED');
CREATE POLICY "Users can view own submissions" ON public.character_art FOR SELECT USING (auth.uid() = submitter_id);
CREATE POLICY "Admins can view all art" ON public.character_art FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Authenticated users can submit art" ON public.character_art FOR INSERT WITH CHECK (auth.uid() = submitter_id);
CREATE POLICY "Users can delete own pending art" ON public.character_art FOR DELETE USING (auth.uid() = submitter_id AND status = 'PENDING');
CREATE POLICY "Admins can update art status" ON public.character_art FOR UPDATE
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Admins can delete any art" ON public.character_art FOR DELETE
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- ----------------------------------------------------------------------------
-- Character Art Likes Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Anyone can view likes" ON public.character_art_likes FOR SELECT USING (TRUE);
CREATE POLICY "Authenticated users can like" ON public.character_art_likes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can unlike their own likes" ON public.character_art_likes FOR DELETE USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Character Art Reports Policies
-- ----------------------------------------------------------------------------
CREATE POLICY "Authenticated users can report" ON public.character_art_reports FOR INSERT WITH CHECK (auth.uid() = reporter_id);
CREATE POLICY "Admins can view reports" ON public.character_art_reports FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY "Admins can update reports" ON public.character_art_reports FOR UPDATE
    USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));


-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Function: Update updated_at timestamp
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Function: Generate default username
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION generate_default_username()
RETURNS TRIGGER AS $
BEGIN
    IF NEW.username IS NULL OR LENGTH(TRIM(NEW.username)) = 0 THEN
        IF NEW.email IS NOT NULL THEN
            NEW.username := 'Reader_' || SUBSTRING(NEW.email FROM 1 FOR POSITION('@' IN NEW.email) - 1);
        ELSE
            NEW.username := 'Reader_' || SUBSTRING(NEW.id::TEXT FROM 1 FOR 8);
        END IF;
    END IF;
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Function: Award badge to user
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION award_badge(p_user_id UUID, p_badge_id TEXT, p_metadata JSONB DEFAULT NULL)
RETURNS BOOLEAN AS $
BEGIN
    INSERT INTO public.user_badges (user_id, badge_id, metadata)
    VALUES (p_user_id, p_badge_id, p_metadata)
    ON CONFLICT (user_id, badge_id) DO NOTHING;
    RETURN FOUND;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Get user badges
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_badges(p_user_id UUID)
RETURNS TABLE (
    badge_id TEXT, badge_name TEXT, badge_description TEXT, badge_icon TEXT,
    badge_category TEXT, badge_rarity TEXT, badge_image_url TEXT, badge_type TEXT,
    is_primary BOOLEAN, is_featured BOOLEAN, earned_at TIMESTAMP WITH TIME ZONE, metadata JSONB
) AS $
BEGIN
    RETURN QUERY
    SELECT b.id, b.name, b.description, b.icon, b.category, b.rarity, b.image_url, b.type,
           ub.is_primary, ub.is_featured, ub.earned_at, ub.metadata
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id = p_user_id
    ORDER BY ub.earned_at DESC;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Get user badges with details
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_badges_with_details(p_user_id UUID)
RETURNS TABLE (
    badge_id TEXT, badge_name TEXT, badge_description TEXT, badge_icon TEXT,
    badge_category TEXT, badge_rarity TEXT, badge_type TEXT, badge_price DECIMAL(10, 2),
    badge_image_url TEXT, earned_at TIMESTAMP WITH TIME ZONE, is_primary BOOLEAN,
    is_featured BOOLEAN, metadata JSONB
) AS $
BEGIN
    RETURN QUERY
    SELECT b.id, b.name, b.description, b.icon, b.category, b.rarity, b.type, b.price,
           b.image_url, ub.earned_at, ub.is_primary, ub.is_featured, ub.metadata
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id = p_user_id
    ORDER BY ub.earned_at DESC;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Grant badge to user (admin)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION grant_badge_to_user(p_user_id UUID, p_badge_id TEXT)
RETURNS JSON LANGUAGE plpgsql SECURITY DEFINER AS $
DECLARE result JSON;
BEGIN
    INSERT INTO public.user_badges (user_id, badge_id, earned_at)
    VALUES (p_user_id, p_badge_id, NOW())
    ON CONFLICT (user_id, badge_id) DO NOTHING
    RETURNING json_build_object('user_id', user_id, 'badge_id', badge_id, 'earned_at', earned_at) INTO result;
    
    IF result IS NULL THEN
        SELECT json_build_object('user_id', user_id, 'badge_id', badge_id, 'earned_at', earned_at, 'already_exists', true)
        INTO result FROM public.user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id;
    END IF;
    RETURN result;
END;
$;

GRANT EXECUTE ON FUNCTION grant_badge_to_user(UUID, TEXT) TO authenticated;

-- ----------------------------------------------------------------------------
-- Function: Check and award achievement badge
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION check_and_award_achievement_badge(p_user_id UUID, p_badge_id TEXT)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER AS $
DECLARE v_already_has_badge BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM public.user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id) INTO v_already_has_badge;
    IF NOT v_already_has_badge THEN
        INSERT INTO public.user_badges (user_id, badge_id, earned_at) VALUES (p_user_id, p_badge_id, NOW())
        ON CONFLICT (user_id, badge_id) DO NOTHING;
        RETURN TRUE;
    END IF;
    RETURN FALSE;
END;
$;

GRANT EXECUTE ON FUNCTION check_and_award_achievement_badge(UUID, TEXT) TO authenticated;

-- ----------------------------------------------------------------------------
-- Function: Get user statistics
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_statistics(p_user_id UUID)
RETURNS TABLE (total_books BIGINT, favorite_books BIGINT, total_chapters BIGINT,
               read_chapters BIGINT, bookmarked_chapters BIGINT, books_in_progress BIGINT) AS $
BEGIN
    RETURN QUERY
    SELECT COUNT(DISTINCT sb.book_id), 0::BIGINT, 0::BIGINT, 0::BIGINT, 0::BIGINT, COUNT(DISTINCT rp.book_id)
    FROM public.users u
    LEFT JOIN public.synced_books sb ON u.id = sb.user_id
    LEFT JOIN public.reading_progress rp ON u.id = rp.user_id
    WHERE u.id = p_user_id GROUP BY u.id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Get popular books
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_popular_books(p_limit INTEGER DEFAULT 50)
RETURNS TABLE (book_id TEXT, title TEXT, book_url TEXT, source_id BIGINT, reader_count INTEGER, last_read BIGINT) AS $
BEGIN
    RETURN QUERY
    SELECT sb.book_id, sb.title, sb.book_url, sb.source_id,
           COUNT(DISTINCT sb.user_id)::INTEGER as reader_count, MAX(sb.last_read) as last_read
    FROM public.synced_books sb
    GROUP BY sb.book_id, sb.title, sb.book_url, sb.source_id
    ORDER BY reader_count DESC, last_read DESC LIMIT p_limit;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Get user leaderboard rank
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_leaderboard_rank(p_user_id UUID)
RETURNS TABLE (rank BIGINT, total_users BIGINT, percentile NUMERIC) AS $
BEGIN
    RETURN QUERY
    WITH user_rank AS (
        SELECT ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as user_rank, l.user_id
        FROM public.leaderboard l
    ),
    total_count AS (SELECT COUNT(*) as total FROM public.leaderboard)
    SELECT ur.user_rank, tc.total, ROUND((ur.user_rank::NUMERIC / tc.total::NUMERIC) * 100, 2)
    FROM user_rank ur, total_count tc WHERE ur.user_id = p_user_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Get top leaderboard users
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_top_leaderboard_users(p_limit INTEGER DEFAULT 10)
RETURNS TABLE (rank BIGINT, user_id UUID, username TEXT, total_reading_time_minutes BIGINT,
               total_chapters_read INTEGER, books_completed INTEGER, reading_streak INTEGER,
               has_badge BOOLEAN, badge_type TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT ROW_NUMBER() OVER (ORDER BY l.total_reading_time_minutes DESC), l.user_id, l.username,
           l.total_reading_time_minutes, l.total_chapters_read, l.books_completed, l.reading_streak,
           l.has_badge, l.badge_type
    FROM public.leaderboard l ORDER BY l.total_reading_time_minutes DESC LIMIT p_limit;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Get donation leaderboard
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_donation_leaderboard(p_limit INTEGER DEFAULT 100, p_offset INTEGER DEFAULT 0)
RETURNS TABLE (user_id TEXT, username TEXT, total_donation_amount DECIMAL, badge_count INTEGER,
               highest_badge_rarity TEXT, avatar_url TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT u.id::TEXT, COALESCE(u.username, split_part(u.email, '@', 1)),
           COALESCE(SUM(b.price), 0), COUNT(ub.badge_id)::INTEGER,
           (SELECT b2.rarity FROM public.user_badges ub2 JOIN public.badges b2 ON ub2.badge_id = b2.id
            WHERE ub2.user_id = u.id AND b2.type = 'PURCHASABLE' AND b2.price IS NOT NULL
            ORDER BY CASE b2.rarity WHEN 'legendary' THEN 4 WHEN 'epic' THEN 3 WHEN 'rare' THEN 2 WHEN 'common' THEN 1 ELSE 0 END DESC LIMIT 1),
           NULL::TEXT
    FROM public.users u
    LEFT JOIN public.user_badges ub ON u.id = ub.user_id
    LEFT JOIN public.badges b ON ub.badge_id = b.id AND b.type = 'PURCHASABLE' AND b.price IS NOT NULL
    GROUP BY u.id, u.username, u.email
    HAVING COALESCE(SUM(b.price), 0) > 0
    ORDER BY total_donation_amount DESC LIMIT p_limit OFFSET p_offset;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_donation_leaderboard(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_donation_leaderboard(INTEGER, INTEGER) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Get user donation rank
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_donation_rank(p_user_id TEXT)
RETURNS TABLE (user_id TEXT, username TEXT, total_donation_amount DECIMAL, badge_count INTEGER,
               highest_badge_rarity TEXT, avatar_url TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT u.id::TEXT, COALESCE(u.username, split_part(u.email, '@', 1)),
           COALESCE(SUM(b.price), 0), COUNT(ub.badge_id)::INTEGER,
           (SELECT b2.rarity FROM public.user_badges ub2 JOIN public.badges b2 ON ub2.badge_id = b2.id
            WHERE ub2.user_id = u.id AND b2.type = 'PURCHASABLE' AND b2.price IS NOT NULL
            ORDER BY CASE b2.rarity WHEN 'legendary' THEN 4 WHEN 'epic' THEN 3 WHEN 'rare' THEN 2 WHEN 'common' THEN 1 ELSE 0 END DESC LIMIT 1),
           NULL::TEXT
    FROM public.users u
    LEFT JOIN public.user_badges ub ON u.id = ub.user_id
    LEFT JOIN public.badges b ON ub.badge_id = b.id AND b.type = 'PURCHASABLE' AND b.price IS NOT NULL
    WHERE u.id::TEXT = p_user_id
    GROUP BY u.id, u.username, u.email;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_user_donation_rank(TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_donation_rank(TEXT) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Get user donation badges
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_donation_badges(p_user_id TEXT)
RETURNS TABLE (badge_id TEXT, badge_name TEXT, badge_icon TEXT, badge_rarity TEXT,
               badge_price DECIMAL, earned_at TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT b.id, b.name, b.icon, b.rarity, b.price, ub.earned_at::TEXT
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id::TEXT = p_user_id AND b.type = 'PURCHASABLE' AND b.price IS NOT NULL
    ORDER BY b.price DESC;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_user_donation_badges(TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_donation_badges(TEXT) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Get daily quote
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_daily_quote()
RETURNS TABLE (quote_id UUID, quote_text TEXT, book_title TEXT, author TEXT,
               chapter_title TEXT, likes_count INTEGER, submitter_username TEXT, submitter_id UUID) AS $
DECLARE
    today DATE := CURRENT_DATE;
    selected_quote_id UUID;
BEGIN
    SELECT dqh.quote_id INTO selected_quote_id FROM public.daily_quote_history dqh WHERE dqh.shown_date = today;
    
    IF selected_quote_id IS NULL THEN
        SELECT cq.id INTO selected_quote_id FROM public.community_quotes cq
        WHERE cq.status = 'APPROVED' ORDER BY RANDOM() LIMIT 1;
        
        IF selected_quote_id IS NOT NULL THEN
            INSERT INTO public.daily_quote_history (quote_id, shown_date)
            VALUES (selected_quote_id, today) ON CONFLICT (shown_date) DO NOTHING;
        END IF;
    END IF;
    
    RETURN QUERY
    SELECT cq.id, cq.quote_text, cq.book_title, cq.author, cq.chapter_title, cq.likes_count, u.username, cq.user_id
    FROM public.community_quotes cq LEFT JOIN public.users u ON cq.user_id = u.id
    WHERE cq.id = selected_quote_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Toggle quote like
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION toggle_quote_like(p_quote_id UUID)
RETURNS BOOLEAN AS $
DECLARE v_user_id UUID := auth.uid(); v_liked BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM public.quote_likes WHERE user_id = v_user_id AND quote_id = p_quote_id) INTO v_liked;
    
    IF v_liked THEN
        DELETE FROM public.quote_likes WHERE user_id = v_user_id AND quote_id = p_quote_id;
        UPDATE public.community_quotes SET likes_count = likes_count - 1 WHERE id = p_quote_id;
        RETURN FALSE;
    ELSE
        INSERT INTO public.quote_likes (user_id, quote_id) VALUES (v_user_id, p_quote_id);
        UPDATE public.community_quotes SET likes_count = likes_count + 1 WHERE id = p_quote_id;
        RETURN TRUE;
    END IF;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Is user admin
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION is_user_admin(p_user_id UUID)
RETURNS BOOLEAN AS $
DECLARE v_is_admin BOOLEAN := FALSE;
BEGIN
    SELECT COALESCE(is_admin, FALSE) INTO v_is_admin FROM public.users WHERE id = p_user_id;
    RETURN COALESCE(v_is_admin, FALSE);
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION is_user_admin(UUID) TO authenticated;

-- ----------------------------------------------------------------------------
-- Function: Approve quote (admin only)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION approve_quote(p_quote_id UUID, p_featured BOOLEAN DEFAULT FALSE)
RETURNS BOOLEAN AS $
DECLARE v_admin_id UUID := auth.uid();
BEGIN
    IF NOT is_user_admin(v_admin_id) THEN RAISE EXCEPTION 'Only admins can approve quotes'; END IF;
    UPDATE public.community_quotes SET status = 'APPROVED', reviewed_at = NOW(), reviewed_by = v_admin_id, featured = p_featured
    WHERE id = p_quote_id;
    RETURN FOUND;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Reject quote (admin only)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION reject_quote(p_quote_id UUID)
RETURNS BOOLEAN AS $
DECLARE v_admin_id UUID := auth.uid();
BEGIN
    IF NOT is_user_admin(v_admin_id) THEN RAISE EXCEPTION 'Only admins can reject quotes'; END IF;
    UPDATE public.community_quotes SET status = 'REJECTED', reviewed_at = NOW(), reviewed_by = v_admin_id WHERE id = p_quote_id;
    RETURN FOUND;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Rate chapter
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION rate_chapter(p_chapter_id UUID, p_rating INTEGER)
RETURNS BOOLEAN AS $
DECLARE v_user_id UUID; v_existing_rating INTEGER; v_new_avg REAL; v_new_count INTEGER;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN RETURN FALSE; END IF;
    
    SELECT rating INTO v_existing_rating FROM public.chapter_ratings WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    
    IF v_existing_rating IS NOT NULL THEN
        UPDATE public.chapter_ratings SET rating = p_rating WHERE chapter_id = p_chapter_id AND user_id = v_user_id;
    ELSE
        INSERT INTO public.chapter_ratings (chapter_id, user_id, rating, created_at)
        VALUES (p_chapter_id, v_user_id, p_rating, EXTRACT(EPOCH FROM NOW()) * 1000);
    END IF;
    
    SELECT AVG(rating)::REAL, COUNT(*)::INTEGER INTO v_new_avg, v_new_count FROM public.chapter_ratings WHERE chapter_id = p_chapter_id;
    UPDATE public.community_chapters SET rating = v_new_avg, rating_count = v_new_count WHERE id = p_chapter_id;
    RETURN TRUE;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION rate_chapter(UUID, INTEGER) TO authenticated;

-- ----------------------------------------------------------------------------
-- Function: Increment book view
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION increment_book_view(p_book_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.community_books SET view_count = view_count + 1 WHERE id = p_book_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION increment_chapter_view(p_chapter_id UUID)
RETURNS VOID AS $
BEGIN
    UPDATE public.community_chapters SET view_count = view_count + 1 WHERE id = p_chapter_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION increment_book_view(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION increment_chapter_view(UUID) TO anon, authenticated;

-- ----------------------------------------------------------------------------
-- Function: Update book metadata (trigger)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_book_metadata()
RETURNS TRIGGER AS $
DECLARE v_chapter_count INTEGER; v_languages TEXT[];
BEGIN
    SELECT COUNT(*) INTO v_chapter_count FROM public.community_chapters WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    SELECT ARRAY_AGG(DISTINCT language) INTO v_languages FROM public.community_chapters WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    UPDATE public.community_books SET chapter_count = v_chapter_count, available_languages = COALESCE(v_languages, '{}'),
           last_updated = EXTRACT(EPOCH FROM NOW()) * 1000 WHERE id = COALESCE(NEW.book_id, OLD.book_id);
    RETURN COALESCE(NEW, OLD);
END;
$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Function: Update glossary updated_at
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_glossary_updated_at()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Function: Increment/decrement art likes
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Function: Auto-approve old pending art
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Admin Functions
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION admin_get_all_users(p_limit INT DEFAULT 50, p_offset INT DEFAULT 0, p_search TEXT DEFAULT NULL)
RETURNS TABLE (id UUID, email TEXT, username TEXT, created_at TIMESTAMPTZ, is_admin BOOLEAN, is_supporter BOOLEAN)
LANGUAGE plpgsql SECURITY DEFINER AS $
BEGIN
    IF NOT is_user_admin(auth.uid()) THEN RAISE EXCEPTION 'Unauthorized: Admin access required'; END IF;
    RETURN QUERY
    SELECT u.id, u.email, u.username, u.created_at, COALESCE(u.is_admin, FALSE), COALESCE(u.is_supporter, FALSE)
    FROM public.users u
    WHERE (p_search IS NULL OR u.email ILIKE '%' || p_search || '%' OR u.username ILIKE '%' || p_search || '%')
    ORDER BY u.created_at DESC LIMIT p_limit OFFSET p_offset;
END;
$;

CREATE OR REPLACE FUNCTION admin_get_user_by_id(p_user_id UUID)
RETURNS TABLE (id UUID, email TEXT, username TEXT, created_at TIMESTAMPTZ, is_admin BOOLEAN, is_supporter BOOLEAN)
LANGUAGE plpgsql SECURITY DEFINER AS $
BEGIN
    IF NOT is_user_admin(auth.uid()) THEN RAISE EXCEPTION 'Unauthorized: Admin access required'; END IF;
    RETURN QUERY
    SELECT u.id, u.email, u.username, u.created_at, COALESCE(u.is_admin, FALSE), COALESCE(u.is_supporter, FALSE)
    FROM public.users u WHERE u.id = p_user_id;
END;
$;

CREATE OR REPLACE FUNCTION admin_assign_badge_to_user(p_user_id UUID, p_badge_id TEXT)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $
BEGIN
    IF NOT is_user_admin(auth.uid()) THEN RAISE EXCEPTION 'Unauthorized: Admin access required'; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.badges WHERE id = p_badge_id) THEN RAISE EXCEPTION 'Badge not found'; END IF;
    IF EXISTS (SELECT 1 FROM public.user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id) THEN RAISE EXCEPTION 'User already has this badge'; END IF;
    INSERT INTO public.user_badges (user_id, badge_id, earned_at) VALUES (p_user_id, p_badge_id, NOW());
END;
$;

CREATE OR REPLACE FUNCTION admin_remove_badge_from_user(p_user_id UUID, p_badge_id TEXT)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $
BEGIN
    IF NOT is_user_admin(auth.uid()) THEN RAISE EXCEPTION 'Unauthorized: Admin access required'; END IF;
    DELETE FROM public.user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id;
    IF NOT FOUND THEN RAISE EXCEPTION 'User does not have this badge'; END IF;
END;
$;

CREATE OR REPLACE FUNCTION admin_send_password_reset(p_email TEXT)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $
BEGIN
    IF NOT is_user_admin(auth.uid()) THEN RAISE EXCEPTION 'Unauthorized: Admin access required'; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.users WHERE email = p_email) THEN RAISE EXCEPTION 'User not found with this email'; END IF;
END;
$;

GRANT EXECUTE ON FUNCTION admin_get_all_users(INT, INT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_get_user_by_id(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_assign_badge_to_user(UUID, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_remove_badge_from_user(UUID, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_send_password_reset(TEXT) TO authenticated;

-- ----------------------------------------------------------------------------
-- Function: Get book reviews with badges
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_book_reviews_with_badges(p_limit INTEGER DEFAULT 50, p_offset INTEGER DEFAULT 0)
RETURNS TABLE (id UUID, user_id UUID, book_title TEXT, rating INTEGER, review_text TEXT,
               created_at TIMESTAMP WITH TIME ZONE, username TEXT, badge_id TEXT, badge_name TEXT,
               badge_icon TEXT, badge_image_url TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT br.id, br.user_id, br.book_title, br.rating, br.review_text, br.created_at,
           u.username, ub.badge_id, b.name, b.icon, b.image_url
    FROM public.book_reviews br
    LEFT JOIN public.users u ON br.user_id = u.id
    LEFT JOIN public.user_badges ub ON br.user_id = ub.user_id AND ub.is_primary = true
    LEFT JOIN public.badges b ON ub.badge_id = b.id
    ORDER BY br.created_at DESC LIMIT p_limit OFFSET p_offset;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_book_reviews_with_badges(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_book_reviews_with_badges(INTEGER, INTEGER) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Get chapter reviews with badges
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_chapter_reviews_with_badges(p_limit INTEGER DEFAULT 50, p_offset INTEGER DEFAULT 0)
RETURNS TABLE (id UUID, user_id UUID, book_title TEXT, chapter_name TEXT, rating INTEGER,
               review_text TEXT, created_at TIMESTAMP WITH TIME ZONE, username TEXT, badge_id TEXT,
               badge_name TEXT, badge_icon TEXT, badge_image_url TEXT) AS $
BEGIN
    RETURN QUERY
    SELECT cr.id, cr.user_id, cr.book_title, cr.chapter_name, cr.rating, cr.review_text, cr.created_at,
           u.username, ub.badge_id, b.name, b.icon, b.image_url
    FROM public.chapter_reviews cr
    LEFT JOIN public.users u ON cr.user_id = u.id
    LEFT JOIN public.user_badges ub ON cr.user_id = ub.user_id AND ub.is_primary = true
    LEFT JOIN public.badges b ON ub.badge_id = b.id
    ORDER BY cr.created_at DESC LIMIT p_limit OFFSET p_offset;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION get_chapter_reviews_with_badges(INTEGER, INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_chapter_reviews_with_badges(INTEGER, INTEGER) TO anon;

-- ----------------------------------------------------------------------------
-- Function: Update leaderboard updated_at
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_leaderboard_updated_at()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;


-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS update_users_updated_at ON public.users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS generate_username_on_insert ON public.users;
CREATE TRIGGER generate_username_on_insert BEFORE INSERT ON public.users FOR EACH ROW EXECUTE FUNCTION generate_default_username();

DROP TRIGGER IF EXISTS update_reading_progress_updated_at ON public.reading_progress;
CREATE TRIGGER update_reading_progress_updated_at BEFORE UPDATE ON public.reading_progress FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_nft_wallets_updated_at ON public.nft_wallets;
CREATE TRIGGER update_nft_wallets_updated_at BEFORE UPDATE ON public.nft_wallets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_leaderboard_timestamp ON public.leaderboard;
CREATE TRIGGER update_leaderboard_timestamp BEFORE UPDATE ON public.leaderboard FOR EACH ROW EXECUTE FUNCTION update_leaderboard_updated_at();

DROP TRIGGER IF EXISTS trigger_update_book_metadata ON public.community_chapters;
CREATE TRIGGER trigger_update_book_metadata AFTER INSERT OR UPDATE OR DELETE ON public.community_chapters FOR EACH ROW EXECUTE FUNCTION update_book_metadata();

DROP TRIGGER IF EXISTS glossary_entries_updated_at ON public.glossary_entries;
CREATE TRIGGER glossary_entries_updated_at BEFORE UPDATE ON public.glossary_entries FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at();

DROP TRIGGER IF EXISTS community_glossaries_updated_at ON public.community_glossaries;
CREATE TRIGGER community_glossaries_updated_at BEFORE UPDATE ON public.community_glossaries FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at();

DROP TRIGGER IF EXISTS character_art_updated_at ON public.character_art;
CREATE TRIGGER character_art_updated_at BEFORE UPDATE ON public.character_art FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- VIEWS
-- ============================================================================

CREATE OR REPLACE VIEW user_reading_summary AS
SELECT u.id AS user_id, u.email, u.username, u.is_supporter,
       COUNT(DISTINCT sb.book_id) AS total_books, 0 AS favorite_books, 0 AS total_chapters,
       0 AS read_chapters, COUNT(DISTINCT rp.id) AS books_in_progress,
       MAX(rp.updated_at) AS last_reading_activity, u.created_at AS user_since
FROM public.users u
LEFT JOIN public.synced_books sb ON u.id = sb.user_id
LEFT JOIN public.reading_progress rp ON u.id = rp.user_id
GROUP BY u.id, u.email, u.username, u.is_supporter, u.created_at;

CREATE OR REPLACE VIEW recent_activity AS
SELECT 'reading_progress' AS activity_type, rp.user_id, u.email, rp.book_id, NULL AS title, rp.updated_at
FROM public.reading_progress rp JOIN public.users u ON rp.user_id = u.id
UNION ALL
SELECT 'synced_book', sb.user_id, u.email, sb.book_id, sb.title, to_timestamp(sb.last_read / 1000.0)
FROM public.synced_books sb JOIN public.users u ON sb.user_id = u.id
ORDER BY updated_at DESC LIMIT 100;

CREATE OR REPLACE VIEW public.leaderboard_with_rank AS
SELECT id, user_id, username, total_reading_time_minutes, total_chapters_read, books_completed,
       reading_streak, has_badge, badge_type, updated_at, created_at,
       ROW_NUMBER() OVER (ORDER BY total_reading_time_minutes DESC) as rank
FROM public.leaderboard ORDER BY total_reading_time_minutes DESC;

CREATE OR REPLACE VIEW book_reviews_with_badges AS
SELECT br.id, br.user_id, br.book_title, br.rating, br.review_text, br.created_at,
       u.username, ub.badge_id, b.name as badge_name, b.icon as badge_icon, b.image_url as badge_image_url
FROM public.book_reviews br
LEFT JOIN public.users u ON br.user_id = u.id
LEFT JOIN public.user_badges ub ON br.user_id = ub.user_id AND ub.is_primary = true
LEFT JOIN public.badges b ON ub.badge_id = b.id
ORDER BY br.created_at DESC;

CREATE OR REPLACE VIEW chapter_reviews_with_badges AS
SELECT cr.id, cr.user_id, cr.book_title, cr.chapter_name, cr.rating, cr.review_text, cr.created_at,
       u.username, ub.badge_id, b.name as badge_name, b.icon as badge_icon, b.image_url as badge_image_url
FROM public.chapter_reviews cr
LEFT JOIN public.users u ON cr.user_id = u.id
LEFT JOIN public.user_badges ub ON cr.user_id = ub.user_id AND ub.is_primary = true
LEFT JOIN public.badges b ON ub.badge_id = b.id
ORDER BY cr.created_at DESC;

-- ============================================================================
-- GRANTS
-- ============================================================================

GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT ON public.leaderboard_with_rank TO authenticated;
GRANT SELECT ON public.leaderboard_with_rank TO anon;
GRANT SELECT ON book_reviews_with_badges TO authenticated;
GRANT SELECT ON book_reviews_with_badges TO anon;
GRANT SELECT ON chapter_reviews_with_badges TO authenticated;
GRANT SELECT ON chapter_reviews_with_badges TO anon;

GRANT SELECT ON public.community_books TO anon, authenticated;
GRANT SELECT ON public.community_chapters TO anon, authenticated;
GRANT SELECT ON public.chapter_ratings TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON public.community_books TO authenticated;
GRANT INSERT, UPDATE, DELETE ON public.community_chapters TO authenticated;
GRANT INSERT, UPDATE ON public.chapter_ratings TO authenticated;
GRANT INSERT ON public.chapter_reports TO authenticated;

-- Enable realtime for leaderboard
ALTER PUBLICATION supabase_realtime ADD TABLE public.leaderboard;

-- ============================================================================
-- INITIAL BADGE DATA
-- ============================================================================

-- Donor badges (PURCHASABLE)
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
    ('patreon_legendary', 'Legendary Patreon', 'Legendary Patreon supporter', '🏆', 'donor', 'legendary', 'PURCHASABLE', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/legendary-patreon.png', TRUE, 75.00)
ON CONFLICT (id) DO NOTHING;

-- Contributor badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('contributor_translator', 'Translator', 'Contributed translations', '🌐', 'contributor', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/translator-min.png', TRUE),
    ('contributor_developer', 'Developer', 'Contributed code', '💻', 'contributor', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/developer-min.png', TRUE),
    ('contributor_designer', 'Designer', 'Contributed designs', '🎨', 'contributor', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/designer-min.png', TRUE),
    ('contributor_code', 'Code Contributor', 'Contributed code to the project', '🔧', 'contributor', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/code-contributor.png', TRUE)
ON CONFLICT (id) DO NOTHING;

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
    ('first_finish', 'First Finish', 'Finished your first book', '🏁', 'reader', 'common', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/first-finish.png', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Reviewer badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, type, image_url, is_available) VALUES
    ('first_critic', 'First Critic', 'Write your first review', '✍️', 'reviewer', 'common', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/first-critic-min.png', TRUE),
    ('thoughtful_critic', 'Thoughtful Critic', 'Write 10 reviews', '🤔', 'reviewer', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/thoughtful-critic-min.png', TRUE),
    ('master_critic', 'Master Critic', 'Write 50 reviews', '🎭', 'reviewer', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/master-critic-min.png', TRUE),
    ('critic', 'Critic', 'Write reviews regularly', '📝', 'reviewer', 'rare', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/critic.png', TRUE),
    ('expert_reviewer', 'Expert Reviewer', 'Write high-quality detailed reviews', '⭐', 'reviewer', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/expert-reviewer.png', TRUE),
    ('legendary_critic', 'Legendary Critic', 'Achieved legendary status as a reviewer', '🌟', 'reviewer', 'legendary', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/legendary-critic.png', TRUE)
ON CONFLICT (id) DO NOTHING;

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
    ('book_patron', 'Book Patron', 'A true patron of literature', '📜', 'special', 'epic', 'ACHIEVEMENT', 'https://raw.githubusercontent.com/IReaderorg/badge-repo/main/book-patron.png', TRUE)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $
BEGIN
    RAISE NOTICE '✅ IReader Complete Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Tables: users, reading_progress, synced_books, book_reviews, chapter_reviews,';
    RAISE NOTICE '        badges, user_badges, payment_proofs, nft_wallets, leaderboard,';
    RAISE NOTICE '        community_quotes, quote_likes, daily_quote_history,';
    RAISE NOTICE '        community_books, community_chapters, chapter_reports, chapter_ratings,';
    RAISE NOTICE '        glossary_entries, community_glossaries,';
    RAISE NOTICE '        character_art, character_art_likes, character_art_reports';
    RAISE NOTICE '';
    RAISE NOTICE 'Features: Badge system, Leaderboard, Quotes, Community Source, Glossary, Character Art';
    RAISE NOTICE '';
    RAISE NOTICE 'To grant admin: UPDATE public.users SET is_admin = TRUE WHERE email = ''admin@example.com'';';
END $;
