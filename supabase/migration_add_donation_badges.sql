-- Migration: Add donation/supporter badges for the badge store
-- These are PURCHASABLE badges that users can buy to support the app

-- Insert donation/supporter badges
INSERT INTO public.badges (id, name, description, icon, category, rarity, price, type, is_available, image_url)
VALUES
    -- Bronze tier donation badges
    ('supporter_bronze', 'Bronze Supporter', 'Support the app with a $5 donation', '🥉', 'donor', 'common', 5.00, 'PURCHASABLE', TRUE, NULL),
    ('coffee_supporter', 'Coffee Supporter', 'Buy the developer a coffee ($3)', '☕', 'donor', 'common', 3.00, 'PURCHASABLE', TRUE, NULL),
    
    -- Silver tier donation badges
    ('supporter_silver', 'Silver Supporter', 'Support the app with a $10 donation', '🥈', 'donor', 'rare', 10.00, 'PURCHASABLE', TRUE, NULL),
    ('book_patron', 'Book Patron', 'Support with a book-sized donation ($15)', '📚', 'donor', 'rare', 15.00, 'PURCHASABLE', TRUE, NULL),
    
    -- Gold tier donation badges
    ('supporter_gold', 'Gold Supporter', 'Support the app with a $25 donation', '🥇', 'donor', 'epic', 25.00, 'PURCHASABLE', TRUE, NULL),
    ('generous_patron', 'Generous Patron', 'A generous $50 donation', '💎', 'donor', 'epic', 50.00, 'PURCHASABLE', TRUE, NULL),
    
    -- Platinum/Legendary tier donation badges
    ('supporter_platinum', 'Platinum Supporter', 'Support the app with a $100 donation', '💠', 'donor', 'legendary', 100.00, 'PURCHASABLE', TRUE, NULL),
    ('legendary_patron', 'Legendary Patron', 'An incredible $250 donation', '👑', 'donor', 'legendary', 250.00, 'PURCHASABLE', TRUE, NULL),
    ('ultimate_benefactor', 'Ultimate Benefactor', 'The ultimate $500 donation', '⭐', 'donor', 'legendary', 500.00, 'PURCHASABLE', TRUE, NULL),
    
    -- Special themed donation badges
    ('early_supporter', 'Early Supporter', 'Support during early development ($20)', '🌟', 'donor', 'rare', 20.00, 'PURCHASABLE', TRUE, NULL),
    ('lifetime_supporter', 'Lifetime Supporter', 'One-time lifetime support ($75)', '♾️', 'donor', 'epic', 75.00, 'PURCHASABLE', TRUE, NULL),
    
    -- Contributor badges (for special contributions - marked as ACHIEVEMENT so admins can grant them)
    ('code_contributor', 'Code Contributor', 'Contributed code to the project', '💻', 'contributor', 'epic', NULL, 'ACHIEVEMENT', TRUE, NULL),
    ('translator', 'Translator', 'Helped translate the app', '🌍', 'contributor', 'rare', NULL, 'ACHIEVEMENT', TRUE, NULL),
    ('bug_hunter', 'Bug Hunter', 'Reported critical bugs', '🐛', 'contributor', 'rare', NULL, 'ACHIEVEMENT', TRUE, NULL),
    
    -- Special limited edition badges
    ('founding_member', 'Founding Member', 'One of the first 100 supporters ($30)', '🏛️', 'special', 'legendary', 30.00, 'PURCHASABLE', TRUE, NULL),
    ('anniversary_supporter', 'Anniversary Supporter', 'Supported during anniversary event ($25)', '🎉', 'special', 'epic', 25.00, 'PURCHASABLE', TRUE, NULL)
ON CONFLICT (id) DO NOTHING;

-- Update existing badges if they exist (in case you already have some)
-- This ensures they are marked as PURCHASABLE and have prices
UPDATE public.badges 
SET 
    type = 'PURCHASABLE',
    is_available = TRUE
WHERE category = 'donor' AND type != 'PURCHASABLE';

-- Add comment
COMMENT ON TABLE public.badges IS 'Available badges including purchasable donation badges and achievement badges';
