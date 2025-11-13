# 🚀 Review Feature - Quick Start Guide

## What's Been Done

✅ **Complete review and badge system implemented**
✅ **Integrated into BookDetailScreen** 
✅ **Koin modules registered** (Android & Desktop)
✅ **Database schema ready** (Supabase)

---

## How to Use

### 1. Build & Run
```bash
./gradlew clean build
./gradlew run  # Desktop
# or
./gradlew installDebug  # Android
```

### 2. Test Book Reviews

1. Open the app
2. Navigate to any book detail screen
3. Scroll down - you'll see a **"Reviews"** section between the book summary and chapter list
4. Click **"Write Review"** button
5. Select stars (1-5) and write your review
6. Click **"Submit"**
7. Your review appears in the list!

### 3. Award Badges (Optional)

To test badges, run this in Supabase SQL Editor:

```sql
-- Award a gold donor badge to current user
SELECT award_badge(
    auth.uid(),  -- Current user
    'donor_gold',
    '{"amount": "25"}'::JSONB
);

-- Award a reviewer badge
SELECT award_badge(
    auth.uid(),
    'reviewer_critic',
    '{"review_count": "10"}'::JSONB
);
```

Then write a review - your badges will appear on your review card!

---

## Available Badges

### 🥉 Donor Badges
- `donor_bronze` - Bronze Supporter ($5+)
- `donor_silver` - Silver Supporter ($10+)
- `donor_gold` - Gold Supporter ($25+)
- `donor_platinum` - Platinum Supporter ($50+)

### 💻 Contributor Badges
- `contributor_translator` - Translator
- `contributor_developer` - Developer
- `contributor_designer` - Designer

### 📚 Reader Badges
- `reader_novice` - Novice Reader (10 chapters)
- `reader_bookworm` - Bookworm (100 chapters)
- `reader_scholar` - Scholar (500 chapters)
- `reader_master` - Reading Master (1000 chapters)

### ✍️ Reviewer Badges
- `reviewer_critic` - Critic (10 reviews)
- `reviewer_expert` - Expert Reviewer (50 reviews)
- `reviewer_master` - Master Critic (100 reviews)

### 🚀 Special Badges
- `special_early_adopter` - Early Adopter
- `special_bug_hunter` - Bug Hunter
- `special_community_hero` - Community Hero

---

## File Locations

### To modify UI:
- **Review cards**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/ReviewCard.kt`
- **Badge display**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/BadgeChip.kt`
- **Write dialog**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/WriteReviewDialog.kt`

### To modify logic:
- **Repository**: `data/src/commonMain/kotlin/ireader/data/review/ReviewRepositoryImpl.kt`
- **Use cases**: `domain/src/commonMain/kotlin/ireader/domain/usecases/review/`

### Integration points:
- **Book reviews**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreen.kt` (line ~140)
- **Koin modules**: 
  - Android: `android/src/main/java/org/ireader/app/MyApplication.kt`
  - Desktop: `desktop/src/main/kotlin/ireader/desktop/Main.kt`

---

## Quick Troubleshooting

### "No definition found for GetBookReviewsUseCase"
- ✅ Fixed! reviewModule is now registered in both Android and Desktop

### Reviews not showing?
- Check Supabase is configured and connected
- Verify user is logged in
- Check console for errors

### Can't submit review?
- Must be logged in
- Rating must be 1-5 stars
- Review text cannot be empty

### Badges not appearing?
- Award badges via Supabase SQL (see above)
- Badges only show on reviews after being awarded

---

## What's Next?

### Optional: Add Chapter Reviews to Reader

The chapter review component is ready but not yet integrated. To add it:

1. Open `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderScreen.kt`
2. Find where you want chapter reviews (e.g., end of chapter content)
3. Add:
```kotlin
ChapterReviewsIntegration(
    bookTitle = book.title,
    chapterName = currentChapter.name,
    modifier = Modifier.padding(16.dp)
)
```

---

## Support

- **Full docs**: See `IMPLEMENTATION_COMPLETE.md`
- **Integration guide**: See `INTEGRATION_EXAMPLE.md`
- **Technical details**: See `REVIEW_FEATURE_INTEGRATION.md`

---

**That's it! The review system is ready to use! 🎉**
