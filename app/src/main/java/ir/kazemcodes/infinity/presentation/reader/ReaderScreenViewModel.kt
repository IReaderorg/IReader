package ir.kazemcodes.infinity.presentation.reader

import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.theme.fonts
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
    private val source: Source,
    private val chapter: Chapter,
    private val book: Book,
    private val chapters: List<Chapter>,
    private val window: Window,
) : ScopedServices.Registered {

    private val _state = mutableStateOf(ReaderScreenState(source = source))
    val state: State<ReaderScreenState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        getContent(chapter = chapter.copy(bookName = book.bookName))

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
            state.value.copy(isReaderModeEnable = enable ?: !state.value.isReaderModeEnable)
    }

    private fun getContent(chapter: Chapter) {
        _state.value = state.value.copy(chapter = chapter)
        getReadingContentLocally()
        readFontSize()
        if (book.inLibrary) {
            toggleLastReadAndUpdateChapterContent()
        }
        readSelectedFontState()
        readBrightness()
    }

    private fun getReadingContentLocally() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.getLocalChapterReadingContentUseCase(state.value.chapter)
                .collect { result ->
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
                                    toggleLastReadAndUpdateChapterContent()
                                }
                            } else {
                                if (!state.value.chapter.isChapterNotEmpty()) {
                                    getReadingContentRemotely()
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

                }
        }
    }

    fun getReadingContentRemotely() {
        remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value
                            .copy(
                                chapter = state.value.chapter.copy(content = result.data?.content
                                    ?: emptyList()),
                                isLoading = false,
                                error = "",
                                isLoaded = true,
                            )
                        if (book.inLibrary) {
                            toggleLastReadAndUpdateChapterContent()
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


    private fun toggleLastReadAndUpdateChapterContent() {
        coroutineScope.launch(Dispatchers.IO) {
            chapters.filter {
                it.lastRead
            }.forEach {
                localUseCase.UpdateLocalChapterContentUseCase(it.copy(lastRead = false))
            }
            localUseCase.UpdateLocalChapterContentUseCase(chapter.copy(lastRead = true,
                content = state.value.chapter.content,
                haveBeenRead = true))
        }
    }

    private fun saveBrightness(brightness: Float) {
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

            preferencesUseCase.saveBrightnessStateUseCase(brightness)

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


    private fun readFontSize() {
        _state.value = state.value.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()

    }
}

