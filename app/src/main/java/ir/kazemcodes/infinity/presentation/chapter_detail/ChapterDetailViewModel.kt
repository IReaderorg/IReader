package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach



class ChapterDetailViewModel(
    private val localUseCase: LocalUseCase,
    private val book: Book,
    private val source: Source,
) : ScopedServices.Registered {

    private val _state = mutableStateOf<ChapterDetailState>(ChapterDetailState())
    val state: State<ChapterDetailState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    private fun getLocalChapters(bookName: String) {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName = bookName ).onEach { result ->

            when (result) {
                is Resource.Success -> {
                    if (!result.data.isNullOrEmpty()) {
                        _state.value = ChapterDetailState(
                            chapters = result.data
                        )
                    }

                }
                is Resource.Error -> {
                    _state.value =
                        ChapterDetailState(error = result.message ?: "An Unknown Error Occurred")
                }
                is Resource.Loading -> {
                    _state.value = ChapterDetailState(isLoading = true)
                }
            }
        }.launchIn(coroutineScope)
    }

    override fun onServiceRegistered() {
        getLocalChapters(bookName = book.bookName)
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}

