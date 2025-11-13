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
import ireader.i18n.resources.MR
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
        // Chapter health and repair
        val chapterHealthChecker: ireader.domain.services.ChapterHealthChecker,
        val chapterHealthRepository: ireader.domain.data.repository.ChapterHealthRepository,
        val autoRepairChapterUseCase: ireader.domain.usecases.chapter.AutoRepairChapterUseCase,
        val params: Param,
        val globalScope: CoroutineScope
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
    
    // Autoscroll speed control
    fun increaseAutoScrollSpeed() {
        val currentSpeed = autoScrollOffset.value
        if (currentSpeed < 20) {
            val newSpeed = (currentSpeed + 1).coerceAtMost(100)
            autoScrollOffset.value = newSpeed
            readerPreferences.autoScrollOffset().set(newSpeed)
        }
    }
    
    fun decreaseAutoScrollSpeed() {
        val currentSpeed = autoScrollOffset.value
        if (currentSpeed > 1) {
            val newSpeed = (currentSpeed - 1).coerceAtLeast(1)
            autoScrollOffset.value = newSpeed
            readerPreferences.autoScrollOffset().set(newSpeed)
        }
    }
    
    fun toggleAutoScroll() {
        autoScrollMode = !autoScrollMode
    }
    
    /**
     * Update brightness value and save to preferences
     */
    fun updateBrightness( newBrightness: Float) {
        readerUseCases.brightnessStateUseCase.saveBrightness(newBrightness)
    }
    
    fun makeSettingTransparent() {
        isSettingChangingJob?.cancel()
        isSettingChangingJob = scope.launch {
            isSettingChanging = true
            delay(500)
            isSettingChanging = false
        }
    }
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
    val topContentPadding = readerPreferences.topContentPadding().asState()
    val screenAlwaysOn = readerPreferences.screenAlwaysOn().asState()
    val autoPreloadNextChapter = readerPreferences.autoPreloadNextChapter().asState()
    val preloadOnlyOnWifi = readerPreferences.preloadOnlyOnWifi().asState()
    val bottomContentPadding = readerPreferences.bottomContentPadding().asState()
    val topMargin = readerPreferences.topMargin().asState()
    val leftMargin = readerPreferences.leftMargin().asState()
    val rightMargin = readerPreferences.rightMargin().asState()
    val bottomMargin = readerPreferences.bottomMargin().asState()
    val textColor = androidUiPreferences.textColorReader().asState()
    var readerThemeSavable by mutableStateOf(false)
    val selectedScrollBarColor = androidUiPreferences.selectedScrollBarColor().asState()
    val unselectedScrollBarColor = androidUiPreferences.unselectedScrollBarColor().asState()
    val lineHeight = readerPreferences.lineHeight().asState()
    val betweenLetterSpaces = readerPreferences.betweenLetterSpaces().asState()
    val textWeight = readerPreferences.textWeight().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asState()
    val showScrollIndicator = readerPreferences.showScrollIndicator().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val orientation = androidUiPreferences.orientation().asState()
    @OptIn(ExperimentalTime::class)
    var lastOrientationChangedTime =
        mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds())
    val scrollIndicatorWith = readerPreferences.scrollIndicatorWith().asState()
    val scrollIndicatorPadding = readerPreferences.scrollIndicatorPadding().asState()
    val scrollIndicatorAlignment = readerPreferences.scrollBarAlignment().asState()
    val autoScrollOffset = readerPreferences.autoScrollOffset().asState()
    var autoScrollInterval = readerPreferences.autoScrollInterval().asState()
    val autoBrightnessMode = readerPreferences.autoBrightness().asState()
    val immersiveMode = readerPreferences.immersiveMode().asState()
    val brightness = readerPreferences.brightness().asState()
    var chapterNumberMode by readerPreferences.showChapterNumberPreferences().asState()
    val isScrollIndicatorDraggable = readerPreferences.scrollbarMode().asState()
    val font = platformUiPreferences.font()?.asState()
    var customFont by mutableStateOf<ireader.domain.models.fonts.CustomFont?>(null)
    val webViewIntegration = readerPreferences.webViewIntegration().asState()
    val selectableMode = readerPreferences.selectableText().asState()
    val fontSize = readerPreferences.fontSize().asState()
    val distanceBetweenParagraphs = readerPreferences.paragraphDistance().asState()
    val bionicReadingMode = readerPreferences.bionicReading().asState()
    val verticalScrolling = readerPreferences.scrollMode().asState()
    val readingMode = readerPreferences.readingMode().asState()
    val fonts = listOf<String>(
        "Poppins",
        "PT Serif",
        "Noto",
        "Open Sans",
        "Roboto Serif",
        "Cooper Arabic",
        "Lora"
    )

    // Translation state and preferences
    val translationState = TranslationStateImpl()
    val showTranslatedContent = readerPreferences.showTranslatedContent().asState()
    val autoSaveTranslations = readerPreferences.autoSaveTranslations().asState()
    val applyGlossaryToTranslations = readerPreferences.applyGlossaryToTranslations().asState()
    val bilingualModeEnabled = readerPreferences.bilingualModeEnabled().asState()
    val bilingualModeLayout = readerPreferences.bilingualModeLayout().asState()
    val volumeKeyNavigation = readerPreferences.volumeKeyNavigation().asState()
    
    // Override to provide translation-aware content
    override fun getCurrentContent(): List<ireader.core.source.model.Page> {
        return getCurrentChapterContent()
    }

    var isToggleInProgress by mutableStateOf(false)

    // Translation progress states
    var isTranslating by mutableStateOf(false)
    var translationProgress by mutableStateOf(0f)
    var translationTotal by mutableStateOf(0)
    var translationCompleted by mutableStateOf(0)

    init {
        val chapterId = globalChapterId.value
        val bookId = globalBookId.value


        if (bookId != null && chapterId != null) {
            val source = runBlocking {
                getBookUseCases.findBookById(bookId)?.let {
                     getLocalCatalog.get(it.sourceId)
                }

            }
                state.catalog = source
                subscribeReaderThemes()
                subscribeChapters(bookId)
                scope.launch {
                    state.book = getBookUseCases.findBookById(bookId)
                    setupChapters(bookId, chapterId)
                    loadGlossary()
                    loadFonts()
                }
        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(MR.strings.something_is_wrong_with_this_book))
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
        
        // Check if chapter is already preloaded
        val preloadedChapter = preloadedChapters[chapterId]
        val chapter = if (preloadedChapter != null && !preloadedChapter.isEmpty()) {
            ireader.core.log.Log.debug("Using preloaded chapter: ${preloadedChapter.name}")
            preloadedChapters.remove(chapterId) // Remove from cache after use
            preloadedChapter
        } else {
            getChapterUseCase.findChapterById(chapterId)
        }
        
        chapter.let {
            stateChapter = it
        }
        if (chapter != null && (chapter.isEmpty() || force)) {
            state.source?.let { source -> getRemoteChapter(chapter) }
        }
        stateChapter?.let { ch -> getChapterUseCase.updateLastReadTime(ch) }
        val index = stateChapters.indexOfFirst { it.id == chapter?.id }
        if (index != -1) {
            currentChapterIndex = index
        }

        isLoading = false
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
        
        // Check chapter health after loading
        stateChapter?.let { ch ->
            checkChapterHealth(ch, chapterHealthChecker, chapterHealthRepository)
        }
        
        // Trigger preload of next chapter
        triggerPreloadNextChapter()
        
        // Load translation if available
        chapterId?.let { id ->
            loadTranslationForChapter(id)
        }
        
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
            },
            onError = { message ->
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
    
    // Preload state
    var isPreloading by mutableStateOf(false)
    private val preloadedChapters = mutableMapOf<Long, Chapter>()

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
        
        // Set the reader mode state - this will trigger the LaunchedEffect in ReaderScreen
        isReaderModeEnable = enable ?: !isReaderModeEnable
        
        // Make sure the bottom content is accessible when reader mode is off
        isMainBottomModeEnable = !isReaderModeEnable
        isSettingModeEnable = false
        
        // Release the toggle lock after a delay
        scope.launch {
            delay(500)
            isToggleInProgress = false
        }
    }
    fun changeBackgroundColor(themeId:Long) {
        readerColors.firstOrNull { it.id == themeId }?.let { theme ->
            readerTheme.value = theme
            val bgColor = theme.backgroundColor
            val textColor = theme.onTextColor
            backgroundColor.value = bgColor.toComposeColor()
            this.textColor.value = textColor.toComposeColor()
            setReaderBackgroundColor(bgColor.toComposeColor())
            setReaderTextColor(textColor.toComposeColor())
        }

    }
     fun setReaderBackgroundColor(color: Color) {
        readerUseCases.backgroundColorUseCase.save(color)
    }

     fun setReaderTextColor(color: Color) {
        readerUseCases.textColorUseCase.save(color)
    }
    fun saveTextAlignment(textAlign: PreferenceValues.PreferenceTextAlignment) {
        readerUseCases.textAlignmentUseCase.save(textAlign)
    }
    suspend fun translate() {
        stateChapter?.let { chapter ->
            // Reset translation progress
            isTranslating = true
            translationProgress = 0f
            translationCompleted = 0
            
            try {
                showSnackBar(UiText.MStringResource(MR.strings.translating))
                
                val contentType = ireader.domain.data.engines.ContentType.values().getOrElse(translatorContentType.value) { 
                    ireader.domain.data.engines.ContentType.GENERAL 
                }
                
                val toneType = ireader.domain.data.engines.ToneType.values().getOrElse(translatorToneType.value) { 
                    ireader.domain.data.engines.ToneType.NEUTRAL 
                }
                
                val preserveStyle = translatorPreserveStyle.value
                
                // Get the active translation engine
                val engine = translationEnginesManager.get()
                
                // Extract text content with null safety
                val content = chapter.content ?: emptyList()
                val texts = content.filterIsInstance<ireader.core.source.model.Text>().map { it.text }
                
                if (texts.isEmpty()) {
                    showSnackBar(UiText.MStringResource(MR.strings.no_text_to_translate))
                    isTranslating = false
                    return
                }
                
                translationTotal = texts.size
                
                // Log which engine is being used
                println("Using translation engine: ${engine.engineName} (ID: ${engine.id})")
                
                if (engine.supportsContextAwareTranslation) {
                    // Use advanced translation with context for AI-powered engines
                    translationEnginesManager.translateWithContext(
                        texts = texts,
                        source = translatorOriginLanguage.value,
                        target = translatorTargetLanguage.value,
                        contentType = contentType,
                        toneType = toneType,
                        preserveStyle = preserveStyle,
                        onProgress = { completed ->
                            translationCompleted = completed
                            translationProgress = completed.toFloat() / translationTotal.toFloat()
                            // Ensure isTranslating stays true during the process
                            if (completed < 100) isTranslating = true
                        },
                        onSuccess = { result ->
                            stateChapter = stateChapter!!.copy(content = result.map { ireader.core.source.model.Text(it) })
                            showSnackBar(UiText.MStringResource(MR.strings.translation_complete))
                            translationProgress = 1f
                            isTranslating = false
                        },
                        onError = { errorMessage ->
                            // Show error message and log detailed info
                            showSnackBar(errorMessage)
                            
                            // Log the error in a readable format - convert MR strings if needed
                            if (errorMessage is UiText.MStringResource) {
                                val stringRes = when (errorMessage.res) {
                                    MR.strings.no_text_to_translate -> "No text to translate"
                                    MR.strings.empty_response -> "Empty response from translation service"
                                    MR.strings.api_response_error -> "Error processing API response"
                                    MR.strings.deepseek_api_key_not_set -> "DeepSeek API key not set"
                                    MR.strings.deepseek_api_key_invalid -> "DeepSeek API key invalid or quota exceeded"
                                    MR.strings.openai_api_key_not_set -> "OpenAI API key not set"
                                    MR.strings.openai_api_key_invalid -> "OpenAI API key invalid"
                                    MR.strings.api_rate_limit_exceeded -> "API rate limit exceeded"
                                    MR.strings.openai_quota_exceeded -> "OpenAI quota exceeded"
                                    else -> "Translation error: ${errorMessage.res}"
                                }
                                println("Translation error: $stringRes")
                            } else {
                                println("Translation error: $errorMessage")
                            }
                            
                            // Reset progress on error
                            translationProgress = 0f
                            translationCompleted = 0
                            isTranslating = false
                        }
                    )
                } else {
                    // Fall back to standard translation for basic engines
                    engine.translate(
                        texts = texts,
                        source = translatorOriginLanguage.value,
                        target = translatorTargetLanguage.value,
                        onProgress = { completed ->
                            translationCompleted = completed
                            translationProgress = completed.toFloat() / translationTotal.toFloat()
                            // Ensure isTranslating stays true during the process
                            if (completed < 100) isTranslating = true
                        },
                        onSuccess = { result ->
                            stateChapter = stateChapter!!.copy(content = result.map { ireader.core.source.model.Text(it) })
                            showSnackBar(UiText.MStringResource(MR.strings.translation_complete))
                            translationProgress = 1f
                            isTranslating = false
                        },
                        onError = { errorMessage ->
                            // Show error message and log detailed info
                            showSnackBar(errorMessage)
                            
                            // Log the error in a readable format - convert MR strings if needed
                            if (errorMessage is UiText.MStringResource) {
                                val stringRes = when (errorMessage.res) {
                                    MR.strings.no_text_to_translate -> "No text to translate"
                                    MR.strings.empty_response -> "Empty response from translation service"
                                    MR.strings.api_response_error -> "Error processing API response"
                                    MR.strings.deepseek_api_key_not_set -> "DeepSeek API key not set"
                                    MR.strings.deepseek_api_key_invalid -> "DeepSeek API key invalid"
                                    MR.strings.openai_api_key_not_set -> "OpenAI API key not set"
                                    MR.strings.openai_api_key_invalid -> "OpenAI API key invalid"
                                    MR.strings.api_rate_limit_exceeded -> "API rate limit exceeded"
                                    MR.strings.openai_quota_exceeded -> "OpenAI quota exceeded"
                                    else -> "Translation error: ${errorMessage.res}"
                                }
                                println("Translation error: $stringRes")
                            } else {
                                println("Translation error: $errorMessage")
                            }
                            
                            // Reset progress on error
                            translationProgress = 0f
                            translationCompleted = 0
                            isTranslating = false
                        }
                    )
                }
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
                // Also reset translation state if an error occurs
                translationProgress = 0f
                translationCompleted = 0
                isTranslating = false
                println("Translation error: $e")
                e.printStackTrace()
            }
        }
    }

    /**
     * Trigger preloading of the next chapter in background
     */
    private fun triggerPreloadNextChapter() {
        // Check if auto-preload is enabled
        if (!autoPreloadNextChapter.value) {
            ireader.core.log.Log.debug("Auto-preload is disabled")
            return
        }
        
        preloadJob?.cancel()
        preloadJob = scope.launch {
            try {
                val nextChapter = kotlin.runCatching { nextChapter() }.getOrNull()
                if (nextChapter != null && !preloadedChapters.containsKey(nextChapter.id)) {
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
    private suspend fun preloadChapter(chapter: Chapter) {
        if (chapter.isEmpty() && state.catalog != null) {
            isPreloading = true
            ireader.core.log.Log.debug("Preloading chapter: ${chapter.name}")
            
            preloadChapterUseCase(
                chapter = chapter,
                catalog = state.catalog,
                onSuccess = { preloadedChapter ->
                    preloadedChapters[chapter.id] = preloadedChapter
                    // Save to database for offline access
                    scope.launch {
                        insertUseCases.insertChapter(preloadedChapter)
                    }
                    ireader.core.log.Log.debug("Successfully preloaded and cached chapter: ${chapter.name}")
                    isPreloading = false
                },
                onError = { error ->
                    ireader.core.log.Log.error("Failed to preload chapter: ${chapter.name} - $error")
                    isPreloading = false
                }
            )
        } else if (!chapter.isEmpty()) {
            // Chapter already has content, just cache it
            preloadedChapters[chapter.id] = chapter
            ireader.core.log.Log.debug("Chapter already loaded, added to cache: ${chapter.name}")
        }
    }
    
    /**
     * Manually preload next N chapters (for user-triggered preload)
     */
    fun preloadNextChapters(count: Int = 3) {
        scope.launch {
            try {
                val currentIndex = stateChapters.indexOfFirst { it.id == stateChapter?.id }
                if (currentIndex != -1) {
                    val chaptersToPreload = stateChapters.drop(currentIndex + 1).take(count)
                    chaptersToPreload.forEach { chapter ->
                        if (!preloadedChapters.containsKey(chapter.id)) {
                            preloadChapter(chapter)
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
     * Clear preloaded chapters cache
     */
    fun clearPreloadCache() {
        preloadedChapters.clear()
        ireader.core.log.Log.debug("Preload cache cleared")
    }

    // ==================== Translation Methods ====================
    
    /**
     * Load translation for the current chapter if available
     */
    suspend fun loadTranslationForChapter(chapterId: Long) {
        translationState.reset()
        
        try {
            val translation = getTranslatedChapterUseCase.execute(
                chapterId = chapterId,
                targetLanguage = translatorTargetLanguage.value,
                engineId = translatorEngine.value
            )
            
            // Note: Glossary is applied during translation, not when loading saved translations
            // If you want to see glossary changes, you need to re-translate the chapter
            translationState.translatedChapter = translation
            translationState.hasTranslation = translation != null
            translationState.isShowingTranslation = 
                showTranslatedContent.value && translation != null
        } catch (e: Exception) {
            // Silently fail if table doesn't exist yet (migration not run)
            if (e.message?.contains("no such table") == true) {
                ireader.core.log.Log.debug("Translation table not yet created, skipping translation load")
            } else {
                ireader.core.log.Log.error("Error loading translation: ${e.message}")
            }
            translationState.hasTranslation = false
        }
    }
    
    /**
     * Toggle between original and translated content
     */
    fun toggleTranslation() {
        if (translationState.hasTranslation) {
            translationState.isShowingTranslation = !translationState.isShowingTranslation
            readerPreferences.showTranslatedContent().set(translationState.isShowingTranslation)
        }
    }
    
    /**
     * Toggle bilingual mode
     */
    fun toggleBilingualMode() {
        val currentValue = bilingualModeEnabled.value
        readerPreferences.bilingualModeEnabled().set(!currentValue)
    }
    
    /**
     * Switch bilingual mode layout
     */
    fun switchBilingualLayout() {
        val currentLayout = bilingualModeLayout.value
        // Toggle between 0 (SIDE_BY_SIDE) and 1 (PARAGRAPH_BY_PARAGRAPH)
        readerPreferences.bilingualModeLayout().set(if (currentLayout == 0) 1 else 0)
    }
    
    /**
     * Get current chapter content (original or translated)
     */
    fun getCurrentChapterContent(): List<ireader.core.source.model.Page> {
        return if (translationState.isShowingTranslation && 
                   translationState.translatedChapter != null) {
            translationState.translatedChapter!!.translatedContent
        } else {
            stateChapter?.content ?: emptyList()
        }
    }
    
    /**
     * Re-translate the current chapter with current glossary
     * This will force a new translation with the latest glossary entries
     */
    fun retranslateWithGlossary() {
        translateCurrentChapter(forceRetranslate = true)
    }
    
    /**
     * Check if API key is required and set for the current translation engine
     */
    fun isApiKeyRequired(): Boolean {
        val engine = translationEnginesManager.get()
        return engine.requiresApiKey
    }
    
    /**
     * Check if API key is set for the current translation engine
     */
    fun isApiKeySet(): Boolean {
        val engine = translationEnginesManager.get()
        if (!engine.requiresApiKey) return true
        
        return when (engine.id) {
            2L -> openAIApiKey.value.isNotBlank() // OpenAI
            3L -> deepSeekApiKey.value.isNotBlank() // DeepSeek
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
    
    /**
     * Translate the current chapter with API key validation
     */
    fun translateCurrentChapter(forceRetranslate: Boolean = false) {
        val chapter = stateChapter ?: return
        
        // Check if API key is required and set
        if (isApiKeyRequired() && !isApiKeySet()) {
            showSnackBar(UiText.DynamicString("Please configure your ${getCurrentEngineName()} API key in settings"))
            return
        }
        
        translationState.isTranslating = true
        translationState.translationError = null
        
        try {
            translateChapterWithStorageUseCase.execute(
                chapter = chapter,
                sourceLanguage = translatorOriginLanguage.value,
                targetLanguage = translatorTargetLanguage.value,
                contentType = ireader.domain.data.engines.ContentType.values()[translatorContentType.value],
                toneType = ireader.domain.data.engines.ToneType.values()[translatorToneType.value],
                preserveStyle = translatorPreserveStyle.value,
                applyGlossary = applyGlossaryToTranslations.value,
                forceRetranslate = forceRetranslate,
                scope = scope,
                onProgress = { progress ->
                    translationState.translationProgress = progress / 100f
                },
                onSuccess = { translatedChapter ->
                    translationState.translatedChapter = translatedChapter
                    translationState.hasTranslation = true
                    translationState.isTranslating = false
                    translationState.isShowingTranslation = true
                    
                    showSnackBar(UiText.MStringResource(MR.strings.success))
                },
                onError = { error ->
                    translationState.translationError = error.toString()
                    translationState.isTranslating = false
                    
                    // Check if it's a database error
                    if (error.toString().contains("no such table")) {
                        showSnackBar(UiText.DynamicString("Database migration needed. Please restart the app."))
                    } else {
                        showSnackBar(error)
                    }
                }
            )
        } catch (e: Exception) {
            translationState.isTranslating = false
            translationState.translationError = e.message
            showSnackBar(UiText.ExceptionString(e))
        }
    }
    
    /**
     * Load glossary for the current book
     */
    fun loadGlossary() {
        val bookId = book?.id ?: return
        
        scope.launch {
            val entries = getGlossaryByBookIdUseCase.execute(bookId)
            translationState.glossaryEntries = entries
        }
    }
    
    /**
     * Add a new glossary entry
     */
    fun addGlossaryEntry(
        sourceTerm: String,
        targetTerm: String,
        termType: ireader.domain.models.entities.GlossaryTermType,
        notes: String?
    ) {
        val bookId = book?.id ?: return
        
        scope.launch {
            saveGlossaryEntryUseCase.execute(
                bookId = bookId,
                sourceTerm = sourceTerm,
                targetTerm = targetTerm,
                termType = termType,
                notes = notes
            )
            loadGlossary()
            showSnackBar(UiText.MStringResource(MR.strings.success))
        }
    }
    
    /**
     * Update an existing glossary entry
     */
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
            loadGlossary()
            showSnackBar(UiText.MStringResource(MR.strings.success))
        }
    }
    
    /**
     * Delete a glossary entry
     */
    fun deleteGlossaryEntry(id: Long) {
        scope.launch {
            deleteGlossaryEntryUseCase.execute(id)
            loadGlossary()
            showSnackBar(UiText.MStringResource(MR.strings.success))
        }
    }

    
    /**
     * Export glossary as JSON
     */
    fun exportGlossary(onSuccess: (String) -> Unit) {
        val book = book ?: return
        
        scope.launch {
            try {
                val json = exportGlossaryUseCase.execute(
                    bookId = book.id,
                    bookTitle = book.title
                )
                onSuccess(json)
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
    
    /**
     * Import glossary from JSON
     */
    fun importGlossary(jsonString: String) {
        val bookId = book?.id ?: return
        
        scope.launch {
            try {
                val count = importGlossaryUseCase.execute(
                    jsonString = jsonString,
                    targetBookId = bookId
                )
                loadGlossary()
                showSnackBar(UiText.MStringResource(MR.strings.success))
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }

    // ==================== Font Management Methods ====================
    
    /**
     * Load system and custom fonts
     */
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
    
    /**
     * Select a font by ID
     */
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
    
    /**
     * Import a custom font
     */
    fun importFont(filePath: String, fontName: String) {
        scope.launch {
            try {
                val result = fontManagementUseCase.importFont(filePath, fontName)
                result.onSuccess { font ->
                    loadFonts()
                    showSnackBar(UiText.DynamicString("Font imported successfully"))
                }.onFailure { error ->
                    showSnackBar(UiText.ExceptionString(error as Exception))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
    
    /**
     * Delete a custom font
     */
    fun deleteFont(fontId: String) {
        scope.launch {
            try {
                val result = fontManagementUseCase.deleteFont(fontId)
                result.onSuccess {
                    loadFonts()
                    // If deleted font was selected, reset to default
                    if (selectedFontId.value == fontId) {
                        readerPreferences.selectedFontId().set("")
                        customFont = null
                    }
                    showSnackBar(UiText.DynamicString("Font deleted successfully"))
                }.onFailure { error ->
                    showSnackBar(UiText.ExceptionString(error as Exception))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
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
        val sourceId = book?.sourceId ?: 0L
        
        scope.launch {
            try {
                val result = reportBrokenChapterUseCase.invoke(
                    chapterId = chapter.id,
                    bookId = bookId,
                    sourceId = sourceId,
                    reason = reason,
                    description = description
                )
                if (result.isSuccess) {
                    showSnackBar(UiText.DynamicString("Chapter reported successfully. Thank you for your feedback!"))
                } else {
                    showSnackBar(UiText.DynamicString("Failed to report chapter: ${result.exceptionOrNull()?.message}"))
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
        if (!translationState.hasTranslation || translationState.translatedChapter == null) {
            return null
        }
        
        val translatedContent = translationState.translatedChapter?.translatedContent ?: return null
        
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
                    showSnackBar(UiText.DynamicString("Chapter reported successfully. Thank you for your feedback!"))
                } else {
                    showSnackBar(UiText.DynamicString("Failed to report chapter: ${result.exceptionOrNull()?.message}"))
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
        
        // Update reading streak when opening a chapter
        scope.launch {
            try {
                trackReadingProgressUseCase.updateReadingStreak(System.currentTimeMillis())
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to update reading streak", e)
            }
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
        
        // Only track if reading duration exceeds 10 seconds
        if (durationMillis > 10_000) {
            scope.launch(Dispatchers.IO) {
                try {
                    // Track reading time
                    trackReadingProgressUseCase.trackReadingTime(durationMillis)
                    trackReadingProgressUseCase.updateReadingStreak(closeTimestamp)
                    
                    // Track chapter progress if we have chapter content
                    currentChapter?.let { chapter ->
                        val content = getCurrentChapterContent()
                        val fullText = content.joinToString("\n") { page ->
                            when (page) {
                                is ireader.core.source.model.Text -> page.text
                                else -> ""
                            }
                        }
                        val wordCount = trackReadingProgressUseCase.estimateWordCount(fullText)
                        
                        // Track progress (assuming 80% read if chapter was open for significant time)
                        trackReadingProgressUseCase.onChapterProgressUpdate(0.8f, wordCount)
                        
                        // Mark chapter as read if not already marked
                        if (!chapter.read) {
                            val updatedChapter = chapter.copy(read = true)
                            insertUseCases.insertChapter(updatedChapter)
                            ireader.core.log.Log.debug("Chapter marked as read: ${chapter.name}")
                        }
                        
                        // Check if all chapters in the book are now read
                        checkAndTrackBookCompletion(chapter.bookId)
                    }
                } catch (e: Exception) {
                    ireader.core.log.Log.error("Failed to track reading progress", e)
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
                val allChaptersRead = chapters.all { it.read }
                if (allChaptersRead) {
                    trackReadingProgressUseCase.trackBookCompletion()
                    ireader.core.log.Log.debug("Book completed! Total books completed counter incremented.")
                }
            }
        } catch (e: Exception) {
            ireader.core.log.Log.error("Failed to check book completion", e)
        }
    }

    // ==================== Reading Break Reminder Methods ====================
    
    /**
     * Start the reading break timer if enabled
     */
    private fun startReadingBreakTimer() {
        if (!readingBreakReminderEnabled.value) {
            return
        }
        
        val intervalMinutes = readingBreakInterval.value
        if (intervalMinutes > 0) {
            readingTimerManager.startTimer(intervalMinutes)
            ireader.core.log.Log.debug("Reading break timer started: $intervalMinutes minutes")
        }
    }
    
    /**
     * Pause the reading break timer
     */
    private fun pauseReadingBreakTimer() {
        readingTimerManager.pauseTimer()
        ireader.core.log.Log.debug("Reading break timer paused")
    }
    
    /**
     * Resume the reading break timer
     */
    fun resumeReadingBreakTimer() {
        if (readingBreakReminderEnabled.value) {
            readingTimerManager.resumeTimer()
            ireader.core.log.Log.debug("Reading break timer resumed")
        }
    }
    
    /**
     * Reset the reading break timer
     */
    fun resetReadingBreakTimer() {
        readingTimerManager.resetTimer()
        ireader.core.log.Log.debug("Reading break timer reset")
    }
    
    /**
     * Called when the reading break interval is reached
     * Shows the reminder dialog if appropriate
     */
    private fun onReadingBreakIntervalReached() {
        // Check if we should show the reminder based on sentence boundaries
        val content = getCurrentChapterContent()
        val lastText = content.lastOrNull()
        
        // Simple sentence boundary detection - check if last character is a sentence ending
        val shouldShowNow = when (lastText) {
            is ireader.core.source.model.Text -> {
                val text = lastText.text.trim()
                text.isEmpty() || text.lastOrNull() in listOf('.', '!', '?', '。', '！', '？')
            }
            else -> true
        }
        
        if (shouldShowNow) {
            showReadingBreakDialog = true
            ireader.core.log.Log.debug("Reading break reminder shown")
        } else {
            // Wait a bit and try again (will check on next timer tick)
            scope.launch {
                delay(5000) // Wait 5 seconds
                if (!showReadingBreakDialog) {
                    showReadingBreakDialog = true
                    ireader.core.log.Log.debug("Reading break reminder shown (delayed)")
                }
            }
        }
    }
    
    /**
     * Handle "Take a Break" action from the reminder dialog
     */
    fun onTakeBreak() {
        showReadingBreakDialog = false
        pauseReadingBreakTimer()

        
        ireader.core.log.Log.debug("User chose to take a break")
    }
    
    /**
     * Handle "Continue Reading" action from the reminder dialog
     */
    fun onContinueReading() {
        showReadingBreakDialog = false
        resetReadingBreakTimer()
        startReadingBreakTimer()
        ireader.core.log.Log.debug("User chose to continue reading")
    }
    
    /**
     * Handle "Snooze" action from the reminder dialog
     * Requirements: 17.4, 17.5
     */
    fun onSnoozeReadingBreak(minutes: Int) {
        showReadingBreakDialog = false
        readingTimerManager.snoozeTimer(minutes)
        ireader.core.log.Log.debug("User snoozed reading break for $minutes minutes")
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
        return readingTimerManager.getRemainingTime()
    }
    
    /**
     * Check if the reading timer is currently running
     */
    fun isReadingTimerRunning(): Boolean {
        return readingTimerManager.isTimerRunning()
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
