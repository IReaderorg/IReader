# Quote Feature Redesign - Implementation Plan

**Date:** 2025-02-26  
**Design Doc:** [2025-02-26-quote-feature-redesign.md](./2025-02-26-quote-feature-redesign.md)  
**Estimated Time:** 9-14 hours

## Overview

This plan breaks down the quote feature redesign into bite-sized tasks following TDD principles. Each task should take 2-5 minutes to complete.

## Phase 1: Setup & Infrastructure (2-3 hours)

### Task 1.1: Add Discord Webhook Configuration
**Time:** 5 min  
**Files:**
- `domain/build.gradle.kts`

**Steps:**
1. Add `DISCORD_QUOTE_WEBHOOK_URL` to BuildConfig
2. Add environment variable support
3. Add default value for development

**Acceptance:**
- BuildConfig contains `DISCORD_QUOTE_WEBHOOK_URL`
- Can access via `BuildConfig.DISCORD_QUOTE_WEBHOOK_URL`

### Task 1.2: Create Discord Quote Repository Interface
**Time:** 3 min  
**Files:**
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/DiscordQuoteRepository.kt`

**Steps:**
1. Create interface with `submitQuote()` method
2. Define parameters: quote, style, username
3. Return `Result<Unit>`

**Acceptance:**
- Interface compiles
- Method signature matches design

### Task 1.3: Implement Discord Quote Repository
**Time:** 15 min  
**Files:**
- `data/src/commonMain/kotlin/ireader/data/quote/DiscordQuoteRepositoryImpl.kt`

**Steps:**
1. Create implementation class
2. Inject HttpClient and QuoteCardGenerator
3. Implement `submitQuote()` method
4. Generate quote card image
5. Build Discord webhook payload
6. Send multipart request with image attachment
7. Handle errors and rate limiting

**Acceptance:**
- Repository compiles
- Can send quote to Discord webhook
- Image attachment works

### Task 1.4: Update DI Module
**Time:** 5 min  
**Files:**
- `di/src/commonMain/kotlin/ireader/di/QuoteModule.kt`

**Steps:**
1. Add `DiscordQuoteRepository` to DI
2. Provide webhook URL from BuildConfig
3. Remove Supabase quote repository binding

**Acceptance:**
- DI module compiles
- Can inject `DiscordQuoteRepository`

### Task 1.5: Add GitHub Actions Secret
**Time:** 2 min  
**Files:**
- `.github/workflows/Release.yaml`
- `.github/workflows/Preview.yaml`

**Steps:**
1. Add `DISCORD_QUOTE_WEBHOOK_URL` to env variables
2. Reference GitHub secret

**Acceptance:**
- Workflow files updated
- Secret placeholder added

---

## Phase 2: UI Components (3-4 hours)

### Task 2.1: Create Quote Style Selector Screen
**Time:** 20 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteStyleSelectorScreen.kt`

**Steps:**
1. Create composable function
2. Add HorizontalPager for 9 styles
3. Show sample quote on each style
4. Add style name overlay
5. Add tap gesture to select style
6. Add back button

**Acceptance:**
- Screen displays all 9 styles
- Can swipe between styles
- Tap selects style and navigates

### Task 2.2: Create Quote Live Preview Component
**Time:** 15 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteLivePreview.kt`

**Steps:**
1. Create composable function
2. Render quote text on styled background
3. Apply selected style gradient/colors
4. Show book title and author
5. Match existing quote card design

**Acceptance:**
- Preview renders correctly
- Updates in real-time as text changes
- Matches quote card visual style

### Task 2.3: Create Quote Story Editor Screen
**Time:** 25 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteStoryEditorScreen.kt`

**Steps:**
1. Create composable function
2. Add styled background (selected style)
3. Add semi-transparent overlay
4. Add multi-line text input for quote
5. Add book title input
6. Add author input
7. Add character counter
8. Add "Save Locally" button
9. Add "Share to Discord" button
10. Integrate live preview

**Acceptance:**
- Screen displays correctly
- All inputs work
- Live preview updates
- Buttons trigger callbacks

### Task 2.4: Create Quote Story Editor ViewModel
**Time:** 20 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteStoryEditorViewModel.kt`

**Steps:**
1. Create ViewModel class
2. Add state properties (quoteText, bookTitle, author, selectedStyle)
3. Add loading states (isSaving, isSharing)
4. Implement `saveLocally()` method
5. Implement `shareToDiscord()` method
6. Add validation logic
7. Add character count property

**Acceptance:**
- ViewModel compiles
- State management works
- Save/share methods functional

### Task 2.5: Implement Save Locally Logic
**Time:** 10 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteStoryEditorViewModel.kt`

**Steps:**
1. Call `LocalQuoteUseCases.saveQuote()`
2. Pass quote data
3. Handle success/error
4. Show success message
5. Navigate back

**Acceptance:**
- Quote saves to SQLDelight
- Success message shown
- Navigation works

