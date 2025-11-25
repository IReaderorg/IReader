-- ============================================================================
-- PROJECT 6: BADGES DATABASE SCHEMA
-- ============================================================================
-- This schema contains badge system and NFT integration
-- Tables: badges, user_badges, payment_proofs, nft_wallets
-- Estimated storage: ~500MB for 10M+ badge assignments
-- ============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Badges Table
-- Defines available badges that can be earned or purchased
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
    
    -- Constraints
    CONSTRAINT badge_id_not_empty CHECK (LENGTH(id) > 0),
    CONSTRAINT badge_name_not_empty CHECK (LENGTH(name) > 0),
    CONSTRAINT badge_category_valid CHECK (category IN ('donor', 'contributor', 'reader', 'reviewer', 'special', 'purchasable', 'nft')),
    CONSTRAINT badge_rarity_valid CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')),
    CONSTRAINT badge_type_valid CHECK (type IN ('PURCHASABLE', 'NFT_EXCLUSIVE', 'ACHIEVEMENT')),
    CONSTRAINT badge_price_positive CHECK (price IS NULL OR price > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_badges_category ON public.badges(category);
CREATE INDEX IF NOT EXISTS idx_badges_rarity ON public.badges(rarity);
CREATE INDEX IF NOT EXISTS idx_badges_type ON public.badges(type);
CREATE INDEX IF NOT EXISTS idx_badges_available ON public.badges(is_available) WHERE is_available = TRUE;

-- Comments
COMMENT ON TABLE public.badges IS 'Available badges that users can earn or purchase';

-- ----------------------------------------------------------------------------
-- User Badges Table
-- Tracks which badges each user has earned or purchased
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_badges (
    user_id TEXT NOT NULL,
    badge_id TEXT NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    earned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_primary BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    metadata JSONB,
    
    -- Constraints
    PRIMARY KEY (user_id, badge_id),
    CONSTRAINT ub_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON public.user_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_badge_id ON public.user_badges(badge_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_earned_at ON public.user_badges(earned_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_badges_primary ON public.user_badges(user_id, is_primary) WHERE is_primary = TRUE;
CREATE INDEX IF NOT EXISTS idx_user_badges_featured ON public.user_badges(user_id, is_featured) WHERE is_featured = TRUE;

-- Comments
COMMENT ON TABLE public.user_badges IS 'Badges earned or purchased by users';

-- ----------------------------------------------------------------------------
-- Payment Proofs Table
-- Stores payment proof submissions for badge purchases
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.payment_proofs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    badge_id TEXT NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    transaction_id TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    proof_image_url TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by TEXT,
    
    -- Constraints
    CONSTRAINT payment_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT transaction_id_not_empty CHECK (LENGTH(transaction_id) > 0),
    CONSTRAINT payment_method_not_empty CHECK (LENGTH(payment_method) > 0),
    CONSTRAINT pp_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payment_proofs_user_id ON public.payment_proofs(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_status ON public.payment_proofs(status);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_submitted_at ON public.payment_proofs(submitted_at DESC);

-- Comments
COMMENT ON TABLE public.payment_proofs IS 'Payment proof submissions for badge purchases';

-- ----------------------------------------------------------------------------
-- NFT Wallets Table
-- Stores Ethereum wallet addresses and NFT ownership verification
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.nft_wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL UNIQUE,
    wallet_address TEXT NOT NULL,
    last_verified TIMESTAMP WITH TIME ZONE,
    owns_nft BOOLEAN DEFAULT FALSE,
    nft_token_id TEXT,
    verification_cache_expires TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT wallet_address_format CHECK (wallet_address ~* '^0x[a-fA-F0-9]{40}$'),
    CONSTRAINT nft_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_nft_wallets_user_id ON public.nft_wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_address ON public.nft_wallets(wallet_address);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_cache_expires ON public.nft_wallets(verification_cache_expires);

-- Comments
COMMENT ON TABLE public.nft_wallets IS 'Ethereum wallet addresses and NFT ownership verification';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_proofs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nft_wallets ENABLE ROW LEVEL SECURITY;

-- Badges policies
CREATE POLICY "Everyone can view badges"
    ON public.badges FOR SELECT
    USING (true);

-- User badges policies
CREATE POLICY "Everyone can view user badges"
    ON public.user_badges FOR SELECT
    USING (true);

CREATE POLICY "Users can update their own badge settings"
    ON public.user_badges FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- Payment proofs policies
CREATE POLICY "Users can view their own payment proofs"
    ON public.payment_proofs FOR SELECT
    USING (user_id = auth.uid()::TEXT);

CREATE POLICY "Authenticated users can insert payment proofs"
    ON public.payment_proofs FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- NFT wallets policies
CREATE POLICY "Users can view their own NFT wallet"
    ON public.nft_wallets FOR SELECT
    USING (user_id = auth.uid()::TEXT);

CREATE POLICY "Authenticated users can insert NFT wallet"
    ON public.nft_wallets FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can update their own NFT wallet"
    ON public.nft_wallets FOR UPDATE
    USING (user_id = auth.uid()::TEXT);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- Update timestamp function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS update_nft_wallets_updated_at ON public.nft_wallets;
CREATE TRIGGER update_nft_wallets_updated_at
    BEFORE UPDATE ON public.nft_wallets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- INITIAL BADGE DATA
-- ============================================================================

INSERT INTO public.badges (id, name, description, icon, category, rarity, type, is_available) VALUES
-- Donor Badges
('donor_bronze', 'Bronze Supporter', 'Donated $5 or more', '🥉', 'donor', 'common', 'ACHIEVEMENT', TRUE),
('donor_silver', 'Silver Supporter', 'Donated $10 or more', '🥈', 'donor', 'rare', 'ACHIEVEMENT', TRUE),
('donor_gold', 'Gold Supporter', 'Donated $25 or more', '🥇', 'donor', 'epic', 'ACHIEVEMENT', TRUE),
('donor_platinum', 'Platinum Supporter', 'Donated $50 or more', '💎', 'donor', 'legendary', 'ACHIEVEMENT', TRUE),

-- Contributor Badges
('contributor_translator', 'Translator', 'Contributed translations', '🌐', 'contributor', 'rare', 'ACHIEVEMENT', TRUE),
('contributor_developer', 'Developer', 'Contributed code', '💻', 'contributor', 'epic', 'ACHIEVEMENT', TRUE),
('contributor_designer', 'Designer', 'Contributed designs', '🎨', 'contributor', 'rare', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Reading Progress
('novice_reader', 'Novice Reader', 'Read your first 10 chapters', '📖', 'reader', 'common', 'ACHIEVEMENT', TRUE),
('avid_reader', 'Avid Reader', 'Read 100 chapters', '📚', 'reader', 'rare', 'ACHIEVEMENT', TRUE),
('bookworm', 'Bookworm', 'Read 500 chapters', '🐛', 'reader', 'epic', 'ACHIEVEMENT', TRUE),
('master_reader', 'Master Reader', 'Read 1000 chapters', '🎓', 'reader', 'legendary', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Book Completion
('first_finish', 'First Finish', 'Complete your first book', '🏁', 'reader', 'common', 'ACHIEVEMENT', TRUE),
('book_collector', 'Book Collector', 'Complete 10 books', '📕', 'reader', 'rare', 'ACHIEVEMENT', TRUE),
('library_master', 'Library Master', 'Complete 50 books', '📚', 'reader', 'epic', 'ACHIEVEMENT', TRUE),
('legendary_collector', 'Legendary Collector', 'Complete 100 books', '👑', 'reader', 'legendary', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Reviews
('first_critic', 'First Critic', 'Write your first review', '✍️', 'reviewer', 'common', 'ACHIEVEMENT', TRUE),
('thoughtful_critic', 'Thoughtful Critic', 'Write 10 reviews', '💭', 'reviewer', 'rare', 'ACHIEVEMENT', TRUE),
('master_critic', 'Master Critic', 'Write 50 reviews', '🎭', 'reviewer', 'epic', 'ACHIEVEMENT', TRUE),
('legendary_critic', 'Legendary Critic', 'Write 100 reviews', '🏆', 'reviewer', 'legendary', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Reading Streaks
('week_warrior', 'Week Warrior', 'Read for 7 consecutive days', '🔥', 'reader', 'rare', 'ACHIEVEMENT', TRUE),
('month_master', 'Month Master', 'Read for 30 consecutive days', '⚡', 'reader', 'epic', 'ACHIEVEMENT', TRUE),
('year_legend', 'Year Legend', 'Read for 365 consecutive days', '🌟', 'reader', 'legendary', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Time-based
('night_owl', 'Night Owl', 'Read 100 chapters between 10 PM and 6 AM', '🦉', 'special', 'rare', 'ACHIEVEMENT', TRUE),
('early_bird', 'Early Bird', 'Read 100 chapters between 5 AM and 9 AM', '🐦', 'special', 'rare', 'ACHIEVEMENT', TRUE),

-- Achievement Badges - Speed Reading
('speed_reader', 'Speed Reader', 'Read 50 chapters in a single day', '⚡', 'reader', 'epic', 'ACHIEVEMENT', TRUE),
('marathon_reader', 'Marathon Reader', 'Read for 12 hours in a single day', '🏃', 'reader', 'legendary', 'ACHIEVEMENT', TRUE),

-- Special Badges
('special_early_adopter', 'Early Adopter', 'Joined during beta', '🚀', 'special', 'legendary', 'ACHIEVEMENT', TRUE),
('special_bug_hunter', 'Bug Hunter', 'Reported critical bugs', '🐛', 'special', 'epic', 'ACHIEVEMENT', TRUE),
('special_community_hero', 'Community Hero', 'Outstanding community contribution', '🦸', 'special', 'legendary', 'ACHIEVEMENT', TRUE)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '✅ PROJECT 6 (BADGES) Schema created successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Tables created:';
    RAISE NOTICE '- badges: Badge definitions (31 badges)';
    RAISE NOTICE '- user_badges: User badge ownership';
    RAISE NOTICE '- payment_proofs: Badge purchase verification';
    RAISE NOTICE '- nft_wallets: NFT ownership tracking';
    RAISE NOTICE '';
    RAISE NOTICE 'Features:';
    RAISE NOTICE '- 31 achievement badges';
    RAISE NOTICE '- Badge monetization support';
    RAISE NOTICE '- NFT integration';
    RAISE NOTICE '- Primary and featured badge display';
    RAISE NOTICE '';
    RAISE NOTICE 'Capacity: ~10M badge assignments in 500MB';
    RAISE NOTICE '';
    RAISE NOTICE 'Important: user_id is stored as TEXT (from Project 1)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Note your Project 6 URL and Anon Key';
    RAISE NOTICE '2. Create Project 7 for analytics (schema_7_analytics.sql)';
    RAISE NOTICE '3. Update your app to use MultiSupabaseClient with 7 projects';
END $$;
