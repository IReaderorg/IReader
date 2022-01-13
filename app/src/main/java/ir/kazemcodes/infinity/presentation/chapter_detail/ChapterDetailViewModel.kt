package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.util.Resource
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
) :  ScopedServices.Registered {

    private val _state = mutableStateOf<ChapterDetailState>(ChapterDetailState(source = source))
    val state: State<ChapterDetailState> = _state

    override fun onServiceRegistered() {
        _state.value = state.value.copy(book = book)
        getLocalChapters()
    }


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    reverseChapters = state.value.reverseChapters.reversed()
                )
            }
            is ChapterDetailEvent.UpdateChapters -> {
                _state.value = state.value.copy(reverseChapters = event.chapters)
            }

        }
    }

    private fun getLocalChapters() {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName = book.bookName)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data,
                                reverseChapters = result.data.reversed(),
                                isLoading = false,
                                error = "")
                        } else {
                            _state.value = state.value.copy(isLoading = false, error = "No Chapter")
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message
                                ?: "An Unknown Error Occurred", isLoading = false)
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }



    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }



}

