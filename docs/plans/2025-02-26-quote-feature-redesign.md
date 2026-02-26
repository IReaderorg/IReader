# Quote Feature Redesign - Instagram Story Style with Discord Integration

**Date:** 2025-02-26  
**Status:** Approved  
**Author:** AI Assistant

## Overview

Redesign the quote feature to provide an Instagram story-like creation experience with Discord webhook submission, while preserving local quote storage for personal collections.

## Goals

1. Create Instagram story-style quote creation UI
2. Integrate Discord webhook for community quote sharing
3. Remove Supabase quote storage and community viewing screens
4. Preserve local quote storage and management
5. Maintain quote card image generation and sharing

## Current State Analysis

### Existing Architecture
- **Local Quotes**: SQLDelight storage, unlimited length, optional context backup
- **Community Quotes**: Supabase storage, 10-1000 characters, auto-approved
- **Quote Creation**: Form-based screen with text inputs
- **Quote Viewing**: Instagram Reels-style vertical pager (Supabase quotes)
- **Quote Sharing**: Platform-specific image generation with 9 visual styles

### Problems with Current Design
- Supabase dependency for community features
- Form-based creation feels dated
- Community quote viewing is disconnected from Discord
- No Discord integration (unlike character art feature)

## Proposed Solution

### Architecture Changes

