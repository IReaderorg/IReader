package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.domain.use_case.GetReadingContentUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getReadingContentUseCase: GetReadingContentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _state = mutableStateOf<ReadingScreenState>(ReadingScreenState())
    val state: State<ReadingScreenState> = _state


    init {
        savedStateHandle.get<String>("url").let { url->

            getReadingContent(url = url?:"" , headers = mutableMapOf(
                Pair<String, String>("Referer","https://readwebnovels.net/")
            ))

        }

        //_state.value = BrowseScreenState(books = BookTest.booksTest)
    }


    private fun getReadingContent(url: String , headers : Map<String,String>) {


        getReadingContentUseCase(url , headers ).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    _state.value = ReadingScreenState(
                        readingContent = result.data ?: ""
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        ReadingScreenState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {

                    _state.value = ReadingScreenState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}