### Task 2.6: Implement Share to Discord Logic
**Time:** 15 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteStoryEditorViewModel.kt`

**Steps:**
1. Validate quote (min 10 chars, book title required)
2. Save locally first
3. Generate quote card image
4. Call `DiscordQuoteRepository.submitQuote()`
5. Handle success/error
6. Show success message
7. Navigate back

**Acceptance:**
- Quote saves locally
- Quote sends to Discord
- Image attachment works
- Error handling works

### Task 2.7: Create Success State UI
**Time:** 10 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/quote/QuoteSuccessDialog.kt`

**Steps:**
1. Create dialog composable
2. Add success animation
3. Add "View in My Quotes" button
4. Add "Create Another" button
5. Add auto-dismiss after 3 seconds

**Acceptance:**
- Dialog displays correctly
- Buttons work
- Auto-dismiss works

---

## Phase 3: Integration (2-3 hours)

### Task 3.1: Update Reading Buddy Screen Navigation
**Time:** 15 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/ReadingBuddyScreen.kt`

**Steps:**
1. Remove "Quotes" tab (community quotes)
2. Remove "Submit" tab
3. Keep "Buddy" and "My Quotes" tabs
4. Add Floating Action Button for "Create Quote"
5. Wire FAB to navigate to style selector

**Acceptance:**
- Only 2 tabs remain (Buddy, My Quotes)
- FAB displays correctly
- FAB navigates to style selector

### Task 3.2: Update Reading Buddy ViewModel
**Time:** 10 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readingbuddy/ReadingBuddyViewModel.kt`

**Steps:**
1. Remove `loadApprovedQuotes()` method
2. Remove `submitQuote()` method
3. Remove `toggleLike()` method
4. Remove Supabase quote state
5. Keep local quote functionality

**Acceptance:**
- ViewModel compiles
- No Supabase references
- Local quotes still work

### Task 3.3: Add Navigation Routes
**Time:** 5 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/core/NavigationRoutes.kt`

**Steps:**
1. Add `quoteStyleSelector` route
2. Add `quoteStoryEditor` route with style parameter
3. Remove `quotes` route
4. Remove `submitQuote` route

**Acceptance:**
- New routes added
- Old routes removed
- Routes compile

### Task 3.4: Wire Up Navigation
**Time:** 10 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/QuoteStyleSelectorScreenSpec.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/QuoteStoryEditorScreenSpec.kt`

**Steps:**
1. Create screen specs for new screens
2. Wire up navigation callbacks
3. Pass parameters between screens
4. Handle back navigation

**Acceptance:**
- Navigation works end-to-end
- Parameters pass correctly
- Back button works

---

## Phase 4: Cleanup (1-2 hours)

### Task 4.1: Delete Supabase Quote Repository
**Time:** 2 min  
**Files:**
- `data/src/commonMain/kotlin/ireader/data/quote/SupabaseQuoteRepository.kt`

**Steps:**
1. Delete file
2. Remove from git

**Acceptance:**
- File deleted
- No compilation errors

### Task 4.2: Delete Community Quotes Screen
**Time:** 2 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/QuotesScreen.kt`

**Steps:**
1. Delete file
2. Remove from git

**Acceptance:**
- File deleted
- No compilation errors

### Task 4.3: Delete Submit Quote Screen
**Time:** 2 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/SubmitQuoteScreen.kt`

**Steps:**
1. Delete file
2. Remove from git

**Acceptance:**
- File deleted
- No compilation errors

### Task 4.4: Delete Quote Repository Interface
**Time:** 2 min  
**Files:**
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/QuoteRepository.kt`

**Steps:**
1. Delete file
2. Remove from git

**Acceptance:**
- File deleted
- No compilation errors

### Task 4.5: Delete Quote Screen Spec
**Time:** 2 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/QuotesScreenSpec.kt`

**Steps:**
1. Delete file
2. Remove from git

**Acceptance:**
- File deleted
- No compilation errors

### Task 4.6: Delete Quote Model (Supabase)
**Time:** 2 min  
**Files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/quote/Quote.kt`

**Steps:**
1. Delete `Quote` data class (keep `LocalQuote`)
2. Delete `QuoteStatus` enum
3. Delete `DailyQuote` data class
4. Keep `QuoteCardStyle` enum
5. Keep `SubmitQuoteRequest` (might be useful)

**Acceptance:**
- Supabase models deleted
- Local models preserved
- No compilation errors

### Task 4.7: Update DI Module (Final Cleanup)
**Time:** 5 min  
**Files:**
- `di/src/commonMain/kotlin/ireader/di/QuoteModule.kt`

**Steps:**
1. Remove `QuoteRepository` binding
2. Remove `NoOpQuoteRepository` binding
3. Remove `SupabaseQuoteRepository` binding
4. Keep `LocalQuoteRepository` binding
5. Keep `DiscordQuoteRepository` binding

**Acceptance:**
- DI module compiles
- Only local and Discord repositories remain

### Task 4.8: Update Reading Hub ViewModel
**Time:** 5 min  
**Files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/ReadingHubViewModel.kt`

