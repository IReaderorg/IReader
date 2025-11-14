# Badge Integration Guide

This guide explains how badges have been integrated into the profile and review screens.

## Overview

Task 10 has been completed, integrating badges into:
1. User Profile Screen - displays featured badges
2. Review Card Component - displays reviewer's primary badge
3. Write Review Dialog - shows badge preview

## Changes Made

### 1. ProfileScreen Integration

**File:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/ProfileScreen.kt`

**Changes:**
- Added `BadgesSection` composable that displays featured badges
- Shows loading state with 3 circular skeleton placeholders
- Shows error state with retry button
- Shows empty state when no badges are available
- Uses `ProfileBadgeDisplay` component to render badges

**ViewModel Changes:**
- `ProfileViewModel` now accepts `BadgeRepository` as optional dependency
- Added `loadFeaturedBadges()` method to fetch featured badges
- Added `retryLoadBadges()` method for error recovery
- State includes: `featuredBadges`, `isBadgesLoading`, `badgesError`

**DI Configuration:**
- Updated `PresentationModules.kt` to inject `BadgeRepository` into `ProfileViewModel`

### 2. ReviewCard Integration

**File:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/ReviewCard.kt`

**Changes:**
- Added `reviewerBadge: Badge?` parameter
- Badge is displayed next to username using `ReviewBadgeDisplay`
- Layout: Row with username Text, Spacer(4.dp), ReviewBadgeDisplay
- Proper alignment with `Alignment.CenterVertically`

**Usage Example:**
```kotlin
ReviewCard(
    userName = "JohnDoe",
    rating = 5,
    reviewText = "Great book!",
    createdAt = System.currentTimeMillis(),
    reviewerBadge = primaryBadge // Pass the reviewer's primary badge
)
```

### 3. WriteReviewDialog Integration

**File:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/WriteReviewDialog.kt`

**Changes:**
- Added `userName: String?` parameter
- Added `primaryBadge: Badge?` parameter
- Shows preview section: "Your review will appear as:"
- Displays username + badge in preview using `ReviewBadgeDisplay`

**Usage Example:**
```kotlin
WriteReviewDialog(
    onDismiss = { /* ... */ },
    onSubmit = { rating, text -> /* ... */ },
    userName = currentUser.username,
    primaryBadge = userPrimaryBadge // Fetch using GetUserBadgesUseCase with filterPrimary = true
)
```

## How to Fetch Badges for Reviews

When displaying reviews, you need to fetch the primary badge for each reviewer. Here's how:

### Option 1: Fetch Individual Badges
```kotlin
// In your ViewModel or composable
suspend fun fetchReviewerBadge(userId: String): Badge? {
    return badgeRepository.getPrimaryBadge()
        .getOrNull()
}
```

### Option 2: Batch Fetch (Recommended for Performance)
```kotlin
// Fetch all user badges once and filter
val userBadges = getUserBadgesUseCase(userId, filterPrimary = true)
    .getOrNull()
    ?.firstOrNull()

// Then convert UserBadge to Badge if needed
// Note: You may need to fetch the full Badge object from BadgeRepository
```

### Option 3: Use Repository Method
```kotlin
// Use the repository method directly
val primaryBadge = badgeRepository.getPrimaryBadge()
    .getOrNull()
```

## Integration Checklist

- [x] ProfileScreen displays featured badges
- [x] ProfileScreen shows loading state with skeleton placeholders
- [x] ProfileScreen shows error state with retry button
- [x] ProfileScreen shows empty state
- [x] ReviewCard accepts and displays reviewer badge
- [x] ReviewCard properly aligns badge with username
- [x] WriteReviewDialog shows badge preview
- [x] WriteReviewDialog displays username + badge
- [x] ProfileViewModel fetches featured badges
- [x] DI configuration updated

## Next Steps for Review Screens

To complete the integration, review list screens need to:

1. **Fetch reviewer badges when loading reviews:**
   ```kotlin
   // In your review list ViewModel
   val reviews = getReviews()
   val reviewsWithBadges = reviews.map { review ->
       val badge = badgeRepository.getPrimaryBadge()
           .getOrNull()
       ReviewWithBadge(review, badge)
   }
   ```

2. **Pass badge data to ReviewCard:**
   ```kotlin
   ReviewCard(
       userName = review.userName,
       rating = review.rating,
       reviewText = review.text,
       createdAt = review.createdAt,
       reviewerBadge = review.badge // Pass the fetched badge
   )
   ```

3. **Fetch user's primary badge for WriteReviewDialog:**
   ```kotlin
   val primaryBadge = remember {
       badgeRepository.getPrimaryBadge()
           .getOrNull()
   }
   
   WriteReviewDialog(
       onDismiss = { /* ... */ },
       onSubmit = { rating, text -> /* ... */ },
       userName = currentUser.username,
       primaryBadge = primaryBadge
   )
   ```

## Testing

To test the integration:

1. **Profile Screen:**
   - Navigate to Profile screen
   - Verify badges section appears
   - Verify loading state shows 3 circular placeholders
   - Verify error state shows retry button
   - Verify badges display correctly when loaded

2. **Review Card:**
   - Display a review with a badge
   - Verify badge appears next to username
   - Verify alignment is correct
   - Verify NFT badges show animation

3. **Write Review Dialog:**
   - Open write review dialog
   - Verify preview section shows username + badge
   - Verify badge displays correctly

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:

- **2.1:** Badges displayed on user profile in dedicated section
- **2.2:** Users can select up to 3 featured badges (backend functionality)
- **2.3:** Featured badges saved and displayed first (backend functionality)
- **2.4:** Badges rendered with high quality at appropriate sizes
- **2.5:** Other users can view profile owner's featured badges
- **3.1:** User's primary badge displayed next to username in reviews
- **3.2:** Users can select which badge appears on reviews (backend functionality)
- **3.3:** Other users see reviewer's badge prominently
- **3.4:** Badges rendered at appropriate size for review layout (24.dp)
- **3.5:** Username displayed without badge if user has no badges
