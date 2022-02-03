package ir.kazemcodes.infinity.feature_reader.presentation.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.kazemcodes.infinity.core.data.network.utils.launchUI
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
import ir.kazemcodes.infinity.core.presentation.theme.fonts
import ir.kazemcodes.infinity.core.presentation.theme.readerScreenBackgroundColors
import ir.kazemcodes.infinity.core.ui.NavigationArgs
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import javax.inject.Inject


/**
 * the order of this screen is
 * first we need to get the book from room then
 * we use the areReversedChapter to understanding the order of
 * chapters for chapterList slider then get Chapters using pagination for chapter drawer.
 */
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ReaderScreenViewModel @Inject constructor(
    private val preferencesUseCase: PreferencesUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: Extensions,
    @ApplicationContext private val context: Context,
) : ViewModel(), KoinComponent {
    private val _state =
        mutableStateOf(ReaderScreenState(source = extensions.mappingSourceNameToSource(0)))
    val state: State<ReaderScreenState> = _state


    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    val webView by inject<WebView>()

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Int>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        if (bookId != null && chapterId != null && sourceId != null) {
            _state.value =
                state.value.copy(source = extensions.mappingSourceNameToSource(sourceId))
            _state.value = state.value.copy(book = state.value.book.copy(id = bookId))
            _state.value =
                state.value.copy(chapter = state.value.chapter.copy(chapterId = chapterId))
            getLocalBookById()
            getLocalChaptersByPaging()
            readPreferences()
        }
    }


    private fun readPreferences() {
        readSelectedFontState()
        readFontSize()
        readBackgroundColor()
        readFontHeight()
        readParagraphDistance()
        readParagraphIndent()
    }

    fun onEvent(event: ReaderEvent) {
        when (event) {

            is ReaderEvent.ChangeBrightness -> {
                saveBrightness(event.brightness, event.context)
            }
            is ReaderEvent.ChangeFontSize -> {
                saveFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                saveFont(event.fontType)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
        }
    }

    private fun getLastChapter() {
        viewModelScope.launch(Dispatchers.IO) {
            getChapterUseCase.getLastReadChapter(state.value.book.id)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value.copy(
                                    chapter = result.data,
                                    isLoading = false,
                                    isLoaded = true,
                                    error = UiText.noError(),
                                    scrollPosition = result.data.scrollPosition
                                )
                                toggleLastReadAndUpdateChapterContent(result.data)
                                if (state.value.chapter.content.joinToString().isBlank()) {
                                    getReadingContentRemotely()
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(
                                    isLoading = false,
                                    isLoaded = false,
                                )
                            _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText ?: UiText.unknownError()
                                .asString()))
                            getReadingContentRemotely()
                        }
                    }
                }
        }

    }

    private fun toggleReaderMode(enable: Boolean? = null) {
        _state.value =
            state.value.copy(isReaderModeEnable = enable ?: !state.value.isReaderModeEnable,
                isMainBottomModeEnable = true,
                isSettingModeEnable = false)
    }

    fun toggleSettingMode(enable: Boolean, returnToMain: Boolean? = null) {
        if (returnToMain.isNull()) {
            _state.value =
                state.value.copy(isSettingModeEnable = enable, isMainBottomModeEnable = false)

        } else {
            _state.value =
                state.value.copy(isSettingModeEnable = false, isMainBottomModeEnable = true)
        }
    }

    private fun getChapters() {
        viewModelScope.launch {
            getChapterUseCase.getChaptersByBookId(bookId = state.value.book.id,
                isAsc = state.value.book.areChaptersReversed)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value.copy(
                                    chapters = result.data,
                                    isChapterLoaded = true,
                                )
                                _state.value =
                                    state.value.copy(currentChapterIndex = result.data.indexOfFirst { state.value.chapter.chapterId == it.chapterId })
                            }
                        }
                        is Resource.Error -> {
                        }
                    }
                }
        }

    }

    fun getChapter(chapter: Chapter) {
        _state.value = state.value.copy(chapter = chapter)
        _state.value = state.value.copy(
            isLoading = true,
            error = UiText.noError(),
            isLoaded = false,
        )
        getChapterUseCase.getOneChapterById(chapterId = chapter.chapterId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value.copy(
                                chapter = result.data,
                                isLoading = false,
                                isLoaded = true,
                                error = UiText.noError(),
                                scrollPosition = result.data.scrollPosition
                            )
                            toggleLastReadAndUpdateChapterContent(result.data)
                            if (state.value.chapter.content.joinToString().isBlank()) {
                                getReadingContentRemotely()
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(
                                isLoading = false,
                                isLoaded = false,
                            )
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText ?: UiText.unknownError()
                            .asString()))
                        getReadingContentRemotely()
                    }
                }

            }.launchIn(viewModelScope)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFromWebView() {
        val webView by inject<WebView>()
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch chapter's content").asString()
            ))

            val chapter = state.value.source.contentFromElementParse(Jsoup.parse(webView.getHtml()))
            if (!chapter.content.isNullOrEmpty() && state.value.isBookLoaded && state.value.isChapterLoaded && webView.originalUrl == state.value.chapter.link) {
                _state.value = state.value.copy(isLoading = false,
                    error = UiText.noError(),
                    chapter = state.value.chapter.copy(content = chapter.content))
                toggleLastReadAndUpdateChapterContent(state.value.chapter.copy(content = chapter.content))

                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("${state.value.chapter.title} of ${state.value.chapter.bookName} was Fetched")
                        .asString()
                ))
                if (state.value.chapter.content.size > 10) {
                    _state.value = state.value.copy(isLoaded = true)
                }
                _state.value = state.value
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("Failed to to get the content").asString()
                ))
            }
        }

    }

    fun getReadingContentRemotely() {
        _state.value = state.value.copy(
            isLoading = true,
            error = UiText.noError(),
            isLoaded = false,
        )
        remoteUseCases.getRemoteReadingContent(state.value.chapter, source = state.value.source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value
                                .copy(
                                    chapter = state.value.chapter.copy(content = result.data.content),
                                    isLoading = false,
                                    error = UiText.noError(),
                                    isLoaded = true,
                                )
                            toggleLastReadAndUpdateChapterContent(state.value.chapter.copy(
                                haveBeenRead = true))
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(
                                isLoading = false,
                                isLoaded = false,
                            )
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText ?: UiText.unknownError()
                            .asString()))
                    }
                }
            }.launchIn(viewModelScope)
    }


    private fun getLocalBookById() {
        viewModelScope.launch {
            getBookUseCases.getBookById(id = state.value.book.id).first { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && result.data != Book.create()) {
                            _state.value = state.value.copy(
                                book = result.data,
                                isBookLoaded = true,
                                isChaptersReversed = result.data.areChaptersReversed,
                                isAsc = result.data.areChaptersReversed,
                            )
                            withContext(Dispatchers.IO) {
                                insertUseCases.insertBook(book = result.data.copy(lastRead = System.currentTimeMillis(),
                                    unread = !result.data.unread))
                            }
                            getChapters()
                            if (state.value.chapter.chapterId != Constants.LAST_CHAPTER) {
                                getChapter(state.value.chapter)
                            } else {
                                getLastChapter()
                            }
                            getLocalChaptersByPaging()
                            true
                        } else {
                            false
                        }
                    }
                    is Resource.Error -> {
                        false
                    }
                }
            }
        }

    }

    private fun toggleLastReadAndUpdateChapterContent(chapter: Chapter) {
        viewModelScope.launch {
            deleteUseCase.deleteChapterByChapter(chapter)
            state.value.chapters.filter {
                it.lastRead
            }.forEach {
                withContext(Dispatchers.IO) {

                    insertUseCases.insertChapter(it.copy(lastRead = false))
                }
            }
            withContext(Dispatchers.IO) {
                insertUseCases.insertChapter(chapter.copy(haveBeenRead = true, lastRead = true))
            }
        }
    }

    fun reverseChapters() {
        _state.value = state.value.copy(isAsc = !state.value.isAsc)
    }

    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging() {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = state.value.book.id,
                isAsc = state.value.isAsc)
                .cachedIn(viewModelScope).collect { snapshot ->
                    _chapters.value = snapshot
                }
        }

    }


    private fun readSelectedFontState() {
        _state.value = state.value.copy(font = preferencesUseCase.readSelectedFontStateUseCase())
    }

    fun readBrightness(context: Context) {
        val brightness = preferencesUseCase.readBrightnessStateUseCase()
        val activity = context.findComponentActivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        _state.value = state.value.copy(brightness = brightness)
    }

    private fun readFontSize() {
        _state.value = state.value.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    private fun readParagraphDistance() {
        _state.value =
            state.value.copy(distanceBetweenParagraphs = preferencesUseCase.readParagraphDistanceUseCase())
    }

    private fun readParagraphIndent() {
        _state.value =
            state.value.copy(paragraphsIndent = preferencesUseCase.readParagraphIndentUseCase())
    }

    private fun readFontHeight() {
        _state.value = state.value.copy(lineHeight = preferencesUseCase.readFontHeightUseCase())
    }

    private fun readBackgroundColor() {
        val color = readerScreenBackgroundColors[preferencesUseCase.getBackgroundColorUseCase()]
        _state.value =
            state.value.copy(backgroundColor = color.color, textColor = color.onTextColor)
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findComponentActivity()!!
        when (preferencesUseCase.readOrientationUseCase()) {
            0 -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
            }
            1 -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                _state.value = state.value.copy(orientation = Orientation.Landscape)
            }
        }
    }

    fun changeBackgroundColor(colorIndex: Int) {
        val color = readerScreenBackgroundColors[colorIndex]
        _state.value =
            state.value.copy(backgroundColor = color.color, textColor = color.onTextColor)
        preferencesUseCase.setBackgroundColorUseCase(colorIndex)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findComponentActivity()!!
        when (state.value.orientation) {
            is Orientation.Landscape -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
                preferencesUseCase.saveOrientationUseCase(Orientation.Portrait.index)
            }
            is Orientation.Portrait -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                _state.value = state.value.copy(orientation = Orientation.Landscape)
                preferencesUseCase.saveOrientationUseCase(Orientation.Landscape.index)
            }
        }
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = state.value.lineHeight
        if (isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight + 1)
            _state.value = state.value.copy(lineHeight = currentFontHeight + 1)

        } else if (currentFontHeight > 20 && !isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight - 1)
            _state.value = state.value.copy(lineHeight = currentFontHeight - 1)
        }
    }

    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = state.value.distanceBetweenParagraphs
        if (isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance + 1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance + 1)

        } else if (currentDistance > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance - 1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance - 1)
        }
    }

    fun saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = state.value.paragraphsIndent
        if (isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent + 1)
            _state.value = state.value.copy(paragraphsIndent = paragraphsIndent + 1)

        } else if (paragraphsIndent > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent - 1)
            _state.value = state.value.copy(paragraphsIndent = paragraphsIndent - 1)
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            _state.value = state.value.copy(fontSize = state.value.fontSize + 1)
            preferencesUseCase.saveFontSizeStateUseCase(state.value.fontSize)
        } else {
            if (state.value.fontSize > 0) {
                _state.value = state.value.copy(fontSize = state.value.fontSize - 1)
                preferencesUseCase.saveFontSizeStateUseCase(state.value.fontSize)
            }
        }
    }

    private fun saveFont(fontType: FontType) {
        _state.value = state.value.copy(font = fontType)
        preferencesUseCase.saveSelectedFontStateUseCase(fonts.indexOf(fontType))
    }

    private fun saveBrightness(brightness: Float, context: Context) {
        val activity = context.findComponentActivity()!!
        val window = activity.window
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

        preferencesUseCase.saveBrightnessStateUseCase(brightness)
    }


    /**
     * need a index, there is no need to confuse the index because the list reversed
     */
    fun updateChapterSliderIndex(index: Int) {
        _state.value = state.value.copy(currentChapterIndex = index)
    }

    /**
     * get the index pf chapter based on the reversed state
     */
    fun getCurrentIndexOfChapter(chapter: Chapter): Int {
        val chaptersById: List<Int> = state.value.chapters.map { it.chapterId }
        return if (chaptersById.indexOf(chapter.chapterId) != -1) chaptersById.indexOf(chapter.chapterId) else 0
    }

    private fun getCurrentIndex(): Int {
        return if (state.value.currentChapterIndex < 0) {
            0
        } else if (state.value.currentChapterIndex > (state.value.chapters.lastIndex)) {
            state.value.chapters.lastIndex
        } else if (state.value.currentChapterIndex == -1) {
            0
        } else {
            state.value.currentChapterIndex
        }
    }

    fun getCurrentChapterByIndex(): Chapter {
        return try {
            state.value.chapters[getCurrentIndex()]
        } catch (e: Exception) {
            state.value.chapters[0]
        }
    }

    fun reverseSlider() {
        if (!state.value.isChapterReversingInProgress) {
            _state.value =
                state.value.copy(
                    book = state.value.book.copy(areChaptersReversed = !state.value.book.areChaptersReversed),
                    isChapterReversingInProgress = true,
                    isAsc = !state.value.isAsc
                )

            viewModelScope.launch(Dispatchers.IO) {
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Reversing Chapters...")
                    .asString()))
                insertUseCases.insertBook(state.value.book.copy(areChaptersReversed = state.value.book.areChaptersReversed))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Chapters were reversed")
                    .asString()))
            }
            updateChapterSliderIndex(getCurrentIndexOfChapter(state.value.chapter))
            getChapters()
            getLocalChaptersByPaging()
            _state.value = state.value.copy(isChapterReversingInProgress = false)
        }

    }

    fun showSnackBar(message: String) {
        viewModelScope.launchUI {
            _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString(message).asString()))

        }
    }


    fun restoreSetting(context: Context) {
        val activity = context.findComponentActivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = -1f
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.attributes = layoutParams

    }


    override fun onCleared() {
        _state.value = state.value.copy(enable = false)
        super.onCleared()
    }

}