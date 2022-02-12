package org.ireader.domain.view_models.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ireader.core.utils.*
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.fonts
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.source.Extensions
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.utils.Resource
import org.ireader.infinity.core.domain.use_cases.local.DeleteUseCase
import org.ireader.infinity.core.domain.use_cases.local.LocalInsertUseCases
import org.ireader.use_cases.remote.RemoteUseCases
import org.jsoup.Jsoup
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
    private val preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val _webView: WebView,
    savedStateHandle: SavedStateHandle,
    extensions: Extensions,
) : ViewModel() {


    var state by mutableStateOf(ReaderScreenState(source = extensions.mappingSourceNameToSource(0)))
        private set
    var prefState by mutableStateOf(ReaderScreenPreferencesState())
        private set


    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    val webView: WebView = _webView


    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Int>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        if (bookId != null && chapterId != null && sourceId != null) {
            state = state.copy(source = extensions.mappingSourceNameToSource(sourceId))
            state = state.copy(book = state.book.copy(id = bookId))
            state = state.copy(chapter = state.chapter.copy(chapterId = chapterId))
            getLocalChaptersByPaging()
            getLocalBookById()
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
        getChapterUseCase.getLastReadChapter(state.book.id)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            clearError()
                            toggleLocalLoading(false)
                            toggleLocalLoaded(true)
                            setChapter(result.data)
                            setScrollPosition(result.data.scrollPosition)
                            toggleLastReadAndUpdateChapterContent(result.data)
                            if (state.chapter.content.joinToString().isBlank()) {
                                getReadingContentRemotely()
                            }
                        }
                    }
                    is Resource.Error -> {
                        toggleLocalLoading(false)
                        toggleLocalLoaded(false)
//                        showSnackBar(result.uiText)
                        getReadingContentRemotely()
                    }
                }
            }.launchIn(viewModelScope)


    }


    private fun getChapters() {
        getChapterUseCase.getChaptersByBookId(bookId = state.book.id,
            isAsc = state.book.areChaptersReversed)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            state = state.copy(
                                chapters = result.data,
                                isChapterLoaded = true,
                            )
                            state =
                                state.copy(currentChapterIndex = result.data.indexOfFirst { state.chapter.chapterId == it.chapterId })
                            if (state.chapter.chapterId == Constants.LAST_CHAPTER && state.chapters.isNotEmpty()) {
                                getChapter(state.chapters.first())
                            }
                        }
                    }
                    is Resource.Error -> {
                    }
                }
            }.launchIn(viewModelScope)


    }

    fun getChapter(chapter: Chapter) {
        state = state.copy(chapter = chapter)
        clearError()
        state = state.copy(
            isLocalLoading = true,
            isLocalLoaded = false,
        )
        getChapterUseCase.getOneChapterById(chapterId = chapter.chapterId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            clearError()
                            setScrollPosition(result.data.scrollPosition)
                            toggleLocalLoading(false)
                            toggleLocalLoaded(true)
                            setChapter(result.data)
                            toggleLastReadAndUpdateChapterContent(result.data)
                            if (state.chapter.content.joinToString().isBlank()) {
                                getReadingContentRemotely()
                            }
                        }
                    }
                    is Resource.Error -> {
                        toggleLocalLoading(false)
                        toggleLocalLoaded(false)
                        showSnackBar(result.uiText)
                        getReadingContentRemotely()
                    }
                }

            }.launchIn(viewModelScope)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFromWebView() {
        try {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.trying_to_fetch_chapters_content))
                val chapter = state.source.contentFromElementParse(Jsoup.parse(webView.getHtml()))
                if (!chapter.content.isNullOrEmpty() && state.isBookLoaded && state.isChapterLoaded && webView.originalUrl == state.chapter.link) {
                    clearError()
                    state = state.copy(isLocalLoading = false,
                        chapter = state.chapter.copy(content = chapter.content))
                    toggleLastReadAndUpdateChapterContent(state.chapter.copy(content = chapter.content))
                    showSnackBar(UiText.DynamicString("${state.chapter.title} of ${state.chapter.bookName} was Fetched"))
                    if (state.chapter.content.size > 10) {
                        state = state.copy(isLocalLoaded = true)
                    }
                    state = state
                } else {
                    showSnackBar(UiText.DynamicString("Failed to to get the content"))
                }
            }
        } catch (e: Exception) {
        }


    }

    fun getReadingContentRemotely() {
        clearError()
        state = state.copy(
            isLocalLoading = true,
            isLocalLoaded = false,
        )
        remoteUseCases.getRemoteReadingContent(state.chapter, source = state.source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            clearError()
                            state = state
                                .copy(
                                    chapter = state.chapter.copy(content = result.data.content),
                                    isLocalLoading = false,
                                    isLocalLoaded = true,
                                )
                            toggleLastReadAndUpdateChapterContent(state.chapter.copy(
                                haveBeenRead = true))
                        }
                    }
                    is Resource.Error -> {
                        state =
                            state.copy(
                                isLocalLoading = false,
                                isLocalLoaded = false,
                            )
                        showSnackBar(result.uiText)
                    }
                }
            }.launchIn(viewModelScope)
    }


    private fun getLocalBookById() {
        viewModelScope.launch {
            getBookUseCases.getBookById(id = state.book.id).first { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && result.data != Book.create()) {
                            setBook(result.data)
                            toggleBookLoaded(true)
                            toggleIsChaptersReversed(result.data.areChaptersReversed)
                            toggleIsAsc(result.data.areChaptersReversed)
                            insertBook(result.data.copy(
                                lastRead = System.currentTimeMillis(),
                                unread = !result.data.unread))
                            getChapters()
                            if (state.chapter.chapterId != Constants.LAST_CHAPTER) {
                                getChapter(state.chapter)
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
            state.chapters.filter {
                it.lastRead
            }.forEach {
                insertChapter(it.copy(lastRead = false))
            }
            insertChapter(chapter.copy(haveBeenRead = true, lastRead = true))
        }
    }

    fun reverseChapters() {
        toggleIsAsc(!prefState.isAsc)
    }

    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging() {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = state.book.id,
                isAsc = prefState.isAsc)
                .cachedIn(viewModelScope).collect { snapshot ->
                    _chapters.value = snapshot
                }
        }

    }


    private fun readSelectedFontState() {
        prefState = prefState.copy(font = preferencesUseCase.readSelectedFontStateUseCase())
    }

    fun readBrightness(context: Context) {
        val brightness = preferencesUseCase.readBrightnessStateUseCase()
        val activity = context.findComponentActivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        prefState = prefState.copy(brightness = brightness)
    }

    private fun readFontSize() {
        prefState = prefState.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    private fun readParagraphDistance() {
        prefState =
            prefState.copy(distanceBetweenParagraphs = preferencesUseCase.readParagraphDistanceUseCase())
    }

    private fun readParagraphIndent() {
        prefState =
            prefState.copy(paragraphsIndent = preferencesUseCase.readParagraphIndentUseCase())
    }

    private fun readFontHeight() {
        prefState = prefState.copy(lineHeight = preferencesUseCase.readFontHeightUseCase())
    }

    private fun readBackgroundColor() {
        val color = readerScreenBackgroundColors[preferencesUseCase.getBackgroundColorUseCase()]
        prefState =
            prefState.copy(backgroundColor = color.color, textColor = color.onTextColor)
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findComponentActivity()!!
        when (preferencesUseCase.readOrientationUseCase()) {
            OrientationMode.Portrait -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                prefState = prefState.copy(orientation = Orientation.Portrait)
            }
            OrientationMode.Landscape -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                prefState = prefState.copy(orientation = Orientation.Landscape)
            }
        }
    }

    fun changeBackgroundColor(colorIndex: Int) {
        val color = readerScreenBackgroundColors[colorIndex]
        prefState =
            prefState.copy(backgroundColor = color.color, textColor = color.onTextColor)
        preferencesUseCase.setBackgroundColorUseCase(colorIndex)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findComponentActivity()!!
        when (prefState.orientation) {
            is Orientation.Landscape -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                prefState = prefState.copy(orientation = Orientation.Portrait)
                preferencesUseCase.saveOrientationUseCase(OrientationMode.Portrait)
            }
            is Orientation.Portrait -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                prefState = prefState.copy(orientation = Orientation.Landscape)
                preferencesUseCase.saveOrientationUseCase(OrientationMode.Landscape)
            }
        }
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = prefState.lineHeight
        if (isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight + 1)
            prefState = prefState.copy(lineHeight = currentFontHeight + 1)

        } else if (currentFontHeight > 20 && !isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight - 1)
            prefState = prefState.copy(lineHeight = currentFontHeight - 1)
        }
    }

    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = prefState.distanceBetweenParagraphs
        if (isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance + 1)
            prefState = prefState.copy(distanceBetweenParagraphs = currentDistance + 1)

        } else if (currentDistance > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance - 1)
            prefState = prefState.copy(distanceBetweenParagraphs = currentDistance - 1)
        }
    }

    fun saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = prefState.paragraphsIndent
        if (isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent + 1)
            prefState = prefState.copy(paragraphsIndent = paragraphsIndent + 1)

        } else if (paragraphsIndent > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent - 1)
            prefState = prefState.copy(paragraphsIndent = paragraphsIndent - 1)
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            prefState = prefState.copy(fontSize = prefState.fontSize + 1)
            preferencesUseCase.saveFontSizeStateUseCase(prefState.fontSize)
        } else {
            if (prefState.fontSize > 0) {
                prefState = prefState.copy(fontSize = prefState.fontSize - 1)
                preferencesUseCase.saveFontSizeStateUseCase(prefState.fontSize)
            }
        }
    }

    private fun saveFont(fontType: FontType) {
        prefState = prefState.copy(font = fontType)
        preferencesUseCase.saveSelectedFontStateUseCase(fonts.indexOf(fontType))
    }

    private fun saveBrightness(brightness: Float, context: Context) {
        val activity = context.findComponentActivity()!!
        val window = activity.window
        prefState = prefState.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

        preferencesUseCase.saveBrightnessStateUseCase(brightness)
    }


    /**
     * need a index, there is no need to confuse the index because the list reversed
     */
    fun updateChapterSliderIndex(index: Int) {
        state = state.copy(currentChapterIndex = index)
    }

    /**
     * get the index pf chapter based on the reversed state
     */
    fun getCurrentIndexOfChapter(chapter: Chapter): Int {
        val chaptersById: List<Int> = state.chapters.map { it.chapterId }
        return if (chaptersById.indexOf(chapter.chapterId) != -1) chaptersById.indexOf(chapter.chapterId) else 0
    }

    private fun getCurrentIndex(): Int {
        return if (state.currentChapterIndex < 0) {
            0
        } else if (state.currentChapterIndex > (state.chapters.lastIndex)) {
            state.chapters.lastIndex
        } else if (state.currentChapterIndex == -1) {
            0
        } else {
            state.currentChapterIndex
        }
    }

    fun getCurrentChapterByIndex(): Chapter {
        return try {
            state.chapters[getCurrentIndex()]
        } catch (e: Exception) {
            state.chapters[0]
        }
    }

    fun reverseSlider() {
        if (!prefState.isChapterReversingInProgress) {
            setBook(state.book.copy(areChaptersReversed = !state.book.areChaptersReversed))
            prefState =
                prefState.copy(
                    isChapterReversingInProgress = true,
                    isAsc = !prefState.isAsc
                )

            viewModelScope.launch(Dispatchers.IO) {
                showSnackBar(UiText.DynamicString("Reversing Chapters..."))
                insertUseCases.insertBook(state.book.copy(areChaptersReversed = state.book.areChaptersReversed))
                showSnackBar(UiText.DynamicString("Chapters were reversed"))
            }
            updateChapterSliderIndex(getCurrentIndexOfChapter(state.chapter))
            getChapters()
            getLocalChaptersByPaging()
            prefState = prefState.copy(isChapterReversingInProgress = false)
        }

    }

    fun restoreSetting(context: Context) {
        val activity = context.findComponentActivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.attributes = layoutParams

    }


    override fun onCleared() {
        state = state.copy(enable = false)
        getChapterJob?.cancel()
        super.onCleared()
    }

    private fun toggleReaderMode(enable: Boolean? = null) {
        state = state.copy(isReaderModeEnable = enable ?: !state.isReaderModeEnable,
            isMainBottomModeEnable = true,
            isSettingModeEnable = false)
    }

    fun toggleSettingMode(enable: Boolean, returnToMain: Boolean? = null) {
        if (returnToMain.isNull()) {
            state =
                state.copy(isSettingModeEnable = enable, isMainBottomModeEnable = false)

        } else {
            state =
                state.copy(isSettingModeEnable = false, isMainBottomModeEnable = true)
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertChapter(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapter(chapter)
        }
    }

    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(R.string.error_unknown)
            )
        )
    }

    private fun setScrollPosition(position: Int) {
        prefState = prefState.copy(scrollPosition = position)
    }

    private fun toggleIsAsc(isAsc: Boolean) {
        prefState = prefState.copy(isAsc = isAsc)
    }

    private fun toggleIsChaptersReversed(value: Boolean) {
        prefState = prefState.copy(isChaptersReversed = value)
    }


    /********************************************************/
    private fun setChapters(chapters: List<Chapter>) {
        state = state.copy(chapters = chapters)
    }

    private fun setChapter(chapter: Chapter) {
        state = state.copy(chapter = chapter)
    }

    /********************************************************/
    private fun toggleBookLoaded(loaded: Boolean) {
        state = state.copy(isBookLoaded = loaded)
    }

    private fun toggleLocalLoaded(loaded: Boolean) {
        state = state.copy(isLocalLoaded = loaded)
    }

    private fun toggleRemoteLoaded(loaded: Boolean) {
        state = state.copy(isRemoteLoaded = loaded)
    }

    private fun toggleLocalLoading(isLoading: Boolean) {
        state = state.copy(isLocalLoading = isLoading)
    }

    private fun toggleRemoteLoading(isLoading: Boolean) {
        state = state.copy(isRemoteLoading = isLoading)
    }

    private fun setBook(book: Book) {
        state = state.copy(book = book)
    }

    private fun clearError() {
        state = state.copy(error = UiText.StringResource(R.string.no_error))
    }
}