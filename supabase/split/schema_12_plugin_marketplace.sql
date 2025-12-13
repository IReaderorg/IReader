-- Schema 12: Plugin Marketplace
-- Tables for plugin purchases, grants, and developer management

-- Plugin purchases table
CREATE TABLE IF NOT EXISTS plugin_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plugin_id TEXT NOT NULL,
    purchase_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expiry_date TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'PENDING_VERIFICATION', 'TRIAL')),
    payment_method TEXT,
    transaction_id TEXT,
    amount DECIMAL(10, 2),
    currency TEXT DEFAULT 'USD',
    granted_by UUID REFERENCES auth.users(id),
    grant_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Plugin access grants table (for developers to grant free access)
CREATE TABLE IF NOT EXISTS plugin_access_grants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    granted_to_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    granted_to_username TEXT NOT NULL,
    granted_by_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    granted_by_username TEXT NOT NULL,
    grant_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expiry_date TIMESTAMPTZ,
    reason TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Developer plugins table (links developers to their plugins)
CREATE TABLE IF NOT EXISTS developer_plugins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    developer_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plugin_id TEXT NOT NULL UNIQUE,
    plugin_name TEXT NOT NULL,
    plugin_version TEXT NOT NULL,
    description TEXT,
    icon_url TEXT,
    monetization_type TEXT NOT NULL DEFAULT 'FREE' CHECK (monetization_type IN ('FREE', 'PREMIUM', 'FREEMIUM')),
    price DECIMAL(10, 2),
    currency TEXT DEFAULT 'USD',
    max_grants INTEGER NOT NULL DEFAULT 10,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Plugin statistics table
CREATE TABLE IF NOT EXISTS plugin_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL UNIQUE,
    total_downloads INTEGER NOT NULL DEFAULT 0,
    total_purchases INTEGER NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10, 2) NOT NULL DEFAULT 0,
    active_users INTEGER NOT NULL DEFAULT 0,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_plugin_purchases_user ON plugin_purchases(user_id);
CREATE INDEX IF NOT EXISTS idx_plugin_purchases_plugin ON plugin_purchases(plugin_id);
CREATE INDEX IF NOT EXISTS idx_plugin_purchases_status ON plugin_purchases(status);
CREATE INDEX IF NOT EXISTS idx_plugin_grants_plugin ON plugin_access_grants(plugin_id);
CREATE INDEX IF NOT EXISTS idx_plugin_grants_user ON plugin_access_grants(granted_to_user_id);
CREATE INDEX IF NOT EXISTS idx_plugin_grants_developer ON plugin_access_grants(granted_by_user_id);
CREATE INDEX IF NOT EXISTS idx_developer_plugins_developer ON developer_plugins(developer_id);

-- RLS Policies
ALTER TABLE plugin_purchases ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_access_grants ENABLE ROW LEVEL SECURITY;
ALTER TABLE developer_plugins ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_statistics ENABLE ROW LEVEL SECURITY;

-- Users can view their own purchases
CREATE POLICY "Users can view own purchases" ON plugin_purchases
    FOR SELECT USING (auth.uid() = user_id);

-- Users can view grants they received
CREATE POLICY "Users can view received grants" ON plugin_access_grants
    FOR SELECT USING (auth.uid() = granted_to_user_id);

-- Developers can view and manage grants they created
CREATE POLICY "Developers can manage own grants" ON plugin_access_grants
    FOR ALL USING (auth.uid() = granted_by_user_id);

-- Developers can view their own plugins
CREATE POLICY "Developers can view own plugins" ON developer_plugins
    FOR SELECT USING (auth.uid() = developer_id);

-- Developers can update their own plugins
CREATE POLICY "Developers can update own plugins" ON developer_plugins
    FOR UPDATE USING (auth.uid() = developer_id);

-- Anyone can view plugin statistics
CREATE POLICY "Anyone can view plugin stats" ON plugin_statistics
    FOR SELECT USING (true);

