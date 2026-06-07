# UI Wireup Fix Plan

## Problem
New UI composables (LeaderboardScreen with level system, PopularBooksScreen with cover/description/source badge) exist in code but are NOT being used by the app. The ScreenSpecs are pointing to old composables.

## Root Cause
The `ScreenSpec` files in `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/` are the entry points for navigation. They determine which composable is actually rendered. Several specs are still pointing to old composables.

## Files to Modify

### 1. LeaderboardScreenSpec.kt
**Current:** Uses `CombinedLeaderboardScreen` (old, no level system)
**Fix:** Update to use new `LeaderboardScreen` with level system
- Remove `DonationLeaderboardViewModel` dependency
- Replace `CombinedLeaderboardScreen(...)` with `LeaderboardScreen(vm = readingViewModel, onBack = onBack)`

### 2. Check all other ScreenSpecs for similar issues
Need to verify each spec is using the latest composable:
- `PopularBooksScreenSpec.kt` - Already uses new `PopularBooksScreen` ✓
- `LeaderboardScreenSpec.kt` - Uses OLD `CombinedLeaderboardScreen` ✗
- Check all other specs in the directory

### 3. Remove dead code
After updating specs, remove unused old composables:
- `CombinedLeaderboardScreen.kt` (if no longer referenced)
- `ReadingLeaderboardContent.kt` (if replaced by new LeaderboardScreen)
- Any other unreferenced files

## Execution Order
1. Update `LeaderboardScreenSpec.kt` to use new `LeaderboardScreen`
2. Run gradle compile to verify
3. Check all other ScreenSpecs for similar issues
4. Remove dead code (old composables no longer referenced)
5. Run final gradle compile
