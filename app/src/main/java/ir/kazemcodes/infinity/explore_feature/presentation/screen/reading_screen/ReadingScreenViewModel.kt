package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.domain.use_case.RemoteUseCase
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val remoteUseCase: RemoteUseCase,
    private val localUseCase: LocalUseCase
) : ViewModel() {


    private val _state = mutableStateOf(ReadingScreenState())
    val state: State<ReadingScreenState> = _state


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
                        if (state.value.chapter.content ==null) {

                        getReadingContentRemotely()
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(error = result.message ?: "An Unknown Error Occurred", isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true ,error = "")
                }
            }

        }.launchIn(viewModelScope)
    }


    private fun getReadingContentRemotely() {
        Timber.d("getReadingContentRemotely Successfully Triggered")
        remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter.link).onEach { result ->
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
                        state.value.copy(error = result.message ?: "An Unknown Error Occurred", isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true , error = "")
                }
            }
        }.launchIn(viewModelScope)


    }

    private fun updateChapterContent(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.UpdateLocalChapterContentUseCase(chapter)
        }
    }

}