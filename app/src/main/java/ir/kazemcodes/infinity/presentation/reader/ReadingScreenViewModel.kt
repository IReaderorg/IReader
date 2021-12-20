package ir.kazemcodes.infinity.presentation.reader

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.domain.network.apis.FreeWebNovel
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.api_feature.network.dataStore
import ir.kazemcodes.infinity.presentation.theme.poppins
import ir.kazemcodes.infinity.presentation.theme.sourceSansPro
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_BRIGHTNESS_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_SIZE_PREFERENCES
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val remoteUseCase: RemoteUseCase,
    private val localUseCase: LocalUseCase,
    private val application: Application,
) : ViewModel() {

    private val _state = mutableStateOf(ReadingScreenState())
    val state: State<ReadingScreenState> = _state
    private val _fontSize = mutableStateOf(18)
    val fontSize = _fontSize
    private val _fontState = mutableStateOf(poppins)
    val fontState = _fontState
    private val fontSizeDataStore = intPreferencesKey(SAVED_FONT_SIZE_PREFERENCES)
    private val fontDatastore = stringPreferencesKey(SAVED_FONT_PREFERENCES)
    private val brightnessDatastore = floatPreferencesKey(SAVED_BRIGHTNESS_PREFERENCES)
    private val _brightness = mutableStateOf(0.5f)
    val brightness = _brightness
    private val _api = mutableStateOf<ParsedHttpSource>(FreeWebNovel())
    val api = _api.value

    fun getReadingContent(chapter: Chapter) {
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

        }.launchIn(viewModelScope)
    }


    private fun getReadingContentRemotely() {
        Timber.d("getReadingContentRemotely Successfully Triggered")
        remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter,api).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("getReadingContentRemotely Successfully Called")
                    Timber.d("TAG "+ result.data)
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
        }.launchIn(viewModelScope)


    }

    private fun updateChapterContent(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.UpdateLocalChapterContentUseCase(chapter)
        }
    }

    fun changeBrightness(brightness : Float) {
        _brightness.value = brightness
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit { preferences ->
                val currentFontSize = preferences[fontSizeDataStore] ?: 18
                preferences[brightnessDatastore] = brightness
            }
        }
    }

    fun increaseFontsSize() {
        fontSize.value++
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit { preferences ->
                val currentFontSize = preferences[fontSizeDataStore] ?: 18
                preferences[fontSizeDataStore] = currentFontSize + 1
            }
        }
    }

    fun decreaseFontSize() {
        fontSize.value--
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit { preferences ->
                val currentFontSize = preferences[fontSizeDataStore] ?: 18
                preferences[fontSizeDataStore] = currentFontSize - 1
            }
        }

    }

    fun readFromDatastore() {
        viewModelScope.launch(Dispatchers.IO) {
            getFontSizeFromDatastore().collectLatest {
                fontSize.value = it

            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getFontFromDatastore().collectLatest {
                    fontState.value = convertStringToFont(it?: poppins.toString())
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getBrightnessFromDatastore().collectLatest {
                    _brightness.value = it
            }
        }
    }


    fun getFontFromDatastore(): Flow<String> {
        return application.dataStore.data
            .map { preferences ->
                preferences[fontDatastore] ?: poppins.toString()
            }
    }
    fun getBrightnessFromDatastore(): Flow<Float> {
        return application.dataStore.data
            .map { preferences ->
                preferences[brightnessDatastore] ?: 1f
            }
    }

    fun getFontSizeFromDatastore(): Flow<Int> {
        return application.dataStore.data
            .map { preferences ->
                preferences[fontSizeDataStore] ?: 18
            }
    }


    fun setFont(font: FontFamily) {
        _fontState.value = font
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit { preferences ->
                preferences[fontDatastore] = fontState.value.toString()
            }
        }

    }

    fun convertStringToFont(font: String?): FontFamily {
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

}