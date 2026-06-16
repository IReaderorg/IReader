# Reader Screen Refactor + 3 Reading Modes

## [S1] Problem

The reader screen codebase has grown to ~5000+ lines across core files with critical structural issues:

1. **ReaderText.kt (2048 lines)** — Monolithic file handling all rendering modes. Contains 3 duplicate paged implementations (`PagedReaderText`, `OptimizedPagedReaderText`, `LegacyPagedReaderText`) plus `ContinuesReaderPage`. All sharing no common interface.

2. **ReaderScreenViewModel.kt (2187 lines)** — Single ViewModel handles: content loading, scroll management, chapter navigation, translation, TTS, statistics, chapter health, settings UI, art generation, find-in-chapter, glossary. This makes it nearly impossible to add new features without risking unrelated regressions.

3. **ReaderState.Success (50+ fields)** — Mixes UI toggles (`isSettingModeEnabled`, `showFindInChapter`), content data (`content`, `translatedContent`), loading states (`isLoadingContent`, `isRefreshing`), feature flags (`autoScrollMode`, `isReaderModeEnabled`), and dialog states (`showReportDialog`, `showChapterArtDialog`). No separation of concerns.

4. **Only 2 reading modes** — `ReadingMode.Page` and `ReadingMode.Continues`. Missing: infinite scroll mode.

### Consequence
Adding 3 switchable reading modes (paged, continuous, infinite scroll) to the current codebase would multiply complexity by ~3x, making bugs untraceable and regressions likely.

## [S2] Solution Overview

**Two-phase approach:**
- **Phase 1:** Refactor codebase into clean modules — same behavior, cleaner code
- **Phase 2:** Implement 3 switchable reading modes on top of the clean architecture

**Constraint:** Each phase produces a working app. No big-bang rewrite.

## [S3] Phase 1: Refactor

### [S3.1] Split ReaderText.kt into mode-specific files

**Before:** Single 2048-line file with 4 rendering implementations.

**After:**

| File | Purpose | Target lines |
|------|---------|-------------|
| `ReaderText.kt` | Thin dispatcher — picks mode, delegates to strategy | ~100 |
| `PagedReaderMode.kt` | Paged swiping (consolidate 3 duplicates into 1) | ~400 |
| `ContinuousReaderMode.kt` | Continuous scroll (from existing `ContinuesReaderPage`) | ~300 |
| `ReaderTextCommon.kt` | Shared: text styling, images, chapter-end UI, find-in-chapter overlay | ~300 |

**Shared components to extract:**
- `StyleTextOptimized` → stays in `ReaderTextCommon.kt`
- Image rendering (ImageUrl handling) → `ReaderTextCommon.kt`
- Chapter end card (next/prev chapter) → `ReaderTextCommon.kt`
- Find-in-chapter highlight overlay → `ReaderTextCommon.kt`

### [S3.2] Split ReaderScreenViewModel.kt into focused VMs

**Before:** Single 2187-line ViewModel.

**After:**

| File | Responsibility | Key state |
|------|---------------|-----------|
| `ReaderContentViewModel.kt` | Chapter loading, content fetching, chapter navigation | content, chapters, currentChapter |
| `ReaderScrollViewModel.kt` | Scroll state, auto-scroll, reading time estimation | scrollState, autoScroll, readingTime |
| `ReaderSettingsViewModel.kt` | (already exists) absorb UI toggle state from Success | readingMode, font, brightness |
| `ReaderScreenViewModel.kt` | Thin coordinator: delegates to sub-VMs, handles events | event channel, error handling |

**Main ViewModel becomes:**
```kotlin
class ReaderScreenViewModel(
    val content: ReaderContentViewModel,
    val scroll: ReaderScrollViewModel,
    val settings: ReaderSettingsViewModel,
    val statistics: ReaderStatisticsViewModel,
    val tts: ReaderTTSViewModel,
    val translation: ReaderTranslationViewModel,
) { /* ~200 lines: event routing, error aggregation */ }
```

