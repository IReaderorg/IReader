package ireader.presentation.ui.home.library.viewmodel

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
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.models.DisplayMode
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.GetLibraryCategory
import ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@OptIn(ExperimentalCoroutinesApi::class)

class LibraryViewModel(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val insertUseCases: LocalInsertUseCases,
    val deleteUseCase: DeleteUseCase,
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val libraryScreenPrefUseCases: LibraryScreenPrefUseCases,
    private val state: LibraryStateImpl,
    private val serviceUseCases: ServiceUseCases,
    private val getLibraryCategory: GetLibraryCategory,
    private val libraryPreferences: LibraryPreferences,
    val markBookAsReadOrNotUseCase: MarkBookAsReadOrNotUseCase,
    val getCategory: CategoriesUseCases
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), LibraryState by state {

    var lastUsedCategory = libraryPreferences.lastUsedCategory().asState()
    var filters = libraryPreferences.filters(true).asState()

    var sorting = libraryPreferences.sorting().asState()
    val showCategoryTabs = libraryPreferences.showCategoryTabs().asState()
    val showAllCategoryTab = libraryPreferences.showAllCategory().asState()
    val showCountInCategory = libraryPreferences.showCountInCategory().asState()

    val readBadge = libraryPreferences.downloadBadges().asState()
    val unreadBadge = libraryPreferences.unreadBadges().asState()
    val goToLastChapterBadge = libraryPreferences.goToLastChapterBadges().asState()

    val bookCategories = getCategory.subscribeBookCategories().asState(emptyList())
    val deleteQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    val addQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    var showDialog: Boolean by mutableStateOf(false)

    val perCategorySettings = libraryPreferences.perCategorySettings().asState()
    val layouts = libraryPreferences.categoryFlags().asState()
    var columnInPortrait = libraryPreferences.columnsInPortrait().asState()
    val columnInLandscape by libraryPreferences.columnsInLandscape().asState()
    val layout by derivedStateOf { DisplayMode.getFlag(layouts.value) ?: DisplayMode.CompactGrid }

    init {
        readLayoutTypeAndFilterTypeAndSortType()
        libraryPreferences.showAllCategory().stateIn(scope)
            .flatMapLatest { showAll ->
                getCategory.subscribe(showAll).onEach { categories ->
                    val lastCategoryId = lastUsedCategory.value

                    val index =
                        categories.indexOfFirst { it.id == lastCategoryId }.takeIf { it >= 0 } ?: 0

                    state.categories = categories
                    state.selectedCategoryIndex = index
                }
            }.launchIn(scope)
    }

    private val loadedManga = mutableMapOf<Long, List<BookItem>>()

    fun onLayoutTypeChange(layoutType: DisplayMode) {
        scope.launch {
            categories.firstOrNull { it.id == lastUsedCategory.value }?.let { category ->

                libraryScreenPrefUseCases.libraryLayoutTypeUseCase.await(
                    category = category.category,
                    displayMode = layoutType
                )
            }
        }
    }

fun downloadChapters() {
    serviceUseCases.startDownloadServicesUseCase(bookIds = selectedBooks.toLongArray())
    selectedBooks.clear()
}

fun readLayoutTypeAndFilterTypeAndSortType() {
    scope.launch {
        val sortType = libraryScreenPrefUseCases.sortersUseCase.read()
        val sortBy = libraryScreenPrefUseCases.sortersDescUseCase.read()
        this@LibraryViewModel.sortType = sortType
        this@LibraryViewModel.desc = sortBy
    }
}

fun toggleFilter(type: LibraryFilter.Type) {
    val newFilters = filters.value
        .map { filterState ->
            if (type == filterState.type) {
                LibraryFilter(
                    type,
                    when (filterState.value) {
                        LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                        LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
                        LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                    }
                )
            } else {
                filterState
            }
        }

    this.filters.value = newFilters
}

fun toggleSort(type: LibrarySort.Type) {
    val currentSort = sorting
    sorting.value = if (type == currentSort.value.type) {
        currentSort.value.copy(isAscending = !currentSort.value.isAscending)
    } else {
        currentSort.value.copy(type = type)
    }
}

fun refreshUpdate() {
    serviceUseCases.startLibraryUpdateServicesUseCase()
}

fun setSelectedPage(index: Int) {
    if (index == selectedCategoryIndex) return
    val categories = categories
    val category = categories.getOrNull(index) ?: return
    state.selectedCategoryIndex = index
    state.selectedCategory
    lastUsedCategory.value = category.id
}

fun unselectAll() {
    state.selectedBooks.clear()
}

fun selectAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedBooks.toList()
    val mangaIds = mangaInCurrentCategory.map { it.id }.filter { it !in currentSelected }
    state.selectedBooks.addAll(mangaIds)
}

fun flipAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedBooks.toList()
    val (toRemove, toAdd) = mangaInCurrentCategory.map { it.id }
        .partition { it in currentSelected }
    state.selectedBooks.removeAll(toRemove)
    state.selectedBooks.addAll(toAdd)
}

fun getDefaultValue(categories: Category): ToggleableState {
    val defaultValue: Boolean = selectedBooks.any { id ->
        id in bookCategories.value.filter { it.categoryId == categories.id }.map { it.bookId }
    }
    return if (defaultValue) ToggleableState.On else ToggleableState.Off
}

@Composable
fun getLibraryForCategoryIndex(categoryIndex: Int): State<List<BookItem>> {
    val scope = rememberCoroutineScope()
    val categoryId = categories.getOrNull(categoryIndex)?.id ?: return remember {
        mutableStateOf(emptyList())
    }

    val unfiltered = remember(sorting.value, filters.value, categoryId, categories.size) {
        getLibraryCategory.subscribe(categoryId, sorting.value, filters.value)
            .map { list ->
                books = list
                list.mapIndexed { index, libraryBook ->
                    libraryBook.toBookItem().copy(column = index.toLong())
                }
            }
            .shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
    }

    return remember(
        sorting.value,
        filters.value,
        searchQuery,
        showAllCategoryTab.value,
        categories.size,
    ) {
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

fun getColumnsForOrientation(isLandscape: Boolean, scope: CoroutineScope): StateFlow<Int> {
    return if (isLandscape) {
        libraryPreferences.columnsInLandscape()
    } else {
        libraryPreferences.columnsInPortrait()
    }.stateIn(scope)
}
}
