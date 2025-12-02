-- ============================================================================
-- QUOTES SYSTEM MIGRATION
-- Daily quotes from books with community submissions and admin verification
-- ============================================================================

-- First, ensure is_admin column exists on users table (if not already)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'users' 
        AND column_name = 'is_admin'
    ) THEN
        ALTER TABLE public.users ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Community Quotes Table
-- Stores quotes submitted by users, requires admin approval
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    quote_text TEXT NOT NULL,
    book_title TEXT NOT NULL,
    author TEXT,
    chapter_title TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID REFERENCES public.users(id),
    likes_count INTEGER DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    
    -- Constraints
    CONSTRAINT quote_text_length CHECK (LENGTH(quote_text) >= 10 AND LENGTH(quote_text) <= 1000),
    CONSTRAINT book_title_not_empty CHECK (LENGTH(book_title) > 0),
    CONSTRAINT quote_status_valid CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Indexes for community_quotes table
CREATE INDEX IF NOT EXISTS idx_community_quotes_user_id ON public.community_quotes(user_id);
CREATE INDEX IF NOT EXISTS idx_community_quotes_status ON public.community_quotes(status);
CREATE INDEX IF NOT EXISTS idx_community_quotes_approved ON public.community_quotes(status, submitted_at DESC) WHERE status = 'APPROVED';
CREATE INDEX IF NOT EXISTS idx_community_quotes_featured ON public.community_quotes(featured, likes_count DESC) WHERE featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_community_quotes_book ON public.community_quotes(book_title);

-- Comments
COMMENT ON TABLE public.community_quotes IS 'Community-submitted quotes from books, requires admin approval';
COMMENT ON COLUMN public.community_quotes.quote_text IS 'The quote text (10-1000 characters)';
COMMENT ON COLUMN public.community_quotes.book_title IS 'Title of the book the quote is from';
COMMENT ON COLUMN public.community_quotes.author IS 'Author of the book (optional)';
COMMENT ON COLUMN public.community_quotes.chapter_title IS 'Chapter the quote is from (optional)';
COMMENT ON COLUMN public.community_quotes.status IS 'Approval status (PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN public.community_quotes.featured IS 'Whether this quote is featured for daily quotes';

-- ----------------------------------------------------------------------------
-- Quote Likes Table
-- Tracks which users liked which quotes
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.quote_likes (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    liked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    PRIMARY KEY (user_id, quote_id)
);

-- Indexes for quote_likes table
CREATE INDEX IF NOT EXISTS idx_quote_likes_quote_id ON public.quote_likes(quote_id);
CREATE INDEX IF NOT EXISTS idx_quote_likes_user_id ON public.quote_likes(user_id);

-- Comments
COMMENT ON TABLE public.quote_likes IS 'Tracks user likes on quotes';

-- ----------------------------------------------------------------------------
-- Daily Quote History Table
-- Tracks which quote was shown on which day
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.daily_quote_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL REFERENCES public.community_quotes(id) ON DELETE CASCADE,
    shown_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_daily_quote_date ON public.daily_quote_history(shown_date DESC);

-- Comments
COMMENT ON TABLE public.daily_quote_history IS 'History of daily quotes shown';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.community_quotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quote_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_quote_history ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- Community Quotes Policies
-- ----------------------------------------------------------------------------

-- Everyone can view approved quotes
CREATE POLICY "Everyone can view approved quotes"
    ON public.community_quotes FOR SELECT
    USING (status = 'APPROVED' OR auth.uid() = user_id OR 
           EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Users can submit their own quotes
CREATE POLICY "Users can submit quotes"
    ON public.community_quotes FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their pending quotes, admins can update any
CREATE POLICY "Users can update pending quotes"
    ON public.community_quotes FOR UPDATE
    USING (
        (auth.uid() = user_id AND status = 'PENDING') OR
        EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE)
    );

-- Users can delete their pending quotes
CREATE POLICY "Users can delete pending quotes"
    ON public.community_quotes FOR DELETE
    USING (auth.uid() = user_id AND status = 'PENDING');

-- ----------------------------------------------------------------------------
-- Quote Likes Policies
-- ----------------------------------------------------------------------------

-- Everyone can view likes
CREATE POLICY "Everyone can view quote likes"
    ON public.quote_likes FOR SELECT
    USING (true);

-- Users can like quotes
CREATE POLICY "Users can like quotes"
    ON public.quote_likes FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can unlike quotes
CREATE POLICY "Users can unlike quotes"
    ON public.quote_likes FOR DELETE
    USING (auth.uid() = user_id);

-- ----------------------------------------------------------------------------
-- Daily Quote History Policies
-- ----------------------------------------------------------------------------

-- Everyone can view daily quote history
CREATE POLICY "Everyone can view daily quote history"
    ON public.daily_quote_history FOR SELECT
    USING (true);

-- Only admins can manage daily quotes (via service role)

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Function: Get today's daily quote
-- Returns the quote for today, or picks a random approved quote if none set
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_daily_quote()
RETURNS TABLE (
    quote_id UUID,
    quote_text TEXT,
    book_title TEXT,
    author TEXT,
    chapter_title TEXT,
    likes_count INTEGER,
    submitter_username TEXT,
    submitter_id UUID
) AS $$
DECLARE
    today DATE := CURRENT_DATE;
    selected_quote_id UUID;
