-- Schema 13: Plugin Social Features
-- Tables for collections, follows, activity feed, and recommendations

-- Plugin collections table
CREATE TABLE IF NOT EXISTS plugin_collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    author_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    plugin_ids TEXT[] NOT NULL DEFAULT '{}',
    cover_image_url TEXT,
    tags TEXT[] NOT NULL DEFAULT '{}',
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    likes_count INTEGER NOT NULL DEFAULT 0,
    saves_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_collections_author ON plugin_collections(author_id);
CREATE INDEX IF NOT EXISTS idx_collections_public ON plugin_collections(is_public);
CREATE INDEX IF NOT EXISTS idx_collections_featured ON plugin_collections(is_featured);

-- Collection likes table
CREATE TABLE IF NOT EXISTS collection_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL REFERENCES plugin_collections(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(collection_id, user_id)
);

-- Collection saves table
CREATE TABLE IF NOT EXISTS collection_saves (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL REFERENCES plugin_collections(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(collection_id, user_id)
);

-- Developer follows table
CREATE TABLE IF NOT EXISTS developer_follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower ON developer_follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON developer_follows(following_id);

-- Developer profiles table
CREATE TABLE IF NOT EXISTS developer_profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    website TEXT,
    plugin_count INTEGER NOT NULL DEFAULT 0,
    total_downloads BIGINT NOT NULL DEFAULT 0,
    follower_count INTEGER NOT NULL DEFAULT 0,
    following_count INTEGER NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Developer badges table
CREATE TABLE IF NOT EXISTS developer_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    developer_id UUID NOT NULL REFERENCES developer_profiles(id) ON DELETE CASCADE,
    badge_type TEXT NOT NULL,
    badge_name TEXT NOT NULL,
    badge_description TEXT NOT NULL,
    icon_url TEXT NOT NULL,
    earned_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_badges_developer ON developer_badges(developer_id);

-- Activity feed table
CREATE TABLE IF NOT EXISTS marketplace_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL,
    plugin_id TEXT,
    plugin_name TEXT,
    collection_id UUID,
    collection_name TEXT,
    developer_id UUID,
    developer_name TEXT,
    rating DECIMAL(2, 1),
    review_snippet TEXT,
    version TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activity_user ON marketplace_activity(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_type ON marketplace_activity(activity_type);
CREATE INDEX IF NOT EXISTS idx_activity_created ON marketplace_activity(created_at DESC);

-- Trending plugins table (updated periodically)
CREATE TABLE IF NOT EXISTS trending_plugins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL UNIQUE,
    plugin_name TEXT NOT NULL,
    plugin_icon_url TEXT,
    developer_name TEXT NOT NULL,
    trend_score DECIMAL(10, 4) NOT NULL,
    installs_this_week INTEGER NOT NULL DEFAULT 0,
    rating_change DECIMAL(3, 2) NOT NULL DEFAULT 0,
    rank INTEGER NOT NULL,
    previous_rank INTEGER,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trending_rank ON trending_plugins(rank);
CREATE INDEX IF NOT EXISTS idx_trending_score ON trending_plugins(trend_score DESC);

-- Plugin recommendations table
CREATE TABLE IF NOT EXISTS plugin_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plugin_id TEXT NOT NULL,
    plugin_name TEXT NOT NULL,
    plugin_icon_url TEXT,
    reason TEXT NOT NULL,
    score DECIMAL(5, 4) NOT NULL,
    based_on_plugin_ids TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '7 days'
);

CREATE INDEX IF NOT EXISTS idx_recommendations_user ON plugin_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_recommendations_expires ON plugin_recommendations(expires_at);

-- RLS Policies
ALTER TABLE plugin_collections ENABLE ROW LEVEL SECURITY;
ALTER TABLE collection_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE collection_saves ENABLE ROW LEVEL SECURITY;
ALTER TABLE developer_follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE developer_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE developer_badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE marketplace_activity ENABLE ROW LEVEL SECURITY;
ALTER TABLE trending_plugins ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_recommendations ENABLE ROW LEVEL SECURITY;

-- Public collections are viewable by all
CREATE POLICY "Public collections are viewable" ON plugin_collections
    FOR SELECT USING (is_public = TRUE);

-- Users can manage their own collections
CREATE POLICY "Users can manage own collections" ON plugin_collections
    FOR ALL USING (auth.uid() = author_id);

-- Users can like collections
CREATE POLICY "Users can like collections" ON collection_likes
    FOR ALL USING (auth.uid() = user_id);

-- Users can save collections
CREATE POLICY "Users can save collections" ON collection_saves
    FOR ALL USING (auth.uid() = user_id);

-- Users can manage their follows
CREATE POLICY "Users can manage follows" ON developer_follows
    FOR ALL USING (auth.uid() = follower_id);

-- Developer profiles are public
CREATE POLICY "Developer profiles are public" ON developer_profiles
    FOR SELECT USING (TRUE);

-- Users can update their own profile
CREATE POLICY "Users can update own profile" ON developer_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Badges are public
CREATE POLICY "Badges are public" ON developer_badges
    FOR SELECT USING (TRUE);

-- Users can view their own activity
CREATE POLICY "Users can view own activity" ON marketplace_activity
    FOR SELECT USING (auth.uid() = user_id);

-- Trending plugins are public
CREATE POLICY "Trending plugins are public" ON trending_plugins
    FOR SELECT USING (TRUE);

-- Users can view their recommendations
CREATE POLICY "Users can view own recommendations" ON plugin_recommendations
    FOR SELECT USING (auth.uid() = user_id);

-- Functions

-- Function to like a collection
CREATE OR REPLACE FUNCTION like_collection(p_collection_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO collection_likes (collection_id, user_id)
    VALUES (p_collection_id, auth.uid())
    ON CONFLICT (collection_id, user_id) DO NOTHING;
    
    UPDATE plugin_collections
    SET likes_count = likes_count + 1
    WHERE id = p_collection_id;
    
    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to unlike a collection
CREATE OR REPLACE FUNCTION unlike_collection(p_collection_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    DELETE FROM collection_likes
    WHERE collection_id = p_collection_id AND user_id = auth.uid();
    
    UPDATE plugin_collections
    SET likes_count = GREATEST(likes_count - 1, 0)
    WHERE id = p_collection_id;
    
    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to follow a developer
CREATE OR REPLACE FUNCTION follow_developer(p_developer_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO developer_follows (follower_id, following_id)
    VALUES (auth.uid(), p_developer_id)
    ON CONFLICT (follower_id, following_id) DO NOTHING;
    
    UPDATE developer_profiles
    SET follower_count = follower_count + 1
    WHERE id = p_developer_id;
    
    UPDATE developer_profiles
    SET following_count = following_count + 1
    WHERE id = auth.uid();
    
    -- Record activity
    INSERT INTO marketplace_activity (user_id, activity_type, developer_id, developer_name)
    SELECT auth.uid(), 'DEVELOPER_FOLLOWED', p_developer_id, display_name
    FROM developer_profiles WHERE id = p_developer_id;
    
    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to unfollow a developer
CREATE OR REPLACE FUNCTION unfollow_developer(p_developer_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    DELETE FROM developer_follows
    WHERE follower_id = auth.uid() AND following_id = p_developer_id;
    
    UPDATE developer_profiles
    SET follower_count = GREATEST(follower_count - 1, 0)
    WHERE id = p_developer_id;
    
    UPDATE developer_profiles
    SET following_count = GREATEST(following_count - 1, 0)
    WHERE id = auth.uid();
    
    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get activity feed for followed developers
CREATE OR REPLACE FUNCTION get_following_activity_feed(p_limit INTEGER DEFAULT 50)
RETURNS TABLE (
    id UUID,
    user_id UUID,
    activity_type TEXT,
    plugin_id TEXT,
    plugin_name TEXT,
    collection_id UUID,
    collection_name TEXT,
    developer_id UUID,
    developer_name TEXT,
    rating DECIMAL,
    review_snippet TEXT,
    version TEXT,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT ma.*
    FROM marketplace_activity ma
    INNER JOIN developer_follows df ON ma.user_id = df.following_id
    WHERE df.follower_id = auth.uid()
    ORDER BY ma.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
