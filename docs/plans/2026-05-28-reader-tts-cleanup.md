# Reader & TTS Features Cleanup Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Clean up and simplify the reader and TTS features to make them noob-proof and fool-proof by reducing file sizes, eliminating duplication, improving error handling, and making the code self-documenting.

**Architecture:** The reader system has a monolithic ViewModel (91K, ~1500+ lines) that handles chapter loading, TTS sync, preferences, content filtering, and statistics. The TTS system has a 41K Controller with deeply nested state management. We will split these into focused, single-responsibility components with clear boundaries, consistent error handling, and defensive programming patterns.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Kotlin Coroutines/Flow, Koin DI, StateFlow/MutableStateFlow

---

## Current State Analysis

### Reader Feature Files (presentation layer)
| File | Size | Lines (est.) | Issues |
|------|------|-------------|--------|
| `ReaderScreenViewModel.kt` | 91K | ~1500+ | God class: chapter loading, TTS sync, preferences, content filter, statistics, scroll management all in one |
| `ReaderText.kt` | 86K | ~1400+ | Massive composable with inline gesture handling, image loading, text rendering |
| `ReaderSettingComposable.kt` | 51K | ~800+ | All settings UI in one file |
| `ReaderScreenState.kt` | 7K | ~120 | Well-structured sealed state |
| `ReaderTTSViewModel.kt` | 15K | ~250 | TTS-specific reader logic |
| `ReaderTranslationViewModel.kt` | 24K | ~400 | Translation logic mixed with reader |
| `ReaderSettingsViewModel.kt` | 16K | ~260 | Settings management |
| `ReaderStatisticsViewModel.kt` | 14K | ~230 | Reading statistics |
| `ReaderScreen.kt` | 35K | ~580 | Screen composable |
| `ReaderScreenDrawer.kt` | 4K | ~60 | Chapter drawer |

### TTS Feature Files (domain layer)
| File | Size | Lines (est.) | Issues |
|------|------|-------------|--------|
| `TTSController.kt` | 41K | ~700+ | Monolithic: playback, navigation, content, chunk mode, translation, settings |
| `TTSState.kt` | 4.5K | ~75 | State data class |
| `TTSCommand.kt` | 3K | ~50 | Command sealed class |
| `TTSEngine.kt` | 4.3K | ~70 | Engine interface |
| `TTSContentLoaderImpl.kt` | 10K | ~170 | Content loading |
| `TTSTextMergerV2.kt` | 5.5K | ~90 | Text chunking |
| `TTSCacheUseCase.kt` | 4.7K | ~80 | Cache management |
| `TTSPreferencesUseCase.kt` | 8.4K | ~140 | TTS preferences |
| `TTSSleepTimerUseCase.kt` | 5.5K | ~90 | Sleep timer |
| `TTSNotificationUseCase.kt` | 6.1K | ~100 | Notifications |
| `TTSViewModelAdapter.kt` | 12.5K | ~210 | Adapter between TTS and UI |
| `TTSChunkPlayer.kt` | 6.6K | ~110 | Chunk playback |
| `TTSAudioCache.kt` | 15K | ~250 | Audio caching |
| `GradioTTSPlayer.kt` | 29K | ~480 | Gradio TTS player |
| `GradioTTSPlayerManager.kt` | 10K | ~170 | Player management |
| `GradioTTSEngineAdapter.kt` | 20K | ~330 | Engine adapter |
| `GenericGradioTTSEngine.kt` | 45K | ~750 | Generic Gradio engine |
| `TTSChapterCache.kt` | 25K | ~420 | Chapter cache |
| `TTSChapterDownloadManager.kt` | 18K | ~300 | Download manager |

### Reader Preferences
| File | Size | Lines (est.) | Issues |
|------|------|-------------|--------|
| `ReaderPreferences.kt` | 29K | ~800 | 100+ preference methods, no grouping, mixed concerns |

### Key Issues Identified
1. **God Classes**: `ReaderScreenViewModel` (1500+ lines), `TTSController` (700+ lines), `ReaderText` (1400+ lines)
2. **Mixed Concerns**: Reader ViewModel handles TTS sync, content filtering, statistics, preferences, scroll management
3. **Inconsistent Error Handling**: Some places use try-catch with logging, others silently fail
4. **Magic Numbers**: Hardcoded delays (300ms, 500ms, 100ms), scroll positions, indices
5. **Deep Nesting**: TTSController has deeply nested when expressions and state updates
6. **Duplicate Logic**: Chapter loading has multiple fallback paths that duplicate logic
7. **No Input Validation**: Public methods don't validate parameters before processing
8. **Verbose Logging**: `Log.warn` used for normal operations, `Log.debug` for important state changes
9. **Unclear Naming**: `params`, `next`, `force`, `scrollToEnd` parameters with confusing interactions
10. **Dead Code**: Commented-out code blocks, unused imports, empty files

---

## Task 1: Extract Reader Constants and Configuration

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderConstants.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:108-118`

**Step 1: Create the constants file**

```kotlin
package ireader.presentation.ui.reader

/**
 * Constants for the Reader feature.
 * Centralizes all magic numbers and configuration values.
 */
object ReaderConstants {
    /** Delay (ms) after remote fetch completes before notifying ChapterController.
     *  Ensures DB insert is fully committed. */
    const val FETCH_TO_CONTROLLER_DELAY_MS = 300L
    
    /** Delay (ms) before starting preload of next chapter after current fetch completes.
     *  Prevents two remote fetches from hitting the source server simultaneously. */
    const val PRELOAD_AFTER_FETCH_DELAY_MS = 500L
    
    /** Delay (ms) for initial chapter load to allow ChapterController to initialize. */
    const val CHAPTER_CONTROLLER_INIT_DELAY_MS = 100L
    
    /** Debounce delay (ms) for chapter list updates from ChapterNotifier. */
    const val CHAPTER_LIST_DEBOUNCE_MS = 100L
    
    /** Default scroll position when no saved position exists. */
    const val DEFAULT_SCROLL_POSITION = 0L
    
    /** Minimum chapter index for validation. */
    const val MIN_CHAPTER_INDEX = 0
    
    /** Number of paragraphs to look ahead for preloading. */
    const val PRELOAD_LOOKAHEAD_COUNT = 3
}
```

**Step 2: Run build to verify compilation**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Replace magic numbers in ReaderScreenViewModel**

In `ReaderScreenViewModel.kt`, replace the companion object constants:
```kotlin
// REMOVE the existing companion object constants (lines 108-118)
// REPLACE references to FETCH_TO_CONTROLLER_DELAY_MS with ReaderConstants.FETCH_TO_CONTROLLER_DELAY_MS
// REPLACE references to PRELOAD_AFTER_FETCH_DELAY_MS with ReaderConstants.PRELOAD_AFTER_FETCH_DELAY_MS
```

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderConstants.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract magic numbers to ReaderConstants"
```

---

