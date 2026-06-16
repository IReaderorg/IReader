# Reader Screen Refactor + 3 Reading Modes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the monolithic reader screen into clean modules, then add 3 switchable reading modes (paged, continuous, infinite scroll).

**Architecture:** Phase 1 extracts shared rendering into `ReaderTextCommon.kt`, consolidates 3 paged implementations into 1 `PagedReaderMode.kt`, splits the 2187-line ViewModel into focused sub-ViewModels, and decomposes the 50+ field state class. Phase 2 adds a `ReaderModeStrategy` interface, implements infinite scroll mode, and wires mode switching.

**Tech Stack:** Jetpack Compose, Compose Foundation (Pager, LazyColumn), Koin DI, Kotlin Coroutines, Flow

---

## Phase 1: Refactor

### Task 1: Extract ReaderTextCommon.kt (shared rendering)

**Covers:** [S3.1]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderTextCommon.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

- [ ] **Step 1: Create ReaderTextCommon.kt with shared components**

Extract these from `ReaderText.kt` into the new file:
- `StyleTextOptimized` composable (~line 940-1270)
- `ChapterEndCard` composable (the next/prev chapter navigation card at chapter end)
- `ImageUrlPage` composable (handles `ImageUrl` page type rendering)
- `FindInChapterOverlay` (the find-match highlight overlay)
- `TextStyleParams` data class if it exists
- Any shared color/text style helpers

The new file should contain ONLY composable functions and data classes that are used by multiple reading modes. Do NOT move mode-specific logic here.

- [ ] **Step 2: Update ReaderText.kt imports**

Replace the moved functions with imports from `ReaderTextCommon.kt`. The calls should remain unchanged (same function names, same signatures).

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Install on device. Open any book → reader should render normally. Verify text styling, images, chapter-end card all display correctly.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderTextCommon.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt
git commit -m "refactor(reader): extract shared rendering to ReaderTextCommon.kt"
```

---

### Task 2: Consolidate paged implementations into PagedReaderMode.kt

**Covers:** [S3.1]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/PagedReaderMode.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

- [ ] **Step 1: Create PagedReaderMode.kt**

Move from `ReaderText.kt`:
- `PagedReaderText` composable (~line 674-710)
- `OptimizedPagedReaderText` composable (~line 710-845) — this is the primary one to keep
- `LegacyPagedReaderText` composable (~line 846-940)

Decision: Keep `OptimizedPagedReaderText` as the primary implementation. Delete `LegacyPagedReaderText` if it's strictly inferior. If `PagedReaderText` is just a wrapper, inline it.

The new file should expose a single entry point:
```kotlin
@Composable
fun PagedReaderContent(
    vm: ReaderScreenViewModel,
    content: List<Page>,
    modifier: Modifier,
)
```

- [ ] **Step 2: Update ReaderText.kt to call PagedReaderMode**

Replace all `PagedReaderText(...)`, `OptimizedPagedReaderText(...)` calls with `PagedReaderContent(...)`.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Install on device. Switch to Page mode. Verify swiping works, pages render correctly.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/PagedReaderMode.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt
git commit -m "refactor(reader): consolidate 3 paged implementations into PagedReaderMode.kt"
```

---

### Task 3: Extract ContinuousReaderMode.kt

**Covers:** [S3.1]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ContinuousReaderMode.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

- [ ] **Step 1: Create ContinuousReaderMode.kt**

Move from `ReaderText.kt`:
- `ContinuesReaderPage` composable (~line 1275+)

Expose a single entry point:
```kotlin
@Composable
fun ContinuousReaderContent(
    vm: ReaderScreenViewModel,
    content: List<Page>,
    modifier: Modifier,
)
```

- [ ] **Step 2: Update ReaderText.kt to call ContinuousReaderMode**

