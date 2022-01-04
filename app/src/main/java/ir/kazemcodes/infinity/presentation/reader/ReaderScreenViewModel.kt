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
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.theme.fonts
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber


class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
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
        readSelectedFontState()
        readBrightness()
        readFontSize()
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
                                if (!state.value.chapter.isChapterNotEmpty() ) {
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

    private fun getReadingContentRemotely() {
        coroutineScope.launch(Dispatchers.IO) {
            remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter, source = source)
                .collect { result ->
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
                                updateChapterContent(state.value.chapter)
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

    private fun updateChapterContent(chapter: Chapter) {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                localUseCase.UpdateLocalChapterContentUseCase(chapter.copy(haveBeenRead = true))

            }
        }
    }

    private fun toggleLastReadAndUpdateChapterContent() {
        coroutineScope.launch(Dispatchers.IO) {
            chapters.filter {
                it.lastRead
            }.forEach {
                localUseCase.UpdateLocalChapterContentUseCase(it.copy(lastRead = false))
            }
            localUseCase.UpdateLocalChapterContentUseCase(chapter.copy(lastRead = true,content = state.value.chapter.content))
        }
    }

    private fun saveBrightness(brightness: Float) {
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        coroutineScope.launch(Dispatchers.IO) {
            dataStoreUseCase.saveBrightnessStateUseCase(brightness)
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            _state.value = state.value.copy(fontSize = state.value.fontSize + 1)
            coroutineScope.launch(Dispatchers.Main) {
                dataStoreUseCase.saveFontSizeStateUseCase(state.value.fontSize)
            }
        } else {
            if (state.value.fontSize > 0) {
                _state.value = state.value.copy(fontSize = state.value.fontSize - 1)
                coroutineScope.launch(Dispatchers.Main) {
                    dataStoreUseCase.saveFontSizeStateUseCase(state.value.fontSize)
                }
            }
        }
    }

    private fun saveFont(fontType: FontType) {
        _state.value = state.value.copy(font = fontType)
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.saveSelectedFontStateUseCase(fonts.indexOf(fontType))
        }
    }


    private fun readSelectedFontState() {
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.readSelectedFontStateUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {

                        _state.value = state.value
                            .copy(
                                font = (result.data ?: FontType.Poppins)
                            )
                    }
                    is Resource.Error -> {
                        Timber.d("Timber: readSelectedFontState have a error : ${result.message ?: ""}")
                    }
                    else -> {
                    }
                }
            }
        }


    }

    private fun readBrightness() {

        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.readBrightnessStateUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {

                        _state.value = state.value
                            .copy(
                                brightness = result.data ?: .8f
                            )
                    }
                    is Resource.Error -> {
                        Timber.d("Timber: readBrightness have a error : ${(result.message ?: "")}")
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun readFontSize() {
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.readFontSizeStateUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value
                            .copy(
                                fontSize = result.data ?: 18
                            )
                    }
                    is Resource.Error -> {
                        Timber.d("Timber: readFontSize have a error : ${(result.message ?: "")}")
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()

    }
}

