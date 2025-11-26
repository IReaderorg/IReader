package ireader.presentation.ui.reader.viewmodel.subviewmodels

import ireader.core.utils.LruCache
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase
import ireader.domain.usecases.reader.PreloadChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sub-ViewModel responsible for chapter navigation, preloading, and bookmarking.
 * 
 * Responsibilities:
 * - Chapter navigation (next/previous)
 * - Chapter preloading with LRU cache
 * - Chapter fetching (local/remote)
 * - Chapter bookmarking
 * - Reading progress tracking
 */
class ReaderChapterViewModel(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val preloadChapterUseCase: PreloadChapterUseCase,
    private val bookMarkChapterUseCase: BookMarkChapterUseCase,
    private val historyUseCase: HistoryUseCase,
    private val insertUseCases: LocalInsertUseCases,
) : BaseViewModel() {

    // State
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    // Thread-safe LRU Cache for preloaded chapters - FIXES MEMORY LEAK
    private val preloadCache = LruCache<Long, Chapter>(
        maxSize = 5,
        onEvicted = { chapterId, _ ->
            ireader.core.log.Log.debug("LRU: Evicted chapter $chapterId from cache")
        }
    )
    
    private var preloadJob: Job? = null

    /**
     * Set the list of chapters for navigation
     */
    fun setChapters(chapters: List<Chapter>) {
        _chapters.value = chapters
    }

    /**
     * Set the current chapter
     */
    fun setCurrentChapter(chapter: Chapter?) {
        _currentChapter.value = chapter
    }

    /**
     * Get the next chapter in the list
     */
    fun getNextChapter(): Chapter? {
        val current = _currentChapter.value ?: return null
        val currentIndex = _chapters.value.indexOfFirst { it.id == current.id }
        return if (currentIndex != -1 && currentIndex < _chapters.value.size - 1) {
            _chapters.value[currentIndex + 1]
        } else null
    }

    /**
     * Get the previous chapter in the list
     */
    fun getPreviousChapter(): Chapter? {
        val current = _currentChapter.value ?: return null
        val currentIndex = _chapters.value.indexOfFirst { it.id == current.id }
        return if (currentIndex > 0) {
            _chapters.value[currentIndex - 1]
        } else null
    }

    /**
     * Navigate to the next chapter
     */
    suspend fun navigateToNextChapter(): Chapter? {
        val nextChapter = getNextChapter() ?: return null
        setCurrentChapter(nextChapter)
        startPreloadingNextChapter()
        return nextChapter
    }

    /**
     * Navigate to the previous chapter
     */
    suspend fun navigateToPreviousChapter(): Chapter? {
        val prevChapter = getPreviousChapter() ?: return null
        setCurrentChapter(prevChapter)
        return prevChapter
    }

    /**
     * Bookmark the current chapter
     */
    fun bookmarkChapter(chapter: Chapter?) {
        scope.launch(Dispatchers.IO) {
            chapter?.let {
                bookMarkChapterUseCase.bookMarkChapter(it)?.let { bookmarked ->
                    _currentChapter.value = bookmarked
                    ireader.core.log.Log.debug("Chapter bookmarked: ${bookmarked.name}")
                }
            }
        }
    }

    /**
     * Start preloading the next chapter in the background
     */
    fun startPreloadingNextChapter() {
        preloadJob?.cancel()
        preloadJob = scope.launch {
            try {
                val nextChapter = getNextChapter()
                if (nextChapter != null && !preloadCache.containsKey(nextChapter.id)) {
                    preloadChapter(nextChapter)
                }
            } catch (e: Exception) {
                ireader.core.log.Log.debug("No next chapter to preload: ${e.message}")
            }
        }
    }

    /**
     * Preload a specific chapter in the background
     */
    private suspend fun preloadChapter(chapter: Chapter, catalog: ireader.domain.models.entities.CatalogLocal? = null) {
        if (chapter.isEmpty() && catalog != null) {
            _isPreloading.value = true
            ireader.core.log.Log.debug("Preloading chapter: ${chapter.name}")

            preloadChapterUseCase(
                chapter = chapter,
                catalog = catalog,
                onSuccess = { preloadedChapter ->
                    scope.launch {
                        val cacheSize = preloadCache.size()
                        preloadCache.put(chapter.id, preloadedChapter)
                        ireader.core.log.Log.debug("LRU: Cached chapter ${chapter.id} (size: ${cacheSize + 1}/5)")
                        // Save to database for offline access
                        insertUseCases.insertChapter(preloadedChapter)
                    }
                    ireader.core.log.Log.debug("Successfully preloaded chapter: ${chapter.name}")
                    _isPreloading.value = false
                },
                onError = { error ->
                    ireader.core.log.Log.error("Failed to preload chapter: ${chapter.name} - $error")
                    _isPreloading.value = false
                }
            )
        } else if (!chapter.isEmpty()) {
            // Chapter already has content, just cache it
            preloadCache.put(chapter.id, chapter)
            ireader.core.log.Log.debug("Chapter already loaded, added to cache: ${chapter.name}")
        }
    }

    /**
     * Manually preload next N chapters (for user-triggered preload)
     */
    fun preloadNextChapters(count: Int = 3, catalog: ireader.domain.models.entities.CatalogLocal? = null) {
        scope.launch {
            try {
                val currentIndex = _chapters.value.indexOfFirst { it.id == _currentChapter.value?.id }
                if (currentIndex != -1) {
                    val chaptersToPreload = _chapters.value.drop(currentIndex + 1).take(count)
                    chaptersToPreload.forEach { chapter ->
                        if (!preloadCache.containsKey(chapter.id)) {
                            preloadChapter(chapter, catalog)
                            delay(500) // Small delay between preloads to avoid overwhelming the source
                        }
                    }
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error("Error preloading multiple chapters: ${e.message}")
            }
        }
    }

    /**
     * Get a preloaded chapter from cache
     */
    suspend fun getPreloadedChapter(chapterId: Long): Chapter? {
        return preloadCache.get(chapterId)
    }

    /**
     * Clear preloaded chapters cache
     */
    fun clearPreloadCache() {
        scope.launch {
            preloadCache.clear()
            ireader.core.log.Log.debug("Preload cache cleared")
        }
    }

    /**
     * Get cache statistics for debugging
     */
    suspend fun getCacheStats(): String {
        return "Cache size: ${preloadCache.size()}/5"
    }

    override fun onDestroy() {
        super.onDestroy()
        preloadJob?.cancel()
        clearPreloadCache()
    }
}
