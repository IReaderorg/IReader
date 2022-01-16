package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.core.utils.Resource
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

    private val _state = mutableStateOf(ChapterDetailState(source = source, book = book))
    val state: State<ChapterDetailState> = _state

    override fun onServiceRegistered() {
        getLocalChapters()
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    localChapters = state.value.localChapters.reversed(),
                    isReversed = !state.value.isReversed
                )
            }

        }
    }
    private fun getLocalChapters() {
        localUseCase.getLocalChaptersByBookNameUseCase(bookName = book.bookName, source = source.name )
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data,
                                localChapters = if (state.value.isReversed ) result.data.reversed() else result.data,
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

    fun getIndexOfChapter(index : Int) : Int {
        return if (state.value.isReversed) ((state.value.localChapters.size - 1 ) - index) else index
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }
}

