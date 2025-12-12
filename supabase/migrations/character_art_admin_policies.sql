-- ============================================
-- Admin Policies for Character Art
-- ============================================
-- Run this after character_art_schema.sql
-- Requires admin role to be set up (is_admin column in users table)
--
-- IMPORTANT: This file uses 'users' table for admin check.
-- If your project uses a different table name (e.g., 'profiles'),
-- update the table name in the policies below.

-- Drop existing restrictive policies if they exist
DROP POLICY IF EXISTS "Anyone can view approved art" ON character_art;
DROP POLICY IF EXISTS "Users can view own submissions" ON character_art;
DROP POLICY IF EXISTS "Admins can view all art" ON character_art;
DROP POLICY IF EXISTS "Admins can update art status" ON character_art;
DROP POLICY IF EXISTS "Admins can delete any art" ON character_art;
DROP POLICY IF EXISTS "Admins can view reports" ON character_art_reports;
DROP POLICY IF EXISTS "Admins can update reports" ON character_art_reports;

-- Recreate with admin support
-- Anyone can view approved art
CREATE POLICY "Anyone can view approved art"
    ON character_art FOR SELECT
    USING (status = 'APPROVED');

-- Users can view their own submissions (any status)
CREATE POLICY "Users can view own submissions"
    ON character_art FOR SELECT
    USING (auth.uid() = submitter_id);

-- Admins can view all art (including pending)
-- NOTE: Uses 'users' table - change to 'profiles' if your schema uses that
CREATE POLICY "Admins can view all art"
    ON character_art FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Admins can update art status (approve/reject)
CREATE POLICY "Admins can update art status"
    ON character_art FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Admins can delete any art
CREATE POLICY "Admins can delete any art"
    ON character_art FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Admins can view all reports
CREATE POLICY "Admins can view reports"
    ON character_art_reports FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- Admins can update report status
CREATE POLICY "Admins can update reports"
    ON character_art_reports FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id = auth.uid() AND is_admin = TRUE
        )
    );

-- ============================================
-- Quick Admin Setup
-- ============================================
-- To make a user an admin, run this SQL in Supabase SQL Editor:
-- UPDATE users SET is_admin = TRUE WHERE email = 'your-email@example.com';
--
-- To verify admin status:
-- SELECT id, email, is_admin FROM users WHERE email = 'your-email@example.com';
