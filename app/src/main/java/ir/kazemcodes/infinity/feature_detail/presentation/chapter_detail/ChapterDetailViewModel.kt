package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ChapterDetailViewModel(
    private val bookId: Int,
    private val source: Source,
    private val getChapterUseCase: LocalGetChapterUseCase,
) : ScopedServices.Registered {

    private val _state = mutableStateOf(ChapterDetailState(source = source, book = Book.create().copy(id = bookId)))
    val state: State<ChapterDetailState> = _state

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    override fun onServiceRegistered() {
        getLocalChapters()
        getLocalChaptersByPaging()
    }


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                _state.value = state.value.copy(
                    localChapters = state.value.localChapters.reversed(),
                    isAsc = !state.value.isAsc
                )
                getLocalChaptersByPaging()
            }

        }
    }
    private fun getLocalChaptersByPaging() {
        coroutineScope.launch(Dispatchers.IO) {
            getChapterUseCase.getLocalChaptersByPaging(bookId = bookId,isAsc=state.value.isAsc).cachedIn(coroutineScope)
                .collect { snapshot ->
                    _chapters.value = snapshot
                }
        }
    }
    private fun getLocalChapters() {
        getChapterUseCase.getChaptersByBookId(bookId = bookId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value.copy(
                                chapters = result.data)
                        }
                    }
                    is Resource.Error -> {

                    }
                    is Resource.Loading -> {
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun getIndexOfChapter(chapter: Chapter): Int {
        val ch = state.value.chapters.indexOf(chapter)
        return if (ch != -1) ch else 0
    }


        override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }
}