### [S3.3] Decompose ReaderState.Success

Split the 50+ field data class into focused state objects:

```kotlin
@Immutable
data class ReaderState.Success(
    val content: ReaderContentState,
    val ui: ReaderUiState,
    val loading: ReaderLoadingState,
)
```

- `ReaderContentState` — book, chapters, currentChapter, content, translatedContent
- `ReaderUiState` — UI toggles, dialog states, find-in-chapter state
- `ReaderLoadingState` — isLoadingContent, isRefreshing, isNavigating, isPreloading

### [S3.4] Refactor commit plan

Each step is a separate commit that produces a working app:

1. Extract `ReaderTextCommon.kt` (shared rendering components)
2. Consolidate 3 paged implementations into 1 `PagedReaderMode.kt`
3. Extract `ContinuousReaderMode.kt`
4. Create `ReaderText.kt` dispatcher (mode selection)
5. Extract `ReaderContentViewModel.kt`
6. Extract `ReaderScrollViewModel.kt`
7. Absorb toggle state into `ReaderSettingsViewModel`
8. Decompose `ReaderState.Success` into 3 sub-states
9. Simplify main `ReaderScreenViewModel` to coordinator

## [S4] Phase 2: Implement 3 Reading Modes

### [S4.1] ReadingMode enum update

```kotlin
enum class ReadingMode {
    Page,           // Swipe pages (existing, refactored)
    Continuous,     // Scroll within chapter, tap to load next (existing "Continues")
    InfiniteScroll  // All chapters concatenated, endless scroll (NEW)
}
```

### [S4.2] Strategy pattern for modes

```kotlin
interface ReaderModeStrategy {
    @Composable
    fun Content(
        content: List<Page>,
        vm: ReaderScreenViewModel,
        modifier: Modifier,
    )
}
```

Each mode implements this interface:
- `PagedModeStrategy` — HorizontalPager with page snapping
- `ContinuousModeStrategy` — VerticalLazyColumn with chapter-end card
- `InfiniteScrollModeStrategy` — VerticalLazyColumn that auto-loads next chapter

### [S4.3] Infinite Scroll specifics

- Loads chapters sequentially, concatenates content
- LazyColumn items keyed by `chapterId_paragraphIndex`
- `LaunchedEffect` triggers next chapter load when scroll reaches 80%
- Loading indicator at bottom during chapter fetch
- Reading position tracked per-chapter (existing progress system)
- Chapter headings inserted as visual separators between concatenated content

### [S4.4] Mode switching UI

- Settings bottom sheet shows 3 radio buttons (was 2)
- `ReaderPreferences.defaultReadingMode()` stores selection
- Mode change triggers immediate UI switch (no restart needed)

### [S4.5] Phase 2 commit plan

1. Add `InfiniteScroll` to `ReadingMode` enum
2. Create `ReaderModeStrategy` interface
3. Refactor `PagedReaderMode.kt` to implement strategy
4. Refactor `ContinuousReaderMode.kt` to implement strategy
5. Implement `InfiniteScrollModeStrategy`
6. Wire mode selection in `ReaderText.kt` dispatcher
7. Update settings UI for 3 modes
8. Test all 3 modes with existing chapter content

## [S5] Testing strategy

- **Phase 1:** Manual smoke test after each commit — open reader, scroll, switch chapters, toggle settings
- **Phase 2:** Test each mode independently:
  - Paged: swipe works, chapter transitions smooth
  - Continuous: scroll to end shows next chapter card
  - Infinite: chapters load seamlessly, reading position tracks correctly
- **Regression:** Verify translation, TTS, find-in-chapter, brightness all work in each mode

## [S6] Risk mitigation

- Each refactor step is a separate commit — easy to revert
- No new features in Phase 1 — only structural changes
- Phase 2 adds features incrementally — one mode at a time
- Strategy pattern isolates modes — changing one doesn't affect others
- State decomposition uses data classes — easy to merge with spread operator