BEGIN
    -- Check if we have a quote for today
    SELECT dqh.quote_id INTO selected_quote_id
    FROM public.daily_quote_history dqh
    WHERE dqh.shown_date = today;
    
    -- If no quote for today, pick a random approved featured quote
    IF selected_quote_id IS NULL THEN
        SELECT cq.id INTO selected_quote_id
        FROM public.community_quotes cq
        WHERE cq.status = 'APPROVED'
        ORDER BY RANDOM()
        LIMIT 1;
        
        -- Record this as today's quote
        IF selected_quote_id IS NOT NULL THEN
            INSERT INTO public.daily_quote_history (quote_id, shown_date)
            VALUES (selected_quote_id, today)
            ON CONFLICT (shown_date) DO NOTHING;
        END IF;
    END IF;
    
    -- Return the quote details
    RETURN QUERY
    SELECT 
        cq.id,
        cq.quote_text,
        cq.book_title,
        cq.author,
        cq.chapter_title,
        cq.likes_count,
        u.username,
        cq.user_id
    FROM public.community_quotes cq
    LEFT JOIN public.users u ON cq.user_id = u.id
    WHERE cq.id = selected_quote_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_daily_quote() IS 'Returns the daily quote, selecting randomly if none set';

-- ----------------------------------------------------------------------------
-- Function: Toggle quote like
-- Likes or unlikes a quote and updates the count
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION toggle_quote_like(p_quote_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_user_id UUID := auth.uid();
    v_liked BOOLEAN;
BEGIN
    -- Check if already liked
    SELECT EXISTS(
        SELECT 1 FROM public.quote_likes 
        WHERE user_id = v_user_id AND quote_id = p_quote_id
    ) INTO v_liked;
    
    IF v_liked THEN
        -- Unlike
        DELETE FROM public.quote_likes 
        WHERE user_id = v_user_id AND quote_id = p_quote_id;
        
        UPDATE public.community_quotes 
        SET likes_count = likes_count - 1 
        WHERE id = p_quote_id;
        
        RETURN FALSE;
    ELSE
        -- Like
        INSERT INTO public.quote_likes (user_id, quote_id)
        VALUES (v_user_id, p_quote_id);
        
        UPDATE public.community_quotes 
        SET likes_count = likes_count + 1 
        WHERE id = p_quote_id;
        
        RETURN TRUE;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION toggle_quote_like(UUID) IS 'Toggles like status on a quote';

-- ----------------------------------------------------------------------------
-- Function: Check if user is admin (handles missing column gracefully)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION is_user_admin(p_user_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_is_admin BOOLEAN := FALSE;
    v_column_exists BOOLEAN;
BEGIN
    -- Check if is_admin column exists
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'users' 
        AND column_name = 'is_admin'
    ) INTO v_column_exists;
    
    IF v_column_exists THEN
        EXECUTE 'SELECT COALESCE(is_admin, FALSE) FROM public.users WHERE id = $1'
        INTO v_is_admin
        USING p_user_id;
    END IF;
    
    RETURN COALESCE(v_is_admin, FALSE);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ----------------------------------------------------------------------------
-- Function: Approve quote (admin only)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION approve_quote(p_quote_id UUID, p_featured BOOLEAN DEFAULT FALSE)
RETURNS BOOLEAN AS $$
DECLARE
    v_admin_id UUID := auth.uid();
BEGIN
    -- Check if user is admin
    IF NOT is_user_admin(v_admin_id) THEN
        RAISE EXCEPTION 'Only admins can approve quotes';
    END IF;
    
    UPDATE public.community_quotes
    SET 
        status = 'APPROVED',
        reviewed_at = NOW(),
        reviewed_by = v_admin_id,
        featured = p_featured
    WHERE id = p_quote_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION approve_quote(UUID, BOOLEAN) IS 'Approves a quote submission (admin only)';

-- ----------------------------------------------------------------------------
-- Function: Reject quote (admin only)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION reject_quote(p_quote_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_admin_id UUID := auth.uid();
BEGIN
    -- Check if user is admin
    IF NOT is_user_admin(v_admin_id) THEN
        RAISE EXCEPTION 'Only admins can reject quotes';
    END IF;
    
    UPDATE public.community_quotes
    SET 
        status = 'REJECTED',
        reviewed_at = NOW(),
        reviewed_by = v_admin_id
    WHERE id = p_quote_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION reject_quote(UUID) IS 'Rejects a quote submission (admin only)';

-- ============================================================================
-- SEED DATA: Sample quotes to get started
-- ============================================================================

-- Insert some sample approved quotes (these will be available immediately)
INSERT INTO public.community_quotes (id, user_id, quote_text, book_title, author, status, featured)
SELECT 
    gen_random_uuid(),
    (SELECT id FROM public.users LIMIT 1),
    quote_text,
    book_title,
    author,
    'APPROVED',
    TRUE
FROM (VALUES
    ('The only way to do great work is to love what you read.', 'The Art of Reading', 'Anonymous'),
    ('A reader lives a thousand lives before he dies. The man who never reads lives only one.', 'A Dance with Dragons', 'George R.R. Martin'),
    ('Books are a uniquely portable magic.', 'On Writing', 'Stephen King'),
    ('Reading is to the mind what exercise is to the body.', 'The Tatler', 'Joseph Addison'),
    ('There is no friend as loyal as a book.', 'Various', 'Ernest Hemingway')
) AS t(quote_text, book_title, author)
WHERE EXISTS (SELECT 1 FROM public.users LIMIT 1);
