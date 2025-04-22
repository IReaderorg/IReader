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
import ireader.core.source.model.Text
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.readerThemes
import ireader.domain.preferences.prefs.*
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        val chapter = getChapterUseCase.findChapterById(chapterId)
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

    override fun onDestroy() {
        getChapterJob?.cancel()
        getContentJob?.cancel()
        webViewManger.destroy()
        super.onDestroy()
    }
}