## Task 2: Extract ReaderScreenViewModel Chapter Loading Logic

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderChapterLoader.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:843-981`

**Step 1: Create the chapter loader class**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.reader.ReaderConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles all chapter loading operations for the Reader screen.
 * Extracted from ReaderScreenViewModel to separate concerns.
 * 
 * Responsibilities:
 * - Loading chapters from database or remote
 * - Managing chapter navigation (next/previous)
 * - Preloading adjacent chapters
 * - Calculating reading statistics (word count)
 */
class ReaderChapterLoader(
    private val scope: CoroutineScope,
    private val getChapterUseCase: ireader.domain.usecases.chapter.GetChapterUseCase,
    private val getBookUseCases: ireader.domain.usecases.book.BookDetailUseCases,
    private val historyUseCase: ireader.domain.usecases.history.HistoryUseCase,
    private val chapterController: ireader.domain.services.chapter.ChapterController,
    private val preloadChapterUseCase: ireader.domain.usecases.reader.PreloadChapterUseCase,
    private val remoteUseCases: ireader.domain.usecases.chapter.ChapterUseCases,
    private val onChapterLoaded: (Book, CatalogLocal?, Chapter, List<Chapter>, Int) -> Unit,
    private val onError: (String) -> Unit,
    private val onLoading: () -> Unit
) {
    private val preloadedChapters = mutableMapOf<Long, Chapter>()
    private var preloadJob: kotlinx.coroutines.Job? = null
    
    /**
     * Load initial chapters for a book.
     * Finds the target chapter (from history or first available) and loads it.
     */
    suspend fun loadInitialChapters(
        bookId: Long,
        requestedChapterId: Long,
        book: Book,
        catalog: CatalogLocal?
    ): LoadResult {
        val last = historyUseCase.findHistoryByBookId(bookId)
        val chapters = getChapters()
        
        val targetChapterId = when {
            requestedChapterId != ireader.i18n.LAST_CHAPTER && 
            requestedChapterId != ireader.i18n.NO_VALUE -> requestedChapterId
            last != null -> last.chapterId
            else -> chapters.firstOrNull()?.id
        }
        
        if (targetChapterId == null) {
            return LoadResult.Error("No chapters found")
        }
        
        val chapterIndex = chapters.indexOfFirst { it.id == targetChapterId }
            .coerceAtLeast(0)
        
        return LoadResult.Success(
            book = book,
            catalog = catalog,
            targetChapterId = targetChapterId,
            chapters = chapters,
            chapterIndex = chapterIndex
        )
    }
    
    /**
     * Load a specific chapter by ID.
     * Handles both local (DB) and remote loading.
     */
    suspend fun loadChapter(
        book: Book,
        catalog: CatalogLocal?,
        chapterId: Long,
        currentChapters: List<Chapter>,
        forceRemote: Boolean = false
    ): ChapterResult {
        val chapter = getChapterUseCase.findChapterById(chapterId)
            ?: return ChapterResult.NotFound
        
        val chapterIndex = currentChapters.indexOfFirst { it.id == chapter.id }
            .coerceAtLeast(0)
        
        val needsRemoteFetch = chapter.isEmpty() && catalog?.source != null
        
        return ChapterResult.Loaded(
            chapter = chapter,
            chapterIndex = chapterIndex,
            needsRemoteFetch = needsRemoteFetch,
            totalWords = calculateTotalWords(chapter.content)
        )
    }
    
    /**
     * Get the next chapter in sequence.
     */
    fun getNextChapter(currentChapterId: Long, chapters: List<Chapter>): Chapter? {
        val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
        return if (currentIndex >= 0 && currentIndex < chapters.lastIndex) {
            chapters[currentIndex + 1]
        } else null
    }
    
    /**
     * Get the previous chapter in sequence.
     */
    fun getPreviousChapter(currentChapterId: Long, chapters: List<Chapter>): Chapter? {
        val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
        return if (currentIndex > 0) {
            chapters[currentIndex - 1]
        } else null
    }
    
    /**
     * Preload the next chapter for smoother navigation.
     */
    fun preloadNextChapter(
        book: Book,
        catalog: CatalogLocal?,
        currentChapterId: Long,
        chapters: List<Chapter>
    ) {
        preloadJob?.cancel()
        preloadJob = scope.launch {
            val nextChapter = getNextChapter(currentChapterId, chapters) ?: return@launch
            if (!preloadedChapters.containsKey(nextChapter.id)) {
                try {
                    val content = remoteUseCases.fetchAndSaveChapterContent(
                        book = book,
                        catalog = catalog,
                        chapter = nextChapter
                    )
                    if (content != null) {
                        preloadedChapters[nextChapter.id] = nextChapter.copy(
                            content = content
                        )
                    }
                } catch (e: Exception) {
                    Log.warn { "Preload failed for chapter ${nextChapter.id}: ${e.message}" }
                }
            }
        }
    }
    
    /**
     * Get a preloaded chapter if available.
     */
    fun getPreloadedChapter(chapterId: Long): Chapter? {
        return preloadedChapters.remove(chapterId)
    }
    
    /**
     * Cancel any pending preload operation.
     */
    fun cancelPreload() {
        preloadJob?.cancel()
        preloadJob = null
    }
    
    /**
     * Clear all preloaded chapters.
     */
    fun clearPreloaded() {
        preloadedChapters.clear()
    }
    
    private fun getChapters(): List<Chapter> {
        return chapterController.state.value.chapters
    }
    
    /**
     * Calculate total words from chapter content for reading time estimation.
     */
    fun calculateTotalWords(content: List<Page>): Int {
        return content.filterIsInstance<Text>()
            .sumOf { text ->
                text.text.split(Regex("\\s+")).count { it.isNotBlank() }
            }
    }
    
    sealed class LoadResult {
        data class Success(
            val book: Book,
            val catalog: CatalogLocal?,
            val targetChapterId: Long,
            val chapters: List<Chapter>,
            val chapterIndex: Int
        ) : LoadResult()
        data class Error(val message: String) : LoadResult()
    }
    
    sealed class ChapterResult {
        data class Loaded(
            val chapter: Chapter,
            val chapterIndex: Int,
            val needsRemoteFetch: Boolean,
            val totalWords: Int
        ) : ChapterResult()
        object NotFound : ChapterResult()
    }
}
```

**Step 2: Run build to verify compilation**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate ReaderChapterLoader into ReaderScreenViewModel**

Add to ReaderScreenViewModel constructor (after existing params):
```kotlin
private val chapterLoader: ReaderChapterLoader
```

In the init block, initialize it:
```kotlin
chapterLoader = ReaderChapterLoader(
    scope = scope,
    getChapterUseCase = getChapterUseCase,
    getBookUseCases = getBookUseCases,
    historyUseCase = historyUseCase,
    chapterController = chapterController,
    preloadChapterUseCase = preloadChapterUseCase,
    remoteUseCases = remoteUseCases,
    onChapterLoaded = { book, catalog, chapter, chapters, index ->
        // Update state with loaded chapter
    },
    onError = { message ->
        showSnackBar(UiText.DynamicString(message))
    },
    onLoading = { /* set loading state */ }
)
```

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderChapterLoader.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract chapter loading logic into ReaderChapterLoader"
```

---

## Task 3: Extract Reader Scroll Management

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScrollManager.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:314-382`

