package org.ireader.domain.view_models.detail.chapter_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
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
    private val state: ChapterDetailStateImpl,
) : ViewModel(), ChapterDetailState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && sourceId != null) {
            getLocalBookById(bookId)
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                this.localChapters = this.localChapters.reversed()
                toggleAsc()
                book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
            }

        }
    }

    fun reverseChapterInDB() {
        toggleAsc()
        book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapters(stateChapters.reversed())
        }
    }

    fun toggleAsc() {
        this.isAsc = !this.isAsc
    }

    fun getLocalBookById(id: Long) {
        viewModelScope.launch {
            getBookUseCases.subscribeBookById(id = id).first { book ->
                if (book != null) {
                    this@ChapterDetailViewModel.book = book
                    getLocalChaptersByPaging(isAsc = isAsc)
                    true
                } else {
                    false
                }
            }
        }

    }

    private var getChapterJob: Job? = null
    fun getLocalChaptersByPaging(isAsc: Boolean = true) {
        val book = state.book
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            if (book != null) {
                getChapterUseCase.getLocalChaptersByPaging(
                    bookId = book.id,
                    isAsc = isAsc,
                    query = query
                )
                    .cachedIn(viewModelScope)
                    .collect { snapshot ->
                        _chapters.value = snapshot
                    }

            }

        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(R.string.error_unknown)
            )
        )
    }
}