-- Function to check if user has access to a plugin
CREATE OR REPLACE FUNCTION has_plugin_access(p_user_id UUID, p_plugin_id TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    -- Check active purchase
    IF EXISTS (
        SELECT 1 FROM plugin_purchases 
        WHERE user_id = p_user_id 
        AND plugin_id = p_plugin_id 
        AND status = 'ACTIVE'
        AND (expiry_date IS NULL OR expiry_date > NOW())
    ) THEN
        RETURN TRUE;
    END IF;
    
    -- Check active grant
    IF EXISTS (
        SELECT 1 FROM plugin_access_grants 
        WHERE granted_to_user_id = p_user_id 
        AND plugin_id = p_plugin_id 
        AND is_active = TRUE
        AND (expiry_date IS NULL OR expiry_date > NOW())
    ) THEN
        RETURN TRUE;
    END IF;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get remaining grants for a plugin
CREATE OR REPLACE FUNCTION get_remaining_grants(p_plugin_id TEXT)
RETURNS INTEGER AS $$
DECLARE
    max_grants INTEGER;
    used_grants INTEGER;
BEGIN
    SELECT COALESCE(dp.max_grants, 10) INTO max_grants
    FROM developer_plugins dp
    WHERE dp.plugin_id = p_plugin_id;
    
    IF max_grants IS NULL THEN
        max_grants := 10;
    END IF;
    
    SELECT COUNT(*) INTO used_grants
    FROM plugin_access_grants
    WHERE plugin_id = p_plugin_id AND is_active = TRUE;
    
    RETURN max_grants - used_grants;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- SECURITY: License Validation Functions
-- These functions ensure paid plugins only work on official apps
-- =====================================================

-- Table for tracking device registrations per license
CREATE TABLE IF NOT EXISTS plugin_device_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plugin_id TEXT NOT NULL,
    device_id TEXT NOT NULL,
    device_fingerprint TEXT,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_validated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(user_id, plugin_id, device_id)
);

CREATE INDEX IF NOT EXISTS idx_device_registrations_user ON plugin_device_registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_device_registrations_plugin ON plugin_device_registrations(plugin_id);

-- RLS for device registrations
ALTER TABLE plugin_device_registrations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own device registrations" ON plugin_device_registrations
    FOR SELECT USING (auth.uid() = user_id);

-- Function to validate plugin license with device binding
CREATE OR REPLACE FUNCTION validate_plugin_license(
    p_user_id UUID,
    p_plugin_id TEXT,
    p_device_id TEXT
)
RETURNS JSON AS $$
DECLARE
    v_purchase RECORD;
    v_grant RECORD;
    v_device_count INTEGER;
    v_max_devices INTEGER := 3;
    v_result JSON;
