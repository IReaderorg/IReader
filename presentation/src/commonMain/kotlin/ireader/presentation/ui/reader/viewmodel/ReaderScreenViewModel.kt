package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import ireader.core.http.WebViewManger
import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.readerThemes
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.ChapterHealthChecker
import ireader.domain.usecases.chapter.AutoRepairChapterUseCase
import ireader.domain.usecases.chapter.ReportBrokenChapterUseCase
import ireader.domain.usecases.fonts.FontManagementUseCase
import ireader.domain.usecases.fonts.FontUseCase
import ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase
import ireader.domain.usecases.glossary.ExportGlossaryUseCase
import ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase
import ireader.domain.usecases.glossary.ImportGlossaryUseCase
import ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.reader.PreloadChapterUseCase
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.statistics.TrackReadingProgressUseCase
import ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase
import ireader.domain.usecases.translate.TranslateParagraphUseCase
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.domain.services.chapter.ChapterCommand
import ireader.domain.services.chapter.ChapterController
import ireader.domain.services.chapter.ChapterEvent
import ireader.domain.services.preferences.PreferenceCommand
import ireader.domain.services.preferences.PreferenceEvent
import ireader.domain.services.preferences.ReaderPreferencesController

import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.removeIf
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.NO_VALUE
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.something_wrong_with_book
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toDomainColor
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reader screen using sealed state pattern.
 *
 * Uses a single immutable StateFlow<ReaderState> instead of multiple mutable states.
 * This provides:
 * - Single source of truth for reader state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 * - Clear Loading/Success/Error states
 */
