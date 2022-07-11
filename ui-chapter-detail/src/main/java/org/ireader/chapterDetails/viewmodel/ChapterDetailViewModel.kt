package org.ireader.chapterDetails.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.ireader.chapterDetails.ChapterSort
import org.ireader.chapterDetails.ChaptersFilters
import org.ireader.chapterDetails.parameter
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core.R
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.history.HistoryUseCase
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
    private val historyUseCase: HistoryUseCase,
    private val readerPreferences: ReaderPreferences,
    val uiPreferences: UiPreferences
) : BaseViewModel(), ChapterDetailState by state {
    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(ChapterSort.default)
    var layout by readerPreferences.showChapterNumberPreferences().asState()

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
            }
        }
    }

    fun getLastReadChapter(book: Book) {
        viewModelScope.launch {
            historyUseCase.subscribeHistoryByBookId(book.id).onEach {
                lastRead = it?.chapterId
            }.launchIn(viewModelScope)
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

    @Composable
    fun getChapters(book: Book): State<List<Chapter>> {
        val scope = rememberCoroutineScope()
        val unfiltered = remember(book.id, sorting.value, filters.value) {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId = book.id,
                sort = sorting.value.parameter,
            ).shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
        }

        return remember(state.query, book.id, sorting.value, filters.value) {
            val query = state.query
            if (query.isNullOrBlank()) {
                unfiltered
            } else {
                unfiltered.map { chapters ->
                    chapters.filter { it.name.contains(query, true) }
                }
            }.map { it.filteredWith(filters.value) }.onEach {
                state.chapters = it
            }
        }.collectAsState(emptyList())
    }

    private fun List<Chapter>.filteredWith(filters: List<ChaptersFilters>): List<Chapter> {
        if (filters.isEmpty()) return this
        val validFilters =
            filters.filter { it.value == ChaptersFilters.Value.Included || it.value == ChaptersFilters.Value.Excluded }
        var filteredList = this
        for (filter in validFilters) {
            val filterFn: (Chapter) -> Boolean = when (filter.type) {
                ChaptersFilters.Type.Unread -> {
                    {
                        !it.read
                    }
                }
                ChaptersFilters.Type.Bookmarked -> {
                    { book -> book.bookmark }
                }
                ChaptersFilters.Type.Downloaded -> {
                    {
                        it.content.joinToString("").isNotBlank()
                    }
                }
            }
            filteredList = when (filter.value) {
                ChaptersFilters.Value.Included -> filter(filterFn)
                ChaptersFilters.Value.Excluded -> filterNot(filterFn)
                ChaptersFilters.Value.Missing -> this
            }
        }

        return filteredList
    }

    fun autoSortChapterInDB() {
        val list = state.chapters.sortedWith(object : Comparator<Chapter> {
            override fun compare(o1: Chapter, o2: Chapter): Int {
                return extractInt(o1) - extractInt(o2)
            }

            fun extractInt(s: Chapter): Int {
                val num = s.name.replace("\\D".toRegex(), "")
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
                getLastReadChapter(book)
                // getLocalChaptersByPaging(isAsc = isAsc)
            }
        }
    }

//    private
//    var getChapterJob: Job? = null
//    fun getLocalChaptersByPaging(isAsc: Boolean = true) {
//        val book = state.book
//        getChapterJob?.cancel()
//        getChapterJob = viewModelScope.launch {
//            if (book != null) {
//                getChapterUseCase.subscribeChaptersByBookId(
//                    bookId = book.id,
//                    isAsc = isAsc,
//                ).collect { chapters ->
//                    this@ChapterDetailViewModel.chapters = chapters.distinctBy { it.id }
//                }
//            }
//        }
//    }

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

    fun toggleFilter(type: ChaptersFilters.Type) {
        val newFilters = filters.value
            .map { filterState ->
                if (type == filterState.type) {
                    ChaptersFilters(
                        type,
                        when (filterState.value) {
                            ChaptersFilters.Value.Included -> ChaptersFilters.Value.Excluded
                            ChaptersFilters.Value.Excluded -> ChaptersFilters.Value.Missing
                            ChaptersFilters.Value.Missing -> ChaptersFilters.Value.Included
                        }
                    )
                } else {
                    filterState
                }
            }
        this.filters.value = newFilters
    }

    fun toggleSort(type: ChapterSort.Type) {
        val currentSort = sorting
        sorting.value = if (type == currentSort.value.type) {
            currentSort.value.copy(isAscending = !currentSort.value.isAscending)
        } else {
            currentSort.value.copy(type = type)
        }
    }
}
