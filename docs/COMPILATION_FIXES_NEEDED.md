# Compilation Fixes Needed

## Overview
The quote feature redesign removed the Supabase `Quote` model and related functionality. Many files still reference the old model and need to be updated or deleted.

## Files That Need Deletion (Old Supabase UI)
These files are part of the old Supabase quote system and should be deleted:

1. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/QuotesScreenSpec.kt` - Old quotes screen spec
2. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/SubmitQuoteScreenSpec.kt` - Old submit quote spec
3. `presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/ShareQuoteCard.kt` - Old share functionality
4. `presentation/src/desktopMain/kotlin/ireader/presentation/ui/readingbuddy/ShareQuoteCard.desktop.kt` - Desktop share
5. `presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/components/QuoteCard.kt` - Old Supabase quote card

## Files That Need Major Updates

### 1. ReadingBuddyScreen.kt
**Issues:**
- References old `Quote` model
- Has old quote submission UI
- References `dailyQuote`, `approvedQuotes`, `isSubmitting`

**Fix:**
- Remove all Supabase quote-related UI components
- Keep only "Buddy" and "My Quotes" tabs
- Remove quote submission dialog

### 2. ReadingBuddyScreenSpec.kt
**Issues:**
- References old `Quote` model
- Missing `onCreateQuote` parameter

**Fix:**
- Update to match new ReadingBuddyScreen signature
- Remove Quote references

### 3. ReadingHubScreenSpec.kt
**Issues:**
- Has `onShareQuote` and `onNavigateToQuotes` parameters that no longer exist

**Fix:**
- Remove these parameters to match updated ReadingHubScreen

### 4. QuoteCreationScreen.kt & QuoteCreationViewModel.kt
**Issues:**
- References `shareQuoteToCommunity`, `maxCommunityLength`, `minCommunityLength`
- Has truncation dialog logic
- `shareQuoteToDiscord` receives wrong parameter type (Long instead of LocalQuote)

**Fix:**
- Remove community sharing logic
- Fix `shareQuoteToDiscord` call to pass LocalQuote object
- Remove truncation dialog

### 5. QuoteStoryEditorViewModel.kt
**Issues:**
- `shareQuoteToDiscord` receives wrong parameter type (Long instead of LocalQuote)

**Fix:**
- Fix parameter type in shareQuoteToDiscord call

### 6. MyQuotesTab.kt
**Issues:**
- References `shareQuoteToCommunity` method

**Fix:**
- Update to use `shareQuoteToDiscord` instead

### 7. ScreenModelModule.kt
**Issues:**
- Destructuring error for QuoteStoryEditorViewModel parameters

**Fix:**
- Check parameter count matches the factory function

## Quick Fix Commands

```bash
# Delete old Supabase quote files
rm presentation/src/commonMain/kotlin/ireader/presentation/core/ui/QuotesScreenSpec.kt
rm presentation/src/commonMain/kotlin/ireader/presentation/core/ui/SubmitQuoteScreenSpec.kt
rm presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/ShareQuoteCard.kt
rm presentation/src/desktopMain/kotlin/ireader/presentation/ui/readingbuddy/ShareQuoteCard.desktop.kt
rm presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/components/QuoteCard.kt
```

## Priority Order

1. **Delete obsolete files** (listed above)
2. **Fix QuoteCreationViewModel & QuoteStoryEditorViewModel** - Wrong parameter types
3. **Fix ReadingBuddyScreen** - Remove Supabase quote UI
4. **Fix Spec files** - Update to match new signatures
5. **Fix ScreenModelModule** - Parameter destructuring

## Notes

- The new system uses `LocalQuote` model (not `Quote`)
- Discord sharing is handled by `DiscordQuoteRepository`
- No more community quotes viewing
- No more quote submission to Supabase
- "My Quotes" is now in Community Hub, not Reading Hub
