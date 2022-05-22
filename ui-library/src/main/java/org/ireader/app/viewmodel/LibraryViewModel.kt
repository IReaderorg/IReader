package org.ireader.app.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.entities.BookCategory
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.LibraryBook
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import org.ireader.core_ui.preferences.LibraryPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.category.GetCategories
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.book_usecases.GetLibraryCategory
import org.ireader.domain.use_cases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val libraryScreenPrefUseCases: LibraryScreenPrefUseCases,
    private val libraryState: LibraryStateImpl,
    private val serviceUseCases: ServiceUseCases,
    private val getLibraryCategory: GetLibraryCategory,
    private val libraryPreferences: LibraryPreferences,
    val getCategory: GetCategories
) : BaseViewModel(), LibraryState by libraryState {

    var lastUsedCategory by libraryPreferences.lastUsedCategory().asState()
    var filters by libraryPreferences.filters(true).asState()

    var sorting by libraryPreferences.sorting().asState()
    val showCategoryTabs by libraryPreferences.showCategoryTabs().asState()
    val showCountInCategory by libraryPreferences.showCountInCategory().asState()
    val categories by getCategory.subscribe().asState(emptyList())
    val uiCategories by derivedStateOf { listOf(Category.allCategory) + categories }
    val bookCategories by getCategory.subscribeBookCategories().asState(emptyList())
    val deleteQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    val addQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    var showDialog: Boolean by mutableStateOf(false)

    init {
        readLayoutTypeAndFilterTypeAndSortType()
        getBooks()
    }
    private val loadedManga = mutableMapOf<Long, List<LibraryBook>>()
    private var getBooksJob: Job? = null
//    fun getLibraryBooks() {
//        getBooksJob?.cancel()
//        getBooksJob = localGetBookUseCases.SubscribeInLibraryBooks(
//                sortType,
//                desc = desc,
//                filters
//            ).onEach { list ->
//                books = list.filter { it.title.contains(searchQuery, true) }
//            }.launchIn(scope)
//    }

    @OptIn(ExperimentalPagerApi::class)
    fun getBooks() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            val page = pager?.currentPage

            if (page != null) {
                uiCategories.getOrNull(page)?.id?.let { id ->
                    getLibraryCategory.subscribe(id, sorting, filters).collect { books ->
                        libraryState.books = books.filter { it.title.contains(searchQuery?:"", true) }
                    }
                }
            }


        }
    }



    fun onLayoutTypeChange(layoutType: DisplayMode) {
        libraryScreenPrefUseCases.libraryLayoutTypeUseCase.save(layoutType.layoutIndex)
        this.layout = layoutType.layout
    }

    fun downloadChapters() {
        serviceUseCases.startDownloadServicesUseCase(bookIds = selection.toLongArray())
        selection.clear()
    }

    fun markAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            selection.forEach { bookId ->
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                insertUseCases.insertChapters(chapters.map { it.copy(read = true) })
            }
            selection.clear()
        }
    }

    fun markAsNotRead() {
        viewModelScope.launch(Dispatchers.IO) {
            selection.forEach { bookId ->
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                insertUseCases.insertChapters(chapters.map { it.copy(read = false) })
            }
            selection.clear()
        }
    }

    fun deleteBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteBookAndChapterByBookIds(selection)
            selection.clear()
        }
    }

    fun readLayoutTypeAndFilterTypeAndSortType() {
        viewModelScope.launch {
            val sortType = libraryScreenPrefUseCases.sortersUseCase.read()
            val layoutType = libraryScreenPrefUseCases.libraryLayoutTypeUseCase.read().layout
            val sortBy = libraryScreenPrefUseCases.sortersDescUseCase.read()
            this@LibraryViewModel.layout = layoutType
            this@LibraryViewModel.sortType = sortType
            this@LibraryViewModel.desc = sortBy
        }
    }

    fun toggleFilter(type: LibraryFilter.Type) {
        val newFilters = filters
            .map { filterState ->
                if (type == filterState.type) {
                    LibraryFilter(
                        type, when (filterState.value) {
                            LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                            LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
                            LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                        }
                    )
                } else {
                    filterState
                }
            }

        this.filters = newFilters
    }

    fun toggleSort(type: LibrarySort.Type) {
        val currentSort = sorting
        sorting = if (type == currentSort.type) {
            currentSort.copy(isAscending = !currentSort.isAscending)
        } else {
            currentSort.copy(type = type)
        }
    }

    fun refreshUpdate() {
        serviceUseCases.startLibraryUpdateServicesUseCase()
    }

    fun getDefaultValue(categories: Category): ToggleableState {
        val defaultValue : Boolean =  selection.any { id -> id in bookCategories.filter { it.categoryId == categories.id }.map { it.bookId }  }

        //categories.id in bookCategories.map { it.categoryId } &&

        return if (defaultValue) ToggleableState.On else ToggleableState.Off
    }

    @Composable
    fun getLibraryForCategoryIndex(categoryIndex: Int): State<List<LibraryBook>> {
        val scope = rememberCoroutineScope()
        val categoryId = categories[categoryIndex].id

        val unfiltered = remember(sorting, filters) {
            getLibraryCategory.subscribe(categoryId, sorting, filters)
                .shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
        }

        return remember(sorting, filters, searchQuery) {
            val query = searchQuery
            if (query.isNullOrBlank()) {
                unfiltered
            } else {
                unfiltered.map { mangas ->
                    mangas.filter { it.title.contains(query, true) }
                }
            }
                .onEach { loadedManga[categoryId] = it }
                .onCompletion { loadedManga.remove(categoryId) }
        }.collectAsState(emptyList())
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}
