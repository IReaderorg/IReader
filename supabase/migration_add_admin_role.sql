-- Migration: Add admin role to users table
-- This allows specific users to verify badge purchases

-- Add is_admin column to users table
ALTER TABLE public.users 
ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Create index for faster admin lookups
CREATE INDEX IF NOT EXISTS idx_users_is_admin ON public.users(is_admin) WHERE is_admin = TRUE;

-- Add comment
COMMENT ON COLUMN public.users.is_admin IS 'Whether user has admin privileges to verify badge purchases';

-- Grant admin privileges to specific users (replace with actual user IDs)
-- Example: UPDATE public.users SET is_admin = TRUE WHERE email = 'admin@example.com';

-- Success message
DO $$
BEGIN
    RAISE NOTICE 'Admin role migration completed successfully!';
    RAISE NOTICE 'To grant admin privileges to a user, run:';
    RAISE NOTICE 'UPDATE public.users SET is_admin = TRUE WHERE email = ''your-admin-email@example.com'';';
END $$;