BEGIN
    -- Check for active purchase
    SELECT * INTO v_purchase
    FROM plugin_purchases
    WHERE user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND status = 'ACTIVE'
    AND (expiry_date IS NULL OR expiry_date > NOW())
    LIMIT 1;
    
    IF v_purchase IS NOT NULL THEN
        -- Count registered devices
        SELECT COUNT(*) INTO v_device_count
        FROM plugin_device_registrations
        WHERE user_id = p_user_id 
        AND plugin_id = p_plugin_id 
        AND is_active = TRUE;
        
        -- Check if this device is already registered
        IF NOT EXISTS (
            SELECT 1 FROM plugin_device_registrations
            WHERE user_id = p_user_id 
            AND plugin_id = p_plugin_id 
            AND device_id = p_device_id
        ) THEN
            -- Check device limit
            IF v_device_count >= v_max_devices THEN
                RETURN json_build_object(
                    'is_valid', FALSE,
                    'status', 'DEVICE_LIMIT',
                    'device_count', v_device_count,
                    'max_devices', v_max_devices
                );
            END IF;
            
            -- Register new device
            INSERT INTO plugin_device_registrations (user_id, plugin_id, device_id)
            VALUES (p_user_id, p_plugin_id, p_device_id);
            
            v_device_count := v_device_count + 1;
        ELSE
            -- Update last validated timestamp
            UPDATE plugin_device_registrations
            SET last_validated_at = NOW()
            WHERE user_id = p_user_id 
            AND plugin_id = p_plugin_id 
            AND device_id = p_device_id;
        END IF;
        
        RETURN json_build_object(
            'is_valid', TRUE,
            'status', 'VALID',
            'expiry_date', EXTRACT(EPOCH FROM v_purchase.expiry_date) * 1000,
            'device_count', v_device_count,
            'max_devices', v_max_devices
        );
    END IF;
    
    -- Check for active grant from developer
    SELECT * INTO v_grant
    FROM plugin_access_grants
    WHERE granted_to_user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND is_active = TRUE
    AND (expiry_date IS NULL OR expiry_date > NOW())
    LIMIT 1;
    
    IF v_grant IS NOT NULL THEN
        RETURN json_build_object(
            'is_valid', TRUE,
            'status', 'GRANTED',
            'expiry_date', EXTRACT(EPOCH FROM v_grant.expiry_date) * 1000,
            'granted_by', v_grant.granted_by_username,
            'device_count', 0,
            'max_devices', v_max_devices
        );
    END IF;
    
    -- Check for trial
    SELECT * INTO v_purchase
    FROM plugin_purchases
    WHERE user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND status = 'TRIAL'
    AND (expiry_date IS NULL OR expiry_date > NOW())
    LIMIT 1;
    
    IF v_purchase IS NOT NULL THEN
        RETURN json_build_object(
            'is_valid', TRUE,
            'status', 'TRIAL',
            'expiry_date', EXTRACT(EPOCH FROM v_purchase.expiry_date) * 1000,
            'device_count', 0,
            'max_devices', v_max_devices
        );
    END IF;
    
    -- No valid license found
    RETURN json_build_object(
        'is_valid', FALSE,
        'status', 'NOT_FOUND',
        'device_count', 0,
        'max_devices', v_max_devices
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to register a device for a plugin license
CREATE OR REPLACE FUNCTION register_plugin_device(
    p_user_id UUID,
    p_plugin_id TEXT,
    p_device_id TEXT,
    p_device_fingerprint TEXT DEFAULT NULL
)
RETURNS JSON AS $$
DECLARE
    v_device_count INTEGER;
    v_max_devices INTEGER := 3;
    v_registration RECORD;
BEGIN
    -- First validate the license
    IF NOT has_plugin_access(p_user_id, p_plugin_id) THEN
        RETURN json_build_object(
            'success', FALSE,
            'error', 'No valid license'
        );
    END IF;
    
    -- Count existing devices
    SELECT COUNT(*) INTO v_device_count
    FROM plugin_device_registrations
    WHERE user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND is_active = TRUE;
    
    -- Check if device already registered
    SELECT * INTO v_registration
    FROM plugin_device_registrations
    WHERE user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND device_id = p_device_id;
    
    IF v_registration IS NOT NULL THEN
        -- Update existing registration
        UPDATE plugin_device_registrations
        SET is_active = TRUE,
            last_validated_at = NOW(),
            device_fingerprint = COALESCE(p_device_fingerprint, device_fingerprint)
        WHERE id = v_registration.id;
        
        RETURN json_build_object(
            'device_id', p_device_id,
            'registered_at', EXTRACT(EPOCH FROM v_registration.registered_at) * 1000,
            'is_active', TRUE
        );
    END IF;
    
    -- Check device limit
    IF v_device_count >= v_max_devices THEN
        RETURN json_build_object(
            'success', FALSE,
            'error', 'Device limit reached',
            'device_count', v_device_count,
            'max_devices', v_max_devices
        );
    END IF;
    
    -- Register new device
    INSERT INTO plugin_device_registrations (user_id, plugin_id, device_id, device_fingerprint)
    VALUES (p_user_id, p_plugin_id, p_device_id, p_device_fingerprint)
    RETURNING * INTO v_registration;
    
    RETURN json_build_object(
        'device_id', v_registration.device_id,
        'registered_at', EXTRACT(EPOCH FROM v_registration.registered_at) * 1000,
        'is_active', v_registration.is_active
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to deactivate a device
CREATE OR REPLACE FUNCTION deactivate_plugin_device(
    p_user_id UUID,
    p_plugin_id TEXT,
    p_device_id TEXT
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE plugin_device_registrations
    SET is_active = FALSE
    WHERE user_id = p_user_id 
    AND plugin_id = p_plugin_id 
    AND device_id = p_device_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