```
┌─────────────────────────────────────────────────┐
│           Quote Feature Architecture            │
├─────────────────────────────────────────────────┤
│                                                 │
│  User Creates Quote                             │
│         ↓                                       │
│  Story Editor UI (Instagram-style)              │
│         ↓                                       │
│  User Choice:                                   │
│  ├─→ Save Locally → SQLDelight                  │
│  └─→ Share to Discord → Discord Webhook +       │
│                         SQLDelight              │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Components to Keep
✅ Local quote storage (SQLDelight)  
✅ Quote card image generation (9 styles)  
✅ "My Quotes" tab (personal collection)  
✅ Quote sharing functionality  
✅ Reading Buddy integration (shows local quotes)

### Components to Remove
❌ Supabase quote repository  
❌ Community quotes viewing screens  
❌ Quote approval workflow  
❌ Community quote browsing  
❌ Daily quote from Supabase

## Detailed Design

### 1. Quote Creation Flow (Story Editor Style)

#### Screen 1: Style Selector
**Purpose:** Let users choose visual style for their quote

**UI Components:**
- Horizontal pager (full-screen)
- 9 quote card styles as pages
- Sample quote text on each style
- Style name overlay
- "Tap to use this style" hint
- Back button

**Interaction:**
- Swipe left/right to browse styles
- Tap anywhere on style to select
- Transitions to Quote Editor with selected style

**Implementation:**
```kotlin
@Composable
fun QuoteStyleSelectorScreen(
    onStyleSelected: (QuoteCardStyle) -> Unit,
    onBack: () -> Unit
)
```

#### Screen 2: Quote Editor
**Purpose:** Write quote with live preview on styled background

**UI Components:**
- Selected style as full-screen background
- Semi-transparent overlay with:
  - Multi-line text input (quote text)
  - Book title input (optional)
  - Author input (optional)
  - Character counter
  - "Save Locally" button
  - "Share to Discord" button (primary)

**Live Preview:**
- Quote text appears on styled card as user types
- Real-time rendering with selected style
- Character limit indicator (no hard limit for local, warning for Discord)

**Validation:**
- Minimum 10 characters for Discord submission
- No maximum for local save
- Book title required for Discord submission

**Implementation:**
```kotlin
@Composable
fun QuoteStoryEditorScreen(
    selectedStyle: QuoteCardStyle,
    onSaveLocally: (QuoteData) -> Unit,
    onShareToDiscord: (QuoteData) -> Unit,
    onBack: () -> Unit
)
```

#### Screen 3: Success State
**Purpose:** Confirm successful save/share

**UI Components:**
- Success message with animation
- "View in My Quotes" button
- "Create Another" button
- Auto-dismiss after 3 seconds

### 2. Discord Integration

#### Discord Webhook Implementation

**Repository:**
```kotlin
class DiscordQuoteRepository(
    private val webhookUrl: String,
    private val httpClient: HttpClient,
    private val quoteCardGenerator: QuoteCardGenerator
) {
    suspend fun submitQuote(
        quote: LocalQuote,
        style: QuoteCardStyle,
        username: String
    ): Result<Unit>
}
```

**Webhook Message Format:**
```json
{
  "embeds": [{
    "title": "📚 New Quote Shared",
    "description": "\"Quote text here...\"",
    "fields": [
      {"name": "Book", "value": "Book Title"},
      {"name": "Author", "value": "Author Name"},
      {"name": "Shared by", "value": "@username"}
    ],
    "color": 5814783,
    "timestamp": "2024-01-01T12:00:00Z",
    "footer": {
      "text": "IReader Community"
    }
  }]
}
```

**Image Attachment:**
- Generate quote card image on-device using existing `QuoteCardSharer`
- Convert to PNG bytes
- Attach to Discord webhook as multipart form data
- Filename: `quote_${timestamp}.png`

**Configuration:**
- Add `DISCORD_QUOTE_WEBHOOK_URL` to BuildConfig
- Environment variable: `DISCORD_QUOTE_WEBHOOK_URL`
- Add to GitHub Actions secrets

### 3. Navigation Changes

#### Remove Routes
```kotlin
// Delete these navigation routes
object NavigationRoutes {
    const val quotes = "quotes" // Community quotes screen
    const val submitQuote = "submit_quote" // Old form-based submission
}
```

#### Add Routes
```kotlin
object NavigationRoutes {
    const val quoteStyleSelector = "quote_style_selector"
    const val quoteStoryEditor = "quote_story_editor/{styleId}"
}
```

#### Update Reading Buddy Screen
**Before:**
- Tabs: Buddy, Daily Quote, Quotes (community), Submit

**After:**
- Tabs: Buddy, My Quotes (local)
- Floating Action Button: "Create Quote" → Opens Style Selector

### 4. Data Models

#### Keep Existing
```kotlin
// domain/src/commonMain/kotlin/ireader/domain/models/quote/LocalQuote.kt
data class LocalQuote(
    val id: Long = 0,
    val text: String,
    val bookId: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterNumber: Int? = null,
    val author: String? = null,
    val createdAt: Long = currentTimeToLong(),
    val hasContextBackup: Boolean = false
)
```

#### Remove
```kotlin
// Delete domain/src/commonMain/kotlin/ireader/domain/models/quote/Quote.kt
// Delete domain/src/commonMain/kotlin/ireader/domain/data/repository/QuoteRepository.kt
```

### 5. ViewModel Architecture

#### QuoteStoryEditorViewModel
```kotlin
class QuoteStoryEditorViewModel(
    private val localQuoteUseCases: LocalQuoteUseCases,
    private val discordQuoteRepository: DiscordQuoteRepository,
    private val quoteCardGenerator: QuoteCardGenerator,
    private val getCurrentUser: suspend () -> User?
) : BaseViewModel() {
    
    var quoteText by mutableStateOf("")
    var bookTitle by mutableStateOf("")
    var author by mutableStateOf("")
    var selectedStyle by mutableStateOf(QuoteCardStyle.GRADIENT_SUNSET)
    
    var isSaving by mutableStateOf(false)
    var isSharing by mutableStateOf(false)
    
    fun saveLocally(onSuccess: () -> Unit)
    fun shareToDiscord(onSuccess: () -> Unit)
    
    val characterCount: Int get() = quoteText.length
    val canShareToDiscord: Boolean get() = 
        quoteText.length >= 10 && bookTitle.isNotBlank()
}
```

### 6. UI Components

#### QuoteStylePreview
```kotlin
@Composable
fun QuoteStylePreview(
    style: QuoteCardStyle,
    sampleQuote: String = "The only way to do great work is to love what you do.",
    sampleBook: String = "Sample Book",
    sampleAuthor: String = "Author Name",
    modifier: Modifier = Modifier
)
```

#### QuoteLivePreview
```kotlin
@Composable
fun QuoteLivePreview(
    quoteText: String,
    bookTitle: String,
    author: String,
    style: QuoteCardStyle,
    modifier: Modifier = Modifier
)
```

## Implementation Plan

### Phase 1: Setup & Infrastructure (2-3 hours)
1. Create Discord webhook configuration
2. Create `DiscordQuoteRepository`
3. Add Discord webhook URL to BuildConfig
4. Update DI module

### Phase 2: UI Components (3-4 hours)
1. Create `QuoteStyleSelectorScreen`
2. Create `QuoteStoryEditorScreen`
3. Create `QuoteStoryEditorViewModel`
4. Implement live preview rendering
5. Add success state UI

### Phase 3: Integration (2-3 hours)
1. Update Reading Buddy navigation
2. Remove community quotes tab
3. Add "Create Quote" FAB
4. Wire up new screens to navigation

### Phase 4: Cleanup (1-2 hours)
1. Delete Supabase quote repository
2. Delete community quotes screens
3. Delete old submit quote screen
4. Remove unused navigation routes
5. Update DI modules

### Phase 5: Testing (1-2 hours)
1. Test quote creation flow
2. Test Discord webhook submission
3. Test local save functionality
4. Test quote card generation
5. Test navigation flow

**Total Estimated Time:** 9-14 hours

## Technical Considerations

### Quote Card Generation
- Reuse existing `QuoteCardSharer` platform implementations
- Generate image before Discord submission
- Support all 9 existing styles
- Maintain image quality (1080x1350px)

### Discord Rate Limiting
- Implement exponential backoff
- Queue submissions if rate limited
- Show user-friendly error messages
- Retry failed submissions

### Offline Support
- Save locally first, then attempt Discord submission
- Queue Discord submissions for later if offline
- Show sync status in UI

### Platform Compatibility
- Android: Full support
- iOS: Full support
- Desktop: Full support (clipboard for Discord URL)

## Migration Strategy

### User Data
- No migration needed (local quotes unchanged)
- Community quotes from Supabase will no longer be accessible
- Users can export their community quotes before migration (optional)

### Backward Compatibility
- Remove Supabase code completely
- No backward compatibility needed (breaking change)
- Update app version to indicate major change

## Success Metrics

1. ✅ Instagram story-like UI implemented
2. ✅ Discord webhook integration working
3. ✅ Local quote storage preserved
4. ✅ Quote card sharing functional
5. ✅ All Supabase code removed
6. ✅ Navigation updated correctly
7. ✅ No crashes or errors in quote flow

## Future Enhancements

- Add more quote card styles
- Support video quote cards
- Add quote templates
- Support quote collections/folders
- Add quote search in Discord channel
- Support quote reactions from Discord

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Discord webhook rate limiting | High | Implement queue and retry logic |
| Image generation performance | Medium | Optimize rendering, use caching |
| Users miss community quotes | Low | Provide Discord channel link |
| Large quote text crashes | Low | Add character limit warnings |

## Conclusion

This redesign transforms the quote feature into a modern, Instagram-like experience while simplifying the architecture by removing Supabase dependency. The Discord integration provides a better community experience, and local quote storage ensures users maintain their personal collections.

The implementation is straightforward, leveraging existing quote card generation and following the proven pattern from the character art feature.
