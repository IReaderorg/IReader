package ireader.presentation.ui.reader.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.http.WebViewManger
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.readerThemes
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.utils.extensions.async.nextAfter
import ireader.domain.utils.extensions.async.prevBefore
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.NO_VALUE
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toDomainColor
import ireader.presentation.ui.core.ui.PreferenceMutableState

@OptIn(ExperimentalTextApi::class)

class ReaderScreenViewModel(
    val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    val remoteUseCases: RemoteUseCases,
    val historyUseCase: HistoryUseCase,
    val getLocalCatalog: GetLocalCatalog,
    val readerUseCases: ReaderPrefUseCases,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    val prefState: ReaderScreenPreferencesStateImpl,
    val state: ReaderScreenStateImpl,
    val prefFunc: PlatformReaderSettingReader,
    val readerPreferences: ReaderPreferences,
    val androidUiPreferences: AppPreferences,
    val platformUiPreferences: PlatformUiPreferences,
    val uiPreferences: UiPreferences,
    val screenAlwaysOnUseCase: ScreenAlwaysOn,
    val webViewManger: WebViewManger,
    val readerThemeRepository: ReaderThemeRepository,
    val bookMarkChapterUseCase: ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase,
    val translationEnginesManager: TranslationEnginesManager,
    val preloadChapterUseCase: ireader.domain.usecases.reader.PreloadChapterUseCase,
        // Translation use cases
    val translateChapterWithStorageUseCase: ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase,
    val translateParagraphUseCase: ireader.domain.usecases.translate.TranslateParagraphUseCase,
    val getTranslatedChapterUseCase: ireader.domain.usecases.translation.GetTranslatedChapterUseCase,
    val getGlossaryByBookIdUseCase: ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase,
    val saveGlossaryEntryUseCase: ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase,
    val deleteGlossaryEntryUseCase: ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase,
    val exportGlossaryUseCase: ireader.domain.usecases.glossary.ExportGlossaryUseCase,
    val importGlossaryUseCase: ireader.domain.usecases.glossary.ImportGlossaryUseCase,
        // Statistics use case
    val trackReadingProgressUseCase: ireader.domain.usecases.statistics.TrackReadingProgressUseCase,
        // Report use case
    val reportBrokenChapterUseCase: ireader.domain.usecases.chapter.ReportBrokenChapterUseCase,
        // Font management use case
    val fontManagementUseCase: ireader.domain.usecases.fonts.FontManagementUseCase,
    val fontUseCase: ireader.domain.usecases.fonts.FontUseCase,
        // Chapter health and repair
    val chapterHealthChecker: ireader.domain.services.ChapterHealthChecker,
    val chapterHealthRepository: ireader.domain.data.repository.ChapterHealthRepository,
    val autoRepairChapterUseCase: ireader.domain.usecases.chapter.AutoRepairChapterUseCase,
    val params: Param,
        // Platform services - Clean architecture
    private val systemInteractionService: ireader.domain.services.platform.SystemInteractionService,
        // NEW: Sub-ViewModels for better separation of concerns
    val settingsViewModel: ReaderSettingsViewModel,
    val translationViewModel: ReaderTranslationViewModel,
    val ttsViewModel: ReaderTTSViewModel,
    val statisticsViewModel: ReaderStatisticsViewModel,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(),
    ReaderScreenPreferencesState by prefState,
    ReaderScreenState by state {
    data class Param(val chapterId: Long?, val bookId: Long?)

    val globalChapterId : State<Long?> = mutableStateOf(params.chapterId)
    val globalBookId : State<Long?> = mutableStateOf(params.bookId)

    val readerColors: SnapshotStateList<ReaderColors> = androidx.compose.runtime.mutableStateListOf<ReaderColors>().apply { addAll(readerThemes) }

    var isSettingChanging by mutableStateOf(false)

    var isSettingChangingJob : Job? = null
    
    var showBrightnessControl by mutableStateOf(false)
    var showFontSizeAdjuster by mutableStateOf(false)
    var showFontPicker by mutableStateOf(false)
    
    // Reading statistics tracking
    private var chapterOpenTimestamp: Long? = null
    
    // Font management state
    var customFonts by mutableStateOf<List<ireader.domain.models.fonts.CustomFont>>(emptyList())
        private set
    var systemFonts by mutableStateOf<List<ireader.domain.models.fonts.CustomFont>>(emptyList())
        private set
    val selectedFontId = readerPreferences.selectedFontId().asState()
    
    // Reading break reminder
    private val readingTimerManager = ireader.domain.services.ReadingTimerManager(
        scope = scope,
        onIntervalReached = { onReadingBreakIntervalReached() }
    )
    val readingBreakReminderEnabled = readerPreferences.readingBreakReminderEnabled().asState()
    val readingBreakInterval = readerPreferences.readingBreakInterval().asState()
    
    // ==================== Delegated Methods to Sub-ViewModels ====================
    // These methods delegate to the appropriate sub-ViewModel for better separation of concerns
    
    // Settings delegation
    fun increaseAutoScrollSpeed() = settingsViewModel.increaseAutoScrollSpeed()
    fun decreaseAutoScrollSpeed() = settingsViewModel.decreaseAutoScrollSpeed()
    fun toggleAutoScroll() = settingsViewModel.toggleAutoScroll()
    fun updateBrightness(newBrightness: Float) = settingsViewModel.updateBrightness(newBrightness)
    suspend fun getCurrentBrightness(): Float = settingsViewModel.getCurrentBrightness()
    fun setSecureScreen(enabled: Boolean) = settingsViewModel.setSecureScreen(enabled)
    fun setKeepScreenOn(enabled: Boolean) = settingsViewModel.setKeepScreenOn(enabled)
    fun changeBackgroundColor(themeId: Long) = settingsViewModel.changeBackgroundColor(themeId, readerColors)
    fun setReaderBackgroundColor(color: Color) = settingsViewModel.setReaderBackgroundColor(color)
    fun setReaderTextColor(color: Color) = settingsViewModel.setReaderTextColor(color)
    fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment) = settingsViewModel.saveTextAlignment(textAlign)
    
    // Translation delegation
    fun toggleTranslation() = translationViewModel.toggleTranslation()
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
    
    // Statistics delegation
    fun onChapterOpenedStats(chapter: Chapter) = statisticsViewModel.onChapterOpened(chapter)
    fun onChapterClosedStats() = statisticsViewModel.onChapterClosed()
    fun updateProgressStats(progress: Float, totalWords: Int = 0) = statisticsViewModel.updateProgress(progress, totalWords)
    
    // TTS delegation
    fun playTTS(chapter: Chapter? = null) = ttsViewModel.play(chapter)
    fun pauseTTS() = ttsViewModel.pause()
    fun stopTTS() = ttsViewModel.stop()
    fun togglePlayPause(chapter: Chapter? = null) = ttsViewModel.togglePlayPause(chapter)
    fun setTTSSpeed(speed: Float) = ttsViewModel.setSpeed(speed)
    fun setTTSPitch(pitch: Float) = ttsViewModel.setPitch(pitch)
    fun setTTSVoice(voiceId: String) = ttsViewModel.setVoice(voiceId)
    
    fun makeSettingTransparent() {
        isSettingChangingJob?.cancel()
        isSettingChangingJob = scope.launch {
            isSettingChanging = true
            delay(500)
            isSettingChanging = false
        }
    }
    // ==================== Preference State (TODO: Remove - Access from preferences directly) ====================
    // These should be accessed directly from readerPreferences, androidUiPreferences, etc. in the UI
    // Keeping for backward compatibility during transition
    val dateFormat by uiPreferences.dateFormat().asState()
    val relativeTime by uiPreferences.relativeTime().asState()
    val translatorOriginLanguage = readerPreferences.translatorOriginLanguage().asState()
    val translatorTargetLanguage = readerPreferences.translatorTargetLanguage().asState()
    val translatorEngine = readerPreferences.translatorEngine().asState()
    val translatorContentType = readerPreferences.translatorContentType().asState()
    val translatorToneType = readerPreferences.translatorToneType().asState()
    val translatorPreserveStyle = readerPreferences.translatorPreserveStyle().asState()
    val readerTheme = androidUiPreferences.readerTheme().asState()
    val backgroundColor = androidUiPreferences.backgroundColorReader().asState()
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()
    // Slider preferences use debounced state to avoid excessive writes during drag
    val topContentPadding = readerPreferences.topContentPadding().asStateDebounced()
    val screenAlwaysOn = readerPreferences.screenAlwaysOn().asState()
    val autoPreloadNextChapter = readerPreferences.autoPreloadNextChapter().asState()
    val preloadOnlyOnWifi = readerPreferences.preloadOnlyOnWifi().asState()
    val bottomContentPadding = readerPreferences.bottomContentPadding().asStateDebounced()
    val topMargin = readerPreferences.topMargin().asStateDebounced()
    val leftMargin = readerPreferences.leftMargin().asStateDebounced()
    val rightMargin = readerPreferences.rightMargin().asStateDebounced()
    val bottomMargin = readerPreferences.bottomMargin().asStateDebounced()
    val textColor = androidUiPreferences.textColorReader().asState()
    var readerThemeSavable by mutableStateOf(false)
    val selectedScrollBarColor = androidUiPreferences.selectedScrollBarColor().asState()
    val unselectedScrollBarColor = androidUiPreferences.unselectedScrollBarColor().asState()
    
    // Wrapper properties for UI components that expect Color instead of DomainColor
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
    val selectedScrollBarColorCompose = object : androidx.compose.runtime.MutableState<Color> {
        override var value: Color
            get() = selectedScrollBarColor.value.toComposeColor()
            set(value) { selectedScrollBarColor.value = value.toDomainColor() }
        override fun component1(): Color = value
        override fun component2(): (Color) -> Unit = { value = it }
    }
    val unselectedScrollBarColorCompose = object : androidx.compose.runtime.MutableState<Color> {
        override var value: Color
            get() = unselectedScrollBarColor.value.toComposeColor()
            set(value) { unselectedScrollBarColor.value = value.toDomainColor() }
        override fun component1(): Color = value
        override fun component2(): (Color) -> Unit = { value = it }
    }
    // Text-related slider preferences - debounced for smooth UI during drag
    val lineHeight = readerPreferences.lineHeight().asStateDebounced()
    val betweenLetterSpaces = readerPreferences.betweenLetterSpaces().asStateDebounced()
    val textWeight = readerPreferences.textWeight().asStateDebounced()
    val paragraphsIndent = readerPreferences.paragraphIndent().asStateDebounced()
    val showScrollIndicator = readerPreferences.showScrollIndicator().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val orientation = androidUiPreferences.orientation().asState()
    @OptIn(ExperimentalTime::class)
    var lastOrientationChangedTime = mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds())
    // Scroll indicator slider preferences - debounced
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asStateDebounced()
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asStateDebounced()
    val scrollIndicatorAlignment = readerPreferences.scrollBarAlignment().asState()
    val autoScrollOffset = readerPreferences.autoScrollOffset().asStateDebounced()
    var autoScrollInterval = readerPreferences.autoScrollInterval().asStateDebounced()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val brightness = readerPreferences.brightness().asStateDebounced()
    var chapterNumberMode by readerPreferences.showChapterNumberPreferences().asState()
    val isScrollIndicatorDraggable = readerPreferences.scrollbarMode().asState()
    val font = platformUiPreferences.font()?.asState()
    var customFont by mutableStateOf<ireader.domain.models.fonts.CustomFont?>(null)
    var fontVersion by mutableStateOf(0); private set
    val webViewIntegration = readerPreferences.webViewIntegration().asState()
    val webViewBackgroundMode = readerPreferences.webViewBackgroundMode().asState()
    val selectableMode = readerPreferences.selectableText().asState()
    val fontSize = readerPreferences.fontSize().asStateDebounced()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asStateDebounced()
    val bionicReadingMode = readerPreferences.bionicReading().asState()
    val verticalScrolling = readerPreferences.scrollMode().asState()
    val readingMode = readerPreferences.readingMode().asState()
    
    // Font loading moved to ReaderSettingsViewModel ✅
    val fonts get() = settingsViewModel.fonts
    val fontsLoading get() = settingsViewModel.fontsLoading

    // Translation state and preferences - delegated to translationViewModel
    val showTranslatedContent = readerPreferences.showTranslatedContent().asState()
    val autoSaveTranslations = readerPreferences.autoSaveTranslations().asState()
    val applyGlossaryToTranslations = readerPreferences.applyGlossaryToTranslations().asState()
    val bilingualModeEnabled = readerPreferences.bilingualModeEnabled().asState()
    val bilingualModeLayout = readerPreferences.bilingualModeLayout().asState()
    val volumeKeyNavigation = readerPreferences.volumeKeyNavigation().asState()
    val paragraphTranslationEnabled = readerPreferences.paragraphTranslationEnabled().asState()
    val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().asState()
    val autoTranslateNextChapter = readerPreferences.autoTranslateNextChapter().asState()
    
    // Override to provide translation-aware content
    override fun getCurrentContent(): List<ireader.core.source.model.Page> {
        return getCurrentChapterContent()
    }

    var isToggleInProgress by mutableStateOf(false)
    
    // Expose translation state for backward compatibility
    val translationState: TranslationStateHolder
        get() = translationViewModel.translationState

    init {
        val chapterId = globalChapterId.value
        val bookId = globalBookId.value


        if (bookId != null && chapterId != null) {
            subscribeReaderThemes()
            subscribeChapters(bookId)
            scope.launch {
                val source = getBookUseCases.findBookById(bookId)?.let {
                    getLocalCatalog.get(it.sourceId)
                }
                state.catalog = source
                state.book = getBookUseCases.findBookById(bookId)
                setupChapters(bookId, chapterId)
                loadGlossary()
                loadFonts()
            }
        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(Res.string.something_wrong_with_book))
            }
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

    private suspend fun setupChapters(bookId: Long, chapterId: Long) {
        val last = historyUseCase.findHistoryByBookId(bookId)
        if (chapterId != LAST_CHAPTER && chapterId != NO_VALUE) {
            getLocalChapter(chapterId)
        } else if (last != null) {
            getLocalChapter(chapterId = last.chapterId)
        } else {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId)
            if (chapters.isNotEmpty()) {
                getLocalChapter(chapters.first().id)
            }
        }
    }

    suspend fun getLocalChapter(
        chapterId: Long?,
        next: Boolean = true,
        force: Boolean = false
    ): Chapter? {
        if (chapterId == null) return null

        isLoading = true
        
        // Track chapter close before loading new chapter
        onChapterClosed()
        
        // Reset translation state for new chapter
        translationViewModel.translationState.reset()
        
        // Check if chapter is already preloaded
        val preloadedChapter = preloadedChapters[chapterId]
        val chapter = if (preloadedChapter != null && !preloadedChapter.isEmpty()) {
            preloadedChapters.remove(chapterId) // Remove from cache after use
            preloadedChapter
        } else {
            getChapterUseCase.findChapterById(chapterId)
        }
        
        chapter.let {
            stateChapter = it
        }
        
        // Track if we need to fetch remote content
        val needsRemoteFetch = chapter != null && (chapter.isEmpty() || force) && state.source != null
        
        if (needsRemoteFetch) {
            // Keep loading state while fetching remote content
            getRemoteChapter(chapter!!)
            // Note: isLoading will be set to false in getRemoteChapter callback
        } else {
            // No remote fetch needed, we can set loading to false
            isLoading = false
        }
        
        stateChapter?.let { ch -> getChapterUseCase.updateLastReadTime(ch) }
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
        }

        initialized = true

        stateChapter?.let {
            if (next) {
                chapterShell.add(it)
            } else {
                chapterShell.add(0, it)
            }
        }
        
        // Track chapter open
        onChapterOpened()
        
        // Check chapter health after loading - only if:
        // 1. Chapter has content
        // 2. We're NOT waiting for remote content (needsRemoteFetch is false)
        // This prevents false positives when content is still loading
        if (!needsRemoteFetch) {
            stateChapter?.let { ch ->
                if (ch.content.isNotEmpty()) {
                    checkChapterHealth(ch, chapterHealthChecker, chapterHealthRepository)
                } else {
                    // Reset broken chapter state if content is empty (might be loading)
                    prefState.isChapterBroken = false
                    prefState.chapterBreakReason = null
                    prefState.showRepairBanner = false
                }
            }
        } else {
            // Remote fetch in progress - reset broken state until content loads
            prefState.isChapterBroken = false
            prefState.chapterBreakReason = null
            prefState.showRepairBanner = false
        }
        
        // Clear old preloaded chapters to prevent memory buildup
        if (preloadedChapters.size > 3) {
            val oldestKeys = preloadedChapters.keys.take(preloadedChapters.size - 3)
            oldestKeys.forEach { preloadedChapters.remove(it) }
        }
        
        // Trigger preload of next chapter
        triggerPreloadNextChapter()
        
        // Load translation if available
        chapterId?.let { id ->
            loadTranslationForChapter(id)
            
            // Auto-translate next chapter if enabled
            if (autoTranslateNextChapter.value && !translationViewModel.translationState.hasTranslation) {
                scope.launch {
                    delay(500) // Small delay to let the chapter load first
                    translateCurrentChapter(forceRetranslate = false)
                }
            }
        }
        
        // Update reading time estimation for new chapter
        updateReadingTimeEstimation(0f)
        
        return stateChapter
    }

    private suspend fun getRemoteChapter(
            chapter: Chapter,
    ) {
        val catalog = catalog
        remoteUseCases.getRemoteReadingContent(
            chapter,
            catalog,
            onSuccess = { result ->
                state.stateChapter = result
                isLoading = false
                
                // Save the chapter content to database
                scope.launch {
                    insertUseCases.insertChapter(result)
                    
                    // Check chapter health after remote content is loaded
                    if (result.content.isNotEmpty()) {
                        checkChapterHealth(result, chapterHealthChecker, chapterHealthRepository)
                    }
                }
            },
            onError = { message ->
                isLoading = false // Set loading to false on error too
                if (message != null) {
                    showSnackBar(message)
                }
            }
        )
    }

    private fun subscribeChapters(bookId: Long) {
        getChapterJob?.cancel()
        getChapterJob = scope.launch {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId = bookId,
                sort = if (prefState.isAsc) "default" else "defaultDesc",
            )
                .collect {
                    stateChapters = it
                }
        }
    }

    var getContentJob: Job? = null
    var getChapterJob: Job? = null
    var preloadJob: Job? = null
    private var chapterNavigationJob: Job? = null
    
    // Preload state
    var isPreloading by mutableStateOf(false)
    // Navigation lock to prevent rapid chapter navigation race conditions
    var isNavigating by mutableStateOf(false)
        private set
    // Use ConcurrentHashMap for thread-safe access from multiple coroutines
    private val preloadedChapters = java.util.concurrent.ConcurrentHashMap<Long, Chapter>()

    fun nextChapter(): Chapter {
        val chapter =
            if (readingMode.value == ReadingMode.Continues) chapterShell.lastOrNull() else stateChapter
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
            return stateChapters.nextAfter(index)
                ?: throw IllegalAccessException("List doesn't contains ${chapter?.name}")
        }
        throw IllegalAccessException("List doesn't contains ${chapter?.name}")
    }

    fun prevChapter(): Chapter {
        val chapter =
            if (readingMode.value == ReadingMode.Continues) chapterShell.getOrNull(0) else stateChapter
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
            return stateChapters.prevBefore(index)
                ?: throw IllegalAccessException("List doesn't contains ${chapter?.name}")
        }
        throw IllegalAccessException("List doesn't contains ${chapter?.name}")
    }
    
    /**
     * Navigate to a chapter with race condition protection.
     * Cancels any pending navigation and prevents concurrent navigation.
     */
    fun navigateToChapter(
        chapterId: Long,
        next: Boolean = true,
        onComplete: () -> Unit = {}
    ) {
        // Cancel any pending navigation
        chapterNavigationJob?.cancel()
        
        chapterNavigationJob = scope.launch {
            try {
                isNavigating = true
                getLocalChapter(chapterId, next)
                onComplete()
            } finally {
                isNavigating = false
            }
        }
    }

    fun bookmarkChapter() {
        scope.launch(Dispatchers.IO) {
            bookMarkChapterUseCase.bookMarkChapter(stateChapter)?.let {
                stateChapter = it
            }
        }
    }

    suspend fun clearChapterShell(scrollState: ScrollState?, force: Boolean = false) {
        if (readingMode.value == ReadingMode.Continues || force) {
            scrollState?.scrollTo(0)
            chapterShell.clear()
        }
    }
    fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean?) {
        // Prevent rapid toggling with debounce
        if (isToggleInProgress) return
        isToggleInProgress = true
        
        // Set the reader mode state
        val newState = enable ?: !isReaderModeEnable
        isReaderModeEnable = newState
        
        // Make sure the bottom content is accessible when reader mode is off
        isMainBottomModeEnable = !isReaderModeEnable
        isSettingModeEnable = false
        
        // Release the toggle lock after a delay
        scope.launch {
            delay(500)
            isToggleInProgress = false
        }
    }
    
    // Removed: changeBackgroundColor, setReaderBackgroundColor, setReaderTextColor, saveTextAlignment
    // Now delegated to settingsViewModel (see delegation section above)
    
    // Removed: Large translate() method (~150 lines)
    // Now delegated to translationViewModel
    suspend fun translate() {
        stateChapter?.let { chapter ->
            translationViewModel.translateChapter(chapter, forceRetranslate = false)
        }
    }

    private fun triggerPreloadNextChapter() {
        if (!autoPreloadNextChapter.value) return
        preloadJob?.cancel()
        preloadJob = scope.launch {
            try {
                val nextChapter = kotlin.runCatching { nextChapter() }.getOrNull()
                if (nextChapter != null && !preloadedChapters.containsKey(nextChapter.id)) {
                    preloadChapter(nextChapter)
                }
            } catch (e: Exception) {
                // No next chapter to preload
            }
        }
    }
    
    private suspend fun preloadChapter(chapter: Chapter) {
        // Check if the chapter already has content in the database
        // (stateChapters uses lightweight mapper without content, so we need to check DB)
        val dbChapter = getChapterUseCase.findChapterById(chapter.id)
        val needsRemoteFetch = dbChapter == null || dbChapter.isEmpty()
        
        if (needsRemoteFetch && state.catalog != null) {
            isPreloading = true
            preloadChapterUseCase(chapter, state.catalog,
                onSuccess = { preloadedChapter ->
                    preloadedChapters[chapter.id] = preloadedChapter
                    scope.launch { insertUseCases.insertChapter(preloadedChapter) }
                    isPreloading = false
                },
                onError = { isPreloading = false }
            )
        } else if (dbChapter != null && !dbChapter.isEmpty()) {
            // Chapter already has content in DB, cache it for quick access
            preloadedChapters[chapter.id] = dbChapter
        }
    }
    
    fun preloadNextChapters(count: Int = 3) {
        scope.launch {
            try {
                val currentIndex = stateChapters.indexOfFirst { it.id == stateChapter?.id }
                if (currentIndex != -1) {
                    stateChapters.drop(currentIndex + 1).take(count).forEach { chapter ->
                        if (!preloadedChapters.containsKey(chapter.id)) {
                            preloadChapter(chapter)
                            delay(500)
                        }
                    }
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error("Error preloading multiple chapters: ${e.message}")
            }
        }
    }
    
    fun clearPreloadCache() {
        preloadedChapters.clear()
    }

    // ==================== Translation Methods ====================
    
    // Delegated to TranslationViewModel
    suspend fun loadTranslationForChapter(chapterId: Long) {
        translationViewModel.loadTranslationForChapter(chapterId)
    }
    
    fun getCurrentChapterContent(): List<ireader.core.source.model.Page> {
        return try {
            // Always prioritize original content if translation is not available or loading
            val originalContent = stateChapter?.content ?: emptyList()
            
            // Only show translated content if:
            // 1. User wants to see translated content
            // 2. Translation is available (hasTranslation is true)
            // 3. Translated content is not empty
            // 4. Not currently translating (to avoid showing partial results)
            val translationState = translationViewModel.translationState
            val shouldShowTranslation = showTranslatedContent.value &&
                translationState.hasTranslation &&
                translationState.translatedContent.isNotEmpty() &&
                !translationViewModel.isTranslating
            
            if (shouldShowTranslation) {
                translationState.translatedContent
            } else {
                originalContent
            }
        } catch (e: Exception) {
            stateChapter?.content ?: emptyList()
        }
    }
    
    fun retranslateWithGlossary() = translateCurrentChapter(forceRetranslate = true)
    fun isApiKeyRequired(): Boolean = translationEnginesManager.get().requiresApiKey
    fun isApiKeySet(): Boolean {
        val engine = translationEnginesManager.get()
        if (!engine.requiresApiKey) return true
        return when (engine.id) {
            2L -> openAIApiKey.value.isNotBlank()
            3L -> deepSeekApiKey.value.isNotBlank()
            else -> true
        }
    }
    
    /**
     * Get the name of the current translation engine
     */
    fun getCurrentEngineName(): String {
        return translationEnginesManager.get().engineName
    }
    
    /**
     * Test the translation API connection
     */
    fun testTranslationConnection(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                // Test with a simple phrase
                translationEnginesManager.translateWithContext(
                    texts = listOf("Hello"),
                    source = "en",
                    target = translatorTargetLanguage.value,
                    onProgress = { },
                    onSuccess = { translatedTexts ->
                        if (translatedTexts.isNotEmpty()) {
                            onSuccess("Connection successful! Translation engine is working correctly.")
                        } else {
                            onError("Connection test failed: Empty response")
                        }
                    },
                    onError = { error ->
                        onError("Connection test failed: ${error.toString()}")
                    }
                )
            } catch (e: Exception) {
                onError("Connection test failed: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    // Removed: translateCurrentChapter (~50 lines)
    // Now delegated to translationViewModel
    fun translateCurrentChapter(forceRetranslate: Boolean = false) {
        val chapter = stateChapter ?: return
        scope.launch {
            translationViewModel.translateChapter(chapter, forceRetranslate)
        }
    }
    
    // ==================== Glossary Methods (Delegated) ====================
    // Removed ~100 lines of glossary methods - now delegated to translationViewModel
    
    fun loadGlossary() {
        val bookId = book?.id ?: return
        scope.launch { translationViewModel.loadGlossary(bookId) }
    }
    
    fun addGlossaryEntry(sourceTerm: String, targetTerm: String, termType: ireader.domain.models.entities.GlossaryTermType, notes: String?) {
        val bookId = book?.id ?: return
        scope.launch { translationViewModel.addGlossaryEntry(bookId, sourceTerm, targetTerm, termType, notes) }
    }
    
    fun updateGlossaryEntry(entry: ireader.domain.models.entities.Glossary) {
        scope.launch { translationViewModel.updateGlossaryEntry(entry) }
    }
    
    fun deleteGlossaryEntry(id: Long) {
        val bookId = book?.id ?: return
        scope.launch {
            val entry = translationViewModel.glossaryEntries.find { it.id == id }
            if (entry != null) {
                translationViewModel.deleteGlossaryEntry(entry)
            }
        }
    }
    
    fun exportGlossary(onSuccess: (String) -> Unit) {
        val bookId = book?.id ?: return
        val bookTitle = book?.title ?: ""
        scope.launch { 
            val json = translationViewModel.exportGlossary(bookId, bookTitle)
            onSuccess(json)
        }
    }
    
    fun importGlossary(jsonString: String) {
        val bookId = book?.id ?: return
        scope.launch { translationViewModel.importGlossary(bookId, jsonString) }
    }

    // ==================== Font Management Methods ====================
    // TODO: Move these to ReaderSettingsViewModel
    // Keeping minimal implementations for now
    
    fun loadFonts() {
        scope.launch {
            try {
                systemFonts = fontManagementUseCase.getSystemFonts()
                customFonts = fontManagementUseCase.getCustomFonts()
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load fonts", e)
            }
        }
    }
    
    fun selectFont(fontId: String) {
        readerPreferences.selectedFontId().set(fontId)
        scope.launch {
            try {
                customFont = fontManagementUseCase.getFontById(fontId)
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load font", e)
            }
        }
    }
    
    fun selectGoogleFont(fontName: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                this@ReaderScreenViewModel.downloadGoogleFontIfNeeded(fontName)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    readerPreferences.selectedFontId().set("")
                    platformUiPreferences.font().set(
                        ireader.domain.preferences.models.FontType(
                            name = fontName,
                            fontFamily = ireader.domain.models.common.FontFamilyModel.Custom(fontName)
                        )
                    )
                    fontVersion++
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to select Google Font: $fontName", e)
                showSnackBar(UiText.DynamicString("Failed to load font: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    fun importFont(filePath: String, fontName: String) {
        scope.launch {
            fontManagementUseCase.importFont(filePath, fontName).onSuccess {
                loadFonts()
                showSnackBar(UiText.DynamicString("Font imported successfully"))
            }.onFailure { showSnackBar(UiText.ExceptionString(it as Exception)) }
        }
    }
    
    fun deleteFont(fontId: String) {
        scope.launch {
            fontManagementUseCase.deleteFont(fontId).onSuccess {
                loadFonts()
                if (selectedFontId.value == fontId) {
                    readerPreferences.selectedFontId().set("")
                    customFont = null
                }
                showSnackBar(UiText.DynamicString("Font deleted successfully"))
            }.onFailure { showSnackBar(UiText.ExceptionString(it as Exception)) }
        }
    }
    
    // ==================== Find in Chapter Methods ====================
    
    /**
     * Toggle find in chapter bar visibility
     */
    fun toggleFindInChapter() {
        showFindInChapter = !showFindInChapter
        if (!showFindInChapter) {
            // Clear search when closing
            findQuery = ""
            findMatches = emptyList()
            currentFindMatchIndex = 0
        }
    }
    
    /**
     * Update find query and search for matches
     */
    fun updateFindQuery(query: String) {
        findQuery = query
        if (query.isEmpty()) {
            findMatches = emptyList()
            currentFindMatchIndex = 0
            return
        }
        
        // Get current chapter content as text
        val content = getCurrentChapterContent()
        val fullText = content.joinToString("\n") { page ->
            when (page) {
                is ireader.core.source.model.Text -> page.text
                else -> ""
            }
        }
        
        // Find all matches using case-insensitive regex
        val matches = mutableListOf<IntRange>()
        val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
        var matchResult = regex.find(fullText)
        
        while (matchResult != null) {
            matches.add(matchResult.range)
            matchResult = matchResult.next()
        }
        
        findMatches = matches
        currentFindMatchIndex = if (matches.isNotEmpty()) 0 else 0
    }
    
    /**
     * Navigate to next match (with wrap-around)
     */
    fun findNext() {
        if (findMatches.isEmpty()) return
        currentFindMatchIndex = (currentFindMatchIndex + 1) % findMatches.size
        scrollToCurrentMatch()
    }
    
    /**
     * Navigate to previous match (with wrap-around)
     */
    fun findPrevious() {
        if (findMatches.isEmpty()) return
        currentFindMatchIndex = if (currentFindMatchIndex == 0) {
            findMatches.size - 1
        } else {
            currentFindMatchIndex - 1
        }
        scrollToCurrentMatch()
    }
    
    /**
     * Scroll to the current match position
     */
    private fun scrollToCurrentMatch() {
        // This will be handled by the UI layer
        // The UI will observe currentFindMatchIndex and scroll accordingly
    }
    
    // ==================== Report Broken Chapter Methods ====================
    
    /**
     * Toggle report dialog visibility
     */
    fun toggleReportDialog() {
        showReportDialog = !showReportDialog
    }
    
    /**
     * Report a broken chapter
     * Note: This is a placeholder implementation. In a real app, you would:
     * 1. Add ReportBrokenChapterUseCase to the ViewModel constructor
     * 2. Call the use case to save the report to the database
     * 3. Optionally send the report to a remote server
     */
    /**
     * Report a broken chapter with reason as String
     * @param reason The reason for the report (e.g., "Missing Content", "Incorrect Order")
     * @param description Additional details about the issue
     */
    fun reportBrokenChapter(
        reason: String,
        description: String
    ) {
        val chapter = stateChapter ?: return
        val bookId = book?.id ?: return
        val currentBook = book ?: return
        
        scope.launch {
            try {
                val result = reportBrokenChapterUseCase.invoke(
                    chapterId = chapter.id,
                    bookId = bookId,
                    sourceId = currentBook.sourceId,
                    reason = reason,
                    description = description
                )
                if (result.isSuccess) {
                    showSnackBar(UiText.DynamicString("Chapter reported successfully"))
                } else {
                    showSnackBar(UiText.DynamicString("Failed to report chapter: ${result.exceptionOrNull()?.message ?: "Unknown error"}"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
    
    // ==================== Bilingual Mode Methods (Duplicates Removed) ====================
    
    /**
     * Get translation for a specific paragraph index
     * Returns null if no translation is available for that paragraph
     */
    fun getTranslationForParagraph(index: Int): String? {
        val translationState = translationViewModel.translationState
        
        // Don't return translation if:
        // 1. No translation available
        // 2. Currently translating (to avoid showing partial results)
        // 3. Translation content is empty
        if (!translationState.hasTranslation || 
            translationViewModel.isTranslating ||
            translationState.translatedContent.isEmpty()) {
            return null
        }
        
        val translatedContent = translationState.translatedContent
        
        // Make sure the index is within bounds
        if (index < 0 || index >= translatedContent.size) {
            return null
        }
        
        // Get the translated page at this index
        val translatedPage = translatedContent.getOrNull(index)
        return when (translatedPage) {
            is ireader.core.source.model.Text -> translatedPage.text
            else -> null
        }
    }
    
    /**
     * Report a broken chapter with IssueCategory enum
     * @param issueCategory The category of the issue
     * @param description Additional details about the issue
     */
    fun reportBrokenChapter(
        issueCategory: ireader.domain.models.entities.IssueCategory,
        description: String
    ) {
        val chapter = stateChapter ?: return
        val bookId = book?.id ?: return
        
        scope.launch {
            try {
                val result = reportBrokenChapterUseCase.execute(
                    chapterId = chapter.id,
                    bookId = bookId,
                    issueCategory = issueCategory,
                    description = description
                )
                if (result.isSuccess) {
                    showSnackBar(UiText.DynamicString("Chapter reported successfully"))
                } else {
                    showSnackBar(UiText.DynamicString("Failed to report chapter: ${result.exceptionOrNull()?.message ?: "Unknown error"}"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
    
    // ==================== Reading Time Estimation Methods ====================
    
    /**
     * Calculate and update reading time estimation for current chapter
     * @param scrollProgress Progress through the chapter (0.0 to 1.0)
     */
    fun updateReadingTimeEstimation(scrollProgress: Float = 0f) {
        val content = getCurrentChapterContent()
        
        // Extract text from content
        val fullText = content.joinToString("\n") { page ->
            when (page) {
                is ireader.core.source.model.Text -> page.text
                else -> ""
            }
        }
        
        // Count total words
        totalWords = countWords(fullText)
        
        // Calculate words remaining based on scroll progress
        val progressClamped = scrollProgress.coerceIn(0f, 1f)
        wordsRemaining = (totalWords * (1f - progressClamped)).toInt().coerceAtLeast(0)
        
        // Get user's reading speed from preferences (default: 225 WPM for average readers)
        val wordsPerMinute = readerPreferences.readingSpeedWPM().get()
        
        // Calculate estimated time
        estimatedReadingMinutes = calculateReadingTime(wordsRemaining, wordsPerMinute)
    }
    
    /**
     * Toggle reading time display
     */
    fun toggleReadingTimeDisplay() {
        showReadingTime = !showReadingTime
    }
    
    /**
     * Count words in text
     */
    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).size
    }
    
    /**
     * Calculate reading time based on word count and reading speed
     */
    private fun calculateReadingTime(wordCount: Int, wordsPerMinute: Int): Int {
        if (wordCount <= 0) return 0
        return (wordCount.toFloat() / wordsPerMinute).toInt().coerceAtLeast(1)
    }

    // ==================== Paragraph Translation Methods ====================
    
    /**
     * Show paragraph translation dialog with the selected text
     */
    fun showParagraphTranslation(text: String) {
        paragraphToTranslate = text
        translatedParagraph = null
        isParagraphTranslating = false
        paragraphTranslationError = null
        showParagraphTranslationDialog = true
        
        // Automatically start translation
        translateParagraphInternal()
    }
    
    /**
     * Hide paragraph translation dialog
     */
    fun hideParagraphTranslation() {
        showParagraphTranslationDialog = false
        paragraphToTranslate = ""
        translatedParagraph = null
        isParagraphTranslating = false
        paragraphTranslationError = null
    }
    
    /**
     * Retry paragraph translation
     */
    fun retryParagraphTranslation() {
        translateParagraphInternal()
    }
    
    /**
     * Internal method to translate a paragraph
     */
    private fun translateParagraphInternal() {
        if (paragraphToTranslate.isBlank()) return
        
        // Check if API key is required and set
        if (isApiKeyRequired() && !isApiKeySet()) {
            showTranslationApiKeyPrompt = true
            isParagraphTranslating = false
            return
        }
        
        isParagraphTranslating = true
        paragraphTranslationError = null
        translatedParagraph = null
        
        scope.launch {
            try {
                translateParagraphUseCase.execute(
                    text = paragraphToTranslate,
                    sourceLanguage = translatorOriginLanguage.value,
                    targetLanguage = translatorTargetLanguage.value,
                    onSuccess = { translatedText ->
                        translatedParagraph = translatedText
                        isParagraphTranslating = false
                    },
                    onError = { error ->
                        paragraphTranslationError = error.toString()
                        isParagraphTranslating = false
                    }
                )
            } catch (e: Exception) {
                paragraphTranslationError = e.message ?: "Unknown error occurred"
                isParagraphTranslating = false
            }
        }
    }
    
    /**
     * Dismiss the translation API key prompt
     */
    fun dismissTranslationApiKeyPrompt() {
        showTranslationApiKeyPrompt = false
    }
    
    /**
     * Navigate to settings from the API key prompt
     * This will be called when user taps "Go to Settings" button
     */
    fun navigateToTranslationSettings() {
        showTranslationApiKeyPrompt = false
        // The navigation will be handled by the UI layer
    }

    // ==================== Reading Statistics Tracking Methods ====================
    
    /**
     * Track when a chapter is opened
     */
    private fun onChapterOpened() {
        chapterOpenTimestamp = System.currentTimeMillis()
        
        // Delegate to statistics view model
        stateChapter?.let { chapter ->
            onChapterOpenedStats(chapter)
        }
        
        // Start reading break timer if enabled
        startReadingBreakTimer()
    }
    
    /**
     * Track when a chapter is closed
     * Calculates reading duration and updates statistics if duration exceeds threshold
     */
    private fun onChapterClosed() {
        val openTimestamp = chapterOpenTimestamp ?: return
        val closeTimestamp = System.currentTimeMillis()
        val durationMillis = closeTimestamp - openTimestamp
        val currentChapter = stateChapter
        
        // Delegate to statistics view model
        onChapterClosedStats()
        
        // Only track if reading duration exceeds 10 seconds
        if (durationMillis > 10_000) {
            scope.launch(Dispatchers.IO) {
                try {
                    // Mark chapter as read if not already marked
                    currentChapter?.let { chapter ->
                        if (!chapter.read) {
                            val updatedChapter = chapter.copy(read = true)
                            insertUseCases.insertChapter(updatedChapter)
                        }
                        
                        // Check if all chapters in the book are now read
                        checkAndTrackBookCompletion(chapter.bookId)
                    }
                } catch (e: Exception) {
                    // Failed to track reading progress
                }
            }
        }
        
        // Reset timestamp
        chapterOpenTimestamp = null
        
        // Pause reading break timer
        pauseReadingBreakTimer()
    }
    
    /**
     * Check if all chapters of a book are read and track book completion
     */
    private suspend fun checkAndTrackBookCompletion(bookId: Long) {
        try {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId)
            if (chapters.isNotEmpty()) {
                chapters.all { it.read }
            }
        } catch (e: Exception) {
            // Failed to check book completion
        }
    }

    // ==================== Reading Break Reminder Methods ====================
    
    /**
     * Start the reading break timer if enabled
     */
    private fun startReadingBreakTimer() {
        if (!readingBreakReminderEnabled.value) return
        
        val intervalMinutes = readingBreakInterval.value
        if (intervalMinutes > 0) {
            readingTimerManager.startTimer(intervalMinutes)
        }
    }
    
    /**
     * Pause the reading break timer
     */
    private fun pauseReadingBreakTimer() {
        readingTimerManager.stopTimer()
    }
    
    /**
     * Resume the reading break timer
     */
    fun resumeReadingBreakTimer() {
        if (readingBreakReminderEnabled.value) {
            val intervalMinutes = readingBreakInterval.value
            if (intervalMinutes > 0) {
                readingTimerManager.startTimer(intervalMinutes)
            }
        }
    }
    
    /**
     * Reset the reading break timer
     */
    fun resetReadingBreakTimer() {
        readingTimerManager.stopTimer()
    }
    
    /**
     * Called when the reading break interval is reached
     */
    private fun onReadingBreakIntervalReached() {
        showReadingBreakDialog = true
    }
    
    /**
     * Handle "Take a Break" action from the reminder dialog
     */
    fun onTakeBreak() {
        showReadingBreakDialog = false
        pauseReadingBreakTimer()
    }
    
    /**
     * Handle "Continue Reading" action from the reminder dialog
     */
    fun onContinueReading() {
        showReadingBreakDialog = false
        resetReadingBreakTimer()
        startReadingBreakTimer()
    }
    
    /**
     * Handle "Snooze" action from the reminder dialog
     */
    fun onSnoozeReadingBreak(minutes: Int) {
        showReadingBreakDialog = false
    }
    
    /**
     * Dismiss the reading break dialog
     */
    fun dismissReadingBreakDialog() {
        showReadingBreakDialog = false
        onContinueReading()
    }
    
    /**
     * Get the remaining time until next break (for UI display)
     */
    fun getRemainingTimeUntilBreak(): Long {
        return 0L // Placeholder - implement if needed
    }
    
    /**
     * Check if the reading timer is currently running
     */
    fun isReadingTimerRunning(): Boolean {
        return false // Placeholder - implement if needed
    }

    override fun onDestroy() {
        // Track chapter close on destroy
        onChapterClosed()
        
        // Stop reading break timer
        readingTimerManager.stopTimer()
        
        getChapterJob?.cancel()
        getContentJob?.cancel()
        preloadJob?.cancel()
        clearPreloadCache()
        webViewManger.destroy()
        super.onDestroy()
    }
}
