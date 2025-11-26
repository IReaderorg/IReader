# Badge Setup Guide

This guide explains how to set up the complete badge system with donation badges, achievement badges, and NFT badges.

## Badge Types

### 1. PURCHASABLE Badges (Shown in Badge Store)
These badges appear in the badge store and can be purchased by users:
- **Donation/Supporter Badges** - Users purchase to support the app
- **Contributor Badges** - Can be manually granted by admins (set price to 0)

### 2. ACHIEVEMENT Badges (Earned through usage)
These badges are earned automatically based on user activity:
- Reading milestones
- Book completion
- Review writing
- Reading streaks
- Special achievements

### 3. NFT_EXCLUSIVE Badges (Earned through NFT ownership)
These badges are granted when users verify NFT ownership.

## Setup Instructions

### Step 1: Run Migrations in Order

Execute these SQL files in your Supabase SQL Editor:

```sql
-- 1. First, ensure the badges table exists (should already be in schema.sql)
-- Check if you have the badges table with the correct structure

-- 2. Add donation/supporter badges (PURCHASABLE)
-- Run: supabase/migration_add_donation_badges.sql

-- 3. Add achievement badges (ACHIEVEMENT)
-- Run: supabase/migration_add_achievement_badges.sql
```

### Step 2: Verify Badge Types

After running migrations, verify the badges are correctly categorized:

```sql
-- Check PURCHASABLE badges (should appear in store)
SELECT id, name, category, rarity, price, type 
FROM public.badges 
WHERE type = 'PURCHASABLE' 
ORDER BY price;

-- Check ACHIEVEMENT badges (earned through usage)
SELECT id, name, category, rarity, type 
FROM public.badges 
WHERE type = 'ACHIEVEMENT' 
ORDER BY category, rarity;

-- Check NFT badges (if any)
SELECT id, name, category, rarity, type 
FROM public.badges 
WHERE type = 'NFT_EXCLUSIVE';
```

### Step 3: Upload Badge Images (Optional but Recommended)

For better visual appeal, upload badge images to Supabase Storage:

1. **Create a storage bucket:**
   ```sql
   -- In Supabase Dashboard: Storage → Create Bucket
   -- Name: badge-images
   -- Public: Yes (for easy access)
   ```

2. **Upload images** for each badge (PNG or SVG recommended)

3. **Update image URLs:**
   ```sql
   -- Example: Update a badge with its image URL
   UPDATE public.badges 
   SET image_url = 'https://your-project.supabase.co/storage/v1/object/public/badge-images/supporter_bronze.png'
   WHERE id = 'supporter_bronze';
   ```

### Step 4: Test the Badge Store

1. Open the app and navigate to Settings → Badge Store
2. You should see only PURCHASABLE badges (donation and contributor badges)
3. Achievement badges should NOT appear in the store
4. Each badge should show its price and rarity

### Step 5: Test Achievement Badges

1. Navigate to Profile screen
2. You should see two badge sections:
   - **Badges** - Your featured badges (max 3)
   - **Achievement Badges** - Badges earned through usage
3. Reading statistics should appear below

## Badge Categories

### Donation Badges (PURCHASABLE)

| Badge ID | Name | Price | Rarity | Icon |
|----------|------|-------|--------|------|
| coffee_supporter | Coffee Supporter | $3 | Common | ☕ |
| supporter_bronze | Bronze Supporter | $5 | Common | 🥉 |
| supporter_silver | Silver Supporter | $10 | Rare | 🥈 |
| book_patron | Book Patron | $15 | Rare | 📚 |
| early_supporter | Early Supporter | $20 | Rare | 🌟 |
| supporter_gold | Gold Supporter | $25 | Epic | 🥇 |
| anniversary_supporter | Anniversary Supporter | $25 | Epic | 🎉 |
| founding_member | Founding Member | $30 | Legendary | 🏛️ |
| generous_patron | Generous Patron | $50 | Epic | 💎 |
| lifetime_supporter | Lifetime Supporter | $75 | Epic | ♾️ |
| supporter_platinum | Platinum Supporter | $100 | Legendary | 💠 |
| legendary_patron | Legendary Patron | $250 | Legendary | 👑 |
| ultimate_benefactor | Ultimate Benefactor | $500 | Legendary | ⭐ |

### Contributor Badges (ACHIEVEMENT - Admin Grant)

These badges are marked as ACHIEVEMENT type so they don't appear in the store, but admins can manually grant them to contributors.

