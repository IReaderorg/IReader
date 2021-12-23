package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.utils.Resource
import ir.kazemcodes.infinity.presentation.theme.fonts
import ir.kazemcodes.infinity.presentation.theme.poppins
import ir.kazemcodes.infinity.presentation.theme.sourceSansPro
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val source: Source,
    private val  chapter: Chapter,
    private val book: Book
) : ScopedServices.Registered{

    private val _state = mutableStateOf(ReaderScreenState())
    val state: State<ReaderScreenState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        getContent(chapter = chapter.copy(bookName = book.bookName))
    }

    fun onEvent(event: ReaderEvent) {
        when(event) {
            is ReaderEvent.ChangeBrightness -> {
                changeBrightness(event.brightness)
            }
            is ReaderEvent.ChangeFontSize -> {
                changeFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                changeFont(event.fontType)
            }
            is ReaderEvent.GetContent -> {
                getContent(event.chapter)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
            else -> {}
        }
    }

    private fun toggleReaderMode(enable : Boolean? =null) {
        _state.value = state.value.copy(isReaderModeEnable = enable?: !state.value.isReaderModeEnable)
    }



    private fun getContent(chapter: Chapter) {
        readSelectedFontState()
        readBrightness()
        readFontSize()
        _state.value = state.value.copy(chapter = chapter)
        if (chapter.content == null) {
            getReadingContentLocally()
        }
    }

    private fun getReadingContentLocally() {
        localUseCase.getLocalChapterReadingContentUseCase(state.value.chapter).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data?.content != null) {
                        Timber.d("getReadingContentLocally Copying" + _state.value)
                        _state.value = state.value.copy(
                            chapter = state.value.chapter.copy(content = result.data.content),
                            isLoading = false,
                            error = ""
                        )
                    } else {
                        if (state.value.chapter.content == null) {
                            getReadingContentRemotely()
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false
                        )
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }

        }.launchIn(coroutineScope)
    }


    private fun getReadingContentRemotely() {
        Timber.d("getReadingContentRemotely Successfully Triggered")
        remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter,source = source).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("getReadingContentRemotely Successfully Called")
                    _state.value = state.value
                        .copy(
                            chapter = state.value.chapter.copy(content = result.data),
                            isLoading = false,
                            error = ""
                        )
                    if (!state.value.chapter.content.isNullOrBlank()) {
                        Timber.d("insertChapterContent Successfully Called")
                        updateChapterContent(state.value.chapter)
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false
                        )
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(coroutineScope)
    }

    private fun updateChapterContent(chapter: Chapter) {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.UpdateLocalChapterContentUseCase(chapter.copy(haveBeenRead = true))
        }
    }
    private fun changeBrightness(brightness : Float) {
        _state.value = state.value.copy(brightness = brightness)
        coroutineScope.launch(Dispatchers.IO) {
            dataStoreUseCase.saveBrightnessStateUseCase(brightness)
        }
    }

    private fun changeFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            _state.value = state.value.copy(fontSize = state.value.fontSize+1)
            coroutineScope.launch(Dispatchers.IO) {
                dataStoreUseCase.saveFontSizeStateUseCase(state.value.fontSize)
            }
        } else {
            _state.value = state.value.copy(fontSize = state.value.fontSize-1)
            coroutineScope.launch(Dispatchers.IO) {
                dataStoreUseCase.saveFontSizeStateUseCase(state.value.fontSize)
            }
        }
    }

    private fun changeFont(fontType: FontType) {
        _state.value = state.value.copy(font = fontType)
        coroutineScope.launch(Dispatchers.IO) {
            dataStoreUseCase.saveSelectedFontStateUseCase(fonts.indexOf(fontType))
        }
    }


    private fun readSelectedFontState() {
        Timber.d("readSelectedFontState Successfully Triggered")
        dataStoreUseCase.readSelectedFontStateUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("readSelectedFontState Successfully Called")
                    _state.value = state.value
                        .copy(
                            font = (result.data?: FontType.Poppins)
                        )
                }
                is Resource.Error -> {
                    Timber.d("Timber: readSelectedFontState have a error : ${result.message?: ""}")
                }
                else -> {}
            }
        }.launchIn(coroutineScope)


    }
    private fun readBrightness() {
        Timber.d("readBrightness Successfully Triggered")
        dataStoreUseCase.readBrightnessStateUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("getFontFromDatastore Successfully Called")
                    _state.value = state.value
                        .copy(
                            brightness = result.data?: .8f
                        )
                }
                is Resource.Error -> {
                    Timber.d("Timber: readBrightness have a error : ${(result.message?: "")}")
                }
                else -> {}
            }
        }.launchIn(coroutineScope)
    }

    private fun readFontSize() {
        Timber.d("readFontSize Successfully Triggered")
        dataStoreUseCase.readFontSizeStateUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("readFontSize Successfully Called")
                    _state.value = state.value
                        .copy(
                            fontSize = result.data?: 18
                        )
                }
                is Resource.Error -> {
                    Timber.d("Timber: readFontSize have a error : ${(result.message?: "")}")
                }
                else -> {}
            }
        }.launchIn(coroutineScope)
    }



    private fun convertStringToFont(font: String?): FontFamily {
        return if (font == poppins.toString()) {
            poppins
        } else if (font == sourceSansPro.toString()) {
            sourceSansPro
        } else {
            poppins
        }
    }

    fun convertFontIntoString(fontFamily: FontFamily): String {
        return if (fontFamily == poppins) {
            "Poppins"
        } else if (fontFamily == sourceSansPro) {
            "Source Sans Pro"
        } else {
            "Unknown"
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}