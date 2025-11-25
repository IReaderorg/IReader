-- ============================================================================
-- Migration: Add Badge Monetization and NFT Integration
-- ============================================================================
-- This migration adds support for:
-- - Purchasable badges with payment proof tracking
-- - NFT-exclusive badges with wallet verification
-- - Badge display preferences (primary for reviews, featured for profile)
--
-- Run this migration if you already have the base schema installed.
-- For fresh installations, use schema.sql instead.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Step 1: Add new columns to badges table
-- ----------------------------------------------------------------------------

-- Add price column for purchasable badges
ALTER TABLE public.badges 
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2);

-- Add type column to distinguish badge types
ALTER TABLE public.badges 
ADD COLUMN IF NOT EXISTS type TEXT NOT NULL DEFAULT 'ACHIEVEMENT';

-- Add image_url column for badge images
ALTER TABLE public.badges 
ADD COLUMN IF NOT EXISTS image_url TEXT;

-- Add is_available column to control badge availability
ALTER TABLE public.badges 
ADD COLUMN IF NOT EXISTS is_available BOOLEAN DEFAULT TRUE;

-- Add constraints
DO $$
BEGIN
    -- Add type constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'badge_type_valid'
    ) THEN
        ALTER TABLE public.badges 
        ADD CONSTRAINT badge_type_valid 
        CHECK (type IN ('PURCHASABLE', 'NFT_EXCLUSIVE', 'ACHIEVEMENT'));
    END IF;

    -- Add price constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'badge_price_positive'
    ) THEN
        ALTER TABLE public.badges 
        ADD CONSTRAINT badge_price_positive 
        CHECK (price IS NULL OR price > 0);
    END IF;
END $$;

-- Update category constraint to include new categories
ALTER TABLE public.badges 
DROP CONSTRAINT IF EXISTS badge_category_valid;

ALTER TABLE public.badges 
ADD CONSTRAINT badge_category_valid 
CHECK (category IN ('donor', 'contributor', 'reader', 'reviewer', 'special', 'purchasable', 'nft'));

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_badges_type ON public.badges(type);
CREATE INDEX IF NOT EXISTS idx_badges_available ON public.badges(is_available) WHERE is_available = TRUE;

-- Add comments
COMMENT ON COLUMN public.badges.price IS 'Price in USD for purchasable badges (NULL for non-purchasable)';
COMMENT ON COLUMN public.badges.type IS 'Badge type (PURCHASABLE, NFT_EXCLUSIVE, ACHIEVEMENT)';
COMMENT ON COLUMN public.badges.image_url IS 'URL to badge image (optional, falls back to icon)';
COMMENT ON COLUMN public.badges.is_available IS 'Whether badge is currently available for purchase/earning';

-- ----------------------------------------------------------------------------
-- Step 2: Add new columns to user_badges table
-- ----------------------------------------------------------------------------

-- Add is_primary column for review display
ALTER TABLE public.user_badges 
ADD COLUMN IF NOT EXISTS is_primary BOOLEAN DEFAULT FALSE;

-- Add is_featured column for profile display
ALTER TABLE public.user_badges 
ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_user_badges_primary 
ON public.user_badges(user_id, is_primary) 
WHERE is_primary = TRUE;

CREATE INDEX IF NOT EXISTS idx_user_badges_featured 
ON public.user_badges(user_id, is_featured) 
WHERE is_featured = TRUE;

-- Add comments
COMMENT ON COLUMN public.user_badges.is_primary IS 'Whether this badge is displayed on user reviews (only one can be primary)';
COMMENT ON COLUMN public.user_badges.is_featured IS 'Whether this badge is featured on user profile (max 3 can be featured)';