| Badge ID | Name | Price | Rarity | Icon |
|----------|------|-------|--------|------|
| bug_hunter | Bug Hunter | N/A | Rare | 🐛 |
| translator | Translator | N/A | Rare | 🌍 |
| code_contributor | Code Contributor | N/A | Epic | 💻 |

### Achievement Badges (ACHIEVEMENT - Auto-Earned)

See `migration_add_achievement_badges.sql` for the complete list of 19 achievement badges.

## Customization

### Adding New Donation Tiers

```sql
INSERT INTO public.badges (id, name, description, icon, category, rarity, price, type, is_available)
VALUES (
    'custom_tier',           -- Unique ID
    'Custom Tier Name',      -- Display name
    'Description',           -- What user gets
    '🎁',                    -- Icon/emoji
    'donor',                 -- Category
    'epic',                  -- Rarity: common, rare, epic, legendary
    99.99,                   -- Price in USD
    'PURCHASABLE',           -- Type (must be PURCHASABLE for store)
    TRUE                     -- Available for purchase
);
```

### Temporarily Hiding Badges

```sql
-- Hide a badge from the store without deleting it
UPDATE public.badges 
SET is_available = FALSE 
WHERE id = 'badge_id';

-- Show it again later
UPDATE public.badges 
SET is_available = TRUE 
WHERE id = 'badge_id';
```

### Manually Granting Badges

Admins can manually grant contributor badges or other special badges to users:

```sql
-- Grant a contributor badge to a specific user
INSERT INTO public.user_badges (user_id, badge_id, earned_at)
VALUES (
    'user-uuid-here',
    'code_contributor',
    NOW()
)
ON CONFLICT (user_id, badge_id) DO NOTHING;

-- Grant multiple badges at once
INSERT INTO public.user_badges (user_id, badge_id, earned_at)
VALUES 
    ('user-uuid-here', 'code_contributor', NOW()),
    ('user-uuid-here', 'translator', NOW()),
    ('user-uuid-here', 'bug_hunter', NOW())
ON CONFLICT (user_id, badge_id) DO NOTHING;
```

## Troubleshooting

### Badge Store is Empty

**Problem:** No badges appear in the badge store.

**Solutions:**
1. Check if badges exist:
   ```sql
   SELECT COUNT(*) FROM public.badges WHERE type = 'PURCHASABLE';
   ```
2. Verify badges are available:
   ```sql
   SELECT * FROM public.badges WHERE type = 'PURCHASABLE' AND is_available = TRUE;
   ```
3. Check app logs for errors in `GetAvailableBadgesUseCase`

### Badge Images Not Loading in Release

**Problem:** Badge images load in debug but not in release builds.

**Solution:** Already fixed in `android/proguard-rules.pro`. Ensure you:
1. Rebuild the release APK after the ProGuard changes
2. Verify image URLs are correct in the database
3. Check that images are publicly accessible (if using Supabase Storage)

### Achievement Badges Not Appearing

**Problem:** Achievement badges don't show on profile.

**Solutions:**
1. Verify user has earned badges:
   ```sql
   SELECT * FROM public.user_badges WHERE user_id = 'user-uuid';
   ```
2. Check badge type:
   ```sql
   SELECT b.* FROM public.user_badges ub
   JOIN public.badges b ON ub.badge_id = b.id
   WHERE ub.user_id = 'user-uuid' AND b.type = 'ACHIEVEMENT';
   ```

## Payment Integration

The current system uses manual payment proof submission. To integrate with actual payment processors:

1. **Stripe Integration** - Add Stripe webhook to automatically grant badges
2. **PayPal Integration** - Use PayPal IPN to verify payments
3. **Crypto Payments** - Verify blockchain transactions

Example webhook handler (pseudo-code):
```kotlin
// When payment is confirmed
suspend fun onPaymentConfirmed(userId: String, badgeId: String) {
    badgeRepository.checkAndAwardAchievementBadge(badgeId)
        .onSuccess {
            // Badge granted successfully
            notifyUser(userId, "Badge unlocked!")
        }
}
```

## Next Steps

1. ✅ Run both migration files
2. ✅ Verify badges appear in store
3. ⬜ Upload badge images to Supabase Storage
4. ⬜ Update image URLs in database
5. ⬜ Test purchase flow
6. ⬜ Integrate payment processor
7. ⬜ Set up achievement tracking
8. ⬜ Test in release build

## Support

For issues or questions:
- Check the main `BADGE_SYSTEM_UPDATES.md` file
- Review Supabase logs for RPC function errors
- Verify ProGuard rules are applied in release builds
