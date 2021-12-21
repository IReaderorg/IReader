package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach



class ChapterDetailViewModel(
    private val localUseCase: LocalUseCase,
    private val source: Source
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
    fun getSource() : Source {
        return source
    }

    fun getLocalChapters(bookName: String) {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName = bookName ).onEach { result ->

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

