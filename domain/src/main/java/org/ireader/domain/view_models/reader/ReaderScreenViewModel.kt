package org.ireader.domain.view_models.reader


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ireader.core.utils.*
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.fonts
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.*
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.Resource
import tachiyomi.source.Source
import javax.inject.Inject


@HiltViewModel
class ReaderScreenViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val catalogStore: CatalogStore,
    private val selectedFontStateUseCase: SelectedFontStateUseCase,
    private val brightnessStateUseCase: BrightnessStateUseCase,
    private val fontHeightUseCase: FontHeightUseCase,
    private val fontSizeStateUseCase: FontSizeStateUseCase,
    private val backgroundColorUseCase: BackgroundColorUseCase,
    private val paragraphDistanceUseCase: ParagraphDistanceUseCase,
    private val paragraphIndentUseCase: ParagraphIndentUseCase,
    private val orientationUseCase: OrientationUseCase,
    private val prefState: ReaderScreenPreferencesStateImpl,
    private val state: ReaderScreenStateImpl,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(), ReaderScreenPreferencesState by prefState, ReaderScreenState by state {


    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters


    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && chapterId != null && sourceId != null) {
            val source = catalogStore.get(sourceId)?.source
            if (source != null) {
                this.source = source
                getLocalChaptersByPaging(bookId)
                getChapters(bookId, source = source)
                getLocalBookById(bookId, chapterId, source = source)
                readPreferences()

            } else {
                viewModelScope.launch {
                    showSnackBar(UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(org.ireader.core.R.string.something_is_wrong_with_this_book))
            }
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
            else -> {}
        }
    }

    private fun getLastChapter(bookId: Long, source: Source) {
        viewModelScope.launch {
            val lastChapter = getChapterUseCase.findLastReadChapter(bookId)
            if (lastChapter != null) {
                clearError()
                toggleLoading(false)
                toggleLocalLoaded(true)
                setChapter(lastChapter)
                setPrefScrollPosition(lastChapter.progress)
                updateLastReadTime(lastChapter)
                val chapter = state.stateChapter
                if (chapter != null && chapter.content.joinToString()
                        .isBlank() && !isRemoteLoading
                ) {
                    getReadingContentRemotely(source = source, chapter = chapter)
                }
            } else {
                if (state.stateChapters.isNotEmpty()) {
                    getChapter(state.stateChapters.first().id, source)
                } else {
                    showSnackBar(UiText.StringResource(R.string.there_was_a_problem_in_getting_chapter))
                }
                toggleLoading(false)
                toggleLocalLoaded(false)
            }
        }
    }


    private fun getChapters(bookId: Long, source: Source) {
        viewModelScope.launch {
            val chapters = getChapterUseCase.findChaptersByBookId(bookId = bookId)
            if (chapters.isNotEmpty()) {
                state.stateChapters = chapters
                state.isChapterLoaded = true
                state.currentChapterIndex =
                    stateChapters.indexOfFirst { state.stateChapter?.id == it.id }

                if (stateChapter?.id == Constants.LAST_CHAPTER && state.stateChapters.isNotEmpty()) {
                    getChapter(state.stateChapters.first().id, source = source)
                }
            }
        }
    }

    fun getChapter(
        chapterId: Long,
        source: Source,
        onGetChapterEnd: () -> Unit = {},
    ) {
        toggleLoading(true)
        toggleLocalLoaded(false)
        viewModelScope.launch {
            val resultChapter = getChapterUseCase.findChapterById(chapterId = chapterId)
            if (resultChapter != null) {
                clearError()
                setPrefScrollPosition(resultChapter.progress)
                this@ReaderScreenViewModel.toggleLoading(false)
                toggleLocalLoaded(true)
                setChapter(resultChapter.copy(content = resultChapter.content))
                val chapter = state.stateChapter
                if (
                    chapter != null &&
                    chapter.content.joinToString()
                        .isBlank() &&
                    !state.isRemoteLoading &&
                    !state.isLoading
                ) {
                    getReadingContentRemotely(chapter = chapter, source = source)
                }
                updateLastReadTime(resultChapter)
                updateChapterSliderIndex(getCurrentIndexOfChapter(resultChapter))
                onGetChapterEnd()
            } else {
                toggleLoading(false)
                toggleLocalLoaded(false)
            }
        }

    }


    var getContentJob: Job? = null
    fun getReadingContentRemotely(chapter: Chapter, source: Source) {
        clearError()
        toggleLocalLoaded(false)
        toggleRemoteLoading(true)
        toggleLoading(true)
        getContentJob?.cancel()
        getContentJob = viewModelScope.launch(Dispatchers.IO) {
            remoteUseCases.getRemoteReadingContent(chapter, source = source)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                insertChapter(chapter.copy(content = result.data))
                                setChapter(chapter.copy(content = result.data))
                                toggleLoading(false)
                                toggleRemoteLoading(false)
                                toggleLocalLoaded(true)
                                toggleRemoteLoaded(true)
                                clearError()
                                getChapter(chapter.id, source = source)

                            } else {
                                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_chapter))
                            }
                        }
                        is Resource.Error -> {
                            toggleRemoteLoading(false)
                            toggleLoading(false)
                            toggleLocalLoaded(false)
                            toggleRemoteLoading(false)
                            showSnackBar(result.uiText)
                        }
                    }
                }
        }


    }


    private fun getLocalBookById(bookId: Long, chapterId: Long, source: Source) {
        viewModelScope.launch {
            val book = getBookUseCases.findBookById(id = bookId)
            if (book != null) {
                setStateChapter(book)
                toggleBookLoaded(true)
                withContext(Dispatchers.IO) {
                    insertBook(book.copy(
                        lastRead = System.currentTimeMillis()))
                }
                if (chapterId != Constants.LAST_CHAPTER && chapterId != Constants.NO_VALUE) {
                    getChapter(chapterId, source = source)
                } else {
                    getLastChapter(bookId, source = source)
                }
                getLocalChaptersByPaging(bookId)

            }
        }

    }

    private suspend fun updateLastReadTime(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            insertChapter(chapter.copy(read = true, lastRead = System.currentTimeMillis()))
        }
    }

    fun reverseChapters() {
        toggleIsAsc(!prefState.isAsc)
    }

    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging(bookId: Long) {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = bookId,
                isAsc = prefState.isAsc)
                .cachedIn(viewModelScope).collect { snapshot ->
                    _chapters.value = snapshot
                }
        }

    }


    private fun readSelectedFontState() {
        font = selectedFontStateUseCase.read()
    }

    fun readBrightness(context: Context) {
        val brightness = brightnessStateUseCase.read()
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
            this.brightness = brightness

        }
    }

    private fun readFontSize() {
        this.fontSize = fontSizeStateUseCase.read()

    }

    private fun readParagraphDistance() {
        distanceBetweenParagraphs = paragraphDistanceUseCase.read()
    }

    private fun readParagraphIndent() {
        paragraphsIndent = paragraphIndentUseCase.read()
    }

    private fun readFontHeight() {
        lineHeight = fontHeightUseCase.read()
    }

    private fun readBackgroundColor() {
        val color = readerScreenBackgroundColors[backgroundColorUseCase.read()]
        backgroundColor = color.color
        textColor = color.onTextColor
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (orientationUseCase.read()) {
                OrientationMode.Portrait -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation = Orientation.Portrait

                }
                OrientationMode.Landscape -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation = Orientation.Landscape
                }
            }
        }
    }

    fun changeBackgroundColor(colorIndex: Int) {
        val color = readerScreenBackgroundColors[colorIndex]
        backgroundColor = color.color
        textColor = color.onTextColor
        backgroundColorUseCase.save(colorIndex)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (prefState.orientation) {
                is Orientation.Landscape -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation = Orientation.Portrait
                    orientationUseCase.save(OrientationMode.Portrait)

                }
                is Orientation.Portrait -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation = Orientation.Landscape
                    orientationUseCase.save(OrientationMode.Landscape)
                }
            }
        }
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = prefState.lineHeight
        if (isIncreased) {
            fontHeightUseCase.save(currentFontHeight + 1)
            lineHeight = currentFontHeight + 1

        } else if (currentFontHeight > 20 && !isIncreased) {
            fontHeightUseCase.save(currentFontHeight - 1)
            lineHeight = currentFontHeight - 1

        }
    }

    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = prefState.distanceBetweenParagraphs
        if (isIncreased) {
            paragraphDistanceUseCase.save(currentDistance + 1)
            distanceBetweenParagraphs = currentDistance + 1

        } else if (currentDistance > 1 && !isIncreased) {
            paragraphDistanceUseCase.save(currentDistance - 1)
            distanceBetweenParagraphs = currentDistance - 1

        }
    }

    fun saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = prefState.paragraphsIndent
        if (isIncreased) {
            paragraphIndentUseCase.save(paragraphsIndent + 1)
            this.paragraphsIndent = paragraphsIndent + 1


        } else if (paragraphsIndent > 1 && !isIncreased) {
            paragraphIndentUseCase.save(paragraphsIndent - 1)
            this.paragraphsIndent = paragraphsIndent - 1
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            this.fontSize = this.fontSize + 1
            fontSizeStateUseCase.save(prefState.fontSize)
        } else {
            if (prefState.fontSize > 0) {
                this.fontSize = this.fontSize - 1
                fontSizeStateUseCase.save(prefState.fontSize)
            }
        }
    }

    private fun saveFont(fontType: FontType) {
        this.font = fontType
        selectedFontStateUseCase.save(fonts.indexOf(fontType))

    }

    private fun saveBrightness(brightness: Float, context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            this.brightness = brightness
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
            brightnessStateUseCase.save(brightness)
        }
    }


    /**
     * need a index, there is no need to confuse the index because the list reversed
     */
    fun updateChapterSliderIndex(index: Int) {
        currentChapterIndex = index
    }

    /**
     * get the index pf chapter based on the reversed state
     */
    fun getCurrentIndexOfChapter(chapter: Chapter): Int {

        val selectedChapter = state.stateChapters.indexOfFirst { it.id == chapter.id }
        return if (selectedChapter != -1) selectedChapter else 0
    }

    private fun getCurrentIndex(): Int {
        return if (state.currentChapterIndex < 0) {
            0
        } else if (state.currentChapterIndex > (state.stateChapters.lastIndex)) {
            state.stateChapters.lastIndex
        } else if (state.currentChapterIndex == -1) {
            0
        } else {
            state.currentChapterIndex
        }
    }

    fun getCurrentChapterByIndex(): Chapter {
        return try {
            state.stateChapters[getCurrentIndex()]
        } catch (e: Exception) {
            state.stateChapters[0]
        }
    }


    fun restoreSetting(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
        }


    }

    override fun onDestroy() {
        enable = false
        getChapterJob?.cancel()
        getContentJob?.cancel()
        super.onDestroy()
    }


    private fun toggleReaderMode(enable: Boolean? = null) {
        isReaderModeEnable = enable ?: !state.isReaderModeEnable
        isMainBottomModeEnable = true
        isSettingModeEnable = false
    }

    fun toggleSettingMode(enable: Boolean, returnToMain: Boolean? = null) {
        if (returnToMain.isNull()) {
            isSettingModeEnable = enable
            isMainBottomModeEnable = false

        } else {
            isSettingModeEnable = false
            isMainBottomModeEnable = true
        }
    }

    suspend fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    suspend fun insertChapter(chapter: Chapter) {
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

    private fun setPrefScrollPosition(position: Int) {
        this.scrollPosition = position

    }

    private fun toggleIsAsc(isAsc: Boolean) {
        this.isAsc = isAsc
    }

    private fun toggleIsChaptersReversed(value: Boolean) {
        this.isChaptersReversed = value
    }


    /********************************************************/
    private fun setChapters(chapters: List<Chapter>) {
        state.stateChapters = chapters
    }

    private fun setChapter(chapter: Chapter) {
        state.stateChapter = chapter
    }

    /********************************************************/
    private fun toggleBookLoaded(loaded: Boolean) {
        state.isBookLoaded = loaded
    }

    private fun toggleLocalLoaded(loaded: Boolean) {
        state.isLocalLoaded = loaded
    }

    private fun toggleRemoteLoaded(loaded: Boolean) {
        state.isRemoteLoaded = loaded
    }

    private fun toggleLoading(isLoading: Boolean) {
        state.isLoading = isLoading
    }

    private fun toggleRemoteLoading(isLoading: Boolean) {
        state.isRemoteLoading = isLoading
    }

//    private fun toggleLoading(isLoading: Boolean) {
//        state = state.copy(isLoading = isLoading)
//    }

    private fun setStateChapter(book: Book) {
        state.book = book
    }

    private fun clearError() {
        state.error = UiText.StringResource(R.string.no_error)
    }

    fun saveScrollState(currentProgress: Int? = 0) {
        val ch = state.stateChapter
        viewModelScope.launch(Dispatchers.IO) {
            if (ch != null) {
                insertUseCases.insertChapter(ch.copy(progress = currentProgress
                    ?: ch.progress))
            }
        }
    }
}