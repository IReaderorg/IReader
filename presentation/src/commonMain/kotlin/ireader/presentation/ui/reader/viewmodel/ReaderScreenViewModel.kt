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
import ireader.domain.services.chapter.ChapterCommand
import ireader.domain.services.chapter.ChapterController
import ireader.domain.services.chapter.ChapterEvent
import ireader.domain.services.preferences.PreferenceCommand
import ireader.domain.services.preferences.PreferenceEvent
import ireader.domain.services.preferences.ReaderPreferencesController
import ireader.domain.usecases.chapter.AutoRepairChapterUseCase
import ireader.domain.usecases.fonts.FontManagementUseCase
import ireader.domain.usecases.fonts.FontUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.reader.ReaderUseCasesAggregate
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.removeIf
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.NO_VALUE
import ireader.i18n.UiText
import ireader.i18n.resources.*
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
 * Refactored to use ReaderUseCasesAggregate to reduce constructor parameters.
 * Target: ≤15 constructor parameters (Requirements: 1.2, 1.4, 1.5)
 *
 * Uses a single immutable StateFlow<ReaderState> instead of multiple mutable states.
 * This provides:
 * - Single source of truth for reader state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 * - Clear Loading/Success/Error states
 */
class ReaderScreenViewModel(
    // Use case aggregate - groups 18 related use cases (Requirements: 1.2, 1.4, 1.5)
    private val readerUseCasesAggregate: ReaderUseCasesAggregate,
    val getLocalCatalog: GetLocalCatalog,
    val readerUseCases: ReaderPrefUseCases,
    val readerPreferences: ReaderPreferences,
    val androidUiPreferences: AppPreferences,
    val platformUiPreferences: PlatformUiPreferences,
    val uiPreferences: UiPreferences,
    val screenAlwaysOnUseCase: ScreenAlwaysOn,
    val webViewManger: WebViewManger,
    val readerThemeRepository: ReaderThemeRepository,
    val translationEnginesManager: TranslationEnginesManager,
    val fontManagementUseCase: FontManagementUseCase,
    val fontUseCase: FontUseCase,
    val chapterHealthChecker: ChapterHealthChecker,
    val chapterHealthRepository: ChapterHealthRepository,
    val autoRepairChapterUseCase: AutoRepairChapterUseCase,
    val params: Param,
    private val systemInteractionService: ireader.domain.services.platform.SystemInteractionService,
    // ChapterController - Reader's own instance for chapter operations
    private val chapterController: ChapterController,
    // ReaderPreferencesController - single source of truth for reader preferences
    private val preferencesController: ReaderPreferencesController,
    // TTSController - for syncing chapter when returning from TTS screen
    private val ttsController: ireader.domain.services.tts_service.v2.TTSController,
    // ChapterNotifier - for reactive chapter change notifications
    private val chapterNotifier: ireader.domain.services.chapter.ChapterNotifier,
    // Sub-ViewModels
    val settingsViewModel: ReaderSettingsViewModel,
    val translationViewModel: ReaderTranslationViewModel,
    val ttsViewModel: ReaderTTSViewModel,
    val statisticsViewModel: ReaderStatisticsViewModel,
) : BaseViewModel() {
    
    companion object {
        /** Delay (ms) after a remote fetch completes before notifying ChapterController.
         *  This ensures the DB insert is fully committed so ChapterController's loadChapter
         *  finds content and doesn't try to re-fetch from remote. */
        private const val FETCH_TO_CONTROLLER_DELAY_MS = 300L
        
        /** Delay (ms) before starting preload of the next chapter after the current
         *  chapter's remote fetch completes. This prevents two remote fetches from
         *  hitting the source server simultaneously. */
        private const val PRELOAD_AFTER_FETCH_DELAY_MS = 500L
    }

    
    // Convenience accessors for aggregate use cases (backward compatibility)
    val getBookUseCases get() = readerUseCasesAggregate.getBookUseCases
    val getChapterUseCase get() = readerUseCasesAggregate.getChapterUseCase
    val insertUseCases get() = readerUseCasesAggregate.insertUseCases
    val remoteUseCases get() = readerUseCasesAggregate.remoteUseCases
    val historyUseCase get() = readerUseCasesAggregate.historyUseCase
    val preloadChapterUseCase get() = readerUseCasesAggregate.preloadChapter
    val bookMarkChapterUseCase get() = readerUseCasesAggregate.bookmarkChapter
    val reportBrokenChapterUseCase get() = readerUseCasesAggregate.reportBrokenChapter
    val trackReadingProgressUseCase get() = readerUseCasesAggregate.trackReadingProgress
    val translateChapterWithStorageUseCase get() = readerUseCasesAggregate.translateChapterWithStorage
    val translateParagraphUseCase get() = readerUseCasesAggregate.translateParagraph
    val getTranslatedChapterUseCase get() = readerUseCasesAggregate.getTranslatedChapter
    val getGlossaryByBookIdUseCase get() = readerUseCasesAggregate.getGlossaryByBookId
    val saveGlossaryEntryUseCase get() = readerUseCasesAggregate.saveGlossaryEntry
    val deleteGlossaryEntryUseCase get() = readerUseCasesAggregate.deleteGlossaryEntry
    val exportGlossaryUseCase get() = readerUseCasesAggregate.exportGlossary
    val importGlossaryUseCase get() = readerUseCasesAggregate.importGlossary
    val contentFilterUseCase get() = readerUseCasesAggregate.contentFilter

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
    
    // Content filter preferences
    val contentFilterEnabled = readerPreferences.contentFilterEnabled().asState()
    val contentFilterPatterns = readerPreferences.contentFilterPatterns().asState()

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
    
    // Custom fonts - must be declared before init block to ensure proper initialization
    var customFonts by mutableStateOf<List<ireader.domain.models.fonts.CustomFont>>(emptyList())
        private set
    val selectedFontId = mutableStateOf("")

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
            // Subscribe to ChapterNotifier for reactive chapter updates
            subscribeToChapterNotifier()
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
        
        // Set up next chapter provider for auto-translate feature
        translationViewModel.setNextChapterProvider {
            getNextChapter()
        }
        
        // Load custom fonts (deferred to ensure property is initialized)
        loadCustomFonts()
        
        // Subscribe to content filter changes to re-filter content
        subscribeToContentFilterChanges()
    }
    
    /**
     * Subscribe to content filter preference changes and re-filter content when they change.
     */
    private fun subscribeToContentFilterChanges() {
        // Watch for filter enabled/disabled changes
        readerPreferences.contentFilterEnabled().changes()
            .onEach { 
                Log.debug { "Content filter enabled changed: $it" }
                contentFilterUseCase.invalidateCache()
                refilterCurrentContent()
            }
            .launchIn(scope)
        
        // Watch for filter patterns changes
        readerPreferences.contentFilterPatterns().changes()
            .onEach { 
                Log.debug { "Content filter patterns changed" }
                contentFilterUseCase.invalidateCache()
                refilterCurrentContent()
            }
            .launchIn(scope)
    }
    
    /**
     * Re-apply content filter to current chapter content.
     * Called when filter settings change.
     * Reloads the chapter from the use case to get freshly filtered content.
     */
    private fun refilterCurrentContent() {
        val currentState = _state.value as? ReaderState.Success ?: return
        val chapter = currentState.currentChapter
        val book = currentState.book
        val catalog = currentState.catalog
        
        // Invalidate the filter cache so new settings are applied
        contentFilterUseCase.invalidateCache()
        
        // Reload the chapter to get freshly filtered content
        scope.launch {
            val freshChapter = getChapterUseCase.findChapterById(chapter.id)
            if (freshChapter != null) {
                updateSuccessState { state ->
                    state.copy(content = freshChapter.content)
                }
                Log.debug { "Re-filtered content by reloading chapter: ${freshChapter.content.size} pages" }
            }
        }
    }
    
    /**
     * Saves the current scroll position for the current chapter.
     * This is called periodically as the user scrolls to prevent data loss.
     * Uses a dedicated update query that only modifies lastPageRead field.
     * Also updates the in-memory state so the value is available if ViewModel is cached.
     * 
     * @param scrollPosition The scroll position (LazyColumn item index) to save
     */
    fun saveScrollPosition(scrollPosition: Long) {
        val chapter = stateChapter ?: return
        
        // Update in-memory state immediately
        _currentScrollPosition = scrollPosition
        
        // Also update the state's currentChapter.lastPageRead so it's reflected in the cached ViewModel
        updateSuccessState { state ->
            state.copy(
                currentChapter = state.currentChapter.copy(lastPageRead = scrollPosition)
            )
        }
        
        // Save to database asynchronously
        scope.launch {
            try {
                // Use the repository's dedicated updateLastPageRead method
                // which only updates the lastPageRead field without touching content
                readerUseCasesAggregate.chapterRepository.updateLastPageRead(chapter.id, scrollPosition)
            } catch (e: Exception) {
                Log.error("Failed to save scroll position", e)
            }
        }
    }
    
    // Track the current scroll position (used for saving when navigating away)
    private var _currentScrollPosition: Long = 0L
    val currentScrollPosition: Long get() = _currentScrollPosition
    
    /**
     * Force save the current scroll position to the database immediately.
     * This should be called before navigating to a new chapter.
     */
    fun saveCurrentScrollPositionToDatabase() {
        val chapter = stateChapter ?: return
        val position = _currentScrollPosition
        if (position > 0) {
            scope.launch {
                try {
                    readerUseCasesAggregate.chapterRepository.updateLastPageRead(chapter.id, position)
                } catch (e: Exception) {
                    Log.error("Failed to force save scroll position", e)
                }
            }
        }
    }
    
    /**
     * Load custom fonts asynchronously.
     * Separated from init to ensure customFonts property delegate is fully initialized.
     */
    private fun loadCustomFonts() {
        scope.launch {
            try {
                customFonts = fontManagementUseCase.getCustomFonts()
            } catch (e: Exception) {
                // Log but don't crash - fonts are optional
                ireader.core.log.Log.warn { "Failed to load custom fonts: ${e.message}" }
            }
        }
    }
    
    // ==================== ChapterController Integration ====================
    /**
     * Subscribe to ChapterController events for error handling.
     * Note: Cross-screen sync removed - each screen has its own ChapterController instance.
     * TTS sync happens only via onTTSScreenPop() when user leaves TTS screen.
     */
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
    
    private var chapterNotifierJob: Job? = null
    
    /**
     * Subscribe to ChapterNotifier for reactive chapter change notifications.
     * This ensures the reader screen stays in sync when chapters are modified.
     * 
     * Performance optimizations:
     * - Uses changesForBook() to filter at source (avoids processing unrelated changes)
     * - Content updates (ContentFetched) are immediate for responsiveness
     * - Chapter list updates are debounced to avoid rapid recomposition
     * - Only refreshes when state is Success
     */
    private fun subscribeToChapterNotifier() {
        chapterNotifierJob?.cancel()
        chapterNotifierJob = scope.launch {
            // Wait for initial state to be set
            val initialBookId = (params.bookId ?: return@launch)
            
            // Use debounced flow for chapter list updates (drawer)
            // 100ms debounce prevents rapid recomposition during batch operations
            chapterNotifier.changesForBookDebounced(initialBookId, debounceMs = 100)
                .collect { change ->
                    val currentState = _state.value as? ReaderState.Success ?: return@collect
                    
                    when (change) {
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.BookChaptersRefreshed -> {
                            Log.debug { "ChapterNotifier: Chapters refreshed for book ${change.bookId}" }
                            refreshChaptersFromController()
                        }
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.ContentFetched -> {
                            Log.debug { "ChapterNotifier: Content fetched for chapter ${change.chapterId}" }
                            if (change.chapterId == currentState.currentChapter.id) {
                                reloadCurrentChapterContent()
                            }
                            // Always refresh chapters list to update downloaded status in drawer
                            refreshChaptersFromController()
                        }
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.ChapterUpdated -> {
                            Log.debug { "ChapterNotifier: Chapter ${change.chapterId} updated" }
                            if (change.chapterId == currentState.currentChapter.id) {
                                reloadCurrentChapterContent()
                            }
                            refreshChaptersFromController()
                        }
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.ChaptersUpdated -> {
                            Log.debug { "ChapterNotifier: ${change.chapterIds.size} chapters updated" }
                            if (change.chapterIds.contains(currentState.currentChapter.id)) {
                                reloadCurrentChapterContent()
                            }
                            refreshChaptersFromController()
                        }
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.CurrentChapterChanged -> {
                            // This is emitted by our own ChapterController, no need to handle
                        }
                        is ireader.domain.services.chapter.ChapterNotifier.ChangeType.FullRefresh -> {
                            Log.debug { "ChapterNotifier: Full refresh requested" }
                            refreshChaptersFromController()
                        }
                        else -> {
                            // Handle other change types if needed
                        }
                    }
                }
        }
    }
    
    /**
     * Refresh chapters list from database.
     * Called when ChapterNotifier signals a change.
     * 
     * Note: We fetch directly from the use case instead of ChapterController
     * because the controller's subscription might be debounced and not yet updated.
     */
    private fun refreshChaptersFromController() {
        val currentState = _state.value as? ReaderState.Success ?: return
        
        scope.launch {
            try {
                // Invalidate cache first to ensure fresh data
                // This is important because the drawer checks is_downloaded based on content length
                ireader.core.log.Log.debug { "refreshChaptersFromController: Refreshing chapters for book ${currentState.book.id}" }
                
                // Fetch fresh chapters from database
                val chapters = getChapterUseCase.findChaptersByBookId(currentState.book.id)
                ireader.core.log.Log.debug { 
                    "refreshChaptersFromController: Got ${chapters.size} chapters, current chapter id=${currentState.currentChapter.id}" 
                }
                
                if (chapters.isNotEmpty()) {
                    updateSuccessState { state ->
                        val newIndex = chapters.indexOfFirst { it.id == state.currentChapter.id }
                            .coerceAtLeast(0)
                        state.copy(
                            chapters = chapters,
                            currentChapterIndex = newIndex
                        )
                    }
                }
            } catch (e: Exception) {
                Log.error { "Failed to refresh chapters: ${e.message}" }
            }
        }
    }
    
    /**
     * Reload current chapter content from database.
     * Also updates the chapter in the chapters list to reflect any changes (e.g., read status).
     */
    private fun reloadCurrentChapterContent() {
        val currentState = _state.value as? ReaderState.Success ?: return
        scope.launch {
            val freshChapter = getChapterUseCase.findChapterById(currentState.currentChapter.id)
            if (freshChapter != null) {
                updateSuccessState { state ->
                    // Update both currentChapter and the chapter in the chapters list
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
    
    /**
     * Refresh the current chapter from the database to get the latest saved scroll position.
     * This should be called when the screen is re-entered (e.g., after navigating back)
     * to ensure the cached ViewModel state has the fresh lastPageRead value from the database.
     * 
     * This is important because the ViewModel may be cached for performance, meaning
     * the lastPageRead in ViewModel state could be stale if scrolling was saved during
     * the previous session but not reflected in the cached state.
     */
    fun refreshCurrentChapterFromDatabase() {
        val currentState = _state.value as? ReaderState.Success ?: return
        scope.launch {
            val freshChapter = getChapterUseCase.findChapterById(currentState.currentChapter.id)
            if (freshChapter != null) {
                updateSuccessState { state ->
                    state.copy(
                        currentChapter = freshChapter,
                        // Preserve content from current state if fresh chapter has no content (light query)
                        content = if (freshChapter.content.isNotEmpty()) freshChapter.content else state.content
                    )
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
        // Ensure preferences are loaded (lazy initialization for startup performance)
        preferencesController.ensurePreferencesLoaded()
        
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
    
    private var ttsChapterSyncJob: Job? = null
    
    /**
     * Subscribe to TTS state changes to sync when TTS stops playing.
     * This syncs Reader with TTS chapter when user returns from TTS screen.
     * Only syncs when TTS is idle/stopped (not while actively playing).
     */
    private fun subscribeTTSChapterChanges() {
        ttsChapterSyncJob?.cancel()
        ttsChapterSyncJob = scope.launch {
            var lastTTSChapterId: Long? = null
            
            ttsController.state.collect { ttsState ->
                val ttsChapter = ttsState.chapter
                val readerState = _state.value
                
                // Track when TTS chapter changes
                if (ttsChapter != null && ttsChapter.id != lastTTSChapterId) {
                    lastTTSChapterId = ttsChapter.id
                    
                    // Only sync when TTS is NOT playing (user has left TTS or stopped playback)
                    // This prevents sync while TTS is actively navigating chapters
                    if (!ttsState.isPlaying && readerState is ReaderState.Success &&
                        ttsState.book?.id == readerState.book.id &&
                        ttsChapter.id != readerState.currentChapter.id) {
                        
                        Log.debug { "TTS stopped on chapter ${ttsChapter.id}, syncing Reader" }
                        loadChapter(
                            readerState.book,
                            readerState.catalog,
                            ttsChapter.id,
                            next = false,
                            force = false,
                            scrollToEnd = false
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Sync Reader with TTS's current chapter.
     * Called explicitly when user returns from TTS screen.
     */
    suspend fun syncWithTTSChapter() {
        val ttsState = ttsController.state.value
        val ttsChapter = ttsState.chapter ?: return
        val readerState = _state.value
        
        if (readerState !is ReaderState.Success) return
        
        // Only sync if same book and different chapter
        if (ttsState.book?.id == readerState.book.id &&
            ttsChapter.id != readerState.currentChapter.id) {
            
            Log.debug { "Syncing Reader with TTS chapter ${ttsChapter.id}" }
            loadChapter(
                readerState.book,
                readerState.catalog,
                ttsChapter.id,
                next = false,
                force = false,
                scrollToEnd = false
            )
        }
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
                // Get chapters from ChapterController state, with fallback to database
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
     * Load a chapter by ID.
     * 
     * Chapters list is managed by ChapterController and synced via ChapterNotifier.
     * This method only loads the current chapter content and updates local UI state.
     * 
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
        // Save current scroll position before loading new chapter
        // This ensures we don't lose progress when navigating between chapters
        saveCurrentScrollPositionToDatabase()
        
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
        
        Log.debug { "loadChapter: chapterId=${chapter.id}, lastPageRead=${chapter.lastPageRead}, contentSize=${chapter.content.size}" }

        // Get chapters - prefer ChapterController but fallback to database
        // This ensures we have chapters even if ChapterController hasn't loaded yet
        val chapters = getChaptersFromController().ifEmpty {
            getChapterUseCase.findChaptersByBookId(book.id)
        }
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
        // Note: chapter.content is already filtered at the use case level (FindChapterById)
        val totalWords = calculateTotalWords(chapter.content)
        
        // Use existing chapters from state if ChapterController hasn't loaded yet
        val effectiveChapters = chapters.ifEmpty { previousSuccessState?.chapters ?: emptyList() }
        val effectiveIndex = if (chapters.isNotEmpty()) chapterIndex else {
            effectiveChapters.indexOfFirst { it.id == chapter.id }
        }
        
        _state.value = ReaderState.Success(
            book = book,
            currentChapter = chapter,
            chapters = effectiveChapters,
            catalog = catalog,
            content = chapter.content,
            currentChapterIndex = if (effectiveIndex != -1) effectiveIndex else 0,
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

        // Fetch remote content if needed
        val needsRemoteFetch = chapter.isEmpty() && catalog?.source != null
        if (needsRemoteFetch && !force) {
            // Fetch content asynchronously via the Reader's own fetch path.
            // IMPORTANT: Do NOT also dispatch ChapterCommand.LoadChapter before this completes,
            // because ChapterController.loadChapter() would also try to fetch from remote
            // and call insertChapter(), causing a race condition.
            fetchRemoteChapter(book, catalog, chapter)
        } else if (!needsRemoteFetch) {
            // Chapter already has content - safe to notify ChapterController immediately
            // since it won't try to fetch from remote when content is present.
            chapterController.dispatch(ChapterCommand.LoadChapter(chapterId))
            // Check chapter health
            checkChapterHealth(chapter)
        }

        // Update last read time
        getChapterUseCase.updateLastReadTime(chapter)

        // Track chapter open (pass isLast to track book completion when last chapter is finished)
        val isLastChapter = effectiveIndex != -1 && effectiveIndex == effectiveChapters.lastIndex
        statisticsViewModel.onChapterOpened(chapter, isLastChapter)

        // Trigger preload with a delay to ensure the current chapter's DB insert
        // (from fetchRemoteChapter) completes first, avoiding insert conflicts.
        // NOTE: We only use the Reader's own preload path (preloadChapter via fetchAndSaveChapterContent),
        // NOT ChapterController.PreloadNextChapter, to avoid duplicate remote fetches.
        if (!needsRemoteFetch) {
            // Chapter already loaded from DB, safe to preload immediately
            triggerPreloadNextChapter()
        } else {
            // Chapter is being fetched from remote - preload will be triggered
            // after fetchRemoteChapter completes (in the onSuccess callback)
        }

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

    /**
     * Fetch chapter content from remote (async).
     * The FetchAndSaveChapterContentUseCase saves to DB before calling onSuccess,
     * ensuring the chapter is persisted.
     * 
     * After fetch completes successfully:
     * 1. Notifies ChapterController (safe now since content is present, no re-fetch)
     * 2. Triggers preload of next chapter (with delay to avoid DB conflicts)
     */
    private suspend fun fetchRemoteChapter(book: Book, catalog: CatalogLocal?, chapter: Chapter) {
        remoteUseCases.fetchAndSaveChapterContent(
            chapter = chapter,
            catalog = catalog,
            onSuccess = { filteredChapter ->
                // Chapter is already saved to DB and filtered
                val totalWords = calculateTotalWords(filteredChapter.content)
                
                updateSuccessState { state ->
                    state.copy(
                        currentChapter = filteredChapter,
                        content = filteredChapter.content,
                        isLoadingContent = false,
                        totalWords = totalWords
                    )
                }
                
                // Notify ChapterNotifier that content was fetched
                // This will trigger refreshChaptersFromController() to update the chapters list
                chapterNotifier.tryNotifyChange(
                    ireader.domain.services.chapter.ChapterNotifier.ChangeType.ContentFetched(
                        chapterId = filteredChapter.id,
                        bookId = book.id
                    )
                )
                
                // NOW it's safe to notify ChapterController - the chapter has content,
                // so ChapterController.loadChapter() will skip remote fetch.
                // Add a small delay to ensure the DB insert has fully committed.
                delay(FETCH_TO_CONTROLLER_DELAY_MS)
                chapterController.dispatch(ChapterCommand.LoadChapter(filteredChapter.id))
                
                // Check chapter health after content loads
                if (filteredChapter.content.isNotEmpty()) {
                    checkChapterHealth(filteredChapter)
                }
                
                // Trigger preload of next chapter now that current fetch is done.
                // This staggering prevents the preload from racing with the current fetch.
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
     * Navigate to the next chapter.
     * Uses ReaderScreenViewModel's own state for reliable navigation.
     */
    fun dispatchNextChapter() {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return
        
        val chapters = currentState.chapters
        val currentChapterIndex = currentState.currentChapterIndex
        
        if (currentChapterIndex < chapters.lastIndex) {
            val nextChapter = chapters.getOrNull(currentChapterIndex + 1)
            if (nextChapter != null) {
                Log.debug { "dispatchNextChapter: navigating from index $currentChapterIndex to ${currentChapterIndex + 1}" }
                navigateToChapter(nextChapter.id, next = true)
            }
        } else {
            Log.debug { "dispatchNextChapter: already at last chapter (index $currentChapterIndex of ${chapters.size})" }
        }
    }
    
    /**
     * Navigate to the previous chapter.
     * Uses ReaderScreenViewModel's own state for reliable navigation.
     */
    fun dispatchPrevChapter() {
        val currentState = _state.value
        if (currentState !is ReaderState.Success) return
        
        val chapters = currentState.chapters
        val currentChapterIndex = currentState.currentChapterIndex
        val currentChapter = currentState.currentChapter
        
        Log.debug { "dispatchPrevChapter: currentChapter=${currentChapter?.name}, currentIndex=$currentChapterIndex, totalChapters=${chapters.size}" }
        
        if (currentChapterIndex > 0) {
            val prevChapter = chapters.getOrNull(currentChapterIndex - 1)
            Log.debug { "dispatchPrevChapter: prevChapter=${prevChapter?.name}, prevChapterId=${prevChapter?.id}" }
            if (prevChapter != null) {
                Log.debug { "dispatchPrevChapter: navigating from '${currentChapter?.name}' (index $currentChapterIndex) to '${prevChapter.name}' (index ${currentChapterIndex - 1})" }
                // Set flag to scroll to end when loading previous chapter
                scrollToEndOnChapterChange = true
                navigateToChapter(prevChapter.id, next = false)
            }
        } else {
            Log.debug { "dispatchPrevChapter: already at first chapter (index $currentChapterIndex)" }
        }
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
     * ChapterController is notified by loadChapter() at the right time
     * (after remote fetch completes, if needed) to avoid race conditions.
     */
    suspend fun getLocalChapter(
        chapterId: Long?,
        next: Boolean = true,
        force: Boolean = false
    ): Chapter? {
        if (chapterId == null) return null

        val currentState = _state.value
        return if (currentState is ReaderState.Success) {
            // NOTE: Do NOT dispatch to ChapterController here.
            // loadChapter() handles ChapterController notification at the right time
            // (after content is available) to prevent race conditions.
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
                // Add a small delay to avoid racing with any in-progress DB operations
                // from the current chapter's fetch/insert cycle
                delay(PRELOAD_AFTER_FETCH_DELAY_MS)
                
                // NOTE: We intentionally do NOT dispatch ChapterCommand.PreloadNextChapter here.
                // ChapterController.preloadNextChapter() uses loadChapterContentUseCase which
                // calls chapterRepository.insertChapter() independently. This would race with
                // our own preloadChapter() which uses fetchAndSaveChapterContent() (also inserts).
                // Using only one path prevents duplicate remote fetches and DB insert conflicts.
                
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
            Log.debug { "preloadChapter: [START] Preloading chapter ${chapter.id} '${chapter.name}' from remote" }
            updateSuccessState { it.copy(isPreloading = true) }

            // Use FetchAndSaveChapterContentUseCase for thread-safe preloading
            // This prevents race conditions with concurrent fetch operations
            remoteUseCases.fetchAndSaveChapterContent(
                chapter = chapter,
                catalog = currentState.catalog,
                onSuccess = { preloadedChapter ->
                    preloadedChapters[chapter.id] = preloadedChapter
                    // Enhanced logging: confirm preloaded chapter saved to DB (Req 3.3)
                    Log.debug { "preloadChapter: [SAVED] Preloaded chapter ${chapter.id} '${chapter.name}' saved to DB with ${preloadedChapter.content.size} pages" }
                    updateSuccessState { it.copy(isPreloading = false) }
                },
                onError = { error ->
                    // Enhanced logging: preload failure without affecting current chapter (Req 3.4)
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

    // ==================== Chapter Art Generation ====================
    
    /**
     * Show the chapter art generation dialog.
     * This allows users to generate an AI image prompt from the current chapter.
     */
    fun showChapterArtDialog() {
        updateSuccessState { it.copy(showChapterArtDialog = true) }
    }
    
    /**
     * Dismiss the chapter art dialog
     */
    fun dismissChapterArtDialog() {
        updateSuccessState { 
            it.copy(
                showChapterArtDialog = false,
                isGeneratingArtPrompt = false,
                generatedArtPrompt = null,
                chapterArtError = null
            ) 
        }
    }
    
    /**
     * Get chapter art dialog visibility
     */
    val showChapterArtDialog: Boolean
        get() = (_state.value as? ReaderState.Success)?.showChapterArtDialog ?: false
    
    /**
     * Get chapter art generation loading state
     */
    val isGeneratingArtPrompt: Boolean
        get() = (_state.value as? ReaderState.Success)?.isGeneratingArtPrompt ?: false
    
    /**
     * Get generated art prompt
     */
    val generatedArtPrompt: String?
        get() = (_state.value as? ReaderState.Success)?.generatedArtPrompt
    
    /**
     * Get chapter art error
     */
    val chapterArtError: String?
        get() = (_state.value as? ReaderState.Success)?.chapterArtError
    
    // Lazy-initialized prompt generator
    private val chapterArtPromptGenerator by lazy {
        ireader.data.characterart.ChapterArtPromptGenerator(
            translationEnginesManager = translationEnginesManager
        )
    }
    
    /**
     * Generate an image prompt from the current chapter using the configured translation engine.
     * @param focus The type of visual element to focus on (CHARACTER, SCENE, SETTING, or AUTO)
     */
    fun generateChapterArtPrompt(focus: ireader.data.characterart.PromptFocus) {
        val currentState = _state.value as? ReaderState.Success ?: return
        
        // Check if translation engine is configured
        val apiKey = translationEnginesManager.getApiKeyForCurrentEngine()
        if (apiKey.isBlank()) {
            updateSuccessState { 
                it.copy(
                    showChapterArtDialog = false,
                    chapterArtError = "Please configure your translation engine API key in Settings > Translation"
                )
            }
            showSnackBar(UiText.DynamicString("Please configure your translation engine API key in Settings > Translation"))
            return
        }
        
        // Extract text from chapter content
        val chapterText = currentState.content
            .filterIsInstance<Text>()
            .joinToString("\n\n") { it.text }
        
        if (chapterText.length < 100) {
            updateSuccessState { 
                it.copy(
                    showChapterArtDialog = false,
                    chapterArtError = "Chapter text is too short to analyze"
                )
            }
            showSnackBar(UiText.DynamicString("Chapter text is too short to analyze"))
            return
        }
        
        // Show loading state
        updateSuccessState { 
            it.copy(
                showChapterArtDialog = false,
                isGeneratingArtPrompt = true,
                chapterArtError = null
            )
        }
        
        scope.launch {
            try {
                chapterArtPromptGenerator.generateImagePrompt(
                    chapterText = chapterText,
                    bookTitle = currentState.book.title,
                    chapterTitle = currentState.currentChapter.name,
                    preferredFocus = focus
                ).onSuccess { result ->
                    updateSuccessState { 
                        it.copy(
                            isGeneratingArtPrompt = false,
                            generatedArtPrompt = result.imagePrompt
                        )
                    }
                }.onFailure { error ->
                    updateSuccessState { 
                        it.copy(
                            isGeneratingArtPrompt = false,
                            chapterArtError = error.message ?: "Failed to generate prompt"
                        )
                    }
                    showSnackBar(UiText.DynamicString("Failed to generate prompt: ${error.message}"))
                }
            } catch (e: Exception) {
                updateSuccessState { 
                    it.copy(
                        isGeneratingArtPrompt = false,
                        chapterArtError = e.message ?: "Failed to generate prompt"
                    )
                }
                showSnackBar(UiText.DynamicString("Failed to generate prompt: ${e.message}"))
            }
        }
    }
    
    /**
     * Clear the generated art prompt (after navigating to upload screen)
     */
    fun clearGeneratedArtPrompt() {
        updateSuccessState { 
            it.copy(
                generatedArtPrompt = null,
                chapterArtError = null
            )
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
    
    // ==================== Quote Copy Mode ====================
    
    /**
     * Whether copy mode is active (text selection enabled for quote capture)
     */
    var copyModeActive by mutableStateOf(false)
        private set
    
    /**
     * Enter copy mode - enables text selection for quote capture
     */
    fun enterCopyMode() {
        // Enable reader mode so the UI is visible for copy mode
        updateSuccessState { it.copy(isReaderModeEnabled = true) }
        // Close settings bottom sheet if open
        showSettingsBottomSheet = false
        // Activate copy mode
        copyModeActive = true
    }
    
    /**
     * Exit copy mode without saving
     */
    fun exitCopyMode() {
        copyModeActive = false
    }
    
    /**
     * Finish copy mode and return params for quote creation
     */
    fun finishCopyMode(): ireader.domain.models.quote.QuoteCreationParams? {
        copyModeActive = false
        
        val successState = _state.value as? ReaderState.Success ?: return null
        val currentBook = successState.book
        val currentChapter = successState.currentChapter
        val chapters = successState.chapters
        val currentIndex = chapters.indexOfFirst { it.id == currentChapter.id }
        
        return ireader.domain.models.quote.QuoteCreationParams(
            bookId = currentBook.id,
            bookTitle = currentBook.title,
            chapterTitle = currentChapter.name,
            chapterNumber = currentChapter.number?.toInt(),
            author = currentBook.author,
            currentChapterId = currentChapter.id,
            prevChapterId = chapters.getOrNull(currentIndex - 1)?.id,
            nextChapterId = chapters.getOrNull(currentIndex + 1)?.id
        )
    }
    
    // Reading time preference
    val readingBreakInterval = readerPreferences.readingBreakInterval().asState()
    
    // Paragraph translation preference
    val paragraphTranslationEnabled = readerPreferences.paragraphTranslationEnabled().asState()
    
    // TTS with translated text preference
    val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().asState()
    
    // Show reading time indicator preference
    val showReadingTimeIndicator = readerPreferences.showReadingTimeIndicator().asState()
    
    // Note: customFonts and selectedFontId are declared before init block
    
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
        // Content is already filtered when loaded into state
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