Replace all `ContinuesReaderPage(...)` calls with `ContinuousReaderContent(...)`.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Install on device. Switch to Continues mode. Verify scrolling works, chapter-end card appears.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ContinuousReaderMode.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt
git commit -m "refactor(reader): extract continuous scroll to ContinuousReaderMode.kt"
```

---

### Task 4: Simplify ReaderText.kt dispatcher

**Covers:** [S3.1]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

- [ ] **Step 1: Reduce ReaderText.kt to dispatcher**

After Tasks 1-3, `ReaderText.kt` should only contain:
- The main `ReadingScreen` composable (the top-level composable that sets up the reading area)
- A `when (readingMode)` dispatcher that calls `PagedReaderContent(...)` or `ContinuousReaderContent(...)`

Remove any dead code, unused imports, and leftover function stubs.

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify line count is reduced**

The file should be under 300 lines (down from 2048).

- [ ] **Step 4: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt
git commit -m "refactor(reader): reduce ReaderText.kt to thin dispatcher"
```

---

### Task 5: Extract ReaderContentViewModel.kt

**Covers:** [S3.2]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderContentViewModel.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

- [ ] **Step 1: Create ReaderContentViewModel.kt**

Extract from `ReaderScreenViewModel`:
- Chapter loading logic (`loadChapter`, `loadInitialChapter`)
- Content fetching (`fetchChapterContent`, `getPageList`)
- Chapter navigation (`goToNextChapter`, `goToPreviousChapter`, `goToChapter`)
- Chapter health checking (`checkChapterHealth`)
- Chapter repair logic

The new ViewModel holds:
- `content: MutableStateFlow<List<Page>>`
- `chapters: MutableStateFlow<List<Chapter>>`
- `currentChapter: MutableStateFlow<Chapter?>`
- `isLoadingContent: MutableStateFlow<Boolean>`

Constructor takes the same dependencies as the parent VM (`ChapterLoaderUseCase`, repository, etc.)

- [ ] **Step 2: Delegate from ReaderScreenViewModel**

Replace the extracted methods in `ReaderScreenViewModel` with calls to `contentVM.method()`. The main VM keeps a reference: `val contentVM = ReaderContentViewModel(...)`.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Install on device. Open a book → chapters load. Navigate between chapters. Verify content loads.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderContentViewModel.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract ReaderContentViewModel for chapter loading"
```

---

### Task 6: Extract ReaderScrollViewModel.kt

**Covers:** [S3.2]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScrollViewModel.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

- [ ] **Step 1: Create ReaderScrollViewModel.kt**

Extract from `ReaderScreenViewModel`:
- Scroll state management
- Auto-scroll logic
- Reading time estimation
- Scroll-to-position helpers
- Reading break reminder timer

The new ViewModel holds:
- `scrollPosition: MutableStateFlow<Int>`
- `autoScrollEnabled: MutableStateFlow<Boolean>`
- `autoScrollSpeed: MutableStateFlow<Float>`
- `readingTimeMs: MutableStateFlow<Long>`

Constructor takes dependencies: `ReaderStatisticsViewModel`, `ReaderPreferences`

- [ ] **Step 2: Delegate from ReaderScreenViewModel**

Replace extracted methods with delegation.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Install on device. Test auto-scroll, reading time display.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScrollViewModel.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract ReaderScrollViewModel for scroll/auto-scroll"
```

---

### Task 7: Absorb UI toggle state into ReaderSettingsViewModel

**Covers:** [S3.2]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderSettingsViewModel.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenState.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

- [ ] **Step 1: Move UI toggle state from ReaderState.Success to ReaderSettingsViewModel**

Move these fields from `ReaderState.Success` into `ReaderSettingsViewModel`:
- `isReaderModeEnabled`, `isSettingModeEnabled`, `isMainBottomModeEnabled`
- `showSettingsBottomSheet`, `isDrawerAsc`
- `showFindInChapter`, `findQuery`, `findMatches`, `currentFindMatchIndex`
- `showBrightnessControl`, `showFontSizeAdjuster`, `showFontPicker`
- `showReadingTime`, `autoScrollMode`

`ReaderSettingsViewModel` already exists at `viewmodel/ReaderSettingsViewModel.kt` — add these as StateFlow properties.

