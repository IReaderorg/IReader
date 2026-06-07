# UI Wireup Fix Plan - Complete

## Problem
New UI composables created during the comprehensive app redesign are NOT being used by the app. The ScreenSpecs (entry points for navigation) are pointing to old composables.

## Root Cause
ScreenSpec files in `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/` determine which composable is rendered. Several specs were still pointing to old composables.

## Completed Fixes

### 1. LeaderboardScreenSpec.kt ✅
- **Was:** Using `CombinedLeaderboardScreen` (old, no level system, with donation tab)
- **Now:** Uses `LeaderboardScreen` (new, with level system, XP, level titles)
- Removed `DonationLeaderboardViewModel` dependency

### 2. Dead Code Removal ✅
- Deleted `CombinedLeaderboardScreen.kt` - old leaderboard, no longer referenced
- Deleted `ReadingLeaderboardContent.kt` - only used by CombinedLeaderboardScreen

### 3. PopularBooksScreenSpec.kt ✅
- Already correctly uses new `PopularBooksScreen` with cover, description, source badge

## New Screens That Need Wiring Up

### 4. RewardScreen (Initiative 3)
- Domain models exist: `Reward.kt`, `XpEvent.kt`
- Use cases exist: `RewardEngineUseCase.kt`, `GetUserRewardsUseCase.kt`
- **Missing:** `presentation/.../ui/reward/RewardScreen.kt` and `RewardViewModel.kt`
- **Action:** Create RewardScreen and wire up via ScreenSpec

### 5. Spirit Stone System (Initiative 7)
- Domain models exist: `SpiritStoneBalance.kt`, `ShopItem.kt`
- **Missing:** `presentation/.../ui/spiritstone/SpiritStoneScreen.kt`
- **Action:** Create SpiritStoneScreen and wire up via ScreenSpec

### 6. User Title System
- Domain models exist: `UserTitle.kt`, `TitleEffect.kt`
- **Missing:** `presentation/.../ui/title/UserTitleScreen.kt`
- **Action:** Create UserTitleScreen and wire up via ScreenSpec

### 7. SourceInstallDialog (Initiative 4)
- Created at `presentation/.../ui/popular/SourceInstallDialog.kt`
- **Needs:** Integration into PopularBooksViewModel flow

## Dead Code to Remove
- `CombinedLeaderboardScreen.kt` - DELETED ✅
- `ReadingLeaderboardContent.kt` - DELETED ✅
- NFT-related files already removed in previous session

## Execution Order
1. ✅ Update LeaderboardScreenSpec.kt to use new LeaderboardScreen
2. ✅ Remove dead code (CombinedLeaderboardScreen.kt, ReadingLeaderboardContent.kt)
3. Create RewardScreen.kt and RewardViewModel.kt
4. Create SpiritStoneScreen.kt
5. Create UserTitleScreen.kt
6. Create ScreenSpecs for each new screen
7. Add navigation routes
8. Integrate SourceInstallDialog into PopularBooksViewModel
9. Final compile and verify
