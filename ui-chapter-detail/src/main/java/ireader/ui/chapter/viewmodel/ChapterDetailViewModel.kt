package ireader.ui.chapter.viewmodel

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import ireader.ui.chapter.ChapterSort
import ireader.ui.chapter.ChaptersFilters
import ireader.ui.chapter.parameter
import ireader.common.models.entities.Book
import ireader.common.models.entities.Chapter
import ireader.common.resources.UiText

import ireader.core.ui.preferences.ReaderPreferences
import ireader.core.ui.preferences.UiPreferences
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.ui.NavigationArgs
import ireader.domain.use_cases.history.HistoryUseCase
import ireader.domain.use_cases.local.DeleteUseCase
import ireader.domain.use_cases.local.LocalGetChapterUseCase
import ireader.domain.use_cases.local.LocalInsertUseCases
import ireader.domain.use_cases.services.ServiceUseCases
import ireader.ui.chapter.R
import ireader.ui.component.Controller
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChapterDetailViewModel(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,

    private val getBookUseCases: ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val state: ChapterDetailStateImpl,
    private val serviceUseCases: ServiceUseCases,
    private val historyUseCase: HistoryUseCase,
    private val readerPreferences: ReaderPreferences,
    val uiPreferences: UiPreferences,
    private val param: Param,
) : BaseViewModel(), ChapterDetailState by state {
    data class Param(val sourceId: Long? , val bookId: Long?)

    companion object  {
        fun createParam(controller: Controller) : Param {
            return Param(controller.navBackStackEntry.arguments?.getLong("bookId"),controller.navBackStackEntry.arguments?.getLong("sourceId"))
        }
    }

    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(ChapterSort.default)
    var layout by readerPreferences.showChapterNumberPreferences().asState()

    init {
        val sourceId = param.sourceId
        val bookId = param.bookId
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