- [ ] **Step 2: Update ReaderState.Success to remove moved fields**

Delete the moved fields from `ReaderState.Success`. Add `@Suppress("unused")` temporarily if needed to catch stale references.

- [ ] **Step 3: Update all references**

Search for every usage of the moved fields (e.g., `state.showFindInChapter`, `vm.showFindInChapter`). Replace with `settingsVM.showFindInChapter.value`.

- [ ] **Step 4: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Manual smoke test**

Install on device. Test brightness control, font picker, find-in-chapter, settings drawer.

- [ ] **Step 6: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/
git commit -m "refactor(reader): move UI toggle state to ReaderSettingsViewModel"
```

---

### Task 8: Decompose ReaderState.Success into sub-states

**Covers:** [S3.3]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenState.kt`

- [ ] **Step 1: Define ReaderContentState, ReaderUiState, ReaderLoadingState**

```kotlin
@Immutable
data class ReaderContentState(
    val book: Book,
    val currentChapter: Chapter,
    val chapters: List<Chapter>,
    val catalog: CatalogLocal?,
    val content: List<Page> = emptyList(),
    val translatedContent: List<Page> = emptyList(),
    val currentChapterIndex: Int = 0,
    val chapterShell: List<Chapter> = emptyList(),
    val isTranslating: Boolean = false,
    val translationProgress: Float = 0f,
    val hasTranslation: Boolean = false,
    val showTranslatedContent: Boolean = false,
    val isChapterBroken: Boolean = false,
    val chapterBreakReason: String? = null,
    val showRepairBanner: Boolean = false,
    val isRepairing: Boolean = false,
    val showRepairSuccess: Boolean = false,
    val repairSuccessSourceName: String? = null,
    val showChapterArtDialog: Boolean = false,
    val isGeneratingArtPrompt: Boolean = false,
    val generatedArtPrompt: String? = null,
    val chapterArtError: String? = null,
)

@Immutable
data class ReaderLoadingState(
    val isLoadingContent: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPreloading: Boolean = false,
    val isNavigating: Boolean = false,
    val isInitialLoading: Boolean = false,
)
```

- [ ] **Step 2: Rewrite ReaderState.Success to compose sub-states**

```kotlin
@Immutable
data class ReaderSuccess(
    val content: ReaderContentState,
    val loading: ReaderLoadingState,
) : ReaderState {
    // Derived properties preserved as before
    val source get() = content.catalog?.source
    val isChapterLoaded get() = content.currentContent.isNotEmpty()
    val currentContent get() = /* same logic as before */
}
```

- [ ] **Step 3: Update all state access sites**

Replace `state.book` → `state.content.book`, `state.isLoadingContent` → `state.loading.isLoadingContent`, etc.

- [ ] **Step 4: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Manual smoke test**

Install on device. Full reader flow: open book, read chapter, switch chapters, toggle settings.

- [ ] **Step 6: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenState.kt
git commit -m "refactor(reader): decompose ReaderState.Success into content/loading sub-states"
```

---

### Task 9: Simplify main ReaderScreenViewModel to coordinator

**Covers:** [S3.2]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

- [ ] **Step 1: Verify all logic is delegated**

After Tasks 5-8, the main VM should only contain:
- Constructor that wires sub-VMs together
- `init` block that calls sub-VM initialization
- Event routing (e.g., `onEvent(ReaderEvent.NavigateToChapter)` → `contentVM.goToChapter()`)
- Error aggregation
- `onCleared()` cleanup

Target: under 300 lines.

- [ ] **Step 2: Remove dead code**

Delete any methods that were moved to sub-VMs but not yet removed. Remove unused imports.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manual smoke test**

Full reader regression test.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): simplify ReaderScreenViewModel to thin coordinator"
```

---

## Phase 2: Implement 3 Reading Modes

### Task 10: Add InfiniteScroll to ReadingMode enum

**Covers:** [S4.1]

