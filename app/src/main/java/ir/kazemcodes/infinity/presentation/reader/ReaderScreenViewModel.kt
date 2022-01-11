package ir.kazemcodes.infinity.presentation.reader

import android.content.pm.ActivityInfo
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.theme.fonts
import ir.kazemcodes.infinity.presentation.theme.readerScreenBackgroundColors
import ir.kazemcodes.infinity.util.Resource
import ir.kazemcodes.infinity.util.isNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
    private val source: Source,
    private val chapterIndex: Int,
    private val book: Book,
    private val chapters: List<Chapter>,
    private val window: Window,
    private val isChaptersReversed: Boolean,
) : ScopedServices.Registered {

    private val _state = mutableStateOf(ReaderScreenState(source = source, book = book, chapters = chapters, chapter = chapters[chapterIndex], isChaptersReversed = isChaptersReversed))
    val state: State<ReaderScreenState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        readPreferences()
        updateState()
        if (chapters.isNotEmpty()) {
            _state.value = state.value.copy(chapter = chapters[chapterIndex])
        }
        getContent(chapter = state.value.chapter.copy(bookName = book.bookName))
    }

    private fun updateState() {
        _state.value = state.value.copy(chapters = chapters,
            currentChapterIndex = if (chapters.isNullOrEmpty() && chapters.indexOf(state.value.chapter) > 0) chapters.indexOf(
                state.value.chapter) else 0,
            textColor = if (state.value.backgroundColor == Color.Black && state.value.textColor == Color.Black) Color.White else Color.Black)
    }

    fun changeBackgroundColor(colorIndex: Int) {
        val color = readerScreenBackgroundColors[colorIndex]
        _state.value = state.value.copy(backgroundColor = color,
            textColor = if (color == Color.Black) Color.White else Color.Black,
            isDarkThemeEnabled = false)
        preferencesUseCase.setBackgroundColorUseCase(colorIndex)
        if (color == Color.Black) {
            _state.value = state.value.copy(isDarkThemeEnabled = true)
        }
    }

    private fun readPreferences() {
        readSelectedFontState()
        readBrightness()
        readFontSize()
        getBackgroundColor()
        readFontHeight()
        readParagraphDistance()
    }

    fun onEvent(event: ReaderEvent) {
        when (event) {
            is ReaderEvent.ChangeBrightness -> {
                saveBrightness(event.brightness)
            }
            is ReaderEvent.ChangeFontSize -> {
                saveFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                saveFont(event.fontType)
            }
            is ReaderEvent.GetContent -> {
                getContent(event.chapter)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
            else -> {
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

    fun getContent(chapter: Chapter) {
        _state.value = state.value.copy(chapter = chapter,
            currentChapterIndex = if (chapters.indexOf(chapter) > 0) chapters.indexOf(chapter) else 0)
        getReadingContentLocally(chapter)
        if (book.inLibrary) {
            toggleLastReadAndUpdateChapterContent(chapter)
        }
    }

    private fun getReadingContentLocally(chapter: Chapter) {
        localUseCase.getLocalChapterReadingContentUseCase(chapter)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && result.data.isChapterNotEmpty()) {
                            _state.value = state.value.copy(
                                chapter = chapter.copy(content = result.data.content),
                                isLoading = false,
                                isLoaded = true,
                                error = ""
                            )
                            if (book.inLibrary) {
                                toggleLastReadAndUpdateChapterContent(state.value.chapter)
                            }
                        } else {
                            if (!state.value.chapter.isChapterNotEmpty()) {
                                getReadingContentRemotely(chapter)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                isLoaded = false,
                            )
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            isLoading = true, error = "",
                            isLoaded = false,
                        )
                    }
                }

            }.launchIn(coroutineScope)
    }

    fun getReadingContentRemotely(chapter: Chapter) {
        remoteUseCase.getRemoteReadingContentUseCase(chapter, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value
                            .copy(
                                chapter = chapter.copy(content = result.data?.content
                                    ?: emptyList()),
                                isLoading = false,
                                error = "",
                                isLoaded = true,
                            )
                        if (book.inLibrary) {
                            toggleLastReadAndUpdateChapterContent(state.value.chapter)
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                isLoaded = false,
                            )
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            isLoading = true, error = "",
                            isLoaded = false,
                        )
                    }
                }
            }.launchIn(coroutineScope)

    }


    private fun toggleLastReadAndUpdateChapterContent(chapter: Chapter) {
        coroutineScope.launch(Dispatchers.Main) {
            chapters.filter {
                it.lastRead
            }.forEach {
                localUseCase.UpdateLocalChapterContentUseCase(it.copy(lastRead = false))
            }
            localUseCase.UpdateLocalChapterContentUseCase(chapter.copy(lastRead = true,
                content = chapter.content,
                haveBeenRead = true))
        }
    }
    fun reverseChapters() {
        _state.value = state.value.copy(chapters = state.value.chapters.reversed(), isChaptersReversed = !state.value.isChaptersReversed)
    }

    private fun saveBrightness(brightness: Float) {
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

        preferencesUseCase.saveBrightnessStateUseCase(brightness)


    }
    fun saveOrientation() {
        val layoutParams: WindowManager.LayoutParams = window.attributes
        val orientation = layoutParams.screenOrientation
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.attributes = layoutParams
//        if(orientation ==Configuration.ORIENTATION_LANDSCAPE ) {
//            layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }else if(orientation ==Configuration.ORIENTATION_PORTRAIT ) {
//            layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        } else {
//            layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        }
//        _state.value = state.value.copy(orientation = orientation)
//
//        window.attributes = layoutParams
//
//        preferencesUseCase.saveOrientationUseCase(orientation)
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


    private fun readSelectedFontState() {
        _state.value = state.value.copy(font = preferencesUseCase.readSelectedFontStateUseCase())


    }

    private fun readBrightness() {
        _state.value =
            state.value.copy(brightness = preferencesUseCase.readBrightnessStateUseCase())

    }

    private fun getBackgroundColor() {
        _state.value =
            state.value.copy(backgroundColor = readerScreenBackgroundColors[preferencesUseCase.getBackgroundColorUseCase()])

    }


    private fun readFontSize() {
        _state.value = state.value.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    fun readParagraphDistance() {
        _state.value = state.value.copy(distanceBetweenParagraphs = preferencesUseCase.readParagraphDistanceUseCase())
    }
    fun readFontHeight() {
        _state.value = state.value.copy(lineHeight = preferencesUseCase.readFontHeightUseCase())
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = state.value.lineHeight
        if (isIncreased)  {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight+1)
            _state.value = state.value.copy(lineHeight = currentFontHeight+1)

        } else if (currentFontHeight > 20 && !isIncreased){
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight-1)
            _state.value = state.value.copy(lineHeight = currentFontHeight-1)
        }
    }
    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = state.value.distanceBetweenParagraphs
        if (isIncreased)  {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance+1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance+1)

        } else if (currentDistance > 0 && !isIncreased){
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance-1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance-1)
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()

    }

    fun updateChapterSliderIndex(index: Int) {
        _state.value = state.value.copy(currentChapterIndex = index)
    }
}