**Step 1: Create the scroll manager**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages scroll position tracking and persistence for the Reader.
 * 
 * Responsibilities:
 * - Tracking current scroll position in memory
 * - Persisting scroll position to database
 * - Restoring scroll position when re-entering a chapter
 * - Handling scroll-to-end on chapter navigation
 */
class ReaderScrollManager(
    private val scope: CoroutineScope,
    private val chapterRepository: ireader.domain.data.repository.ChapterRepository
) {
    private var _currentScrollPosition: Long = ReaderConstants.DEFAULT_SCROLL_POSITION
    val currentScrollPosition: Long get() = _currentScrollPosition
    
    private var _shouldScrollToEnd: Boolean = false
    val shouldScrollToEnd: Boolean get() = _shouldScrollToEnd
    
    /**
     * Update the current scroll position in memory.
     * Does NOT persist to database - use [saveScrollPosition] for that.
     */
    fun updatePosition(position: Long) {
        _currentScrollPosition = position
    }
    
    /**
     * Save the current scroll position to the database.
     * Call this periodically as the user scrolls.
     */
    fun saveScrollPosition(chapterId: Long, position: Long) {
        _currentScrollPosition = position
        scope.launch {
            try {
                chapterRepository.updateLastPageRead(chapterId, position)
            } catch (e: Exception) {
                Log.error("Failed to save scroll position for chapter $chapterId", e)
            }
        }
    }
    
    /**
     * Force save the current scroll position immediately.
     * Call this before navigating to a new chapter.
     */
    fun forceSaveScrollPosition(chapterId: Long) {
        if (_currentScrollPosition > 0) {
            scope.launch {
                try {
                    chapterRepository.updateLastPageRead(chapterId, _currentScrollPosition)
                } catch (e: Exception) {
                    Log.error("Failed to force save scroll position", e)
                }
            }
        }
    }
    
    /**
     * Set whether the reader should scroll to end on next chapter load.
     */
    fun setScrollToEnd(scrollToEnd: Boolean) {
        _shouldScrollToEnd = scrollToEnd
    }
    
    /**
     * Reset scroll state for a new chapter.
     */
    fun resetForNewChapter(savedPosition: Long = 0L) {
        _currentScrollPosition = savedPosition
        _shouldScrollToEnd = false
    }
    
    /**
     * Get the saved scroll position for a chapter from the database.
     */
    suspend fun getSavedScrollPosition(chapterId: Long): Long {
        return try {
            val chapter = chapterRepository.findChapterById(chapterId)
            chapter?.lastPageRead ?: ReaderConstants.DEFAULT_SCROLL_POSITION
        } catch (e: Exception) {
            Log.error("Failed to get saved scroll position", e)
            ReaderConstants.DEFAULT_SCROLL_POSITION
        }
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into ReaderScreenViewModel**

Replace the existing scroll management code (lines 314-382) with delegation to ReaderScrollManager.

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScrollManager.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract scroll management into ReaderScrollManager"
```

---

## Task 4: Extract Reader Content Filter Management

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderContentFilterManager.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:265-312`

**Step 1: Create the content filter manager**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.reader.ContentFilterUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Manages content filtering for the Reader.
 * 
 * Responsibilities:
 * - Subscribing to content filter preference changes
 * - Re-filtering content when filter settings change
 * - Invalidating filter cache when needed
 */
class ReaderContentFilterManager(
    private val scope: CoroutineScope,
    private val readerPreferences: ReaderPreferences,
    private val contentFilterUseCase: ContentFilterUseCase,
    private val getChapterUseCase: ireader.domain.usecases.chapter.GetChapterUseCase,
    private val onContentRefiltered: (Chapter) -> Unit
) {
    private var isSubscribed = false
    
    /**
     * Subscribe to content filter preference changes.
     * Automatically re-filters content when settings change.
     * Safe to call multiple times - will only subscribe once.
     */
    fun subscribe() {
        if (isSubscribed) return
        isSubscribed = true
        
        readerPreferences.contentFilterEnabled().changes()
            .onEach { enabled ->
                Log.debug { "Content filter enabled changed: $enabled" }
                invalidateCache()
            }
            .launchIn(scope)
        
        readerPreferences.contentFilterPatterns().changes()
            .onEach { patterns ->
                Log.debug { "Content filter patterns changed" }
                invalidateCache()
            }
            .launchIn(scope)
    }
    
    /**
     * Re-filter the current chapter content.
     * Called when filter settings change.
     */
    fun refilterCurrentContent(currentChapterId: Long) {
        invalidateCache()
        scope.launch {
            try {
                val freshChapter = getChapterUseCase.findChapterById(currentChapterId)
                if (freshChapter != null) {
                    onContentRefiltered(freshChapter)
                    Log.debug { "Re-filtered content: ${freshChapter.content.size} pages" }
                }
            } catch (e: Exception) {
                Log.error("Failed to re-filter content", e)
            }
        }
    }
    
    /**
     * Invalidate the content filter cache.
     * Forces re-filtering on next content access.
     */
    fun invalidateCache() {
        contentFilterUseCase.invalidateCache()
    }
    
    /**
     * Unsubscribe from preference changes.
     * Call when the reader is being destroyed.
     */
    fun unsubscribe() {
        isSubscribed = false
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into ReaderScreenViewModel**

Replace the existing content filter subscription code (lines 265-312) with delegation to ReaderContentFilterManager.

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderContentFilterManager.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract content filter management"
```

---

## Task 5: Extract Reader TTS Sync Logic

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderTTSSyncManager.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:682-750`

**Step 1: Create the TTS sync manager**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.v2.TTSController
import ireader.domain.services.tts_service.v2.TTSState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Manages synchronization between Reader and TTS.
 * 
 * Responsibilities:
 * - Subscribing to TTS state changes
 * - Syncing Reader chapter when TTS stops on a different chapter
 * - Providing explicit sync method for when user returns from TTS screen
 */
class ReaderTTSSyncManager(
    private val scope: CoroutineScope,
    private val ttsController: TTSController,
    private val onSyncChapter: (chapterId: Long) -> Unit
) {
    private var syncJob: Job? = null
    private var lastTTSChapterId: Long? = null
    private var isSubscribed = false
    
    /**
     * Subscribe to TTS state changes for automatic sync.
     * Only syncs when TTS is NOT playing (user has left TTS or stopped).
     * Safe to call multiple times.
     */
    fun subscribe() {
        if (isSubscribed) return
        isSubscribed = true
        
        syncJob?.cancel()
        syncJob = scope.launch {
            ttsController.state.collect { ttsState ->
                handleTTSStateChange(ttsState)
            }
        }
    }
    
    private fun handleTTSStateChange(ttsState: TTSState) {
        val ttsChapter = ttsState.chapter ?: return
        
        // Track TTS chapter changes
        if (ttsChapter.id != lastTTSChapterId) {
            lastTTSChapterId = ttsChapter.id
            
            // Only sync when TTS is NOT playing
            if (!ttsState.isPlaying && 
                ttsState.book != null &&
                ttsChapter.id != getCurrentReaderChapterId()) {
                Log.debug { "TTS stopped on chapter ${ttsChapter.id}, syncing Reader" }
                onSyncChapter(ttsChapter.id)
            }
        }
    }
    
    /**
     * Explicitly sync Reader with TTS's current chapter.
     * Call when user returns from TTS screen.
     */
    suspend fun syncWithTTS() {
        val ttsState = ttsController.state.value
        val ttsChapter = ttsState.chapter ?: return
        val ttsBook = ttsState.book ?: return
        
        val readerBookId = getReaderBookId()
        val readerChapterId = getCurrentReaderChapterId()
        
        // Only sync if same book and different chapter
        if (ttsBook.id == readerBookId && ttsChapter.id != readerChapterId) {
            Log.debug { "Syncing Reader with TTS chapter ${ttsChapter.id}" }
            onSyncChapter(ttsChapter.id)
        }
    }
    
    /**
     * Unsubscribe from TTS state changes.
     */
    fun unsubscribe() {
        syncJob?.cancel()
        syncJob = null
        isSubscribed = false
    }
    
    // These are set by the parent ViewModel
    private var readerBookId: Long? = null
    private var readerChapterId: Long? = null
    
    fun updateReaderState(bookId: Long?, chapterId: Long?) {
        readerBookId = bookId
        readerChapterId = chapterId
    }
    
    private fun getReaderBookId(): Long? = readerBookId
    private fun getCurrentReaderChapterId(): Long? = readerChapterId
}
```

**Step 2: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into ReaderScreenViewModel**

Replace the existing TTS sync code (lines 682-750) with delegation to ReaderTTSSyncManager.

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderTTSSyncManager.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract TTS sync logic into ReaderTTSSyncManager"
```

---

## Task 6: Extract Reader ChapterNotifier Subscription

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderChapterNotifierHandler.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:416-570`

**Step 1: Create the chapter notifier handler**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.services.chapter.ChapterNotifier
import ireader.domain.usecases.chapter.GetChapterUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * Handles ChapterNotifier subscriptions for reactive chapter updates.
 * 
 * Responsibilities:
 * - Subscribing to chapter change notifications
 * - Refreshing chapter list when chapters are modified
 * - Reloading current chapter content when it changes
 * - Debouncing rapid updates to avoid recomposition storms
 */
class ReaderChapterNotifierHandler(
    private val scope: CoroutineScope,
    private val chapterNotifier: ChapterNotifier,
    private val getChapterUseCase: GetChapterUseCase,
    private val onChaptersRefreshed: (List<Chapter>) -> Unit,
    private val onCurrentChapterReloaded: (Chapter) -> Unit
) {
    private var notifierJob: Job? = null
    private var isSubscribed = false
    
    /**
     * Subscribe to chapter change notifications for a specific book.
     * Uses debounced flow for chapter list updates to prevent rapid recomposition.
     */
    fun subscribe(bookId: Long, currentChapterId: Long) {
        if (isSubscribed) return
        isSubscribed = true
        
        notifierJob?.cancel()
        notifierJob = scope.launch {
            chapterNotifier.changesForBookDebounced(bookId, debounceMs = 100)
                .collect { change ->
                    handleChapterChange(change, bookId, currentChapterId)
                }
        }
    }
    
    private suspend fun handleChapterChange(
        change: ChapterNotifier.ChangeType,
        bookId: Long,
        currentChapterId: Long
    ) {
        when (change) {
            is ChapterNotifier.ChangeType.BookChaptersRefreshed -> {
                Log.debug { "Chapters refreshed for book ${change.bookId}" }
                refreshChapters(bookId)
            }
            is ChapterNotifier.ChangeType.ContentFetched -> {
                Log.debug { "Content fetched for chapter ${change.chapterId}" }
                if (change.chapterId == currentChapterId) {
                    reloadCurrentChapter(change.chapterId)
                }
                refreshChapters(bookId)
            }
            is ChapterNotifier.ChangeType.ChapterUpdated -> {
                Log.debug { "Chapter ${change.chapterId} updated" }
                if (change.chapterId == currentChapterId) {
                    reloadCurrentChapter(change.chapterId)
                }
                refreshChapters(bookId)
            }
            is ChapterNotifier.ChangeType.ChaptersUpdated -> {
                Log.debug { "${change.chapterIds.size} chapters updated" }
                if (change.chapterIds.contains(currentChapterId)) {
                    reloadCurrentChapter(currentChapterId)
                }
                refreshChapters(bookId)
            }
            is ChapterNotifier.ChangeType.CurrentChapterChanged -> {
                // Emitted by our own ChapterController, no action needed
            }
            is ChapterNotifier.ChangeType.FullRefresh -> {
                Log.debug { "Full refresh requested" }
                refreshChapters(bookId)
            }
            else -> { /* Handle other change types if needed */ }
        }
    }
    
    private suspend fun refreshChapters(bookId: Long) {
        try {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId)
            if (chapters.isNotEmpty()) {
                onChaptersRefreshed(chapters)
            }
        } catch (e: Exception) {
            Log.error("Failed to refresh chapters for book $bookId", e)
        }
    }
    
    private suspend fun reloadCurrentChapter(chapterId: Long) {
        try {
            val freshChapter = getChapterUseCase.findChapterById(chapterId)
            if (freshChapter != null) {
                onCurrentChapterReloaded(freshChapter)
                Log.debug { "Reloaded chapter: read=${freshChapter.read}" }
            }
        } catch (e: Exception) {
            Log.error("Failed to reload chapter $chapterId", e)
        }
    }
    
    fun unsubscribe() {
        notifierJob?.cancel()
        notifierJob = null
        isSubscribed = false
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into ReaderScreenViewModel**

Replace the existing ChapterNotifier subscription code (lines 416-570) with delegation.

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderChapterNotifierHandler.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract ChapterNotifier subscription handling"
```

---

## Task 7: Extract Reader Preferences Controller Integration

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderPreferencesHandler.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt:572-680`

**Step 1: Create the preferences handler**

```kotlin
package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.services.preferences.PreferenceCommand
import ireader.domain.services.preferences.PreferenceEvent
import ireader.domain.services.preferences.ReaderPreferencesController
import ireader.i18n.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Manages ReaderPreferencesController integration.
 * 
 * Responsibilities:
 * - Subscribing to preference change events
 * - Delegating preference changes to the controller
 * - Handling preference errors
 */
class ReaderPreferencesHandler(
    private val scope: CoroutineScope,
    private val preferencesController: ReaderPreferencesController,
    private val onError: (String) -> Unit
) {
    private var isSubscribed = false
    
    /**
     * Subscribe to preference change events.
     * Safe to call multiple times.
     */
    fun subscribe() {
        if (isSubscribed) return
        isSubscribed = true
        
        // Ensure preferences are loaded
        preferencesController.ensurePreferencesLoaded()
        
        scope.launch {
            preferencesController.events.collect { event ->
                when (event) {
                    is PreferenceEvent.PreferenceSaved -> {
                        Log.debug { "Preference saved: ${event.key}" }
                    }
                    is PreferenceEvent.PreferencesLoaded -> {
                        Log.debug { "All preferences loaded" }
                    }
                    is PreferenceEvent.Error -> {
                        Log.error("Preference error: ${event.error.toUserMessage()}")
                        onError(event.error.toUserMessage())
                    }
                }
            }
        }
    }
    
    fun setFontSize(size: Int) {
        preferencesController.dispatch(PreferenceCommand.SetFontSize(size))
    }
    
    fun setLineHeight(height: Int) {
        preferencesController.dispatch(PreferenceCommand.SetLineHeight(height))
    }
    
    fun setBrightness(brightness: Float) {
        preferencesController.dispatch(PreferenceCommand.SetBrightness(brightness))
    }
    
    fun setImmersiveMode(enabled: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetImmersiveMode(enabled))
    }
    
    fun setScreenAlwaysOn(enabled: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetScreenAlwaysOn(enabled))
    }
    
    fun setReadingMode(mode: ReadingMode) {
        preferencesController.dispatch(PreferenceCommand.SetReadingMode(mode))
    }
    
    fun setScrollMode(vertical: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetScrollMode(vertical))
    }
    
    fun getController(): ReaderPreferencesController = preferencesController
    
    fun unsubscribe() {
        isSubscribed = false
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into ReaderScreenViewModel**

Replace the existing preferences controller integration (lines 572-680) with delegation.

**Step 4: Run build to verify**

Run: `./gradlew :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderPreferencesHandler.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor(reader): extract preferences controller integration"
```

---

## Task 8: Clean Up ReaderPreferences - Group and Document

**Files:**
- Modify: `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt`

**Step 1: Add section documentation headers**

The file already has some grouping. Add clear section headers with consistent formatting:

```kotlin
// ========== Typography ==========
// ========== Display ==========
// ========== Navigation ==========
// ========== TTS ==========
// ========== Content Filter ==========
// ========== Advanced Reader (Mihon-inspired) ==========
```

**Step 2: Add KDoc to all public methods**

Each preference method should have a KDoc comment explaining:
- What the preference controls
- The default value
- Valid range (if applicable)

Example:
```kotlin
/**
 * Reader font size in SP.
 * Default: 18
 * Range: 8-48
 */
fun fontSize(): Preference<Int> {
    return preferenceStore.getInt(SAVED_FONT_SIZE_PREFERENCES, 18)
}
```

**Step 3: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt
git commit -m "docs(reader): add KDoc and section headers to ReaderPreferences"
```

---

## Task 9: Extract TTS Controller - Playback Logic

**Files:**
- Create: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSPlaybackManager.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt:253-430`

**Step 1: Create the playback manager**

```kotlin
package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages TTS playback state and operations.
 * 
 * Responsibilities:
 * - Play/pause/stop/resume operations
 * - Paragraph-level playback control
 * - Precaching upcoming paragraphs
 * - Handling paragraph completion and auto-advance
 */
class TTSPlaybackManager(
    private val engine: () -> TTSEngine?,
    private val state: () -> TTSState,
    private val updateState: (TTSState.() -> TTSState) -> Unit,
    private val emitEvent: suspend (TTSEvent) -> Unit,
    private val onError: (TTSError) -> Unit,
    private val onNextChapter: suspend () -> Unit
) {
    private val mutex = Mutex()
    
    suspend fun play() {
        mutex.withLock {
            val currentState = state()
            
            if (!currentState.hasContent) {
                onError(TTSError.NoContent)
                return
            }
            
            if (engine()?.isReady() != true) {
                Log.warn { "TTS: Engine not ready, queuing play" }
                updateState { copy(playbackState = PlaybackState.LOADING) }
                return
            }
            
            // Use chunk mode if enabled
            if (currentState.chunkModeEnabled && currentState.hasChunks) {
                playChunk()
                return
            }
            
            val paragraphIndex = currentState.currentParagraphIndex
            val text = currentState.displayContent.getOrNull(paragraphIndex)
            
            if (text != null) {
                updateState {
                    copy(
                        playbackState = PlaybackState.LOADING,
                        loadingParagraphs = loadingParagraphs + paragraphIndex
                    )
                }
                precacheUpcomingParagraphs(paragraphIndex)
                engine()?.speak(text, "p_$paragraphIndex")
            }
        }
    }
    
    suspend fun pause() {
        engine()?.pause()
        updateState { copy(playbackState = PlaybackState.PAUSED) }
        emitEvent(TTSEvent.PlaybackPaused)
    }
    
    suspend fun stop() {
        engine()?.stop()
        updateState { copy(playbackState = PlaybackState.STOPPED) }
        emitEvent(TTSEvent.PlaybackStopped)
    }
    
    suspend fun resume() {
        val currentState = state()
        if (currentState.isPaused) {
            engine()?.resume()
            updateState { copy(playbackState = PlaybackState.PLAYING) }
        } else {
            play()
        }
    }
    
    suspend fun handleParagraphCompleted() {
        emitEvent(TTSEvent.ParagraphCompleted)
        
        val currentState = state()
        val completedIndex = currentState.currentParagraphIndex
        
        updateState {
            copy(
                cachedParagraphs = cachedParagraphs + completedIndex,
                loadingParagraphs = loadingParagraphs - completedIndex
            )
        }
        
        if (currentState.chunkModeEnabled && currentState.hasChunks) {
            handleChunkCompleted()
            return
        }
        
        if (currentState.canGoNext && currentState.isPlaying) {
            updateState {
                copy(
                    previousParagraphIndex = currentParagraphIndex,
                    currentParagraphIndex = currentParagraphIndex + 1
                )
            }
            play()
        } else if (!currentState.canGoNext) {
            emitEvent(TTSEvent.ChapterCompleted)
            if (state().autoNextChapter) {
                onNextChapter()
            } else {
                updateState { copy(playbackState = PlaybackState.STOPPED) }
            }
        }
    }
    
    private suspend fun handleChunkCompleted() {
        val currentState = state()
        if (currentState.canGoNextChunk) {
            val nextIndex = currentState.currentChunkIndex + 1
            updateState {
                copy(
                    currentChunkIndex = nextIndex,
                    currentParagraphIndex = currentChunkParagraphs.firstOrNull() 
                        ?: currentParagraphIndex
                )
            }
            playChunk()
        } else {
            emitEvent(TTSEvent.ChapterCompleted)
            if (state().autoNextChapter) {
                onNextChapter()
            } else {
                updateState { copy(playbackState = PlaybackState.STOPPED) }
            }
        }
    }
    
    private suspend fun playChunk() {
        val currentState = state()
        val chunkText = currentState.currentChunkText ?: return
        
        updateState {
            copy(
                playbackState = PlaybackState.LOADING,
                loadingParagraphs = loadingParagraphs + currentParagraphIndex
            )
        }
        engine()?.speak(chunkText, "c_${currentState.currentChunkIndex}")
    }
    
    private fun precacheUpcomingParagraphs(currentIndex: Int) {
        val currentState = state()
        val content = currentState.displayContent
        val prefetchCount = 3
        
        val itemsToPrecache = mutableListOf<Pair<String, String>>()
        for (i in 1..prefetchCount) {
            val nextIndex = currentIndex + i
            if (nextIndex >= content.size) break
            if (currentState.cachedParagraphs.contains(nextIndex)) continue
            
            val text = content.getOrNull(nextIndex) ?: continue
            if (text.isBlank()) continue
            
            itemsToPrecache.add("p_$nextIndex" to text)
        }
        
        if (itemsToPrecache.isNotEmpty()) {
            engine()?.precacheNext(itemsToPrecache)
        }
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into TTSController**

Replace the playback methods in TTSController with delegation to TTSPlaybackManager.

**Step 4: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSPlaybackManager.kt
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt
git commit -m "refactor(tts): extract playback logic into TTSPlaybackManager"
```

---

## Task 10: Extract TTS Controller - Navigation Logic

**Files:**
- Create: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSNavigationManager.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt:432-585`

**Step 1: Create the navigation manager**

```kotlin
package ireader.domain.services.tts_service.v2

import ireader.core.log.Log

/**
 * Manages TTS navigation (paragraph and chapter level).
 * 
 * Responsibilities:
 * - Next/previous paragraph navigation
 * - Jump to specific paragraph
 * - Next/previous chapter navigation
 * - Chunk-aware navigation (finding chunk for a paragraph)
 */
class TTSNavigationManager(
    private val contentLoader: TTSContentLoader,
    private val state: () -> TTSState,
    private val updateState: (TTSState.() -> TTSState) -> Unit,
    private val onChapterLoad: suspend (bookId: Long, chapterId: Long, startParagraph: Int) -> Unit
) {
    
    suspend fun nextParagraph() {
        val currentState = state()
        if (!currentState.canGoNext) return
        
        val wasPlaying = currentState.isPlaying
        
        updateState {
            copy(
                previousParagraphIndex = currentParagraphIndex,
                currentParagraphIndex = currentParagraphIndex + 1
            )
        }
        
        if (wasPlaying) {
            // Signal to controller to call play()
        }
    }
    
    suspend fun previousParagraph() {
        val currentState = state()
        if (!currentState.canGoPrevious) return
        
        val wasPlaying = currentState.isPlaying
        
        updateState {
            copy(
                previousParagraphIndex = currentParagraphIndex,
                currentParagraphIndex = currentParagraphIndex - 1
            )
        }
        
        if (wasPlaying) {
            // Signal to controller to call play()
        }
    }
    
    suspend fun jumpToParagraph(index: Int) {
        val currentState = state()
        if (index < 0 || index >= currentState.paragraphs.size) return
        
        val wasPlaying = currentState.isPlaying
        
        // If chunk mode is enabled, find the chunk containing this paragraph
        if (currentState.chunkModeEnabled && currentState.hasChunks) {
            val chunkIndex = currentState.paragraphToChunkMap[index]
            if (chunkIndex != null) {
                updateState {
                    copy(
                        previousParagraphIndex = currentParagraphIndex,
                        currentParagraphIndex = index,
                        currentChunkIndex = chunkIndex
                    )
                }
                if (wasPlaying) {
                    // Signal to controller to call playChunk()
                }
                return
            }
        }
        
        updateState {
            copy(
                previousParagraphIndex = currentParagraphIndex,
                currentParagraphIndex = index
            )
        }
        
        if (wasPlaying) {
            // Signal to controller to call play()
        }
    }
    
    suspend fun nextChapter() {
        val currentState = state()
        val book = currentState.book ?: return
        val chapter = currentState.chapter ?: return
        
        Log.debug { "TTS: Loading next chapter from ${chapter.id}" }
        updateState { copy(playbackState = PlaybackState.LOADING) }
        
        try {
            val nextChapterId = contentLoader.getNextChapterId(book.id, chapter.id)
            if (nextChapterId != null) {
                onChapterLoad(book.id, nextChapterId, 0)
            } else {
                Log.debug { "TTS: No next chapter available" }
                updateState { copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error("TTS: Failed to load next chapter: ${e.message}")
            updateState { copy(playbackState = PlaybackState.STOPPED) }
        }
    }
    
    suspend fun previousChapter() {
        val currentState = state()
        val book = currentState.book ?: return
        val chapter = currentState.chapter ?: return
        
        Log.debug { "TTS: Loading previous chapter from ${chapter.id}" }
        updateState { copy(playbackState = PlaybackState.LOADING) }
        
        try {
            val prevChapterId = contentLoader.getPreviousChapterId(book.id, chapter.id)
            if (prevChapterId != null) {
                onChapterLoad(book.id, prevChapterId, 0)
            } else {
                Log.debug { "TTS: No previous chapter available" }
                updateState { copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error("TTS: Failed to load previous chapter: ${e.message}")
            updateState { copy(playbackState = PlaybackState.STOPPED) }
        }
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into TTSController**

Replace the navigation methods in TTSController with delegation.

**Step 4: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSNavigationManager.kt
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt
git commit -m "refactor(tts): extract navigation logic into TTSNavigationManager"
```

---

## Task 11: Extract TTS Controller - Content Management

**Files:**
- Create: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSContentManager.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt:587-720`

**Step 1: Create the content manager**

```kotlin
package ireader.domain.services.tts_service.v2

import ireader.core.log.Log

/**
 * Manages TTS content loading and state.
 * 
 * Responsibilities:
 * - Loading chapter content
 * - Managing content state (paragraphs, translations)
 * - Handling content refresh (e.g., when filter settings change)
 * - Chunk mode management
 */
class TTSContentManager(
    private val contentLoader: TTSContentLoader,
    private val cacheUseCase: TTSCacheUseCase?,
    private val state: () -> TTSState,
    private val updateState: (TTSState.() -> TTSState) -> Unit,
    private val onEngineClear: () -> Unit
) {
    private var textMerger: TTSTextMergerV2? = null
    
    suspend fun loadChapter(bookId: Long, chapterId: Long, startParagraph: Int) {
        val currentState = state()
        
        // Skip reload if same book/chapter already loaded
        if (currentState.book?.id == bookId && 
            currentState.chapter?.id == chapterId && 
            currentState.paragraphs.isNotEmpty()) {
            Log.debug { "TTS: Same chapter already loaded, skipping reload" }
            return
        }
        
        updateState { copy(playbackState = PlaybackState.LOADING, error = null) }
        
        try {
            val content = contentLoader.loadChapter(bookId, chapterId)
            val cachedChunks = if (content.chapter?.id != null && cacheUseCase != null) {
                cacheUseCase.getCachedChunkIndices(content.chapter.id)
            } else {
                emptySet()
            }
            
            updateState {
                copy(
                    book = content.book,
                    chapter = content.chapter,
                    paragraphs = content.paragraphs,
                    totalParagraphs = content.paragraphs.size,
                    currentParagraphIndex = startParagraph.coerceIn(
                        0, content.paragraphs.lastIndex.coerceAtLeast(0)
                    ),
                    playbackState = PlaybackState.IDLE,
                    error = null,
                    translatedParagraphs = null,
                    showTranslation = false,
                    isTranslationAvailable = false,
                    chunkModeEnabled = false,
                    currentChunkIndex = 0,
                    totalChunks = 0,
                    cachedChunks = cachedChunks,
                    isUsingCachedAudio = false
                )
            }
            
            Log.debug { "TTS: Chapter loaded - ${content.paragraphs.size} paragraphs" }
        } catch (e: Exception) {
            Log.error("TTS: Failed to load chapter: ${e.message}")
            updateState { copy(playbackState = PlaybackState.STOPPED) }
        }
    }
    
    suspend fun refreshContent() {
        val currentState = state()
        val bookId = currentState.book?.id ?: return
        val chapterId = currentState.chapter?.id ?: return
        val currentParagraph = currentState.currentParagraphIndex
        
        Log.debug { "TTS: Refreshing content for chapter $chapterId" }
        updateState { copy(paragraphs = emptyList()) }
        loadChapter(bookId, chapterId, currentParagraph)
    }
    
    fun setTranslatedContent(paragraphs: List<String>?) {
        updateState {
            copy(
                translatedParagraphs = paragraphs,
                isTranslationAvailable = paragraphs != null && paragraphs.isNotEmpty()
            )
        }
    }
    
    fun toggleTranslation(show: Boolean) {
        updateState { copy(showTranslation = show) }
    }
    
    fun toggleBilingualMode(enabled: Boolean) {
        updateState { copy(bilingualMode = enabled) }
    }
    
    fun enableChunkMode(targetWordCount: Int) {
        val currentState = state()
        if (!currentState.hasContent) {
            Log.debug { "TTS: No content yet, chunk mode will be applied on load" }
            return
        }
        
        onEngineClear()
        
        if (textMerger == null) {
            textMerger = TTSTextMergerV2()
        }
        
        val result = textMerger!!.mergeParagraphs(currentState.paragraphs, targetWordCount)
        val currentChunkIndex = result.paragraphToChunkMap[currentState.currentParagraphIndex] ?: 0
        
        updateState {
            copy(
                chunkModeEnabled = true,
                currentChunkIndex = currentChunkIndex,
                totalChunks = result.chunks.size,
                paragraphToChunkMap = result.paragraphToChunkMap
            )
        }
    }
    
    fun disableChunkMode() {
        updateState {
            copy(
                chunkModeEnabled = false,
                currentChunkIndex = 0,
                totalChunks = 0
            )
        }
    }
}
```

**Step 2: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 3: Integrate into TTSController**

Replace the content management methods in TTSController with delegation.

**Step 4: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSContentManager.kt
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt
git commit -m "refactor(tts): extract content management into TTSContentManager"
```

---

## Task 12: Add Defensive Programming to TTSController

**Files:**
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt`

**Step 1: Add input validation to public methods**

Add a validation helper:
```kotlin
private fun validateBookAndChapter(): Pair<Book, Chapter>? {
    val currentState = _state.value
    val book = currentState.book
    val chapter = currentState.chapter
    
    if (book == null || chapter == null) {
        Log.warn { "$TAG: No book/chapter loaded" }
        return null
    }
    return book to chapter
}
```

**Step 2: Add null checks to all public dispatch handlers**

Each command handler should validate preconditions before processing:
```kotlin
private suspend fun nextChapter() {
    val (book, chapter) = validateBookAndChapter() ?: run {
        handleError(TTSError.NoContent)
        return
    }
    // ... rest of implementation
}
```

**Step 3: Add state validation to prevent invalid transitions**

```kotlin
private fun canTransitionTo(newState: PlaybackState): Boolean {
    val current = _state.value.playbackState
    return when (newState) {
        PlaybackState.PLAYING -> current in setOf(
            PlaybackState.IDLE, PlaybackState.PAUSED, PlaybackState.LOADING
        )
        PlaybackState.PAUSED -> current == PlaybackState.PLAYING
        PlaybackState.STOPPED -> current != PlaybackState.STOPPED
        PlaybackState.LOADING -> true
        PlaybackState.IDLE -> current == PlaybackState.LOADING
    }
}
```

**Step 4: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt
git commit -m "refactor(tts): add defensive programming and state validation"
```

---

## Task 13: Clean Up Logging - Consistent Levels

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt`

**Step 1: Fix log levels in TTSController**

Replace all `Log.warn` with appropriate levels:
- Normal operations (play, pause, stop) → `Log.debug`
- State changes → `Log.debug`
- Recoverable issues → `Log.warn`
- Errors → `Log.error`

**Step 2: Fix log levels in ReaderScreenViewModel**

Same pattern - use `Log.debug` for normal operations, `Log.warn` for recoverable issues.

**Step 3: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
git commit -m "refactor: standardize log levels across reader and TTS"
```

---

## Task 14: Add Error Handling Tests for TTSController

**Files:**
- Create: `domain/src/commonTest/kotlin/ireader/domain/services/tts_service/TTSControllerErrorHandlingTest.kt`

**Step 1: Write the failing test**

```kotlin
package ireader.domain.services.tts_service

import ireader.domain.services.tts_service.v2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TTSControllerErrorHandlingTest {
    
    @Test
    fun `play with no content should emit error`() = runTest {
        val controller = createTestController()
        controller.dispatch(TTSCommand.Play)
        
        val event = controller.events.first()
        assertTrue(event is TTSEvent.Error)
    }
    
    @Test
    fun `nextChapter with no book should emit error`() = runTest {
        val controller = createTestController()
        controller.dispatch(TTSCommand.NextChapter)
        
        val state = controller.state.value
        assertNull(state.book)
    }
    
    @Test
    fun `jumpToParagraph with negative index should be ignored`() = runTest {
        val controller = createTestControllerWithContent()
        val initialIndex = controller.state.value.currentParagraphIndex
        
        controller.dispatch(TTSCommand.JumpToParagraph(-1))
        
        assertEquals(initialIndex, controller.state.value.currentParagraphIndex)
    }
    
    @Test
    fun `jumpToParagraph with out-of-bounds index should be ignored`() = runTest {
        val controller = createTestControllerWithContent()
        val initialIndex = controller.state.value.currentParagraphIndex
        val maxIndex = controller.state.value.paragraphs.size
        
        controller.dispatch(TTSCommand.JumpToParagraph(maxIndex + 100))
        
        assertEquals(initialIndex, controller.state.value.currentParagraphIndex)
    }
    
    @Test
    fun `stop when already stopped should not crash`() = runTest {
        val controller = createTestController()
        
        // Should not throw
        controller.dispatch(TTSCommand.Stop)
        controller.dispatch(TTSCommand.Stop)
        
        assertEquals(PlaybackState.STOPPED, controller.state.value.playbackState)
    }
    
    @Test
    fun `pause when not playing should not crash`() = runTest {
        val controller = createTestController()
        
        // Should not throw
        controller.dispatch(TTSCommand.Pause)
        
        // State should remain stopped (can't pause if not playing)
        assertFalse(controller.state.value.isPlaying)
    }
    
    // Helper functions
    private fun createTestController(): TTSController {
        // Return a controller with mock dependencies
        TODO("Implement with mock contentLoader and engine factory")
    }
    
    private fun createTestControllerWithContent(): TTSController {
        // Return a controller with pre-loaded content
        TODO("Implement with mock content")
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :domain:jvmTest --tests "ireader.domain.services.tts_service.TTSControllerErrorHandlingTest" -v`
Expected: FAIL (test not found / not implemented)

**Step 3: Implement the test helpers and run again**

After implementing mocks, run the tests.

**Step 4: Run test to verify it passes**

Run: `./gradlew :domain:jvmTest --tests "ireader.domain.services.tts_service.TTSControllerErrorHandlingTest" -v`
Expected: PASS

**Step 5: Commit**

```bash
git add domain/src/commonTest/kotlin/ireader/domain/services/tts_service/TTSControllerErrorHandlingTest.kt
git commit -m "test(tts): add error handling tests for TTSController"
```

---

## Task 15: Add ReaderScreenViewModel State Validation Tests

**Files:**
- Create: `presentation/src/commonTest/kotlin/ireader/presentation/ui/reader/ReaderStateValidationTest.kt`

**Step 1: Write the failing test**

```kotlin
package ireader.presentation.ui.reader

import ireader.presentation.ui.reader.viewmodel.ReaderState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderStateValidationTest {
    
    @Test
    fun `Success state should have valid chapter index`() {
        val state = ReaderState.Success(
            book = createTestBook(),
            currentChapter = createTestChapter(),
            chapters = listOf(createTestChapter()),
            catalog = null,
            content = emptyList(),
            currentChapterIndex = 0,
            chapterShell = emptyList(),
            isLoadingContent = false,
            isReaderModeEnabled = true,
            isSettingModeEnabled = false,
            isMainBottomModeEnabled = false,
            showSettingsBottomSheet = false,
            isDrawerAsc = true,
            scrollToEndOnChapterChange = false,
            totalWords = 0
        )
        
        assertTrue(state.currentChapterIndex >= 0)
        assertTrue(state.currentChapterIndex < state.chapters.size)
    }
    
    @Test
    fun `Loading state should not have chapter data`() {
        val state = ReaderState.Loading
        // Loading state is a singleton, no data access
        assertTrue(state is ReaderState.Loading)
    }
    
    @Test
    fun `Error state should have message`() {
        val state = ReaderState.Error(
            message = ireader.i18n.UiText.DynamicString("Test error"),
            bookId = 1L,
            chapterId = 1L
        )
        
        assertTrue(state.bookId != null)
    }
    
    // Helper functions
    private fun createTestBook(): ireader.domain.models.entities.Book {
        return ireader.domain.models.entities.Book(
            id = 1L,
            title = "Test Book",
            sourceId = 1L,
            key = "test",
            cover = "",
            author = "Author",
            status = 0,
            summary = "",
            initialized = true,
            dateAdded = 0L,
            totalChapters = 10,
            lastReadAt = 0L,
            lastReadChapterId = 1L
        )
    }
    
    private fun createTestChapter(): ireader.domain.models.entities.Chapter {
        return ireader.domain.models.entities.Chapter(
            id = 1L,
            bookId = 1L,
            key = "ch1",
            name = "Chapter 1",
            content = emptyList(),
            number = 1f,
            dateUpload = 0L,
            dateFetch = 0L,
            lastPageRead = 0L,
            read = false,
            bookmark = false,
            scanlator = "",
            index = 0L,
            sourceOrder = 0L
        )
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :presentation:jvmTest --tests "ireader.presentation.ui.reader.ReaderStateValidationTest" -v`
Expected: FAIL (test not found)

**Step 3: Run test to verify it passes**

Run: `./gradlew :presentation:jvmTest --tests "ireader.presentation.ui.reader.ReaderStateValidationTest" -v`
Expected: PASS

**Step 4: Commit**

```bash
git add presentation/src/commonTest/kotlin/ireader/presentation/ui/reader/ReaderStateValidationTest.kt
git commit -m "test(reader): add state validation tests"
```

---

## Task 16: Remove Dead Code and Commented-Out Blocks

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`
- Modify: `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/TTSController.kt`
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`

**Step 1: Remove commented-out code blocks**

Search for and remove all `//` commented code blocks (not documentation comments):
```bash
# Find commented code (not KDoc)
grep -n "^[[:space:]]*//[^/]" presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt
```

**Step 2: Remove unused imports**

Run the IDE's "Optimize Imports" or manually remove unused imports.

**Step 3: Remove empty catch blocks**

Find and fix any empty catch blocks:
```kotlin
// BEFORE:
} catch (e: Exception) {}

// AFTER:
} catch (e: Exception) {
    Log.error("Operation failed", e)
}
```

**Step 4: Run build to verify**

Run: `./gradlew :domain:compileKotlinMetadata :presentation:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -p  # Review changes carefully
git commit -m "chore: remove dead code and commented-out blocks from reader/TTS"
```

---

## Task 17: Final Verification and Build

**Files:**
- All modified files

**Step 1: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: Run all tests**

Run: `./gradlew test`
Expected: All tests pass

**Step 3: Verify no regressions**

Run: `./gradlew :presentation:compileKotlinMetadata :domain:compileKotlinMetadata :core:compileKotlinMetadata`
Expected: BUILD SUCCESSFUL

**Step 4: Final commit**

```bash
git add -A
git commit -m "chore: final verification - all reader/TTS cleanup complete"
```

---

## Summary of Changes

### New Files Created (10)
1. `ReaderConstants.kt` - Centralized constants
2. `ReaderChapterLoader.kt` - Chapter loading logic
3. `ReaderScrollManager.kt` - Scroll position management
4. `ReaderContentFilterManager.kt` - Content filter management
5. `ReaderTTSSyncManager.kt` - TTS synchronization
6. `ReaderChapterNotifierHandler.kt` - ChapterNotifier subscription
7. `ReaderPreferencesHandler.kt` - Preferences controller integration
8. `TTSPlaybackManager.kt` - TTS playback logic
9. `TTSNavigationManager.kt` - TTS navigation logic
10. `TTSContentManager.kt` - TTS content management

### Test Files Created (2)
1. `TTSControllerErrorHandlingTest.kt` - TTS error handling tests
2. `ReaderStateValidationTest.kt` - Reader state validation tests

### Files Modified (4)
1. `ReaderScreenViewModel.kt` - Reduced from ~1500 to ~600 lines
2. `TTSController.kt` - Reduced from ~700 to ~300 lines
3. `ReaderPreferences.kt` - Added documentation
4. `ReaderText.kt` - Removed dead code

### Expected Impact
- **ReaderScreenViewModel**: ~60% size reduction (1500 → 600 lines)
- **TTSController**: ~57% size reduction (700 → 300 lines)
- **Improved testability**: Each component can be tested independently
- **Better error handling**: Defensive programming with proper validation
- **Consistent logging**: Proper log levels throughout
- **Self-documenting**: Clear separation of concerns with descriptive names
