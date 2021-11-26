package ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.GetLocalChaptersByBookNameUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLocalChaptersByBookNameUseCase: GetLocalChaptersByBookNameUseCase
) : ViewModel() {

    private val _state = mutableStateOf<ChapterDetailState>(ChapterDetailState())
    val state: State<ChapterDetailState> = _state

    init {
        savedStateHandle.get<Int>(Constants.PARAM_BOOK_ID)?.let { bookId ->
        //getLocalChapters(bookId = bookId)
        }
    }

    fun getLocalChapters(bookName: String) {
        getLocalChaptersByBookNameUseCase(bookName = bookName ).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    _state.value = ChapterDetailState(
                        chapters = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        ChapterDetailState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {
                    _state.value = ChapterDetailState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}

data class ChapterDetailState (
    val isLoading : Boolean = false,
    val chapters : List<Chapter> = emptyList(),
    val error: String = ""
)