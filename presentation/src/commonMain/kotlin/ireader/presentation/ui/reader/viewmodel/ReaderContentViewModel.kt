package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.IssueCategory
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.services.ChapterHealthChecker
import ireader.domain.services.chapter.ChapterCommand
import ireader.domain.services.chapter.ChapterController
import ireader.domain.services.chapter.ChapterEvent
import ireader.domain.services.chapter.ChapterNotifier
import ireader.domain.usecases.chapter.AutoRepairChapterUseCase
import ireader.domain.usecases.reader.ReaderUseCasesAggregate
import ireader.domain.utils.extensions.ioDispatcher
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.NO_VALUE
import ireader.i18n.UiText
import ireader.presentation.ui.reader.ReaderConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for chapter loading, content fetching, chapter navigation,
 * chapter health checking, and chapter repair logic.
 *
 * Extracted from ReaderScreenViewModel to separate content concerns from UI/preference concerns.
 * Holds content-related state flows that the parent VM observes and syncs into ReaderState.
 *
 * Requirements: 5.1, 9.2, 9.4, 9.5
 */
class ReaderContentViewModel(
    private val scope: CoroutineScope,
    private val readerUseCasesAggregate: ReaderUseCasesAggregate,
    private val chapterHealthChecker: ChapterHealthChecker,
    private val autoRepairChapterUseCase: AutoRepairChapterUseCase,
    private val chapterController: ChapterController,
    private val chapterNotifier: ChapterNotifier,
    private val getLocalCatalog: GetLocalCatalog,
    private val readingMode: () -> ReadingMode,
    private val autoPreloadNextChapter: () -> Boolean,
    // State management callbacks
    private val getState: () -> ReaderState,
    private val setState: (ReaderState) -> Unit,
    private val updateSuccessState: ((ReaderState.Success) -> ReaderState.Success) -> Unit,
    private val showSnackBar: (UiText) -> Unit,
    // Cross-cutting concerns called around chapter loading
    private val onBeforeChapterLoad: suspend () -> Unit = {},
    private val onAfterChapterLoad: suspend (chapter: Chapter, isLastChapter: Boolean) -> Unit = { _, _ -> },
) {

    // Convenience accessors for aggregate use cases
    private val getBookUseCases get() = readerUseCasesAggregate.getBookUseCases
    private val getChapterUseCase get() = readerUseCasesAggregate.getChapterUseCase
    private val remoteUseCases get() = readerUseCasesAggregate.remoteUseCases
    private val historyUseCase get() = readerUseCasesAggregate.historyUseCase
    private val reportBrokenChapterUseCase get() = readerUseCasesAggregate.reportBrokenChapter
    private val getGlossaryByBookIdUseCase get() = readerUseCasesAggregate.getGlossaryByBookId

    // ==================== Content State Flows ====================

    val content = MutableStateFlow<List<Page>>(emptyList())
    val chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val currentChapter = MutableStateFlow<Chapter?>(null)
    val isLoadingContent = MutableStateFlow(false)

    // ==================== Internal State ====================

    private var preloadJob: Job? = null
    private var chapterNavigationJob: Job? = null
    private var chapterControllerEventJob: Job? = null
    private var chapterNotifierJob: Job? = null
    private val preloadedChapters = mutableMapOf<Long, Chapter>()

    // ==================== Initialization ====================

    fun initializeReader(bookId: Long, chapterId: Long) {
        subscribeToChapterControllerEvents()
        subscribeToChapterNotifier(bookId)

        scope.launch {
            try {
                val book = getBookUseCases.findBookById(bookId)
                if (book == null) {
                    setState(
                        ReaderState.Error(
                            message = UiText.DynamicString("Book not found"),
                            bookId = bookId
                        )
                    )
                    return@launch
                }

                val catalog = getLocalCatalog.get(book.sourceId)

                chapterController.setCatalog(catalog)
                chapterController.dispatch(ChapterCommand.LoadBook(bookId))

                delay(ReaderConstants.CHAPTER_CONTROLLER_INIT_DELAY_MS)

                setupChapters(book, catalog, bookId, chapterId)

                loadGlossary(bookId)

            } catch (e: Exception) {
                Log.error("Failed to initialize reader", e)
                setState(
                    ReaderState.Error(
                        message = UiText.DynamicString(e.message ?: "Failed to initialize reader"),
                        bookId = bookId,
                        chapterId = chapterId
                    )
                )
            }
        }
    }

    private suspend fun setupChapters(
        book: Book,
        catalog: CatalogLocal?,
        bookId: Long,
        chapterId: Long
    ) {
        val last = historyUseCase.findHistoryByBookId(bookId)

        val targetChapterId = when {
            chapterId != LAST_CHAPTER && chapterId != NO_VALUE -> chapterId
            last != null -> last.chapterId
            else -> {
                val chaptersList = getChaptersFromController().ifEmpty {
                    getChapterUseCase.findChaptersByBookId(bookId)
                }
                chaptersList.firstOrNull()?.id
            }
        }

        if (targetChapterId != null) {
            loadChapter(book, catalog, targetChapterId, next = true)
        } else {
            setState(
                ReaderState.Error(
                    message = UiText.DynamicString("No chapters found"),
                    bookId = bookId
                )
            )
        }
    }

    // ==================== ChapterController Integration ====================

    private fun getChaptersFromController(): List<Chapter> {
        return chapterController.state.value.chapters
    }

    private fun subscribeToChapterControllerEvents() {
        chapterControllerEventJob?.cancel()
        chapterControllerEventJob = scope.launch {
            chapterController.events.collect { event ->
                when (event) {
                    is ChapterEvent.ChapterLoaded -> {
                        Log.debug { "ChapterController: Chapter loaded - ${event.chapter.id}" }
                    }
                    is ChapterEvent.Error -> {
                        Log.error { "ChapterController: Error - ${event.error}" }
                        showSnackBar(UiText.DynamicString(event.error.toUserMessage()))
                    }
                    is ChapterEvent.ContentFetched -> {
                        Log.debug { "ChapterController: Content fetched for chapter ${event.chapterId}" }
                    }
                    is ChapterEvent.ProgressSaved -> {
                        Log.debug { "ChapterController: Progress saved for chapter ${event.chapterId}" }
                    }
                    is ChapterEvent.ChapterCompleted -> {
                        Log.debug { "ChapterController: Chapter completed" }
                    }
                }
            }
        }
    }

    // ==================== ChapterNotifier Integration ====================

    private fun subscribeToChapterNotifier(bookId: Long) {
        chapterNotifierJob?.cancel()
        chapterNotifierJob = scope.launch {
            chapterNotifier.changesForBookDebounced(bookId, debounceMs = 100)
                .collect { change ->
                    val currentState = getState() as? ReaderState.Success ?: return@collect

                    when (change) {
                        is ChapterNotifier.ChangeType.BookChaptersRefreshed -> {
                            Log.debug { "ChapterNotifier: Chapters refreshed for book ${change.bookId}" }
                            refreshChaptersFromController()
                        }
                        is ChapterNotifier.ChangeType.ContentFetched -> {
                            Log.debug { "ChapterNotifier: Content fetched for chapter ${change.chapterId}" }
                            if (change.chapterId == currentState.currentChapter.id) {
                                reloadCurrentChapterContent()
                            }
                            refreshChaptersFromController()
                        }
                        is ChapterNotifier.ChangeType.ChapterUpdated -> {
                            Log.debug { "ChapterNotifier: Chapter ${change.chapterId} updated" }
                            if (change.chapterId == currentState.currentChapter.id) {
                                reloadCurrentChapterContent()
                            }
                            refreshChaptersFromController()
                        }
                        is ChapterNotifier.ChangeType.ChaptersUpdated -> {
                            Log.debug { "ChapterNotifier: ${change.chapterIds.size} chapters updated" }
                            if (change.chapterIds.contains(currentState.currentChapter.id)) {
                                reloadCurrentChapterContent()
                            }
                            refreshChaptersFromController()
                        }
                        is ChapterNotifier.ChangeType.CurrentChapterChanged -> {
                        }
                        is ChapterNotifier.ChangeType.FullRefresh -> {
                            Log.debug { "ChapterNotifier: Full refresh requested" }
                            refreshChaptersFromController()
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun refreshChaptersFromController() {
        val currentState = getState() as? ReaderState.Success ?: return

        scope.launch {
            try {
                Log.debug { "refreshChaptersFromController: Refreshing chapters for book ${currentState.book.id}" }

                val chaptersList = getChapterUseCase.findChaptersByBookId(currentState.book.id)
                Log.debug {
                    "refreshChaptersFromController: Got ${chaptersList.size} chapters, current chapter id=${currentState.currentChapter.id}"
                }

                if (chaptersList.isNotEmpty()) {
                    updateSuccessState { state ->
                        val newIndex = chaptersList.indexOfFirst { it.id == state.currentChapter.id }
                            .coerceAtLeast(0)
                        state.copy(
                            chapters = chaptersList,
                            currentChapterIndex = newIndex
                        )
                    }
                }
            } catch (e: Exception) {
                Log.error { "Failed to refresh chapters: ${e.message}" }
            }
        }
    }

    private fun reloadCurrentChapterContent() {
        val currentState = getState() as? ReaderState.Success ?: return
        scope.launch {
            val freshChapter = getChapterUseCase.findChapterById(currentState.currentChapter.id)
            if (freshChapter != null) {
                updateSuccessState { state ->
                    val updatedChapters = state.chapters.map { ch ->
                        if (ch.id == freshChapter.id) freshChapter else ch
                    }
                    state.copy(
                        currentChapter = freshChapter,
                        content = if (freshChapter.content.isNotEmpty()) freshChapter.content else state.content,
                        chapters = updatedChapters
                    )
                }
                Log.debug { "Reloaded chapter: read=${freshChapter.read}, content=${freshChapter.content.size} pages" }
            }
        }
    }

    fun refreshCurrentChapterFromDatabase() {
        val currentState = getState() as? ReaderState.Success ?: return
        scope.launch {
            val freshChapter = getChapterUseCase.findChapterById(currentState.currentChapter.id)
            if (freshChapter != null) {
                updateSuccessState { state ->
                    state.copy(
                        currentChapter = freshChapter,
                        content = if (freshChapter.content.isNotEmpty()) freshChapter.content else state.content
                    )
                }
            }
        }
    }

    // ==================== Chapter Loading ====================

    /**
     * Load a chapter by ID.
     * This is the core content loading method. Pre/post hooks (statistics, translation, scroll save)
     * are handled via [onBeforeChapterLoad] and [onAfterChapterLoad] callbacks.
     */
    internal suspend fun loadChapter(
        book: Book,
        catalog: CatalogLocal?,
        chapterId: Long,
        next: Boolean = true,
        force: Boolean = false,
        scrollToEnd: Boolean? = null,
        forceRemote: Boolean = false
    ): Chapter? {
        onBeforeChapterLoad()

        val preloadedChapter = preloadedChapters.remove(chapterId)
        val chapter = preloadedChapter?.takeIf { !it.isEmpty() }
            ?: getChapterUseCase.findChapterById(chapterId)

        if (chapter == null) {
            showSnackBar(UiText.DynamicString("Chapter not found"))
            return null
        }

        Log.debug { "loadChapter: chapterId=${chapter.id}, lastPageRead=${chapter.lastPageRead}, contentSize=${chapter.content.size}" }

        val chaptersList = getChaptersFromController().ifEmpty {
            getChapterUseCase.findChaptersByBookId(book.id)
        }
        val chapterIndex = chaptersList.indexOfFirst { it.id == chapter.id }

        val currentState = getState()
        val newChapterShell = if (currentState is ReaderState.Success) {
            if (next) {
                currentState.chapterShell + chapter
            } else {
                listOf(chapter) + currentState.chapterShell
            }
        } else {
            listOf(chapter)
        }

        val previousSuccessState = currentState as? ReaderState.Success

        val totalWords = calculateTotalWords(chapter.content)

        val effectiveChapters =
            chaptersList.ifEmpty { previousSuccessState?.chapters ?: emptyList() }
        val effectiveIndex = if (chaptersList.isNotEmpty()) chapterIndex else {
            effectiveChapters.indexOfFirst { it.id == chapter.id }
        }

        setState(
            ReaderState.Success(
                book = book,
                currentChapter = chapter,
                chapters = effectiveChapters,
                catalog = catalog,
                content = chapter.content,
                currentChapterIndex = if (effectiveIndex != -1) effectiveIndex else 0,
                chapterShell = newChapterShell,
                isLoadingContent = forceRemote || (chapter.isEmpty() && catalog?.source != null),
                isReaderModeEnabled = previousSuccessState?.isReaderModeEnabled ?: true,
                isSettingModeEnabled = previousSuccessState?.isSettingModeEnabled ?: false,
                isMainBottomModeEnabled = previousSuccessState?.isMainBottomModeEnabled ?: false,
                showSettingsBottomSheet = previousSuccessState?.showSettingsBottomSheet ?: false,
                isDrawerAsc = previousSuccessState?.isDrawerAsc ?: true,
                scrollToEndOnChapterChange = scrollToEnd ?: !next,
                totalWords = totalWords,
            )
        )

        val needsRemoteFetch = chapter.isEmpty() && catalog?.source != null
        if (forceRemote && catalog?.source != null) {
            fetchRemoteChapter(book, catalog, chapter)
        } else if (needsRemoteFetch && !force) {
            fetchRemoteChapter(book, catalog, chapter)
        } else if (!needsRemoteFetch) {
            chapterController.dispatch(ChapterCommand.LoadChapter(chapterId))
            checkChapterHealth(chapter)
        }

        getChapterUseCase.updateLastReadTime(chapter)

        val isLastChapter =
            effectiveIndex != -1 && effectiveIndex == effectiveChapters.lastIndex
        onAfterChapterLoad(chapter, isLastChapter)

        if (!needsRemoteFetch) {
            triggerPreloadNextChapter()
        }

        return chapter
    }

    private fun calculateTotalWords(content: List<Page>): Int {
        return content.filterIsInstance<Text>()
            .sumOf { text ->
                text.text.split(Regex("\\s+")).count { it.isNotBlank() }
            }
    }

    // ==================== Remote Fetching ====================

    private suspend fun fetchRemoteChapter(
        book: Book,
        catalog: CatalogLocal?,
        chapter: Chapter
    ) {
        remoteUseCases.fetchAndSaveChapterContent(
            chapter = chapter,
            catalog = catalog,
            onSuccess = { filteredChapter ->
                val totalWords = calculateTotalWords(filteredChapter.content)

                updateSuccessState { state ->
                    state.copy(
                        currentChapter = filteredChapter,
                        content = filteredChapter.content,
                        isLoadingContent = false,
                        totalWords = totalWords
                    )
                }

                chapterNotifier.tryNotifyChange(
                    ChapterNotifier.ChangeType.ContentFetched(
                        chapterId = filteredChapter.id,
                        bookId = book.id
                    )
                )

                delay(ReaderConstants.FETCH_TO_CONTROLLER_DELAY_MS)
                chapterController.dispatch(ChapterCommand.LoadChapter(filteredChapter.id))

                if (filteredChapter.content.isNotEmpty()) {
                    checkChapterHealth(filteredChapter)
                }

                triggerPreloadNextChapter()
            },
            onError = { message ->
                updateSuccessState { it.copy(isLoadingContent = false) }
                if (message != null) {
                    showSnackBar(message)
                }
            }
        )
    }

    // ==================== Chapter Navigation ====================

    fun getNextChapter(): Chapter? {
        val currentState = getState()
        if (currentState !is ReaderState.Success) return null

        val chapter = if (readingMode() == ReadingMode.Continues) {
            currentState.chapterShell.lastOrNull()
        } else {
            currentState.currentChapter
        }

        val index = currentState.chapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1 && index < currentState.chapters.size - 1) {
            return currentState.chapters[index + 1]
        }
        return null
    }

    fun getPrevChapter(): Chapter? {
        val currentState = getState()
        if (currentState !is ReaderState.Success) return null

        val chapter = if (readingMode() == ReadingMode.Continues) {
            currentState.chapterShell.firstOrNull()
        } else {
            currentState.currentChapter
        }

        val index = currentState.chapters.indexOfFirst { it.id == chapter?.id }
        if (index > 0) {
            return currentState.chapters[index - 1]
        }
        return null
    }

    fun dispatchNextChapter() {
        val currentState = getState()
        if (currentState !is ReaderState.Success) return

        val chaptersList = currentState.chapters
        val currentChapterIndex = currentState.currentChapterIndex

        if (currentChapterIndex < chaptersList.lastIndex) {
            val nextChapter = chaptersList.getOrNull(currentChapterIndex + 1)
            if (nextChapter != null) {
                Log.debug { "dispatchNextChapter: navigating from index $currentChapterIndex to ${currentChapterIndex + 1}" }
                navigateToChapter(nextChapter.id, next = true)
            }
        } else {
            Log.debug { "dispatchNextChapter: already at last chapter (index $currentChapterIndex of ${chaptersList.size})" }
        }
    }

    fun dispatchPrevChapter() {
        val currentState = getState()
        if (currentState !is ReaderState.Success) return

        val chaptersList = currentState.chapters
        val currentChapterIndex = currentState.currentChapterIndex
        val current = currentState.currentChapter

        Log.debug { "dispatchPrevChapter: currentChapter=${current?.name}, currentIndex=$currentChapterIndex, totalChapters=${chaptersList.size}" }

        if (currentChapterIndex > 0) {
            val prevChapter = chaptersList.getOrNull(currentChapterIndex - 1)
            Log.debug { "dispatchPrevChapter: prevChapter=${prevChapter?.name}, prevChapterId=${prevChapter?.id}" }
            if (prevChapter != null) {
                Log.debug { "dispatchPrevChapter: navigating from '${current?.name}' (index $currentChapterIndex) to '${prevChapter.name}' (index ${currentChapterIndex - 1})" }
                updateSuccessState { it.copy(scrollToEndOnChapterChange = true) }
                navigateToChapter(prevChapter.id, next = false)
            }
        } else {
            Log.debug { "dispatchPrevChapter: already at first chapter (index $currentChapterIndex)" }
        }
    }

    fun navigateToChapter(
        chapterId: Long,
        next: Boolean = true,
        onComplete: () -> Unit = {}
    ) {
        chapterNavigationJob?.cancel()

        chapterNavigationJob = scope.launch {
            try {
                updateSuccessState { it.copy(isNavigating = true) }

                val currentState = getState()
                if (currentState is ReaderState.Success) {
                    loadChapter(
                        currentState.book,
                        currentState.catalog,
                        chapterId,
                        next
                    )
                }

                onComplete()
            } finally {
                updateSuccessState { it.copy(isNavigating = false) }
            }
        }
    }

    suspend fun getLocalChapter(
        chapterId: Long?,
        next: Boolean = true,
        force: Boolean = false
    ): Chapter? {
        if (chapterId == null) return null

        val currentState = getState()
        return if (currentState is ReaderState.Success) {
            loadChapter(currentState.book, currentState.catalog, chapterId, next, force)
        } else {
            null
        }
    }

    suspend fun fetchChapterFromRemote(chapterId: Long?): Chapter? {
        if (chapterId == null) {
            return null
        }
        var currentState = getState()
        if (currentState !is ReaderState.Success) {
            return null
        }

        var book = currentState.book
        var catalog = currentState.catalog

        // Wait for catalog to be available (source loading is async)
        if (catalog?.source == null) {
            repeat(20) { attempt ->
                kotlinx.coroutines.delay(100)
                currentState = getState()
                if (currentState is ReaderState.Success) {
                    catalog = currentState.catalog
                    book = currentState.book
                    if (catalog?.source != null) {
                    }
                }
            }
        }

        if (catalog?.source == null) {
            updateSuccessState { it.copy(isLoadingContent = false) }
            showSnackBar(UiText.DynamicString("Source not available — try again"))
            return null
        }

        updateSuccessState { it.copy(isLoadingContent = true) }

        return try {
            preloadedChapters.remove(chapterId)
            loadChapter(
                book = book,
                catalog = catalog,
                chapterId = chapterId,
                next = true,
                force = false,
                scrollToEnd = null,
                forceRemote = true
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to fetch chapter from remote")
            updateSuccessState { it.copy(isLoadingContent = false) }
            null
        }
    }

    suspend fun loadChapterFromLocal(chapterId: Long?): Chapter? {
        if (chapterId == null) return null
        val currentState = getState() as? ReaderState.Success ?: return null

        val freshChapter = getChapterUseCase.findChapterById(chapterId)
        if (freshChapter != null) {
            val totalWords = calculateTotalWords(freshChapter.content)
            updateSuccessState { state ->
                val updatedChapters = state.chapters.map { ch ->
                    if (ch.id == freshChapter.id) freshChapter else ch
                }
                state.copy(
                    currentChapter = freshChapter,
                    content = if (freshChapter.content.isNotEmpty()) freshChapter.content else state.content,
                    chapters = updatedChapters,
                    isLoadingContent = false,
                    totalWords = totalWords
                )
            }
            checkChapterHealth(freshChapter)
            return freshChapter
        }

        return currentState.currentChapter
    }

    // ==================== Chapter Shell Management ====================

    suspend fun clearChapterShell(
        scrollState: androidx.compose.foundation.ScrollState?,
        force: Boolean = false
    ) {
        if (readingMode() == ReadingMode.Continues || force) {
            scrollState?.scrollTo(0)
            updateSuccessState { it.copy(chapterShell = emptyList()) }
        }
    }

    // ==================== Preloading ====================

    private fun triggerPreloadNextChapter() {
        if (!autoPreloadNextChapter()) return

        preloadJob?.cancel()
        preloadJob = scope.launch {
            try {
                delay(ReaderConstants.PRELOAD_AFTER_FETCH_DELAY_MS)

                val next = getNextChapter()
                if (next != null && !preloadedChapters.containsKey(next.id)) {
                    preloadChapter(next)
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun preloadChapter(chapter: Chapter) {
        val currentState = getState()
        if (currentState !is ReaderState.Success) return

        val dbChapter = getChapterUseCase.findChapterById(chapter.id)
        val needsRemoteFetch = dbChapter == null || dbChapter.isEmpty()

        if (needsRemoteFetch && currentState.catalog != null) {
            Log.debug { "preloadChapter: [START] Preloading chapter ${chapter.id} '${chapter.name}' from remote" }
            updateSuccessState { it.copy(isPreloading = true) }

            remoteUseCases.fetchAndSaveChapterContent(
                chapter = chapter,
                catalog = currentState.catalog,
                onSuccess = { preloadedChapter ->
                    preloadedChapters[chapter.id] = preloadedChapter
                    Log.debug { "preloadChapter: [SAVED] Preloaded chapter ${chapter.id} '${chapter.name}' saved to DB with ${preloadedChapter.content.size} pages" }
                    updateSuccessState { it.copy(isPreloading = false) }
                },
                onError = { error ->
                    Log.warn { "preloadChapter: [FAILED] Preload failed for chapter ${chapter.id} '${chapter.name}': $error - current chapter display unaffected" }
                    updateSuccessState { it.copy(isPreloading = false) }
                }
            )
        } else if (dbChapter != null && !dbChapter.isEmpty()) {
            Log.debug { "preloadChapter: [CACHE-HIT] Chapter ${chapter.id} '${chapter.name}' already in DB with ${dbChapter.content.size} pages" }
            preloadedChapters[chapter.id] = dbChapter
        }
    }

    fun preloadNextChapters(count: Int = 3) {
        scope.launch {
            try {
                val currentState = getState()
                if (currentState !is ReaderState.Success) return@launch

                val currentIndex = currentState.chapters.indexOfFirst {
                    it.id == currentState.currentChapter.id
                }

                if (currentIndex != -1) {
                    currentState.chapters.drop(currentIndex + 1).take(count).forEach { chapter ->
                        if (!preloadedChapters.containsKey(chapter.id)) {
                            preloadChapter(chapter)
                            delay(ReaderConstants.PRELOAD_AFTER_FETCH_DELAY_MS)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error("Error preloading multiple chapters: ${e.message}")
            }
        }
    }

    fun clearPreloadCache() {
        preloadedChapters.clear()
    }

    // ==================== Chapter Health ====================

    private fun checkChapterHealth(chapter: Chapter) {
        scope.launch(ioDispatcher) {
            try {
                val isBroken = chapterHealthChecker.isChapterBroken(chapter.content)
                val breakReason = if (isBroken) {
                    chapterHealthChecker.getBreakReason(chapter.content)?.name
                } else null

                updateSuccessState { state ->
                    state.copy(
                        isChapterBroken = isBroken,
                        chapterBreakReason = breakReason,
                        showRepairBanner = isBroken
                    )
                }
            } catch (e: Exception) {
                Log.error("Failed to check chapter health", e)
            }
        }
    }

    fun repairChapter() {
        scope.launch(ioDispatcher) {
            val currentState = getState()
            if (currentState !is ReaderState.Success) return@launch

            updateSuccessState { it.copy(isRepairing = true) }

            try {
                val result = autoRepairChapterUseCase(
                    currentState.currentChapter,
                    currentState.book
                )

                result.onSuccess { repairedChapter ->
                    updateSuccessState { state ->
                        state.copy(
                            currentChapter = repairedChapter,
                            content = repairedChapter.content,
                            isRepairing = false,
                            showRepairBanner = false,
                            showRepairSuccess = true,
                            repairSuccessSourceName = "alternative source"
                        )
                    }
                }.onFailure { error ->
                    updateSuccessState { it.copy(isRepairing = false) }
                    showSnackBar(UiText.DynamicString("Repair failed: ${error.message}"))
                }
            } catch (e: Exception) {
                updateSuccessState { it.copy(isRepairing = false) }
                showSnackBar(UiText.DynamicString("Repair failed: ${e.message}"))
            }
        }
    }

    fun dismissRepairBanner() {
        updateSuccessState { it.copy(showRepairBanner = false) }
    }

    fun dismissRepairSuccessBanner() {
        updateSuccessState { it.copy(showRepairSuccess = false) }
    }

    // ==================== Report Broken Chapter ====================

    fun toggleReportDialog() {
        updateSuccessState { it.copy(showReportDialog = !it.showReportDialog) }
    }

    fun reportBrokenChapter(category: IssueCategory, description: String) {
        scope.launch {
            val currentState = getState()
            if (currentState !is ReaderState.Success) return@launch

            try {
                val result = reportBrokenChapterUseCase(
                    chapterId = currentState.currentChapter.id,
                    bookId = currentState.book.id,
                    sourceId = currentState.book.sourceId,
                    reason = category.name,
                    description = description
                )

                if (result.isSuccess) {
                    showSnackBar(UiText.DynamicString("Chapter reported successfully"))
                    updateSuccessState { it.copy(showReportDialog = false) }
                } else {
                    showSnackBar(UiText.DynamicString("Failed to report chapter"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to report chapter: ${e.message}"))
            }
        }
    }

    // ==================== Glossary ====================

    private fun loadGlossary(bookId: Long) {
        scope.launch(ioDispatcher) {
            try {
                getGlossaryByBookIdUseCase.subscribe(bookId).collect { _ ->
                }
            } catch (e: Exception) {
                Log.error("Failed to load glossary", e)
            }
        }
    }

    // ==================== Cleanup ====================

    /**
     * Fetch chapter content for infinite scroll mode.
     * Returns the pages for a chapter, fetching from remote if not cached.
     * Does NOT update any state flows — caller manages its own state.
     */
    suspend fun fetchChapterContentForInfiniteScroll(chapter: Chapter): List<Page> {
        // Try local DB first
        val dbChapter = getChapterUseCase.findChapterById(chapter.id)
        if (dbChapter != null && !dbChapter.isEmpty()) {
            return dbChapter.content
        }

        // Fetch from remote if available
        val currentState = getState()
        val catalog = (currentState as? ReaderState.Success)?.catalog ?: return emptyList()

        // Use a CompletableDeferred to wait for the fetch result
        val result = kotlinx.coroutines.CompletableDeferred<List<Page>>()
        remoteUseCases.fetchAndSaveChapterContent(
            chapter = chapter,
            catalog = catalog,
            onSuccess = { filteredChapter ->
                result.complete(filteredChapter.content)
            },
            onError = { _ ->
                result.complete(emptyList())
            }
        )
        return result.await()
    }

    /**
     * Track the currently visible chapter in infinite scroll mode WITHOUT
     * triggering scroll restoration. Updates only the display-level state.
     * 
     * IMPORTANT: Must NOT call updateSuccessState — that triggers the scroll
     * restoration logic in ReaderText.kt which would scroll away from the
     * user's current position.
     */
    fun updateCurrentChapterForInfiniteScroll(chapter: Chapter) {
        // Update the chapter index for display (chapter name in top bar, progress etc.)
        // but DON'T touch currentChapter — that would trigger scroll restoration
        _infiniteScrollVisibleChapter.value = chapter
    }

    private val _infiniteScrollVisibleChapter = MutableStateFlow<Chapter?>(null)
    val infiniteScrollVisibleChapter: kotlinx.coroutines.flow.StateFlow<Chapter?> = _infiniteScrollVisibleChapter

    fun cleanup() {
        preloadJob?.cancel()
        chapterNavigationJob?.cancel()
        chapterControllerEventJob?.cancel()
        chapterNotifierJob?.cancel()
        preloadedChapters.clear()
        chapterController.dispatch(ChapterCommand.Cleanup)
    }
}
