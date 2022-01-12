package ir.kazemcodes.infinity.presentation.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
import ir.kazemcodes.infinity.util.findActivity
import ir.kazemcodes.infinity.util.isNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach





class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
    private val source: Source,
    private val book: Book,
    private val chapter: Chapter,

) : ScopedServices.Registered {
    private val _state = mutableStateOf(ReaderScreenState(source = source, book = book, chapter = chapter))

    val state: State<ReaderScreenState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        readPreferences()
        updateState()
        getContent(chapter = state.value.chapter.copy(bookName = book.bookName))
        _state.value = state.value.copy(isScreenLoaded = true)
        getLocalChapters()
    }

    private fun updateState() {
        _state.value = state.value.copy(
            currentChapterIndex = if (state.value.chapters.isNullOrEmpty() && state.value.chapters.indexOf(state.value.chapter) > 0) state.value.chapters.indexOf(
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

    fun readPreferences() {
        readSelectedFontState()
        readFontSize()
        readBackgroundColor()
        readFontHeight()
        readParagraphDistance()
    }

    fun onEvent(event: ReaderEvent) {
        when (event) {
            is ReaderEvent.ChangeBrightness -> {
                saveBrightness(event.brightness,event.context)
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
            currentChapterIndex = if (state.value.chapters.indexOf(chapter) > 0) state.value.chapters.indexOf(chapter) else 0)
        getReadingContentLocally(chapter)
        if (book.inLibrary) {
            toggleLastReadAndUpdateChapterContent(chapter)
        }
    }

    private fun getLocalChapters() {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName = book.bookName)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data, listChapters = result.data, isLoading = false, error = "")
                        } else {
                            _state.value = state.value.copy(isLoading = false, error = "")
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message
                                ?: "An Unknown Error Occurred")
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(coroutineScope)
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
            state.value.chapters.filter {
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
        _state.value = state.value.copy(listChapters = state.value.listChapters.reversed())
    }







    private fun readSelectedFontState() {
        _state.value = state.value.copy(font = preferencesUseCase.readSelectedFontStateUseCase())


    }

    fun readBrightness(context: Context) {
        val brightness = preferencesUseCase.readBrightnessStateUseCase()
        val activity = context.findActivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        _state.value = state.value.copy(brightness = brightness)
    }
    private fun readFontSize() {
        _state.value = state.value.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    fun readParagraphDistance() {
        _state.value =
            state.value.copy(distanceBetweenParagraphs = preferencesUseCase.readParagraphDistanceUseCase())
    }

    fun readFontHeight() {
        _state.value = state.value.copy(lineHeight = preferencesUseCase.readFontHeightUseCase())
    }

    private fun readBackgroundColor() {
        _state.value =
            state.value.copy(backgroundColor = readerScreenBackgroundColors[preferencesUseCase.getBackgroundColorUseCase()])
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findActivity()!!
        when(preferencesUseCase.readOrientationUseCase()) {
             1 -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
            }
             0 -> {
                activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
                _state.value = state.value.copy(orientation = Orientation.Landscape)

            }
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findActivity()!!
        when(state.value.orientation) {
            is Orientation.Landscape -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
                preferencesUseCase.saveOrientationUseCase(Orientation.Portrait.index)
            }
            is Orientation.Portrait -> {
                activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
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

        } else if (currentDistance > 0 && !isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance - 1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance - 1)
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
    private fun saveBrightness(brightness: Float,context: Context) {
        val activity = context.findActivity()!!
        val window = activity.window
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

        preferencesUseCase.saveBrightnessStateUseCase(brightness)
    }


    fun updateChapterSliderIndex(index: Int) {
        _state.value = state.value.copy(currentChapterIndex = index)
    }



    override fun onServiceUnregistered() {
        coroutineScope.cancel()

    }
}

