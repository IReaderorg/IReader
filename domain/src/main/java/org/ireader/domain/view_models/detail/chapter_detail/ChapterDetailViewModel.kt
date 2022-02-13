package org.ireader.domain.view_models.detail.chapter_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.source.Extensions
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    extensions: Extensions,
) : ViewModel() {

    var state by mutableStateOf(ChapterDetailState(source = extensions.mappingSourceNameToSource(0)))
        private set

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && sourceId != null) {
            state = state.copy(source = extensions.mappingSourceNameToSource(sourceId))
            state = state.copy(book = state.book.copy(id = bookId))
            getLocalBookById(bookId)
        }
    }

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                state = state.copy(
                    localChapters = state.localChapters.reversed(),
                    isAsc = !state.isAsc
                )
                getLocalChaptersByPaging(bookId = state.book.id, isAsc = state.isAsc)
            }

        }
    }

    fun reverseChapterInDB() {
        state = state.copy(
            isAsc = !state.isAsc)
        getLocalChaptersByPaging(bookId = state.book.id, isAsc = state.isAsc)
        /**
         * this line insert a book with a field ofu areChapterReversed
         *  true
         */
        insertBook(state.book)

    }

    fun getLocalBookById(id: Long) {
        viewModelScope.launch {
            getBookUseCases.getBookById(id = id).first { book ->
                if (book != null) {
                    state = state.copy(
                        book = book,
                    )
                    getLocalChaptersByPaging(bookId = state.book.id, isAsc = state.isAsc)
                    true
                } else {
                    false
                }
            }
        }

    }

    private var getChapterJob: Job? = null
    private fun getLocalChaptersByPaging(bookId: Long, isAsc: Boolean) {
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(
                bookId = bookId,
                isAsc = isAsc
            )
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _chapters.value = snapshot
                }
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }
}

