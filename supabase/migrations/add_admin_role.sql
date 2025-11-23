-- Migration: Add is_admin field to users table
-- This allows certain users to access admin features like badge verification

-- Add is_admin column to users table
ALTER TABLE public.users 
ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Add index for faster admin user lookups
CREATE INDEX IF NOT EXISTS idx_users_is_admin ON public.users(is_admin) WHERE is_admin = TRUE;

-- Add comment
COMMENT ON COLUMN public.users.is_admin IS 'Whether user has admin privileges for badge verification';

-- Update RLS policies to allow admins to view payment proofs
DROP POLICY IF EXISTS "Admins can view all payment proofs" ON public.payment_proofs;
CREATE POLICY "Admins can view all payment proofs"
    ON public.payment_proofs FOR SELECT
    USING (
        auth.uid() = user_id OR 
        EXISTS (
            SELECT 1 FROM public.users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Update RLS policies to allow admins to update payment proof status
DROP POLICY IF EXISTS "Admins can update payment proofs" ON public.payment_proofs;
CREATE POLICY "Admins can update payment proofs"
    ON public.payment_proofs FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM public.users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Note: To grant admin access to a user, run:
-- UPDATE public.users SET is_admin = TRUE WHERE email = 'admin@example.com';
