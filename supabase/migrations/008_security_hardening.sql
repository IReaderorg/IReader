-- ============================================================================
-- Migration 008: Security Hardening for RLS, RPC Functions & Sync Tables
-- ============================================================================
-- This migration addresses security vulnerabilities identified during code audit:
-- 1. Privilege escalation on public.users (prevent regular users setting is_admin / is_supporter)
-- 2. Authorization checks on badge grant RPC functions and tampering protection on public.user_badges
-- 3. Hardening RLS policies on synced tables (reading_progress, synced_books, sync_manifest, synced_chapters)
-- 4. Validation and protection on public.leaderboard
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Prevent User Privilege Escalation on public.users
-- ----------------------------------------------------------------------------
-- Revoke broad UPDATE permissions so users cannot modify sensitive columns like is_admin or is_supporter.
REVOKE UPDATE ON public.users FROM authenticated, anon, public;

-- Grant column-level UPDATE permission only for user-editable fields.
GRANT UPDATE (username, eth_wallet_address, updated_at) ON public.users TO authenticated;

-- Defense-in-depth trigger to prevent unauthorized privilege changes
CREATE OR REPLACE FUNCTION public.prevent_user_privilege_escalation()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.is_admin IS DISTINCT FROM OLD.is_admin OR NEW.is_supporter IS DISTINCT FROM OLD.is_supporter) THEN
        IF auth.role() != 'service_role' AND NOT EXISTS (
            SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
        ) THEN
            RAISE EXCEPTION 'Unauthorized: cannot modify administrative privileges';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_prevent_user_privilege_escalation ON public.users;
CREATE TRIGGER tr_prevent_user_privilege_escalation
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.prevent_user_privilege_escalation();

-- Ensure UPDATE RLS policy requires authentic user ownership
DROP POLICY IF EXISTS "Users can update their own data" ON public.users;
CREATE POLICY "Users can update their own data" ON public.users FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- ----------------------------------------------------------------------------
-- 2. Secure Badge RPC Functions & User Badges
-- ----------------------------------------------------------------------------

-- A. grant_badge_to_user: Only admin or service_role can grant arbitrary badges
CREATE OR REPLACE FUNCTION grant_badge_to_user(p_user_id UUID, p_badge_id TEXT)
RETURNS JSON LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE result JSON;
BEGIN
    IF auth.role() != 'service_role' AND NOT EXISTS (
        SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
    ) THEN
        RAISE EXCEPTION 'Unauthorized: only admins can grant badges';
    END IF;

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
$$;

GRANT EXECUTE ON FUNCTION grant_badge_to_user(UUID, TEXT) TO authenticated;

-- B. check_and_award_achievement_badge: User can only award to themselves and only ACHIEVEMENT badges
CREATE OR REPLACE FUNCTION check_and_award_achievement_badge(p_user_id UUID, p_badge_id TEXT)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE 
    v_already_has_badge BOOLEAN;
    v_badge_type TEXT;
BEGIN
    -- Only allow awarding to self unless service_role or admin
    IF auth.role() != 'service_role' AND p_user_id != auth.uid() AND NOT EXISTS (
        SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
    ) THEN
        RAISE EXCEPTION 'Unauthorized: cannot award badges to other users';
    END IF;

    -- Verify badge exists and is of type ACHIEVEMENT
    SELECT type INTO v_badge_type FROM public.badges WHERE id = p_badge_id;
    IF v_badge_type IS NULL THEN
        RAISE EXCEPTION 'Badge not found: %', p_badge_id;
    END IF;
    
    IF v_badge_type != 'ACHIEVEMENT' AND auth.role() != 'service_role' AND NOT EXISTS (
        SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
    ) THEN
        RAISE EXCEPTION 'Unauthorized: only achievement badges can be claimed via this function';
    END IF;

    SELECT EXISTS(SELECT 1 FROM public.user_badges WHERE user_id = p_user_id AND badge_id = p_badge_id) INTO v_already_has_badge;
    IF NOT v_already_has_badge THEN
        INSERT INTO public.user_badges (user_id, badge_id, earned_at) VALUES (p_user_id, p_badge_id, NOW())
        ON CONFLICT (user_id, badge_id) DO NOTHING;
        RETURN TRUE;
    END IF;
    RETURN FALSE;
END;
$$;

GRANT EXECUTE ON FUNCTION check_and_award_achievement_badge(UUID, TEXT) TO authenticated;

-- C. award_badge: Restrict internal award function from PUBLIC execution
CREATE OR REPLACE FUNCTION award_badge(p_user_id UUID, p_badge_id TEXT, p_metadata JSONB DEFAULT NULL)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    IF auth.role() != 'service_role' AND NOT EXISTS (
        SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
    ) THEN
        RAISE EXCEPTION 'Unauthorized: admin only';
    END IF;

    INSERT INTO public.user_badges (user_id, badge_id, metadata)
    VALUES (p_user_id, p_badge_id, p_metadata)
    ON CONFLICT (user_id, badge_id) DO NOTHING;
    RETURN FOUND;
END;
$$;

REVOKE EXECUTE ON FUNCTION award_badge(UUID, TEXT, JSONB) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION award_badge(UUID, TEXT, JSONB) TO service_role;

-- D. user_badges: Prevent users tampering with badge_id, user_id, or earned_at
REVOKE UPDATE ON public.user_badges FROM authenticated, anon, public;
GRANT UPDATE (is_primary, is_featured) ON public.user_badges TO authenticated;