**Files:**
- Modify: `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/preferences/PreferenceTypes.kt`

- [ ] **Step 1: Update ReadingMode enum**

```kotlin
enum class ReadingMode {
    Page,
    Continues,       // Keep existing name for backward compat
    InfiniteScroll;

    companion object {
        fun valueOf(index: Int): ReadingMode {
            return entries.getOrElse(index) { Page }
        }
    }
}
```

- [ ] **Step 2: Update PreferenceTypes**

```kotlin
val readingMode: ReadingMode = ReadingMode.Page,
```
(Already correct — enum deserialization handles the new value automatically)

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :domain:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt domain/src/commonMain/kotlin/ireader/domain/services/preferences/PreferenceTypes.kt
git commit -m "feat(reader): add InfiniteScroll to ReadingMode enum"
```

---

### Task 11: Create ReaderModeStrategy interface

**Covers:** [S4.2]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderModeStrategy.kt`

- [ ] **Step 1: Create the interface file**

```kotlin
package ireader.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.core.source.model.Page

/**
 * Strategy interface for reading mode rendering.
 * Each reading mode (paged, continuous, infinite scroll) implements this.
 */
interface ReaderModeStrategy {
    @Composable
    fun Content(
        content: List<Page>,
        state: ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel,
        modifier: Modifier,
    )
}
```

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderModeStrategy.kt
git commit -m "feat(reader): add ReaderModeStrategy interface"
```

---

### Task 12: Refactor PagedReaderMode to implement strategy

**Covers:** [S4.2]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/PagedReaderMode.kt`

- [ ] **Step 1: Create PagedModeStrategy class**

```kotlin
class PagedModeStrategy : ReaderModeStrategy {
    @Composable
    override fun Content(content: List<Page>, state: ReaderScreenViewModel, modifier: Modifier) {
        PagedReaderContent(vm = state, content = content, modifier = modifier)
    }
}
```

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/PagedReaderMode.kt
git commit -m "feat(reader): wrap paged mode in PagedModeStrategy"
```

---

### Task 13: Refactor ContinuousReaderMode to implement strategy

**Covers:** [S4.2]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ContinuousReaderMode.kt`

- [ ] **Step 1: Create ContinuousModeStrategy class**

```kotlin
class ContinuousModeStrategy : ReaderModeStrategy {
    @Composable
    override fun Content(content: List<Page>, state: ReaderScreenViewModel, modifier: Modifier) {
        ContinuousReaderContent(vm = state, content = content, modifier = modifier)
    }
}
```

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ContinuousReaderMode.kt
git commit -m "feat(reader): wrap continuous mode in ContinuousModeStrategy"
```

---

### Task 14: Implement InfiniteScrollModeStrategy

**Covers:** [S4.2, S4.3]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/InfiniteScrollReaderMode.kt`

- [ ] **Step 1: Create InfiniteScrollReaderMode.kt**

This is the core new feature. Implement:

```kotlin
class InfiniteScrollModeStrategy : ReaderModeStrategy {
    @Composable
    override fun Content(content: List<Page>, state: ReaderScreenViewModel, modifier: Modifier) {
        InfiniteScrollReaderContent(vm = state, content = content, modifier = modifier)
    }
}
```

`InfiniteScrollReaderContent` should:
1. Use `LazyColumn` with items keyed by `"$chapterId_$paragraphIndex"`
2. Render `Text` and `ImageUrl` pages from the concatenated content
3. At item index 80%, trigger `vm.contentVM.loadNextChapter()`
4. Show a loading indicator at the bottom during fetch
5. Insert chapter heading dividers between chapters (styled as `<h3>` with book title)
6. Handle scroll-to-position for "continue reading" and TTS highlighting

Key implementation details:
- Content list is built by concatenating all loaded chapters' pages
- `state.contentVM.allChapterContent: StateFlow<List<Pair<Chapter, List<Page>>>>` provides the multi-chapter data
- Loading state: `state.loading.isPreloading` flag triggers bottom spinner
- Chapter divider: a `Box` with `Text(chapter.title)` centered, with top/bottom dividers

