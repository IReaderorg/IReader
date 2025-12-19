-- Schema 15: Plugin Reviews
-- Tables for plugin reviews and ratings

-- Plugin reviews table
CREATE TABLE IF NOT EXISTS plugin_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    review_text TEXT,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT review_text_length CHECK (review_text IS NULL OR LENGTH(review_text) <= 2000),
    CONSTRAINT unique_user_plugin_review UNIQUE(user_id, plugin_id)
);

CREATE INDEX IF NOT EXISTS idx_plugin_reviews_plugin ON plugin_reviews(plugin_id);
CREATE INDEX IF NOT EXISTS idx_plugin_reviews_user ON plugin_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_plugin_reviews_rating ON plugin_reviews(rating DESC);
CREATE INDEX IF NOT EXISTS idx_plugin_reviews_created ON plugin_reviews(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_plugin_reviews_helpful ON plugin_reviews(helpful_count DESC);

COMMENT ON TABLE plugin_reviews IS 'User reviews for plugins in the marketplace';

-- Plugin review helpful votes table
CREATE TABLE IF NOT EXISTS plugin_review_helpful (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES plugin_reviews(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT unique_user_review_helpful UNIQUE(user_id, review_id)
);

CREATE INDEX IF NOT EXISTS idx_review_helpful_review ON plugin_review_helpful(review_id);
CREATE INDEX IF NOT EXISTS idx_review_helpful_user ON plugin_review_helpful(user_id);

COMMENT ON TABLE plugin_review_helpful IS 'Tracks helpful votes on plugin reviews';

-- Plugin rating statistics table (aggregated)
CREATE TABLE IF NOT EXISTS plugin_rating_stats (
    plugin_id TEXT PRIMARY KEY,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    rating_1_count INTEGER NOT NULL DEFAULT 0,
    rating_2_count INTEGER NOT NULL DEFAULT 0,
    rating_3_count INTEGER NOT NULL DEFAULT 0,
    rating_4_count INTEGER NOT NULL DEFAULT 0,
    rating_5_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rating_stats_avg ON plugin_rating_stats(average_rating DESC);

COMMENT ON TABLE plugin_rating_stats IS 'Aggregated rating statistics for plugins';

-- RLS Policies
ALTER TABLE plugin_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_review_helpful ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_rating_stats ENABLE ROW LEVEL SECURITY;

-- Anyone can view reviews
CREATE POLICY "Anyone can view plugin reviews" ON plugin_reviews
    FOR SELECT USING (TRUE);

-- Users can create their own reviews
CREATE POLICY "Users can create own reviews" ON plugin_reviews
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Users can update their own reviews
CREATE POLICY "Users can update own reviews" ON plugin_reviews
    FOR UPDATE USING (auth.uid() = user_id);

-- Users can delete their own reviews
CREATE POLICY "Users can delete own reviews" ON plugin_reviews
    FOR DELETE USING (auth.uid() = user_id);

-- Users can manage their helpful votes
CREATE POLICY "Users can manage helpful votes" ON plugin_review_helpful
    FOR ALL USING (auth.uid() = user_id);

-- Anyone can view rating stats
CREATE POLICY "Anyone can view rating stats" ON plugin_rating_stats
    FOR SELECT USING (TRUE);

-- Functions

-- Function to submit or update a plugin review
CREATE OR REPLACE FUNCTION submit_plugin_review(
    p_plugin_id TEXT,
    p_rating INTEGER,
    p_review_text TEXT DEFAULT NULL
)
RETURNS JSON AS $$
DECLARE
    v_review_id UUID;
    v_result JSON;
BEGIN
    -- Validate rating
    IF p_rating < 1 OR p_rating > 5 THEN
        RETURN json_build_object('success', FALSE, 'error', 'Rating must be between 1 and 5');
    END IF;
    
    -- Upsert review
    INSERT INTO plugin_reviews (plugin_id, user_id, rating, review_text)
    VALUES (p_plugin_id, auth.uid(), p_rating, p_review_text)
    ON CONFLICT (user_id, plugin_id) DO UPDATE SET
        rating = EXCLUDED.rating,
        review_text = EXCLUDED.review_text,
        updated_at = NOW()
    RETURNING id INTO v_review_id;
    
    -- Update rating stats
    PERFORM update_plugin_rating_stats(p_plugin_id);
    
    RETURN json_build_object(
        'success', TRUE,
        'review_id', v_review_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to update plugin rating statistics
CREATE OR REPLACE FUNCTION update_plugin_rating_stats(p_plugin_id TEXT)
RETURNS VOID AS $$
DECLARE
    v_avg DECIMAL(3, 2);
    v_total INTEGER;
    v_r1 INTEGER;
    v_r2 INTEGER;
    v_r3 INTEGER;
    v_r4 INTEGER;
    v_r5 INTEGER;
BEGIN
    SELECT 
        COALESCE(AVG(rating), 0),
        COUNT(*),
        COUNT(*) FILTER (WHERE rating = 1),
        COUNT(*) FILTER (WHERE rating = 2),
        COUNT(*) FILTER (WHERE rating = 3),
        COUNT(*) FILTER (WHERE rating = 4),
        COUNT(*) FILTER (WHERE rating = 5)
    INTO v_avg, v_total, v_r1, v_r2, v_r3, v_r4, v_r5
    FROM plugin_reviews
    WHERE plugin_id = p_plugin_id;
    
    INSERT INTO plugin_rating_stats (
        plugin_id, average_rating, total_reviews,
        rating_1_count, rating_2_count, rating_3_count, rating_4_count, rating_5_count
    )
    VALUES (p_plugin_id, v_avg, v_total, v_r1, v_r2, v_r3, v_r4, v_r5)
    ON CONFLICT (plugin_id) DO UPDATE SET
        average_rating = EXCLUDED.average_rating,
        total_reviews = EXCLUDED.total_reviews,
        rating_1_count = EXCLUDED.rating_1_count,
        rating_2_count = EXCLUDED.rating_2_count,
        rating_3_count = EXCLUDED.rating_3_count,
        rating_4_count = EXCLUDED.rating_4_count,
        rating_5_count = EXCLUDED.rating_5_count,
        updated_at = NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to mark a review as helpful
CREATE OR REPLACE FUNCTION mark_review_helpful(p_review_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO plugin_review_helpful (review_id, user_id)
    VALUES (p_review_id, auth.uid())
    ON CONFLICT (user_id, review_id) DO NOTHING;
    
    IF FOUND THEN
        UPDATE plugin_reviews
        SET helpful_count = helpful_count + 1
        WHERE id = p_review_id;
        RETURN TRUE;
    END IF;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to unmark a review as helpful
CREATE OR REPLACE FUNCTION unmark_review_helpful(p_review_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    DELETE FROM plugin_review_helpful
    WHERE review_id = p_review_id AND user_id = auth.uid();
    
    IF FOUND THEN
        UPDATE plugin_reviews
        SET helpful_count = GREATEST(helpful_count - 1, 0)
        WHERE id = p_review_id;
        RETURN TRUE;
    END IF;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get plugin reviews with user info
CREATE OR REPLACE FUNCTION get_plugin_reviews(
    p_plugin_id TEXT,
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0,
    p_order_by TEXT DEFAULT 'created_at'
)
RETURNS TABLE (
    id UUID,
    plugin_id TEXT,
    user_id UUID,
    username TEXT,
    rating INTEGER,
    review_text TEXT,
    helpful_count INTEGER,
    is_helpful BOOLEAN,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        pr.id,
        pr.plugin_id,
        pr.user_id,
        COALESCE(u.username, 'Anonymous') as username,
        pr.rating,
        pr.review_text,
        pr.helpful_count,
        EXISTS(
            SELECT 1 FROM plugin_review_helpful prh 
            WHERE prh.review_id = pr.id AND prh.user_id = auth.uid()
        ) as is_helpful,
        pr.created_at,
        pr.updated_at
    FROM plugin_reviews pr
    LEFT JOIN users u ON pr.user_id = u.id
    WHERE pr.plugin_id = p_plugin_id
    ORDER BY 
        CASE WHEN p_order_by = 'helpful' THEN pr.helpful_count END DESC,
        CASE WHEN p_order_by = 'rating' THEN pr.rating END DESC,
        pr.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to update stats when review is deleted
CREATE OR REPLACE FUNCTION trigger_update_rating_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM update_plugin_rating_stats(OLD.plugin_id);
        RETURN OLD;
    ELSIF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        PERFORM update_plugin_rating_stats(NEW.plugin_id);
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_rating_stats_trigger
AFTER INSERT OR UPDATE OR DELETE ON plugin_reviews
FOR EACH ROW EXECUTE FUNCTION trigger_update_rating_stats();
