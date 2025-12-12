-- Admin User Management Functions
-- These functions allow admin users to manage other users, assign badges, and reset passwords

-- Function to check if a user is an admin
CREATE OR REPLACE FUNCTION is_user_admin(p_user_id UUID)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_is_admin BOOLEAN;
BEGIN
    SELECT is_admin INTO v_is_admin
    FROM users
    WHERE id = p_user_id;
    
    RETURN COALESCE(v_is_admin, FALSE);
END;
$$;

-- Function to get all users (admin only)
CREATE OR REPLACE FUNCTION admin_get_all_users(
    p_limit INT DEFAULT 50,
    p_offset INT DEFAULT 0,
    p_search TEXT DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    email TEXT,
    username TEXT,
    created_at TIMESTAMPTZ,
    is_admin BOOLEAN,
    is_supporter BOOLEAN
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Verify caller is admin
    IF NOT is_user_admin(auth.uid()) THEN
        RAISE EXCEPTION 'Unauthorized: Admin access required';
    END IF;
    
    RETURN QUERY
    SELECT 
        u.id,
        u.email,
        u.username,
        u.created_at,
        COALESCE(u.is_admin, FALSE) as is_admin,
        COALESCE(u.is_supporter, FALSE) as is_supporter
    FROM users u
    WHERE (p_search IS NULL OR 
           u.email ILIKE '%' || p_search || '%' OR 
           u.username ILIKE '%' || p_search || '%')
    ORDER BY u.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$;

-- Function to get a specific user by ID (admin only)
CREATE OR REPLACE FUNCTION admin_get_user_by_id(p_user_id UUID)
RETURNS TABLE (
    id UUID,
    email TEXT,
    username TEXT,
    created_at TIMESTAMPTZ,
    is_admin BOOLEAN,
    is_supporter BOOLEAN
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Verify caller is admin
    IF NOT is_user_admin(auth.uid()) THEN
        RAISE EXCEPTION 'Unauthorized: Admin access required';
    END IF;
    
    RETURN QUERY
    SELECT 
        u.id,
        u.email,
        u.username,
        u.created_at,
        COALESCE(u.is_admin, FALSE) as is_admin,
        COALESCE(u.is_supporter, FALSE) as is_supporter
    FROM users u
    WHERE u.id = p_user_id;
END;
$$;

-- Function to assign a badge to a user (admin only)
CREATE OR REPLACE FUNCTION admin_assign_badge_to_user(
    p_user_id UUID,
    p_badge_id TEXT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Verify caller is admin
    IF NOT is_user_admin(auth.uid()) THEN
        RAISE EXCEPTION 'Unauthorized: Admin access required';
    END IF;
    
    -- Check if badge exists
    IF NOT EXISTS (SELECT 1 FROM badges WHERE id = p_badge_id) THEN
        RAISE EXCEPTION 'Badge not found';
    END IF;
    
    -- Check if user already has the badge
    IF EXISTS (SELECT 1 FROM user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id) THEN
        RAISE EXCEPTION 'User already has this badge';
    END IF;
    
    -- Assign the badge
    INSERT INTO user_badges (user_id, badge_id, earned_at)
    VALUES (p_user_id, p_badge_id, NOW());
END;
$$;

-- Function to remove a badge from a user (admin only)
CREATE OR REPLACE FUNCTION admin_remove_badge_from_user(
    p_user_id UUID,
    p_badge_id TEXT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Verify caller is admin
    IF NOT is_user_admin(auth.uid()) THEN
        RAISE EXCEPTION 'Unauthorized: Admin access required';
    END IF;
    
    -- Remove the badge
    DELETE FROM user_badges
    WHERE user_id = p_user_id AND badge_id = p_badge_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'User does not have this badge';
    END IF;
END;
$$;

-- Function to send password reset email (admin only)
-- Note: This function triggers Supabase Auth's password reset flow
CREATE OR REPLACE FUNCTION admin_send_password_reset(p_email TEXT)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Verify caller is admin
    IF NOT is_user_admin(auth.uid()) THEN
        RAISE EXCEPTION 'Unauthorized: Admin access required';
    END IF;
    
    -- Check if user exists
    IF NOT EXISTS (SELECT 1 FROM users WHERE email = p_email) THEN
        RAISE EXCEPTION 'User not found with this email';
    END IF;
    
    -- Note: The actual password reset email is sent via Supabase Auth API
    -- This function just validates the request
    -- The client should call supabase.auth.resetPasswordForEmail() after this succeeds
END;
$$;

-- Grant execute permissions
GRANT EXECUTE ON FUNCTION is_user_admin(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_get_all_users(INT, INT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_get_user_by_id(UUID) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_assign_badge_to_user(UUID, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_remove_badge_from_user(UUID, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION admin_send_password_reset(TEXT) TO authenticated;

-- Add is_admin column to users table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'is_admin'
    ) THEN
        ALTER TABLE users ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- Add is_supporter column to users table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'is_supporter'
    ) THEN
        ALTER TABLE users ADD COLUMN is_supporter BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- Create index for faster admin queries
CREATE INDEX IF NOT EXISTS idx_users_is_admin ON users(is_admin) WHERE is_admin = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_email_btree ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username_btree ON users(username);
