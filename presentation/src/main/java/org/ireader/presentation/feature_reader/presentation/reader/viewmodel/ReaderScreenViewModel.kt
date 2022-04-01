package org.ireader.presentation.feature_reader.presentation.reader.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.core.utils.*
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.fonts
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.History
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.withIOContext
import tachiyomi.source.Source
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ReaderScreenViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val historyUseCase: HistoryUseCase,
    private val catalogStore: CatalogStore,
    private val readerUseCases: ReaderPrefUseCases,
    private val prefState: ReaderScreenPreferencesStateImpl,
    private val state: ReaderScreenStateImpl,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(), ReaderScreenPreferencesState by prefState, ReaderScreenState by state {



    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && chapterId != null && sourceId != null) {
            val source = catalogStore.get(sourceId)?.source
            if (source != null) {
                this.source = source
                viewModelScope.launch {
                    getLocalBookById(bookId, chapterId, source = source)
                    readPreferences()
                }


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
        font = readerUseCases.selectedFontStateUseCase.read()
        readerUseCases.selectedFontStateUseCase.save(0)


        this.fontSize = readerUseCases.fontSizeStateUseCase.read()

        readBackgroundColor()
        readTextColor()

        lineHeight = readerUseCases.fontHeightUseCase.read()
        distanceBetweenParagraphs = readerUseCases.paragraphDistanceUseCase.read()
        paragraphsIndent = readerUseCases.paragraphIndentUseCase.read()
        verticalScrolling = readerUseCases.scrollModeUseCase.read()
        scrollIndicatorPadding = readerUseCases.scrollIndicatorUseCase.readPadding()
        scrollIndicatorWith = readerUseCases.scrollIndicatorUseCase.readWidth()
        autoScrollInterval = readerUseCases.autoScrollMode.readInterval()
        autoScrollOffset = readerUseCases.autoScrollMode.readOffset()
        autoBrightnessMode = readerUseCases.brightnessStateUseCase.readAutoBrightness()
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
                saveFont(event.index)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
            else -> {}
        }
    }


    suspend fun getChapter(
        chapterId: Long,
        source: Source,
    ) {
        toggleLoading(true)
        toggleLocalLoaded(false)
        viewModelScope.launch {
            val resultChapter = getChapterUseCase.findChapterById(
                chapterId = chapterId,
                state.book?.id,
            )
            if (resultChapter != null) {

                clearError()
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
                if (!initialized) {
                    initialized = true
                }
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
            remoteUseCases.getRemoteReadingContent(
                chapter,
                source = source,
                onSuccess = { content ->
                    if (content != null) {
                        insertChapter(content)
                        setChapter(content)
                        toggleLoading(false)
                        toggleRemoteLoading(false)
                        toggleLocalLoaded(true)
                        toggleRemoteLoaded(true)
                        clearError()
                        getChapter(chapter.id, source = source)

                    } else {
                        showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_chapter))
                    }
                },
                onError = { message ->
                    toggleRemoteLoading(false)
                    toggleLoading(false)
                    toggleLocalLoaded(false)
                    toggleRemoteLoading(false)
                    if (message != null) {
                        showSnackBar(message)
                    }
                }
            )
        }


    }


    private suspend fun getLocalBookById(bookId: Long, chapterId: Long, source: Source) {
        viewModelScope.launch {
            val book = getBookUseCases.findBookById(id = bookId)
            if (book != null) {
                setStateChapter(book)
                toggleBookLoaded(true)
                getLocalChaptersByPaging(bookId)
                val last = historyUseCase.findHistoryByBookId(bookId)
                if (chapterId != Constants.LAST_CHAPTER && chapterId != Constants.NO_VALUE) {
                    getChapter(chapterId, source = source)
                } else if (last != null) {
                    getChapter(chapterId = last.chapterId, source = source)
                } else {
                    val chapters = getChapterUseCase.findChaptersByBookId(bookId)
                    if (chapters.isNotEmpty()) {
                        getChapter(chapters.first().id, source = source)
                    }
                }
                if (stateChapters.isNotEmpty()) {
                    state.currentChapterIndex =
                        stateChapters.indexOfFirst { state.stateChapter?.id == it.id }

//                    if (stateChapter == null && state.stateChapters.isNotEmpty()) {
//                        getChapter(state.stateChapters.first().id, source = source)
//                    }
                }


            }
        }

    }

    private fun updateLastReadTime(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapter(
                chapter = chapter.copy(read = true)
            )
            historyUseCase.insertHistory(History(
                bookId = chapter.bookId,
                chapterId = chapter.id,
                readAt = currentTimeToLong()))
        }

    }

    fun reverseChapters() {
        toggleIsAsc(!prefState.isAsc)
    }

    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging(bookId: Long) {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.subscribeChaptersByBookId(bookId = bookId,
                isAsc = prefState.isAsc, "")
                .collect {
                    stateChapters = it
                }
        }

    }


    fun readBrightness(context: Context) {
        val brightness = readerUseCases.brightnessStateUseCase.readBrightness()
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            if (!autoBrightnessMode) {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                layoutParams.screenBrightness = brightness
                window.attributes = layoutParams
                this.brightness = brightness
            } else {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                showSystemBars(context = context)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                window.attributes = layoutParams
            }
        }
    }


    fun readBackgroundColor(): Color {
        val color = Color(readerUseCases.backgroundColorUseCase.read())
        this.backgroundColor = color
        return color
    }

    fun readTextColor(): Color {
        val textColor = Color(readerUseCases.textColorUseCase.read())
        this.textColor = textColor
        return textColor
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (readerUseCases.orientationUseCase.read()) {
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
        val bgColor = readerScreenBackgroundColors[colorIndex].color
        val textColor = readerScreenBackgroundColors[colorIndex].onTextColor
        backgroundColor = bgColor
        this.textColor = textColor
        setReaderBackgroundColor(bgColor)
        setReaderTextColor(textColor)
        Timber.e(backgroundColor.toArgb().toString())
        Timber.e(this.textColor.toArgb().toString())
    }

    fun setReaderBackgroundColor(color: Color) {
        readerUseCases.backgroundColorUseCase.save(color.toArgb())
    }

    fun setReaderTextColor(color: Color) {
        readerUseCases.textColorUseCase.save(color.toArgb())
    }

    fun setAutoScrollIntervalReader(increase: Boolean) {
        if (increase) {
            autoScrollInterval += 500
            readerUseCases.autoScrollMode.saveInterval(autoScrollInterval + 500)
        } else {
            autoScrollInterval -= 500
            readerUseCases.autoScrollMode.saveInterval(autoScrollInterval - 500)
        }

    }

    fun setAutoScrollOffsetReader(increase: Boolean) {
        if (increase) {
            autoScrollOffset += 50
            readerUseCases.autoScrollMode.saveOffset(autoScrollOffset + 50)
        } else {
            autoScrollOffset -= 50
            readerUseCases.autoScrollMode.saveOffset(autoScrollOffset - 50)
        }


    }

    fun toggleAutoBrightness() {
        if (autoBrightnessMode) {
            autoBrightnessMode = false
            readerUseCases.brightnessStateUseCase.saveAutoBrightness(false)
        } else {
            autoBrightnessMode = true
            readerUseCases.brightnessStateUseCase.saveAutoBrightness(true)
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (prefState.orientation) {
                is Orientation.Landscape -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation = Orientation.Portrait
                    readerUseCases.orientationUseCase.save(OrientationMode.Portrait)

                }
                is Orientation.Portrait -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation = Orientation.Landscape
                    readerUseCases.orientationUseCase.save(OrientationMode.Landscape)
                }
            }
        }
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = prefState.lineHeight
        if (isIncreased) {
            readerUseCases.fontHeightUseCase.save(currentFontHeight + 1)
            lineHeight = currentFontHeight + 1

        } else if (currentFontHeight > 20 && !isIncreased) {
            readerUseCases.fontHeightUseCase.save(currentFontHeight - 1)
            lineHeight = currentFontHeight - 1

        }
    }

    fun saveScrollIndicatorPadding(increase: Boolean) {
        if (increase) {
            scrollIndicatorPadding += 1

            readerUseCases.scrollIndicatorUseCase.savePadding(scrollIndicatorPadding + 1)
        } else {
            scrollIndicatorPadding -= 1
            readerUseCases.scrollIndicatorUseCase.savePadding(scrollIndicatorPadding - 1)
        }

    }

    fun saveScrollIndicatorWidth(increase: Boolean) {
        if (increase) {
            scrollIndicatorWith += 1
            readerUseCases.scrollIndicatorUseCase.saveWidth(scrollIndicatorWith + 1)
        } else {
            scrollIndicatorWith -= 1
            readerUseCases.scrollIndicatorUseCase.saveWidth(scrollIndicatorWith - 1)
        }
    }

    fun readScrollIndicatorWidth(): Int {
        return readerUseCases.scrollIndicatorUseCase.readWidth()
    }


    fun readScrollIndicatorPadding(): Int {
        return readerUseCases.scrollIndicatorUseCase.readPadding()
    }


    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = prefState.distanceBetweenParagraphs
        if (isIncreased) {
            readerUseCases.paragraphDistanceUseCase.save(currentDistance + 1)
            distanceBetweenParagraphs = currentDistance + 1

        } else if (currentDistance > 1 && !isIncreased) {
            readerUseCases.paragraphDistanceUseCase.save(currentDistance - 1)
            distanceBetweenParagraphs = currentDistance - 1

        }
    }

    fun toggleScrollMode() {
        verticalScrolling = !verticalScrolling
        readerUseCases.scrollModeUseCase.save(verticalScrolling)

    }

    fun toggleAutoScrollMode() {
        autoScrollMode = !autoScrollMode
    }

    fun toggleImmersiveMode(context: Context) {
        immersiveMode = !immersiveMode
        readerUseCases.immersiveModeUseCase.save(immersiveMode)
        if (immersiveMode) {
            hideSystemBars(context = context)
        } else {
            showSystemBars(context)
        }
    }

    fun readImmersiveMode(context: Context) {
        immersiveMode = readerUseCases.immersiveModeUseCase.read()
        if (immersiveMode) {
            hideSystemBars(context = context)
        } else {
            showSystemBars(context)
        }
    }

    fun saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = prefState.paragraphsIndent
        if (isIncreased) {
            readerUseCases.paragraphIndentUseCase.save(paragraphsIndent + 1)
            this.paragraphsIndent = paragraphsIndent + 1


        } else if (paragraphsIndent > 1 && !isIncreased) {
            readerUseCases.paragraphIndentUseCase.save(paragraphsIndent - 1)
            this.paragraphsIndent = paragraphsIndent - 1
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            this.fontSize = this.fontSize + 1
            readerUseCases.fontSizeStateUseCase.save(prefState.fontSize)
        } else {
            if (prefState.fontSize > 0) {
                this.fontSize = this.fontSize - 1
                readerUseCases.fontSizeStateUseCase.save(prefState.fontSize)
            }
        }
    }

    private fun saveFont(index: Int) {
        this.font = fonts[index]
        readerUseCases.selectedFontStateUseCase.save(index)

    }

    private fun saveBrightness(brightness: Float, context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            this.brightness = brightness
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
            readerUseCases.brightnessStateUseCase.saveBrightness(brightness)
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

        val selectedChapter =
            state.stateChapters.indexOfFirst { it.id == chapter.id && it.title == chapter.title }
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

    fun hideSystemBars(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null && immersiveMode) {
            val window = activity.window
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            //  windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun showSystemBars(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


            // Hide both the status bar and the navigation bar
            //windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun restoreSetting(context: Context, scrollState: LazyListState) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            showSystemBars(context = context)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
            stateChapter?.let {
                activity.lifecycleScope.launch {
                    insertChapter(it.copy(progress = scrollState.firstVisibleItemScrollOffset))
                }
            }
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
        insertUseCases.insertBook(book)
    }

    suspend fun insertChapter(chapter: Chapter) {
        withIOContext {
            insertUseCases.insertChapter(chapter)
        }
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