CREATE OR REPLACE FUNCTION public.prevent_badge_tampering()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.badge_id IS DISTINCT FROM OLD.badge_id OR NEW.user_id IS DISTINCT FROM OLD.user_id OR NEW.earned_at IS DISTINCT FROM OLD.earned_at) THEN
        IF auth.role() != 'service_role' AND NOT EXISTS (
            SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE
        ) THEN
            RAISE EXCEPTION 'Unauthorized: cannot alter badge identification or earned date';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_prevent_badge_tampering ON public.user_badges;
CREATE TRIGGER tr_prevent_badge_tampering
    BEFORE UPDATE ON public.user_badges
    FOR EACH ROW
    EXECUTE FUNCTION public.prevent_badge_tampering();

DROP POLICY IF EXISTS "Users can update their own badge settings" ON public.user_badges;
CREATE POLICY "Users can update their own badge settings" ON public.user_badges FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- 3. Hardening Sync Tables RLS Policies
-- ----------------------------------------------------------------------------
-- Drop wide-open public policies from migrations 005 and 006
DROP POLICY IF EXISTS "Allow public sync_manifest access" ON public.sync_manifest;
DROP POLICY IF EXISTS "Allow public synced_books access" ON public.synced_books;
DROP POLICY IF EXISTS "Allow public synced_chapters access" ON public.synced_chapters;

-- reading_progress: remove "OR auth.uid() IS NULL"
DROP POLICY IF EXISTS "Users can view their own reading progress" ON public.reading_progress;
DROP POLICY IF EXISTS "Users can insert their own reading progress" ON public.reading_progress;
DROP POLICY IF EXISTS "Users can update their own reading progress" ON public.reading_progress;
DROP POLICY IF EXISTS "Users can delete their own reading progress" ON public.reading_progress;

CREATE POLICY "Users can view their own reading progress" ON public.reading_progress FOR SELECT
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can insert their own reading progress" ON public.reading_progress FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can update their own reading progress" ON public.reading_progress FOR UPDATE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT)
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own reading progress" ON public.reading_progress FOR DELETE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);

-- synced_books: remove "OR auth.uid() IS NULL"
DROP POLICY IF EXISTS "Users can view their own synced books" ON public.synced_books;
DROP POLICY IF EXISTS "Users can insert their own synced books" ON public.synced_books;
DROP POLICY IF EXISTS "Users can update their own synced books" ON public.synced_books;
DROP POLICY IF EXISTS "Users can delete their own synced books" ON public.synced_books;

CREATE POLICY "Users can view their own synced books" ON public.synced_books FOR SELECT
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can insert their own synced books" ON public.synced_books FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can update their own synced books" ON public.synced_books FOR UPDATE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT)
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own synced books" ON public.synced_books FOR DELETE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);

-- sync_manifest: remove "OR auth.uid() IS NULL"
DROP POLICY IF EXISTS "Users can view their own sync manifest" ON public.sync_manifest;
DROP POLICY IF EXISTS "Users can insert their own sync manifest" ON public.sync_manifest;
DROP POLICY IF EXISTS "Users can update their own sync manifest" ON public.sync_manifest;
DROP POLICY IF EXISTS "Users can delete their own sync manifest" ON public.sync_manifest;

CREATE POLICY "Users can view their own sync manifest" ON public.sync_manifest FOR SELECT
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can insert their own sync manifest" ON public.sync_manifest FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can update their own sync manifest" ON public.sync_manifest FOR UPDATE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT)
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own sync manifest" ON public.sync_manifest FOR DELETE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);

-- synced_chapters: remove "OR auth.uid() IS NULL"
DROP POLICY IF EXISTS "Users can view their own synced chapters" ON public.synced_chapters;
DROP POLICY IF EXISTS "Users can insert their own synced chapters" ON public.synced_chapters;
DROP POLICY IF EXISTS "Users can update their own synced chapters" ON public.synced_chapters;
DROP POLICY IF EXISTS "Users can delete their own synced chapters" ON public.synced_chapters;

CREATE POLICY "Users can view their own synced chapters" ON public.synced_chapters FOR SELECT
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can insert their own synced chapters" ON public.synced_chapters FOR INSERT
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can update their own synced chapters" ON public.synced_chapters FOR UPDATE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT)
    WITH CHECK (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);
CREATE POLICY "Users can delete their own synced chapters" ON public.synced_chapters FOR DELETE
    USING (auth.uid() IS NOT NULL AND user_id = auth.uid()::TEXT);

-- ----------------------------------------------------------------------------
-- 4. Leaderboard Validation Trigger
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.validate_leaderboard_update()
RETURNS TRIGGER AS $$
BEGIN
    -- Prevent claiming badge status on leaderboard without actually having a badge
    IF NEW.has_badge = TRUE THEN
        IF NOT EXISTS (SELECT 1 FROM public.user_badges WHERE user_id = NEW.user_id) THEN
            NEW.has_badge := FALSE;
            NEW.badge_type := NULL;
        END IF;
    END IF;

    -- Sanity check: prevent negative numbers
    IF NEW.total_reading_time_minutes < 0 THEN NEW.total_reading_time_minutes := 0; END IF;
    IF NEW.total_chapters_read < 0 THEN NEW.total_chapters_read := 0; END IF;
    IF NEW.books_completed < 0 THEN NEW.books_completed := 0; END IF;
    IF NEW.reading_streak < 0 THEN NEW.reading_streak := 0; END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_validate_leaderboard ON public.leaderboard;
CREATE TRIGGER tr_validate_leaderboard
    BEFORE INSERT OR UPDATE ON public.leaderboard
    FOR EACH ROW
    EXECUTE FUNCTION public.validate_leaderboard_update();
