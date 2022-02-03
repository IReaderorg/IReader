package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.ui.NavigationArgs
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getBookUseCases: LocalGetBookUseCases,
    extensions: Extensions,
) : ViewModel() {

    private val _state =
        mutableStateOf(ChapterDetailState(source = extensions.mappingSourceNameToSource(0)))
    val state: State<ChapterDetailState> = _state

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        if (bookId != null && sourceId != null) {
            _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(sourceId))
            _state.value = state.value.copy(book = state.value.book.copy(id = bookId))
            getLocalBookById()
        }
    }

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

    fun reverseChapterInDB() {
        _state.value =
            state.value.copy(book = state.value.book.copy(areChaptersReversed = !state.value.book.areChaptersReversed), isAsc = !state.value.isAsc)
        getLocalChaptersByPaging()
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(state.value.book)
        }
    }

    fun getLocalBookById() {
        viewModelScope.launch {
            getBookUseCases.getBookById(id = state.value.book.id).first { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && result.data != Book.create()) {
                            _state.value = state.value.copy(
                                book = result.data,
                                isAsc = result.data.areChaptersReversed
                            )
                            getLocalChaptersByPaging()
                            true
                        } else {
                            false
                        }
                    }
                    is Resource.Error -> {
                        false
                    }
                }
            }
        }

    }
    private var getChapterJob : Job? = null
    private fun getLocalChaptersByPaging() {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = state.value.book.id,
                isAsc = state.value.isAsc)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _chapters.value = snapshot
                }
        }
    }


}