-- ----------------------------------------------------------------------------
-- Step 3: Create payment_proofs table
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
    
    -- Constraints
    CONSTRAINT payment_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT transaction_id_not_empty CHECK (LENGTH(transaction_id) > 0),
    CONSTRAINT payment_method_not_empty CHECK (LENGTH(payment_method) > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payment_proofs_user_id ON public.payment_proofs(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_status ON public.payment_proofs(status);
CREATE INDEX IF NOT EXISTS idx_payment_proofs_submitted_at ON public.payment_proofs(submitted_at DESC);

-- Comments
COMMENT ON TABLE public.payment_proofs IS 'Payment proof submissions for badge purchases';
COMMENT ON COLUMN public.payment_proofs.user_id IS 'User who submitted the payment proof';
COMMENT ON COLUMN public.payment_proofs.badge_id IS 'Badge being purchased';
COMMENT ON COLUMN public.payment_proofs.transaction_id IS 'Transaction ID from payment provider';
COMMENT ON COLUMN public.payment_proofs.payment_method IS 'Payment method used (crypto, direct transfer, etc.)';
COMMENT ON COLUMN public.payment_proofs.proof_image_url IS 'Optional URL to payment proof image';
COMMENT ON COLUMN public.payment_proofs.status IS 'Verification status (PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN public.payment_proofs.reviewed_by IS 'Admin who reviewed the payment proof';

-- ----------------------------------------------------------------------------
-- Step 4: Create nft_wallets table
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
    
    -- Constraints
    CONSTRAINT wallet_address_format CHECK (wallet_address ~* '^0x[a-fA-F0-9]{40}$')
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_nft_wallets_user_id ON public.nft_wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_address ON public.nft_wallets(wallet_address);
CREATE INDEX IF NOT EXISTS idx_nft_wallets_cache_expires ON public.nft_wallets(verification_cache_expires);

-- Comments
COMMENT ON TABLE public.nft_wallets IS 'Ethereum wallet addresses and NFT ownership verification';
COMMENT ON COLUMN public.nft_wallets.user_id IS 'User who owns the wallet';
COMMENT ON COLUMN public.nft_wallets.wallet_address IS 'Ethereum wallet address (0x format)';
COMMENT ON COLUMN public.nft_wallets.last_verified IS 'Last time NFT ownership was verified';
COMMENT ON COLUMN public.nft_wallets.owns_nft IS 'Whether user owns an IReader NFT';
COMMENT ON COLUMN public.nft_wallets.nft_token_id IS 'Token ID of owned NFT (if any)';
COMMENT ON COLUMN public.nft_wallets.verification_cache_expires IS 'When verification cache expires (24h from last_verified)';

-- ----------------------------------------------------------------------------
-- Step 5: Enable RLS on new tables
-- ----------------------------------------------------------------------------

ALTER TABLE public.payment_proofs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nft_wallets ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- Step 6: Create RLS policies for new tables
-- ----------------------------------------------------------------------------

-- Payment Proofs Policies
CREATE POLICY "Users can view their own payment proofs"
    ON public.payment_proofs FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own payment proofs"
    ON public.payment_proofs FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- NFT Wallets Policies
CREATE POLICY "Users can view their own NFT wallet"
    ON public.nft_wallets FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own NFT wallet"
    ON public.nft_wallets FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own NFT wallet"
    ON public.nft_wallets FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Update user_badges policy to allow users to update their own badge settings
DROP POLICY IF EXISTS "Users can update their own badge settings" ON public.user_badges;
CREATE POLICY "Users can update their own badge settings"
    ON public.user_badges FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Step 7: Create triggers
-- ----------------------------------------------------------------------------

-- Trigger for nft_wallets updated_at
DROP TRIGGER IF EXISTS update_nft_wallets_updated_at ON public.nft_wallets;
CREATE TRIGGER update_nft_wallets_updated_at
    BEFORE UPDATE ON public.nft_wallets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Step 8: Create new functions
-- ----------------------------------------------------------------------------

-- Function to get user badges with complete details
CREATE OR REPLACE FUNCTION get_user_badges_with_details(p_user_id UUID)
RETURNS TABLE (
    badge_id TEXT,
    badge_name TEXT,
    badge_description TEXT,
    badge_icon TEXT,
    badge_category TEXT,
    badge_rarity TEXT,
    badge_type TEXT,
    badge_price DECIMAL(10, 2),
    badge_image_url TEXT,
    earned_at TIMESTAMP WITH TIME ZONE,
    is_primary BOOLEAN,
    is_featured BOOLEAN,
    metadata JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.id,
        b.name,
        b.description,
        b.icon,
        b.category,
        b.rarity,
        b.type,
        b.price,
        b.image_url,
        ub.earned_at,
        ub.is_primary,
        ub.is_featured,
        ub.metadata
    FROM public.user_badges ub
    JOIN public.badges b ON ub.badge_id = b.id
    WHERE ub.user_id = p_user_id
    ORDER BY ub.earned_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_user_badges_with_details(UUID) IS 'Returns all badges earned by a user with complete details including primary/featured flags';

-- ============================================================================
-- Migration Complete
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE 'Badge monetization migration completed successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'New tables created:';
    RAISE NOTICE '- payment_proofs: Track badge purchase payments';
    RAISE NOTICE '- nft_wallets: Store wallet addresses and NFT verification';
    RAISE NOTICE '';
    RAISE NOTICE 'Badges table updated:';
    RAISE NOTICE '- Added price, type, image_url, is_available columns';
    RAISE NOTICE '- Updated category constraint to include purchasable and nft';
    RAISE NOTICE '';
    RAISE NOTICE 'User badges table updated:';
    RAISE NOTICE '- Added is_primary and is_featured columns';
    RAISE NOTICE '- Users can now set primary badge for reviews';
    RAISE NOTICE '- Users can feature up to 3 badges on profile';
    RAISE NOTICE '';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Deploy verify-nft-ownership Edge Function';
    RAISE NOTICE '2. Add purchasable badges to badges table';
    RAISE NOTICE '3. Add NFT exclusive badge to badges table';
    RAISE NOTICE '4. Configure Polygon RPC URL in Edge Function environment';
END $$;
