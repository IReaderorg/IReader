package org.ireader.chapterDetails.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import javax.inject.Inject

@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val state: ChapterDetailStateImpl,
    private val serviceUseCases: ServiceUseCases,
) : BaseViewModel(), ChapterDetailState by state {

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && sourceId != null) {
            viewModelScope.launch {
                getLocalBookById(bookId)
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                this.chapters = this.chapters.reversed()
                toggleAsc()
                book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
            }
        }
    }

    fun getLastReadChapter(book: Book) {
        viewModelScope.launch {
            lastRead = getChapterUseCase.findLastReadChapter(book.id)?.id
        }
    }

    fun getLastChapterIndex(): Int {
        return when (val index = chapters.indexOfFirst { it.id == lastRead }) {
            -1 -> {
                throw Exception("chapter not found")
            }
            else -> {
                index
            }
        }
    }

    fun autoSortChapterInDB() {
        val list = state.chapters.sortedWith(object : Comparator<Chapter> {
            override fun compare(o1: Chapter, o2: Chapter): Int {
                return extractInt(o1) - extractInt(o2)
            }

            fun extractInt(s: Chapter): Int {
                val num = s.title.replace("\\D".toRegex(), "")
                // return 0 if no digits found
                return if (num.isEmpty()) 0 else Integer.parseInt(num)
            }
        }
        )
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(list)
            insertUseCases.insertChapters(list.map { it.copy(id = 0) })
        }
    }

    fun reverseChapterInDB() {
        toggleAsc()
       // book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
            insertUseCases.insertChapters(chapters.reversed().map { it.copy(id = 0) })
        }
    }

    fun toggleAsc() {
        this.isAsc = !this.isAsc
    }

    private suspend fun getLocalBookById(id: Long) {
        viewModelScope.launch {
            val book = getBookUseCases.findBookById(id = id)
            if (book != null) {
                this@ChapterDetailViewModel.book = book
                getLocalChaptersByPaging(isAsc = isAsc)
                getLastReadChapter(book)
            }
        }
    }

    private
    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging(isAsc: Boolean = true) {
        val book = state.book
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            if (book != null) {
//                getChapterUseCase.getLocalChaptersByPaging(
//                    bookId = book.id,
//                    isAsc = isAsc,
//                    query = query
//                )
//                    .cachedIn(viewModelScope)
//                    .collect { snapshot ->
//                        _chapters.value = snapshot
//                    }
                getChapterUseCase.subscribeChaptersByBookId(
                    bookId = book.id,
                    isAsc = isAsc,
                    query = query
                ).collect { chapters ->
                    this@ChapterDetailViewModel.chapters = chapters.distinctBy { it.id }
                }
            }
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertChapters(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapters(chapters)
        }
    }

    fun deleteChapters(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
        }
    }

    fun downloadChapters() {
        book?.let { book ->
            serviceUseCases.startDownloadServicesUseCase(chapterIds = this@ChapterDetailViewModel.selection.toLongArray())
        }
    }
}