**Steps:**
1. Remove quote-related state
2. Remove quote-related methods
3. Keep other Reading Hub functionality

**Acceptance:**
- ViewModel compiles
- No quote references

### Task 4.9: Remove Quote Use Cases (Supabase)
**Time:** 3 min  
**Files:**
- `domain/src/commonMain/kotlin/ireader/domain/usecases/quote/QuoteUseCases.kt`

**Steps:**
1. Remove Supabase-related use cases
2. Keep local quote use cases
3. Add Discord submission use case if needed

**Acceptance:**
- Use cases compile
- Only local and Discord use cases remain

---

## Phase 5: Testing (1-2 hours)

### Task 5.1: Test Quote Creation Flow
**Time:** 10 min  
**Manual Testing:**
1. Open Reading Buddy
2. Tap "Create Quote" FAB
3. Swipe through styles
4. Select a style
5. Enter quote text
6. Enter book title and author
7. Verify live preview updates
8. Tap "Save Locally"
9. Verify success message
10. Verify quote appears in "My Quotes"

**Acceptance:**
- All steps work without errors
- Quote saves correctly

### Task 5.2: Test Discord Submission
**Time:** 10 min  
**Manual Testing:**
1. Create a quote
2. Tap "Share to Discord"
3. Verify success message
4. Check Discord channel for message
5. Verify quote card image attached
6. Verify embed fields correct

**Acceptance:**
- Quote appears in Discord
- Image attachment works
- Embed formatting correct

### Task 5.3: Test Local Quote Management
**Time:** 10 min  
**Manual Testing:**
1. Open "My Quotes" tab
2. Verify all local quotes display
3. Search for a quote
4. Tap a quote to view details
5. Share a quote as image
6. Delete a quote

**Acceptance:**
- All local quote features work
- No regressions

### Task 5.4: Test Navigation Flow
**Time:** 5 min  
**Manual Testing:**
1. Navigate to style selector
2. Back button works
3. Navigate to editor
4. Back button works
5. Success dialog dismisses
6. "View in My Quotes" navigates correctly
7. "Create Another" navigates correctly

**Acceptance:**
- All navigation works
- No crashes

### Task 5.5: Test Error Handling
**Time:** 10 min  
**Manual Testing:**
1. Try to share with empty quote
2. Try to share with no book title
3. Disable internet and try to share
4. Test with very long quote text
5. Test with special characters

**Acceptance:**
- Appropriate error messages shown
- No crashes
- Graceful degradation

### Task 5.6: Test Platform Compatibility
**Time:** 15 min  
**Manual Testing:**
1. Test on Android
2. Test on iOS (if available)
3. Test on Desktop (if available)
4. Verify quote card generation works on all platforms
5. Verify Discord submission works on all platforms

**Acceptance:**
- Works on all target platforms
- No platform-specific issues

---

## Rollout Plan

### Pre-Release
1. Add `DISCORD_QUOTE_WEBHOOK_URL` to GitHub secrets
2. Test Discord webhook with staging channel
3. Verify all tests pass
4. Code review

### Release
1. Merge to main branch
2. Create release build
3. Deploy to production
4. Monitor Discord channel for submissions
5. Monitor crash reports

### Post-Release
1. Gather user feedback
2. Monitor Discord webhook usage
3. Fix any reported issues
4. Plan future enhancements

---

## Success Criteria Checklist

- [ ] Instagram story-like UI implemented
- [ ] Discord webhook integration working
- [ ] Local quote storage preserved
- [ ] Quote card sharing functional
- [ ] All Supabase code removed
- [ ] Navigation updated correctly
- [ ] No crashes or errors in quote flow
- [ ] All manual tests pass
- [ ] Works on all platforms

---

## Notes

- Follow TDD principles where applicable
- Use existing quote card generation code
- Reuse Discord webhook pattern from character art
- Keep local quote functionality unchanged
- Test thoroughly before removing Supabase code

---

## Estimated Timeline

| Phase | Tasks | Time |
|-------|-------|------|
| Phase 1: Setup | 5 tasks | 2-3 hours |
| Phase 2: UI | 7 tasks | 3-4 hours |
| Phase 3: Integration | 4 tasks | 2-3 hours |
| Phase 4: Cleanup | 9 tasks | 1-2 hours |
| Phase 5: Testing | 6 tasks | 1-2 hours |
| **Total** | **31 tasks** | **9-14 hours** |

---

## Dependencies

- Discord webhook URL (from user)
- Existing quote card generation code
- Existing local quote storage
- HttpClient for Discord API
- Navigation framework

---

## Risk Mitigation

1. **Discord Rate Limiting**: Implement exponential backoff and queue
2. **Image Generation Performance**: Use caching and optimize rendering
3. **Offline Support**: Save locally first, queue Discord submissions
4. **Platform Compatibility**: Test on all platforms before release
