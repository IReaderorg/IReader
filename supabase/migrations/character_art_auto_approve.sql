-- Migration: Add auto_approved column to character_art table
-- This column tracks whether art was auto-approved after 7 days

-- Add auto_approved column if it doesn't exist
ALTER TABLE character_art 
ADD COLUMN IF NOT EXISTS auto_approved BOOLEAN DEFAULT FALSE;

-- Add index for querying pending art by submission date (for auto-approve)
CREATE INDEX IF NOT EXISTS idx_character_art_pending_submitted 
ON character_art (submitted_at) 
WHERE status = 'PENDING';

-- Function to auto-approve pending art older than N days
CREATE OR REPLACE FUNCTION auto_approve_old_pending_art(days_threshold INTEGER DEFAULT 7)
RETURNS INTEGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    approved_count INTEGER;
BEGIN
    -- Update pending art older than threshold to approved
    WITH updated AS (
        UPDATE character_art
        SET 
            status = 'APPROVED',
            auto_approved = TRUE
        WHERE 
            status = 'PENDING'
            AND submitted_at < (EXTRACT(EPOCH FROM NOW()) * 1000 - (days_threshold * 24 * 60 * 60 * 1000))
        RETURNING id
    )
    SELECT COUNT(*) INTO approved_count FROM updated;
    
    RETURN approved_count;
END;
$$;

-- Grant execute permission to authenticated users (admin check should be done in app)
GRANT EXECUTE ON FUNCTION auto_approve_old_pending_art(INTEGER) TO authenticated;

-- Comment for documentation
COMMENT ON COLUMN character_art.auto_approved IS 'True if this art was automatically approved after being pending for 7+ days';
COMMENT ON FUNCTION auto_approve_old_pending_art IS 'Auto-approves pending character art older than specified days. Returns count of approved items.';
