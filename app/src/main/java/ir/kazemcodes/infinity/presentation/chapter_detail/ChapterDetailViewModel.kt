package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter.GetLocalChaptersByBookNameUseCase
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


    fun onEvent(event: ChapterDetailEvent) {
        when(event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    chapters = state.value.chapters.asReversed()
                )
            }
            is ChapterDetailEvent.UpdateChapters -> {
                _state.value = state.value.copy(chapters=event.chapters)
            }
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
    val error: String = "",
    val chapterOrderType: OrderType = OrderType.Ascending
)