class ReaderScreenViewModel(
    val getBookUseCases: LocalGetBookUseCases,
    val getChapterUseCase: LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val historyUseCase: HistoryUseCase,
    val getLocalCatalog: GetLocalCatalog,
    val readerUseCases: ReaderPrefUseCases,
    val insertUseCases: LocalInsertUseCases,
    val readerPreferences: ReaderPreferences,
    val androidUiPreferences: AppPreferences,
    val platformUiPreferences: PlatformUiPreferences,
    val uiPreferences: UiPreferences,
    val screenAlwaysOnUseCase: ScreenAlwaysOn,
    val webViewManger: WebViewManger,
    val readerThemeRepository: ReaderThemeRepository,
    val bookMarkChapterUseCase: BookMarkChapterUseCase,
    val translationEnginesManager: TranslationEnginesManager,
    val preloadChapterUseCase: PreloadChapterUseCase,
    val translateChapterWithStorageUseCase: TranslateChapterWithStorageUseCase,
    val translateParagraphUseCase: TranslateParagraphUseCase,
    val getTranslatedChapterUseCase: GetTranslatedChapterUseCase,
    val getGlossaryByBookIdUseCase: GetGlossaryByBookIdUseCase,
    val saveGlossaryEntryUseCase: SaveGlossaryEntryUseCase,
    val deleteGlossaryEntryUseCase: DeleteGlossaryEntryUseCase,
    val exportGlossaryUseCase: ExportGlossaryUseCase,
    val importGlossaryUseCase: ImportGlossaryUseCase,
    val trackReadingProgressUseCase: TrackReadingProgressUseCase,
    val reportBrokenChapterUseCase: ReportBrokenChapterUseCase,
    val fontManagementUseCase: FontManagementUseCase,
    val fontUseCase: FontUseCase,
    val chapterHealthChecker: ChapterHealthChecker,
    val chapterHealthRepository: ChapterHealthRepository,
    val autoRepairChapterUseCase: AutoRepairChapterUseCase,
    val params: Param,
    private val systemInteractionService: ireader.domain.services.platform.SystemInteractionService,
    // ChapterController - single source of truth for chapter operations (Requirements: 9.2, 9.4, 9.5)
    private val chapterController: ChapterController,
    // ReaderPreferencesController - single source of truth for reader preferences (Requirements: 4.1, 4.2)
    private val preferencesController: ReaderPreferencesController,
    // Sub-ViewModels
    val settingsViewModel: ReaderSettingsViewModel,
    val translationViewModel: ReaderTranslationViewModel,
    val ttsViewModel: ReaderTTSViewModel,
    val statisticsViewModel: ReaderStatisticsViewModel,
) : BaseViewModel() {

    data class Param(val chapterId: Long?, val bookId: Long?)

    // ==================== State Management ====================

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    // Dialog state (separate for simpler updates)
    var currentDialog by mutableStateOf<ReaderDialog>(ReaderDialog.None)
        private set

    // Helper to update Success state only
    private inline fun updateSuccessState(crossinline update: (ReaderState.Success) -> ReaderState.Success) {
        _state.update { current ->
            when (current) {
                is ReaderState.Success -> update(current)
                else -> current
            }
        }
    }

    // ==================== Reader Themes ====================

    val readerColors = mutableStateListOf<ReaderColors>().apply { addAll(readerThemes) }

    // ==================== Preferences (exposed as State) ====================

    val dateFormat by uiPreferences.dateFormat().asState()
    val relativeTime by uiPreferences.relativeTime().asState()
    val translatorOriginLanguage = readerPreferences.translatorOriginLanguage().asState()
    val translatorTargetLanguage = readerPreferences.translatorTargetLanguage().asState()
    val translatorEngine = readerPreferences.translatorEngine().asState()
    val readerTheme = androidUiPreferences.readerTheme().asState()
    val backgroundColor = androidUiPreferences.backgroundColorReader().asState()
    val textColor = androidUiPreferences.textColorReader().asState()
    val screenAlwaysOn = readerPreferences.screenAlwaysOn().asState()
    val autoPreloadNextChapter = readerPreferences.autoPreloadNextChapter().asState()
    val fontSize = readerPreferences.fontSize().asStateDebounced()
    val lineHeight = readerPreferences.lineHeight().asStateDebounced()
    val textAlignment = readerPreferences.textAlign().asState()
    val orientation = androidUiPreferences.orientation().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val brightness = readerPreferences.brightness().asStateDebounced()
    val verticalScrolling = readerPreferences.scrollMode().asState()
    val readingMode = readerPreferences.readingMode().asState()
    val showTranslatedContent = readerPreferences.showTranslatedContent().asState()
    val bilingualModeEnabled = readerPreferences.bilingualModeEnabled().asState()
    val bilingualModeLayout = readerPreferences.bilingualModeLayout().asState()
    val volumeKeyNavigation = readerPreferences.volumeKeyNavigation().asState()
    val autoTranslateNextChapter = readerPreferences.autoTranslateNextChapter().asState()
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()

    // Compose color wrappers
    val backgroundColorCompose = object : androidx.compose.runtime.MutableState<Color> {
        override var value: Color
            get() = backgroundColor.value.toComposeColor()
            set(value) { backgroundColor.value = value.toDomainColor() }
        override fun component1(): Color = value
        override fun component2(): (Color) -> Unit = { value = it }
    }

    val textColorCompose = object : androidx.compose.runtime.MutableState<Color> {
        override var value: Color
            get() = textColor.value.toComposeColor()
            set(value) { textColor.value = value.toDomainColor() }
        override fun component1(): Color = value
        override fun component2(): (Color) -> Unit = { value = it }
    }

    // ==================== Jobs ====================

    private var preloadJob: Job? = null
    private var chapterNavigationJob: Job? = null
    private var chapterControllerEventJob: Job? = null
    private val preloadedChapters = mutableMapOf<Long, Chapter>()
    
    // Flag to indicate the current loadChapter was triggered by ChapterController sync
    // When true, we should NOT notify ChapterController back (would cause infinite loop)
    private var isLoadingFromChapterControllerSync: Boolean = false

    // ==================== Initialization ====================

    init {
        val chapterId = params.chapterId
        val bookId = params.bookId

        if (bookId != null && chapterId != null) {
            subscribeReaderThemes()
            // Subscribe to ChapterController events (Requirements: 9.2, 9.4, 9.5)
            subscribeToChapterControllerEvents()
            // Subscribe to ReaderPreferencesController events (Requirements: 4.1, 4.2)
            subscribeToPreferencesControllerEvents()
            initializeReader(bookId, chapterId)
            // Subscribe to TTS chapter changes for sync
            subscribeTTSChapterChanges()
        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(Res.string.something_wrong_with_book))
                _state.value = ReaderState.Error(
                    message = UiText.MStringResource(Res.string.something_wrong_with_book)
                )
            }
        }
        
        // Load custom fonts
        scope.launch {
            customFonts = fontManagementUseCase.getCustomFonts()
        }
        
        // Set up next chapter provider for auto-translate feature
        translationViewModel.setNextChapterProvider {
            getNextChapter()
        }
    }
    
    // ==================== ChapterController Integration ====================
    // Requirements: 9.2, 9.4, 9.5
    
    /**
     * Subscribe to ChapterController events for chapter loading notifications.
     * This handles events like ChapterLoaded, Error, etc.
     */
    private fun subscribeToChapterControllerEvents() {
        chapterControllerEventJob?.cancel()
        chapterControllerEventJob = scope.launch {
            // Subscribe to events
            launch {
                chapterController.events.collect { event ->
                    when (event) {
                        is ChapterEvent.ChapterLoaded -> {
                            Log.debug { "ChapterController: Chapter loaded - ${event.chapter.id}" }
                            // Chapter loading is handled by the loadChapter method
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
            
            // Subscribe to state changes for cross-screen sync (TTS <-> Reader)
            launch {
                chapterController.state.collect { chapterState ->
                    val currentChapter = chapterState.currentChapter
                    val readerState = _state.value
                    
                    // Sync chapters list when it changes (for drawer to show cached status)
                    if (readerState is ReaderState.Success && 
                        chapterState.chapters.isNotEmpty() &&
                        readerState.book.id == chapterState.book?.id) {
                        // Update chapters list if it has changed
                        if (chapterState.chapters != readerState.chapters) {
                            updateSuccessState { it.copy(chapters = chapterState.chapters) }
                        }
                    }
                    
                    // Only sync chapter navigation if:
                    // 1. ChapterController has a current chapter
                    // 2. Reader has a book loaded (same book)
                    // 3. The chapter is different from what Reader currently has
                    if (currentChapter != null && 
                        readerState is ReaderState.Success &&
                        readerState.book.id == chapterState.book?.id &&
                        currentChapter.id != readerState.currentChapter.id) {
                        
                        Log.debug { "ChapterController state changed to chapter ${currentChapter.id}, syncing Reader" }
                        
                        // Load the new chapter in the reader
                        // Mark that this load is from ChapterController sync to prevent notifying back
                        // Don't scroll to end - preserve scroll position or scroll to start
                        isLoadingFromChapterControllerSync = true
                        try {
                            loadChapter(
                                readerState.book,
                                readerState.catalog,
                                currentChapter.id,
                                next = false,
                                force = false,
                                scrollToEnd = false  // Don't scroll to end when syncing from ChapterController
                            )
                        } finally {
                            isLoadingFromChapterControllerSync = false
                        }
                    }
                }
            }
        }
    }
    
    // ==================== ReaderPreferencesController Integration ====================
    // Requirements: 4.1, 4.2, 4.4, 4.5, 5.4
    
    private var preferencesControllerEventJob: Job? = null
    
    /**
     * Subscribe to ReaderPreferencesController events for preference change notifications.
     * This handles events like PreferenceSaved, Error, etc.
     * Requirements: 4.1, 4.2
     */
    private fun subscribeToPreferencesControllerEvents() {
        preferencesControllerEventJob?.cancel()
        preferencesControllerEventJob = scope.launch {
            // Subscribe to events
            launch {
                preferencesController.events.collect { event ->
                    when (event) {
                        is PreferenceEvent.PreferenceSaved -> {
                            Log.debug { "PreferencesController: Preference saved - ${event.key}" }
                        }
                        is PreferenceEvent.PreferencesLoaded -> {
                            Log.debug { "PreferencesController: All preferences loaded" }
                        }
                        is PreferenceEvent.Error -> {
                            Log.error { "PreferencesController: Error - ${event.error.toUserMessage()}" }
                            showSnackBar(UiText.DynamicString(event.error.toUserMessage()))
                        }
                    }
                }
            }
            
            // Subscribe to state changes for preference sync
            launch {
                preferencesController.state.collect { prefState ->
                    // Preferences are synced through the Controller state
                    // UI components can observe preferencesController.state directly
                    Log.debug { "PreferencesController: State updated - fontSize=${prefState.fontSize}, brightness=${prefState.brightness}" }
                }
            }
        }
    }
    
    // ==================== Preference Delegation to Controller ====================
    // Requirements: 4.2
    
    /**
     * Set font size via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setFontSize(size: Int) {
        preferencesController.dispatch(PreferenceCommand.SetFontSize(size))
    }
    
    /**
     * Set line height via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setLineHeight(height: Int) {
        preferencesController.dispatch(PreferenceCommand.SetLineHeight(height))
    }
    
    /**
     * Set brightness via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setBrightness(brightness: Float) {
        preferencesController.dispatch(PreferenceCommand.SetBrightness(brightness))
    }
    
    /**
     * Set immersive mode via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setImmersiveMode(enabled: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetImmersiveMode(enabled))
    }
    
    /**
     * Set screen always on via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setScreenAlwaysOn(enabled: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetScreenAlwaysOn(enabled))
    }
    
    /**
     * Set reading mode via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setReadingMode(mode: ReadingMode) {
        preferencesController.dispatch(PreferenceCommand.SetReadingMode(mode))
    }
    
    /**
     * Set scroll mode via ReaderPreferencesController.
     * Delegates to Controller for SSOT pattern.
     */
    fun setScrollMode(vertical: Boolean) {
        preferencesController.dispatch(PreferenceCommand.SetScrollMode(vertical))
    }
    
    /**
     * Get the ReaderPreferencesController for direct state observation.
     * UI components can use this to observe preference state.
     */
    fun getPreferencesController(): ReaderPreferencesController = preferencesController
    
    // ==================== TTS Chapter Sync ====================
    
    /**
     * Subscribe to TTS chapter changes to keep reader in sync.
     * Note: TTS V2 architecture handles chapter sync through TTSController state.
     * This method is kept for potential future integration.
     */
    private fun subscribeTTSChapterChanges() {
        // TTS V2 uses TTSController which manages its own state
        // Reader can observe TTSController.state.chapter if sync is needed
    }
    
    /**
     * Manually sync reader state with TTS state.
     * Note: TTS V2 architecture handles chapter sync through TTSController state.
     */
    fun syncWithTTSState() {
        // TTS V2 uses TTSController which manages its own state
        // Reader can observe TTSController.state.chapter if sync is needed
    }

    private fun subscribeReaderThemes() {
        readerThemeRepository.subscribe().onEach { list ->
            readerColors.removeIf { !it.isDefault }
            readerColors.addAll(0, list.map { theme ->
                ReaderColors(
                    id = theme.id,
                    backgroundColor = ireader.domain.models.common.ColorModel.fromArgb(theme.backgroundColor),
                    onTextColor = ireader.domain.models.common.ColorModel.fromArgb(theme.onTextColor),
                    isDefault = false
                )
            }.reversed())
        }.launchIn(scope)
    }

    private fun initializeReader(bookId: Long, chapterId: Long) {
        scope.launch {
            try {
                val book = getBookUseCases.findBookById(bookId)
                if (book == null) {
                    _state.value = ReaderState.Error(
                        message = UiText.DynamicString("Book not found"),
                        bookId = bookId
                    )
                    return@launch
                }

                val catalog = getLocalCatalog.get(book.sourceId)
                
                // Set catalog on ChapterController for remote content loading
                chapterController.setCatalog(catalog)
                
                // Load book via ChapterController (Requirements: 9.2, 9.4, 9.5)
                // This subscribes to chapters reactively
                chapterController.dispatch(ChapterCommand.LoadBook(bookId))

                // Wait for chapters to load from ChapterController
                delay(100)

                // Setup initial chapter
                setupChapters(book, catalog, bookId, chapterId)

                // Load glossary
                loadGlossary(bookId)

            } catch (e: Exception) {
                Log.error("Failed to initialize reader", e)
                _state.value = ReaderState.Error(
                    message = UiText.DynamicString(e.message ?: "Failed to initialize reader"),
                    bookId = bookId,
                    chapterId = chapterId
                )
            }
        }
    }
    
    /**
     * Get chapters from ChapterController state.
     * This replaces the deprecated subscribeChapters method.
     * Requirements: 9.2, 9.4, 9.5
     */
    private fun getChaptersFromController(): List<Chapter> {
        return chapterController.state.value.chapters
    }

    private suspend fun setupChapters(book: Book, catalog: CatalogLocal?, bookId: Long, chapterId: Long) {
        val last = historyUseCase.findHistoryByBookId(bookId)

        val targetChapterId = when {
            chapterId != LAST_CHAPTER && chapterId != NO_VALUE -> chapterId
            last != null -> last.chapterId
            else -> {
                // Get chapters from ChapterController state (Requirements: 9.2, 9.4, 9.5)
                val chapters = getChaptersFromController().ifEmpty {
                    // Fallback to direct query if controller hasn't loaded yet
                    getChapterUseCase.findChaptersByBookId(bookId)
                }
                chapters.firstOrNull()?.id
            }
        }

        if (targetChapterId != null) {
            loadChapter(book, catalog, targetChapterId, next = true)
        } else {
            _state.value = ReaderState.Error(
                message = UiText.DynamicString("No chapters found"),
                bookId = bookId
            )
        }
    }


    // ==================== Chapter Loading ====================

    /**
     * Load a chapter by ID
     * @param scrollToEnd Override scroll behavior. If null, uses `next` parameter logic.
     *                    If true, scrolls to end. If false, scrolls to start.
     */
    private suspend fun loadChapter(
        book: Book,
        catalog: CatalogLocal?,
        chapterId: Long,
        next: Boolean = true,
        force: Boolean = false,
        scrollToEnd: Boolean? = null
    ): Chapter? {
        // Track chapter close before loading new chapter
        statisticsViewModel.onChapterClosed()

        // Reset translation state
        translationViewModel.translationState.reset()

        // Check if chapter is preloaded
        val preloadedChapter = preloadedChapters.remove(chapterId)
        val chapter = preloadedChapter?.takeIf { !it.isEmpty() }
            ?: getChapterUseCase.findChapterById(chapterId)

        if (chapter == null) {
            showSnackBar(UiText.DynamicString("Chapter not found"))
            return null
        }

        // Get all chapters for navigation
        val chapters = getChapterUseCase.findChaptersByBookId(book.id)
        val chapterIndex = chapters.indexOfFirst { it.id == chapter.id }

        // Create or update Success state
        val currentState = _state.value
        val newChapterShell = if (currentState is ReaderState.Success) {
            if (next) {
                currentState.chapterShell + chapter
            } else {
                listOf(chapter) + currentState.chapterShell
            }
        } else {
            listOf(chapter)
        }

        // Preserve UI state from previous Success state
        val previousSuccessState = currentState as? ReaderState.Success
        
        // Calculate total words for reading time estimation
        val totalWords = calculateTotalWords(chapter.content)
        
        _state.value = ReaderState.Success(
            book = book,
            currentChapter = chapter,
            chapters = chapters,
            catalog = catalog,
            content = chapter.content,
            currentChapterIndex = if (chapterIndex != -1) chapterIndex else 0,
            chapterShell = newChapterShell,
            isLoadingContent = chapter.isEmpty() && catalog?.source != null,
            // Preserve UI state from previous state
            isReaderModeEnabled = previousSuccessState?.isReaderModeEnabled ?: true,
            isSettingModeEnabled = previousSuccessState?.isSettingModeEnabled ?: false,
            isMainBottomModeEnabled = previousSuccessState?.isMainBottomModeEnabled ?: false,
            showSettingsBottomSheet = previousSuccessState?.showSettingsBottomSheet ?: false,
            isDrawerAsc = previousSuccessState?.isDrawerAsc ?: true,
            // When navigating to previous chapter (next=false), scroll to end
            // Use explicit scrollToEnd parameter if provided, otherwise use !next logic
            scrollToEndOnChapterChange = scrollToEnd ?: !next,
            // Word count for reading time estimation
            totalWords = totalWords,
        )
        
        Log.debug { "loadChapter: chapterId=${chapter.id}, next=$next, scrollToEnd=$scrollToEnd, scrollToEndOnChapterChange=${scrollToEnd ?: !next}" }

        // Fetch remote content if needed
        val needsRemoteFetch = chapter.isEmpty() && catalog?.source != null
        if (needsRemoteFetch && !force) {
            fetchRemoteChapter(book, catalog, chapter)
        } else if (!needsRemoteFetch) {
            // Check chapter health
            checkChapterHealth(chapter)
        }

        // Update last read time
        getChapterUseCase.updateLastReadTime(chapter)
        
        // Notify ChapterController about the chapter load (Requirements: 9.2, 9.4, 9.5)
        // This keeps ChapterController in sync with the reader's current chapter
        // BUT skip if this load was triggered BY ChapterController (would cause infinite loop)
        if (!isLoadingFromChapterControllerSync) {
            chapterController.dispatch(ChapterCommand.LoadChapter(chapterId))
        }

        // Track chapter open (pass isLast to track book completion when last chapter is finished)
        val isLastChapter = chapterIndex != -1 && chapterIndex == chapters.lastIndex
        statisticsViewModel.onChapterOpened(chapter, isLastChapter)

        // Trigger preload via ChapterController (Requirements: 9.2, 9.4, 9.5)
        triggerPreloadNextChapter()

        // Load translation if available
        loadTranslationForChapter(chapterId)

        return chapter
    }

    /**
     * Calculate total words from chapter content for reading time estimation
     */
    private fun calculateTotalWords(content: List<Page>): Int {
        return content.filterIsInstance<Text>()
            .sumOf { text ->
                // Split by whitespace and count non-empty words
                text.text.split(Regex("\\s+")).count { it.isNotBlank() }
            }
    }

    private suspend fun fetchRemoteChapter(book: Book, catalog: CatalogLocal?, chapter: Chapter) {
        remoteUseCases.getRemoteReadingContent(
            chapter,
            catalog,
            onSuccess = { result ->
                // Calculate total words for reading time estimation
                val totalWords = calculateTotalWords(result.content)
                
                updateSuccessState { state ->
                    state.copy(
                        currentChapter = result,
                        content = result.content,
                        isLoadingContent = false,
                        totalWords = totalWords
                    )
                }

                // Save to database
                scope.launch {
                    insertUseCases.insertChapter(result)

                    // Check chapter health after content loads
                    if (result.content.isNotEmpty()) {
                        checkChapterHealth(result)
                    }
                }
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
    // Navigation methods now delegate to ChapterController (Requirements: 9.2, 9.4, 9.5)

    /**
     * Get the next chapter without navigating.
     * Used for preloading and auto-translate features.
     */
    fun getNextChapter(): Chapter? {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return null

        val chapter = if (readingMode.value == ReadingMode.Continues) {
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

    /**
     * Get the previous chapter without navigating.
     */
    fun getPrevChapter(): Chapter? {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return null

        val chapter = if (readingMode.value == ReadingMode.Continues) {
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
    
    /**
     * Dispatch next chapter command to ChapterController.
     * Requirements: 9.2, 9.4, 9.5
     */
    fun dispatchNextChapter() {
        chapterController.dispatch(ChapterCommand.NextChapter)
    }
    
    /**
     * Dispatch previous chapter command to ChapterController.
     * Requirements: 9.2, 9.4, 9.5
     */
    fun dispatchPrevChapter() {
        chapterController.dispatch(ChapterCommand.PreviousChapter)
    }

    /**
     * Navigate to a chapter with race condition protection
     */
    fun navigateToChapter(
        chapterId: Long,
        next: Boolean = true,
        onComplete: () -> Unit = {}
    ) {
        chapterNavigationJob?.cancel()

        chapterNavigationJob = scope.launch {
            try {
                updateSuccessState { it.copy(isNavigating = true) }

                val currentState = _state.value
                if (currentState is ReaderState.Success) {
                    loadChapter(currentState.book, currentState.catalog, chapterId, next)
                }

                onComplete()
            } finally {
                updateSuccessState { it.copy(isNavigating = false) }
            }
        }
    }

    /**
     * Load chapter (public API for backward compatibility).
     * Also dispatches to ChapterController for state synchronization.
     * Requirements: 9.2, 9.4, 9.5
     */
    suspend fun getLocalChapter(
        chapterId: Long?,
        next: Boolean = true,
        force: Boolean = false
    ): Chapter? {
        if (chapterId == null) return null

        val currentState = _state.value
        return if (currentState is ReaderState.Success) {
            // Dispatch to ChapterController for state sync (Requirements: 9.2, 9.4, 9.5)
            chapterController.dispatch(ChapterCommand.LoadChapter(chapterId))
            loadChapter(currentState.book, currentState.catalog, chapterId, next, force)
        } else {
            null
        }
    }

    // ==================== Chapter Shell Management ====================

    /**
     * Clear chapter shell (for reading mode changes)
     */
    suspend fun clearChapterShell(scrollState: ScrollState?, force: Boolean = false) {
        if (readingMode.value == ReadingMode.Continues || force) {
            scrollState?.scrollTo(0)
            updateSuccessState { it.copy(chapterShell = emptyList()) }
        }
    }

    // ==================== Preloading ====================

    private fun triggerPreloadNextChapter() {
        if (!autoPreloadNextChapter.value) return

        preloadJob?.cancel()
        preloadJob = scope.launch {
            try {
                // Use ChapterController for preloading (Requirements: 9.2, 9.4, 9.5)
                chapterController.dispatch(ChapterCommand.PreloadNextChapter)
                
                // Also preload locally for faster access
                val next = getNextChapter()
                if (next != null && !preloadedChapters.containsKey(next.id)) {
                    preloadChapter(next)
                }
            } catch (e: Exception) {
                // No next chapter to preload
            }
        }
    }

    private suspend fun preloadChapter(chapter: Chapter) {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return

        val dbChapter = getChapterUseCase.findChapterById(chapter.id)
        val needsRemoteFetch = dbChapter == null || dbChapter.isEmpty()

        if (needsRemoteFetch && currentState.catalog != null) {
            updateSuccessState { it.copy(isPreloading = true) }

            preloadChapterUseCase(chapter, currentState.catalog,
                onSuccess = { preloadedChapter ->
                    preloadedChapters[chapter.id] = preloadedChapter
                    scope.launch { insertUseCases.insertChapter(preloadedChapter) }
                    updateSuccessState { it.copy(isPreloading = false) }
                },
                onError = {
                    updateSuccessState { it.copy(isPreloading = false) }
                }
            )
        } else if (dbChapter != null && !dbChapter.isEmpty()) {
            preloadedChapters[chapter.id] = dbChapter
        }
    }

    fun preloadNextChapters(count: Int = 3) {
        scope.launch {
            try {
                val currentState = _state.value
                if (currentState !is ReaderState.Success) return@launch

                val currentIndex = currentState.chapters.indexOfFirst {
                    it.id == currentState.currentChapter.id
                }

                if (currentIndex != -1) {
                    currentState.chapters.drop(currentIndex + 1).take(count).forEach { chapter ->
                        if (!preloadedChapters.containsKey(chapter.id)) {
                            preloadChapter(chapter)
                            delay(500)
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

    // ==================== UI State Management ====================

    /**
     * Toggle reader mode (show/hide controls)
     */
    fun toggleReaderMode(enable: Boolean? = null) {
        updateSuccessState { state ->
            val newState = enable ?: !state.isReaderModeEnabled
            state.copy(
                isReaderModeEnabled = newState,
                isMainBottomModeEnabled = !newState,
                isSettingModeEnabled = false
            )
        }
    }

    /**
     * Toggle settings bottom sheet
     */
    fun toggleSettingsBottomSheet(show: Boolean) {
        updateSuccessState { it.copy(showSettingsBottomSheet = show) }
    }

    /**
     * Toggle drawer sort order
     */
    fun toggleDrawerAsc() {
        updateSuccessState { it.copy(isDrawerAsc = !it.isDrawerAsc) }
    }

    // ==================== Find in Chapter ====================

    fun toggleFindInChapter() {
        updateSuccessState { state ->
            val newShow = !state.showFindInChapter
            state.copy(
                showFindInChapter = newShow,
                findQuery = if (!newShow) "" else state.findQuery,
                findMatches = if (!newShow) emptyList() else state.findMatches,
                currentFindMatchIndex = if (!newShow) 0 else state.currentFindMatchIndex
            )
        }
    }

    fun updateFindQuery(query: String) {
        updateSuccessState { state ->
            if (query.isEmpty()) {
                return@updateSuccessState state.copy(
                    findQuery = "",
                    findMatches = emptyList(),
                    currentFindMatchIndex = 0
                )
            }

            val fullText = state.currentContent.filterIsInstance<Text>()
                .joinToString("\n") { it.text }

            val matches = mutableListOf<IntRange>()
            val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
            var matchResult = regex.find(fullText)

            while (matchResult != null) {
                matches.add(matchResult.range)
                matchResult = matchResult.next()
            }

            state.copy(
                findQuery = query,
                findMatches = matches,
                currentFindMatchIndex = if (matches.isNotEmpty()) 0 else 0
            )
        }
    }

    fun findNext() {
        updateSuccessState { state ->
            if (state.findMatches.isEmpty()) return@updateSuccessState state
            state.copy(
                currentFindMatchIndex = (state.currentFindMatchIndex + 1) % state.findMatches.size
            )
        }
    }

    fun findPrevious() {
        updateSuccessState { state ->
            if (state.findMatches.isEmpty()) return@updateSuccessState state
            state.copy(
                currentFindMatchIndex = if (state.currentFindMatchIndex == 0) {
                    state.findMatches.size - 1
                } else {
                    state.currentFindMatchIndex - 1
                }
            )
        }
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
            val currentState = _state.value
            if (currentState !is ReaderState.Success) return@launch

            updateSuccessState { it.copy(isRepairing = true) }

            try {
                val result = autoRepairChapterUseCase(currentState.currentChapter, currentState.book)

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

    fun reportBrokenChapter(category: ireader.domain.models.entities.IssueCategory, description: String) {
        scope.launch {
            val currentState = _state.value
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

    // ==================== Bookmark ====================

    fun bookmarkChapter() {
        scope.launch(ioDispatcher) {
            val currentState = _state.value
            if (currentState !is ReaderState.Success) return@launch

            bookMarkChapterUseCase.bookMarkChapter(currentState.currentChapter)?.let { updated ->
                updateSuccessState { it.copy(currentChapter = updated) }
            }
        }
    }

    // ==================== Translation ====================

    fun toggleTranslation() {
        val newValue = !showTranslatedContent.value
        // Update preference
        scope.launch {
            readerPreferences.showTranslatedContent().set(newValue)
        }
        // Sync translation state from translationViewModel and update showTranslatedContent
        val translationState = translationViewModel.translationState
        updateSuccessState { state ->
            state.copy(
                showTranslatedContent = newValue,
                hasTranslation = translationState.hasTranslation,
                translatedContent = translationState.translatedContent
            )
        }
    }

    fun toggleBilingualMode() {
        val currentValue = bilingualModeEnabled.value
        translationViewModel.toggleBilingualMode(!currentValue)
    }

    fun switchBilingualLayout() {
        val currentLayout = bilingualModeLayout.value
        val newLayout = if (currentLayout == 0) 1 else 0
        scope.launch {
            readerPreferences.bilingualModeLayout().set(newLayout)
        }
    }

    fun setSourceLanguage(language: String) = translationViewModel.setSourceLanguage(language)
    fun setTargetLanguage(language: String) = translationViewModel.setTargetLanguage(language)
    fun setTranslationEngine(engineId: Long) = translationViewModel.setTranslationEngine(engineId)

    fun translateCurrentChapter(forceRetranslate: Boolean = false) {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return

        scope.launch {
            translationViewModel.translateChapter(currentState.currentChapter, forceRetranslate)
        }
    }

    private suspend fun loadTranslationForChapter(chapterId: Long) {
        // First, clear any existing translation state to prevent showing stale content
        // This is critical to fix the sync bug where chapter 200's translation shows for chapter 220
        translationViewModel.translationState.clearTranslation()
        updateSuccessState { state ->
            state.copy(
                hasTranslation = false,
                translatedContent = emptyList(),
                isTranslating = false,
                translationProgress = 0f
            )
        }
        
        // Now load the translation for the new chapter
        translationViewModel.loadTranslationForChapter(chapterId)

        // After loading completes, sync the state
        val translationState = translationViewModel.translationState
        updateSuccessState { state ->
            state.copy(
                hasTranslation = translationState.hasTranslation,
                translatedContent = translationState.translatedContent,
                isTranslating = translationState.isTranslating,
                translationProgress = translationState.translationProgress
            )
        }
    }

    // ==================== Glossary ====================

    private fun loadGlossary(bookId: Long) {
        scope.launch(ioDispatcher) {
            try {
                getGlossaryByBookIdUseCase.subscribe(bookId).collect { entries: List<ireader.domain.models.entities.Glossary> ->
                    // Glossary loaded - can be used for translation
                }
            } catch (e: Exception) {
                Log.error("Failed to load glossary", e)
            }
        }
    }

    // ==================== Platform Compatibility Properties ====================
    
    // These properties are required by platform-specific code (ReaderPrefFunctions.kt)
    val stateChapter: Chapter?
        get() = (_state.value as? ReaderState.Success)?.currentChapter
    
    val stateChapters: List<Chapter>
        get() = (_state.value as? ReaderState.Success)?.chapters ?: emptyList()
    
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val lastOrientationChangedTime = mutableStateOf(0L)
    
    // ==================== UI Compatibility Properties ====================
    // These delegate to settingsViewModel for backward compatibility with UI components
    
    // Loading and initialization state
    val isLoading: Boolean
        get() = (_state.value as? ReaderState.Success)?.isLoadingContent ?: (_state.value is ReaderState.Loading)
    
    val initialized: Boolean
        get() = _state.value is ReaderState.Success
    
    val isPreloading: Boolean
        get() = (_state.value as? ReaderState.Success)?.isPreloading ?: false
    
    val chapterShell: List<Chapter>
        get() = (_state.value as? ReaderState.Success)?.chapterShell ?: emptyList()
    
    val currentChapterIndex: Int
        get() = (_state.value as? ReaderState.Success)?.currentChapterIndex ?: 0
    
    val book: Book?
        get() = (_state.value as? ReaderState.Success)?.book
    
    // Settings delegations
    val font get() = settingsViewModel.font
    val fontVersion get() = 0 // Simplified - font version tracking
    val fonts get() = settingsViewModel.fonts
    val fontsLoading get() = settingsViewModel.fontsLoading
    val topMargin get() = settingsViewModel.topMargin
    val bottomMargin get() = settingsViewModel.bottomMargin
    val leftMargin get() = settingsViewModel.leftMargin
    val rightMargin get() = settingsViewModel.rightMargin
    val topContentPadding get() = settingsViewModel.topContentPadding
    val bottomContentPadding get() = settingsViewModel.bottomContentPadding
    val distanceBetweenParagraphs get() = settingsViewModel.distanceBetweenParagraphs
    val paragraphsIndent get() = settingsViewModel.paragraphsIndent
    val betweenLetterSpaces get() = settingsViewModel.betweenLetterSpaces
    val textWeight get() = settingsViewModel.textWeight
    val scrollIndicatorPadding get() = settingsViewModel.scrollIndicatorPadding
    val scrollIndicatorWith get() = settingsViewModel.scrollIndicatorWith
    val showScrollIndicator get() = settingsViewModel.showScrollIndicator
    val unselectedScrollBarColor get() = settingsViewModel.unselectedScrollBarColor
    val selectedScrollBarColor get() = settingsViewModel.selectedScrollBarColor
    val isScrollIndicatorDraggable get() = settingsViewModel.isScrollIndicatorDraggable
    val scrollIndicatorAlignment get() = settingsViewModel.scrollIndicatorAlignment
    val selectableMode get() = settingsViewModel.selectableMode
    val bionicReadingMode get() = settingsViewModel.bionicReadingMode
    val webViewIntegration get() = settingsViewModel.webViewIntegration
    val webViewBackgroundMode get() = settingsViewModel.webViewBackgroundMode
    
    // Auto-scroll delegations
    var autoScrollMode: Boolean
        get() = settingsViewModel.autoScrollMode
        set(value) { settingsViewModel.autoScrollMode = value }
    val autoScrollOffset get() = settingsViewModel.autoScrollOffset
    val autoScrollInterval get() = settingsViewModel.autoScrollInterval
    
    // UI state from sealed state
    val isReaderModeEnable: Boolean
        get() = (_state.value as? ReaderState.Success)?.isReaderModeEnabled ?: true
    
    // Scroll target when chapter changes (true = scroll to end, false = scroll to start)
    var scrollToEndOnChapterChange: Boolean
        get() = (_state.value as? ReaderState.Success)?.scrollToEndOnChapterChange ?: false
        set(value) { updateSuccessState { it.copy(scrollToEndOnChapterChange = value) } }
    
    var showSettingsBottomSheet: Boolean
        get() = (_state.value as? ReaderState.Success)?.showSettingsBottomSheet ?: false
        set(value) { updateSuccessState { it.copy(showSettingsBottomSheet = value) } }
    
    val showRepairBanner: Boolean
        get() = (_state.value as? ReaderState.Success)?.showRepairBanner ?: false
    
    val chapterBreakReason: String?
        get() = (_state.value as? ReaderState.Success)?.chapterBreakReason
    
    val isRepairing: Boolean
        get() = (_state.value as? ReaderState.Success)?.isRepairing ?: false
    
    val showRepairSuccess: Boolean
        get() = (_state.value as? ReaderState.Success)?.showRepairSuccess ?: false
    
    val repairSuccessSourceName: String?
        get() = (_state.value as? ReaderState.Success)?.repairSuccessSourceName
    
    val showFindInChapter: Boolean
        get() = (_state.value as? ReaderState.Success)?.showFindInChapter ?: false
    
    val findQuery: String
        get() = (_state.value as? ReaderState.Success)?.findQuery ?: ""
    
    val findMatches: List<IntRange>
        get() = (_state.value as? ReaderState.Success)?.findMatches ?: emptyList()
    
    val currentFindMatchIndex: Int
        get() = (_state.value as? ReaderState.Success)?.currentFindMatchIndex ?: 0
    
    var showReadingTime: Boolean
        get() = (_state.value as? ReaderState.Success)?.showReadingTime ?: false
        set(value) { updateSuccessState { it.copy(showReadingTime = value) } }
    
    val estimatedReadingMinutes: Int
        get() = (_state.value as? ReaderState.Success)?.estimatedReadingMinutes ?: 0
    
    val wordsRemaining: Int
        get() = (_state.value as? ReaderState.Success)?.wordsRemaining ?: 0
    
    val showReportDialog: Boolean
        get() = (_state.value as? ReaderState.Success)?.showReportDialog ?: false
    
    val showParagraphTranslationDialog: Boolean
        get() = (_state.value as? ReaderState.Success)?.showParagraphTranslationDialog ?: false
    
    val paragraphToTranslate: String
        get() = (_state.value as? ReaderState.Success)?.paragraphToTranslate ?: ""
    
    val translatedParagraph: String?
        get() = (_state.value as? ReaderState.Success)?.translatedParagraph
    
    val isParagraphTranslating: Boolean
        get() = (_state.value as? ReaderState.Success)?.isParagraphTranslating ?: false
    
    val paragraphTranslationError: String?
        get() = (_state.value as? ReaderState.Success)?.paragraphTranslationError
    
    val showTranslationApiKeyPrompt: Boolean
        get() = (_state.value as? ReaderState.Success)?.showTranslationApiKeyPrompt ?: false
    
    val showReadingBreakDialog: Boolean
        get() = (_state.value as? ReaderState.Success)?.showReadingBreakDialog ?: false
    
    // Expand top menu state
    var expandTopMenu by mutableStateOf(false)
    
    // Reader theme savable state
    var readerThemeSavable by mutableStateOf(false)
    
    // Reading time preference
    val readingBreakInterval = readerPreferences.readingBreakInterval().asState()
    
    // Paragraph translation preference
    val paragraphTranslationEnabled = readerPreferences.paragraphTranslationEnabled().asState()
    
    // TTS with translated text preference
    val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().asState()
    
    // Custom fonts
    var customFonts by mutableStateOf<List<ireader.domain.models.fonts.CustomFont>>(emptyList())
        private set
    val selectedFontId = mutableStateOf("")
    
    // ==================== UI Action Methods ====================
    
    fun updateBrightness(brightness: Float) {
        settingsViewModel.updateBrightness(brightness)
    }
    
    fun makeSettingTransparent() {
        settingsViewModel.isSettingChanging = true
        scope.launch {
            delay(100)
            settingsViewModel.isSettingChanging = false
        }
    }
    
    fun increaseAutoScrollSpeed() {
        settingsViewModel.increaseAutoScrollSpeed()
    }
    
    fun decreaseAutoScrollSpeed() {
        settingsViewModel.decreaseAutoScrollSpeed()
    }
    
    fun toggleAutoScroll() {
        settingsViewModel.toggleAutoScroll()
    }
    
    fun changeBackgroundColor(themeId: Long) {
        settingsViewModel.changeBackgroundColor(themeId, readerColors)
    }
    
    fun saveTextAlignment(alignment: ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment) {
        settingsViewModel.saveTextAlignment(alignment)
    }
    
    fun hideParagraphTranslation() {
        updateSuccessState { it.copy(showParagraphTranslationDialog = false) }
    }
    
    fun retryParagraphTranslation() {
        // Retry translation logic
    }
    
    fun getCurrentEngineName(): String {
        return translationEnginesManager.get().engineName
    }
    
    fun dismissTranslationApiKeyPrompt() {
        updateSuccessState { it.copy(showTranslationApiKeyPrompt = false) }
    }
    
    fun navigateToTranslationSettings() {
        // Navigation handled by UI
    }
    
    fun onTakeBreak() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }
    
    fun onContinueReading() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }
    
    fun onSnoozeReadingBreak(minutes: Int) {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }
    
    fun dismissReadingBreakDialog() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }
    
    fun showParagraphTranslation(paragraph: String) {
        updateSuccessState { 
            it.copy(
                showParagraphTranslationDialog = true,
                paragraphToTranslate = paragraph
            )
        }
    }
    
    fun updateReadingTimeEstimation(scrollProgress: Float) {
        val successState = _state.value as? ReaderState.Success ?: return
        val totalWords = successState.totalWords
        statisticsViewModel.updateProgress(scrollProgress, totalWords)
        
        // Update state with estimated reading time
        val estimatedMinutes = (statisticsViewModel.estimatedTimeRemaining ?: 0L) / 60000
        val wordsRemaining = if (totalWords > 0) ((1f - scrollProgress) * totalWords).toInt() else 0
        updateSuccessState { 
            it.copy(
                estimatedReadingMinutes = estimatedMinutes.toInt(),
                wordsRemaining = wordsRemaining
            )
        }
    }
    
    fun getCurrentContent(): List<Page> {
        val successState = _state.value as? ReaderState.Success ?: return emptyList()
        return successState.currentContent
    }
    
    fun getTranslationForParagraph(index: Int): String? {
        // Get translated content from state if available
        val successState = _state.value as? ReaderState.Success ?: return null
        val translatedContent = successState.translatedContent
        if (index < 0 || index >= translatedContent.size) return null
        val page = translatedContent.getOrNull(index)
        return (page as? ireader.core.source.model.Text)?.text
    }
    
    // Font management
    fun selectFont(fontId: String) {
        selectedFontId.value = fontId
        scope.launch {
            // Get the font and apply it
            val font = fontManagementUseCase.getFontById(fontId)
            if (font != null) {
                platformUiPreferences.font()?.set(
                    ireader.domain.preferences.models.FontType(
                        name = font.name,
                        fontFamily = ireader.domain.models.common.FontFamilyModel.Custom(font.name)
                    )
                )
            }
        }
    }
    
    fun selectGoogleFont(fontName: String) {
        scope.launch {
            try {
                // Set the font preference directly - Google Fonts are loaded dynamically
                platformUiPreferences.font()?.set(
                    ireader.domain.preferences.models.FontType(
                        name = fontName,
                        fontFamily = ireader.domain.models.common.FontFamilyModel.Custom(fontName)
                    )
                )
            } catch (e: Exception) {
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to load font: ${e.message}"))
            }
        }
    }
    
    fun deleteFont(fontId: String) {
        scope.launch {
            fontManagementUseCase.deleteFont(fontId)
        }
    }
    
    fun loadGlossary() {
        val bookId = book?.id ?: return
        loadGlossary(bookId)
    }
    
    fun addGlossaryEntry(source: String, target: String, type: ireader.domain.models.entities.GlossaryTermType, notes: String?) {
        val bookId = book?.id ?: return
        scope.launch {
            saveGlossaryEntryUseCase.execute(
                bookId = bookId,
                sourceTerm = source,
                targetTerm = target,
                termType = type,
                notes = notes
            )
        }
    }
    
    fun updateGlossaryEntry(entry: ireader.domain.models.entities.Glossary) {
        scope.launch {
            saveGlossaryEntryUseCase.execute(
                bookId = entry.bookId,
                sourceTerm = entry.sourceTerm,
                targetTerm = entry.targetTerm,
                termType = entry.termType,
                notes = entry.notes,
                entryId = entry.id
            )
        }
    }
    
    fun deleteGlossaryEntry(id: Long) {
        scope.launch {
            deleteGlossaryEntryUseCase.execute(id)
        }
    }
    
    fun exportGlossary(onSuccess: (String) -> Unit) {
        val currentBook = book ?: return
        scope.launch {
            val json = exportGlossaryUseCase.execute(currentBook.id, currentBook.title)
            onSuccess(json)
        }
    }
    
    fun importGlossary(json: String) {
        val bookId = book?.id ?: return
        scope.launch {
            importGlossaryUseCase.execute(json, bookId)
        }
    }

    // ==================== Cleanup ====================

    override fun onCleared() {
        super.onCleared()
        // Cancel all jobs
        preloadJob?.cancel()
        chapterNavigationJob?.cancel()
        chapterControllerEventJob?.cancel()
        preferencesControllerEventJob?.cancel()
        preloadedChapters.clear()
        statisticsViewModel.onChapterClosed()
        
        // Cleanup ChapterController state (Requirements: 9.2, 9.4, 9.5)
        chapterController.dispatch(ChapterCommand.Cleanup)
    }
}
