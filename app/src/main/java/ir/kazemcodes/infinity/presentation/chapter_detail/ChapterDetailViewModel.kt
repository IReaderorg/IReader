package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
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
    private val chapters: List<Chapter>,
    private val source: Source,
) : ScopedServices.Registered {

    private val _state = mutableStateOf<ChapterDetailState>(ChapterDetailState(chapters = chapters, listChapter = chapters))
    val state: State<ChapterDetailState> = _state

    init {
        _state.value = state.value.copy(book = book, chapters = chapters)
    }

    override fun onServiceRegistered() {

        getLocalChapters()
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    listChapter = state.value.listChapter.reversed()
                )
            }
            is ChapterDetailEvent.UpdateChapters -> {
                _state.value = state.value.copy(listChapter = event.chapters)
            }

        }
    }

    fun getSource(): Source {
        return source
    }

    private fun getLocalChapters() {
        localUseCase.getLocalChaptersByBookNameByBookNameUseCase(bookName = book.bookName)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data, listChapter = chapters, isLoading = false, error = "")
                        } else {
                            _state.value = state.value.copy(isLoading = false, error = "")
                        }
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message
                                ?: "An Unknown Error Occurred")
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(coroutineScope)
    }


    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}

