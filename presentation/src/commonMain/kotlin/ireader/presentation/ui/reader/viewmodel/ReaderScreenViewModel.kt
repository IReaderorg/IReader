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
import ireader.presentation.ui.core.theme.ReaderColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        val getTranslatedChapterUseCase: ireader.domain.usecases.translation.GetTranslatedChapterUseCase,
        val getGlossaryByBookIdUseCase: ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase,
        val saveGlossaryEntryUseCase: ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase,
        val deleteGlossaryEntryUseCase: ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase,
        val exportGlossaryUseCase: ireader.domain.usecases.glossary.ExportGlossaryUseCase,
        val importGlossaryUseCase: ireader.domain.usecases.glossary.ImportGlossaryUseCase,
        val params: Param,
        val globalScope: CoroutineScope
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(),
    ReaderScreenPreferencesState by prefState,
    ReaderScreenState by state {
    data class Param(val chapterId: Long?, val bookId: Long?)

    val globalChapterId : State<Long?> = mutableStateOf(params.chapterId)
    val globalBookId : State<Long?> = mutableStateOf(params.bookId)

    val readerColors: SnapshotStateList<ReaderColors> = readerThemes

    var isSettingChanging by mutableStateOf(false)

    var isSettingChangingJob : Job? = null
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
    var lastOrientationChangedTime =
        mutableStateOf(kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
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
        "Cooper Arabic"
    )

    // Translation state and preferences
    val translationState = TranslationStateImpl()
    val showTranslatedContent = readerPreferences.showTranslatedContent().asState()
    val autoSaveTranslations = readerPreferences.autoSaveTranslations().asState()
    val applyGlossaryToTranslations = readerPreferences.applyGlossaryToTranslations().asState()
    
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
            readerColors.addAll(0, list.map { it.ReaderColors() }.reversed())
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
        isReaderModeEnable = enable ?: !state.isReaderModeEnable
        
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
            backgroundColor.value = bgColor
            this.textColor.value = textColor
            setReaderBackgroundColor(bgColor)
            setReaderTextColor(textColor)
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
     * Translate the current chapter
     */
    fun translateCurrentChapter(forceRetranslate: Boolean = false) {
        val chapter = stateChapter ?: return
        
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

    override fun onDestroy() {
        getChapterJob?.cancel()
        getContentJob?.cancel()
        preloadJob?.cancel()
        clearPreloadCache()
        webViewManger.destroy()
        super.onDestroy()
    }
}