- [ ] **Step 2: Add `loadNextChapter()` to ReaderContentViewModel**

In `ReaderContentViewModel.kt`, add:
```kotlin
suspend fun loadNextChapter(): Boolean {
    val chapters = _chapters.value
    val currentIndex = chapters.indexOfFirst { it.id == _currentChapter.value?.id }
    val nextChapter = chapters.getOrNull(currentIndex + 1) ?: return false
    
    // Fetch content for next chapter
    val nextPageContent = chapterLoader.getChapterContent(nextChapter)
    // Append to accumulated content
    _allChapterContent.update { it + listOf(nextChapter to nextPageContent) }
    return true
}
```

- [ ] **Step 3: Add `allChapterContent` StateFlow to ReaderContentViewModel**

```kotlin
val allChapterContent: StateFlow<List<Pair<Chapter, List<Page>>>> 
```

- [ ] **Step 4: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Manual smoke test**

Install on device. Switch to InfiniteScroll mode. Verify:
- Content loads from current chapter
- Scrolling down loads next chapter seamlessly
- Chapter dividers appear between chapters
- Loading indicator shows during chapter fetch

- [ ] **Step 6: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/InfiniteScrollReaderMode.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderContentViewModel.kt
git commit -m "feat(reader): implement InfiniteScrollModeStrategy"
```

---

### Task 15: Wire mode selection in ReaderText dispatcher

**Covers:** [S4.2]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

- [ ] **Step 1: Update the `when` dispatcher to use strategy pattern**

```kotlin
@Composable
fun ReadingScreen(...) {
    val strategy = remember(vm.readingMode.value) {
        when (vm.readingMode.value) {
            ReadingMode.Page -> PagedModeStrategy()
            ReadingMode.Continues -> ContinuousModeStrategy()
            ReadingMode.InfiniteScroll -> InfiniteScrollModeStrategy()
        }
    }
    
    strategy.Content(
        content = state.currentContent,
        state = vm,
        modifier = modifier,
    )
}
```

- [ ] **Step 2: Remove old `when (readingMode)` branching**

Delete the old `when` block that directly called `PagedReaderContent(...)` / `ContinuousReaderContent(...)`.

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt
git commit -m "feat(reader): wire mode selection via strategy pattern"
```

---

### Task 16: Update settings UI for 3 modes

**Covers:** [S4.4]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReaderSettingComposable.kt`

- [ ] **Step 1: Add InfiniteScroll option to mode selector**

Find the reading mode selector (currently shows Page/Continues radio buttons). Add `InfiniteScroll` option.

The selector should show:
- 📄 Page (swipe to turn pages)
- 📜 Continuous (scroll within chapter, tap for next)  
- ♾️ Infinite Scroll (all chapters merged, endless scroll)

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Manual smoke test**

Install on device. Open reader settings → verify 3 mode options appear. Switch between all 3 → verify correct mode loads.

- [ ] **Step 4: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReaderSettingComposable.kt
git commit -m "feat(reader): add 3-mode selector to reader settings"
```

---

### Task 17: Full integration test

**Covers:** [S5]

**Files:** None (testing only)

- [ ] **Step 1: Run full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Smoke test all 3 modes**

For each mode (Page, Continuous, InfiniteScroll):
1. Open any book with 10+ chapters
2. Read first chapter — verify content renders
3. Navigate to next chapter — verify transition
4. Switch modes mid-chapter — verify no crash
5. Test translation toggle — verify works in all modes
6. Test TTS — verify plays in all modes
7. Test find-in-chapter — verify works in all modes
8. Test brightness/font — verify works in all modes

- [ ] **Step 3: Regression check**

Verify:
- Chapter drawer opens/closes
- Chapter progress saves correctly
- Reading statistics track time
- Chapter break reminder fires
- Copy mode works

- [ ] **Step 4: Final commit (if any fixes needed)**

```bash
git commit -m "fix(reader): address integration test issues"
```
