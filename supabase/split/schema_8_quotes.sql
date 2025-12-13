-- ============================================================================
-- PROJECT 8: QUOTES DATABASE SCHEMA
-- ============================================================================
-- This schema contains community quotes system
-- Tables: community_quotes, quote_likes, daily_quote_history
-- Estimated storage: ~500MB for 500K+ quotes
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLES
-- ============================================================================

-- Community Quotes Table
CREATE TABLE IF NOT EXISTS public.community_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    quote_text TEXT NOT NULL,
    book_title TEXT NOT NULL,
    author TEXT,
    chapter_title TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by TEXT,
    likes_count INTEGER DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT quote_text_length CHECK (LENGTH(quote_text) >= 10 AND LENGTH(quote_text) <= 1000),
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT quote_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT cq_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_community_quotes_user_id ON public.community_quotes(user_id);
CREATE INDEX IF NOT EXISTS idx_community_quotes_status ON public.community_quotes(status);
CREATE INDEX IF NOT EXISTS idx_community_quotes_approved ON public.community_quotes(status, submitted_at DESC) WHERE status = 'APPROVED';
CREATE INDEX IF NOT EXISTS idx_community_quotes_featured ON public.community_quotes(featured, likes_count DESC) WHERE featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_community_quotes_book ON public.community_quotes(book_title);

COMMENT ON TABLE public.community_quotes IS 'Community-submitted quotes from books';

-- Quote Likes Table
CREATE TABLE IF NOT EXISTS public.quote_likes (
    user_id TEXT NOT NULL,
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    liked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    PRIMARY KEY (user_id, quote_id),
    CONSTRAINT ql_user_id_not_empty CHECK (LENGTH(user_id) > 0)
);

CREATE INDEX IF NOT EXISTS idx_quote_likes_quote_id ON public.quote_likes(quote_id);
CREATE INDEX IF NOT EXISTS idx_quote_likes_user_id ON public.quote_likes(user_id);

COMMENT ON TABLE public.quote_likes IS 'Tracks user likes on quotes';

-- Daily Quote History Table
CREATE TABLE IF NOT EXISTS public.daily_quote_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    shown_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_daily_quote_date ON public.daily_quote_history(shown_date DESC);

COMMENT ON TABLE public.daily_quote_history IS 'History of daily quotes shown';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.community_quotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quote_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_quote_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view approved quotes" ON public.community_quotes FOR SELECT
    USING (status = 'APPROVED' OR user_id = auth.uid()::TEXT);
CREATE POLICY "Users can submit quotes" ON public.community_quotes FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update pending quotes" ON public.community_quotes FOR UPDATE
    USING (user_id = auth.uid()::TEXT AND status = 'PENDING');
CREATE POLICY "Users can delete pending quotes" ON public.community_quotes FOR DELETE
    USING (user_id = auth.uid()::TEXT AND status = 'PENDING');

CREATE POLICY "Everyone can view quote likes" ON public.quote_likes FOR SELECT USING (true);
CREATE POLICY "Users can like quotes" ON public.quote_likes FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can unlike quotes" ON public.quote_likes FOR DELETE USING (user_id = auth.uid()::TEXT);

CREATE POLICY "Everyone can view daily quote history" ON public.daily_quote_history FOR SELECT USING (true);

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

CREATE OR REPLACE FUNCTION get_daily_quote()
RETURNS TABLE (quote_id UUID, quote_text TEXT, book_title TEXT, author TEXT,
               chapter_title TEXT, likes_count INTEGER, submitter_id TEXT) AS $
DECLARE
    today DATE := CURRENT_DATE;
    selected_quote_id UUID;
BEGIN
    SELECT dqh.quote_id INTO selected_quote_id FROM public.daily_quote_history dqh WHERE dqh.shown_date = today;
    
    IF selected_quote_id IS NULL THEN
        SELECT cq.id INTO selected_quote_id FROM public.community_quotes cq
        WHERE cq.status = 'APPROVED' ORDER BY RANDOM() LIMIT 1;
        
        IF selected_quote_id IS NOT NULL THEN
            INSERT INTO public.daily_quote_history (quote_id, shown_date)
            VALUES (selected_quote_id, today) ON CONFLICT (shown_date) DO NOTHING;
        END IF;
    END IF;
    
    RETURN QUERY
    SELECT cq.id, cq.quote_text, cq.book_title, cq.author, cq.chapter_title, cq.likes_count, cq.user_id
    FROM public.community_quotes cq WHERE cq.id = selected_quote_id;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION toggle_quote_like(p_quote_id UUID)
RETURNS BOOLEAN AS $
DECLARE v_user_id TEXT := auth.uid()::TEXT; v_liked BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM public.quote_likes WHERE user_id = v_user_id AND quote_id = p_quote_id) INTO v_liked;
    
    IF v_liked THEN
        DELETE FROM public.quote_likes WHERE user_id = v_user_id AND quote_id = p_quote_id;
        UPDATE public.community_quotes SET likes_count = likes_count - 1 WHERE id = p_quote_id;
        RETURN FALSE;
    ELSE
        INSERT INTO public.quote_likes (user_id, quote_id) VALUES (v_user_id, p_quote_id);
        UPDATE public.community_quotes SET likes_count = likes_count + 1 WHERE id = p_quote_id;
        RETURN TRUE;
    END IF;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $
BEGIN
    RAISE NOTICE '✅ PROJECT 8 (QUOTES) Schema created successfully!';
    RAISE NOTICE 'Tables: community_quotes, quote_likes, daily_quote_history';
    RAISE NOTICE 'Capacity: ~500K quotes in 500MB';
END $;
