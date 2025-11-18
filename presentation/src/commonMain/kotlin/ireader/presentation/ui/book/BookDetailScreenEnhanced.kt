package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.presentation.core.ui.ErrorScreenAction
import ireader.presentation.core.ui.IReaderErrorScreen
import ireader.presentation.core.ui.IReaderFastScrollLazyColumn
import ireader.presentation.core.ui.IReaderLoadingScreen
import ireader.presentation.core.ui.IReaderScaffold
import ireader.presentation.core.ui.TwoPanelBoxStandalone
import ireader.presentation.core.ui.getIViewModel
import ireader.presentation.ui.book.components.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.reusable_composable.AppTextField
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import org.koin.compose.koinInject
/**
 * Enhanced BookDetailScreen following Mihon's patterns with StateScreenModel and responsive design.
 * Replaces the current ViewModel-based approach with predictable state management.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun BookDetailScreenEnhanced(
    bookId: Long,
    onNavigateUp: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onWebViewClick: () -> Unit,
    onCopyTitle: (String) -> Unit,
    appbarPadding: Dp = 0.dp,
) {
    val vm: BookDetailViewModel = getIViewModel<BookDetailViewModel>(parameters = { parametersOf(BookDetailViewModel.Param(bookId)) })
    
    val book = vm.booksState.book
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberLazyListState()
    
    // Check if we're on a tablet for responsive design
    val isTablet = isTableUi()

    IReaderScaffold(
        topBar = { scrollBehavior ->
            BookDetailTopAppBar(
                source = vm.catalogSource?.source,
                onDownload = { vm.downloadChapters() },
                onRefresh = { 
                    book?.let { vm.scope.launch { vm.getRemoteChapterDetail(it, vm.catalogSource) } }
                },
                onPopBackStack = onNavigateUp,
                onCommand = { /* TODO: Handle commands */ },
                onShare = { vm.shareBook() },
                scrollBehavior = scrollBehavior,
                state = vm,
                onClickCancelSelection = { vm.selection.clear() },
                onClickSelectAll = { 
                    vm.selection.clear()
                    vm.selection.addAll(vm.chapters.map { it.id })
                },
                onClickInvertSelection = { 
                    val currentSelection = vm.selection.toSet()
                    vm.selection.clear()
                    vm.selection.addAll(vm.chapters.filter { it.id !in currentSelection }.map { it.id })
                },
                onSelectBetween = { 
                    if (vm.selection.size >= 2) {
                        val indices = vm.selection.mapNotNull { id -> vm.chapters.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
                        if (indices.size >= 2) {
                            val min = indices.minOrNull() ?: 0
                            val max = indices.maxOrNull() ?: 0
                            vm.selection.clear()
                            vm.selection.addAll(vm.chapters.subList(min, max + 1).map { it.id })
                        }
                    }
                },
                paddingValues = PaddingValues(0.dp),
                onInfo = { vm.showDialog = true },
                onArchive = { book?.let { vm.archiveBook(it) } },
                onUnarchive = { book?.let { vm.unarchiveBook(it) } },
                isArchived = book?.isArchived ?: false,
                onShareBook = { vm.shareBook() },
                onExportEpub = { vm.showEpubExportDialog = true }
            )
        }
    ) { paddingValues ->
        when {
            vm.detailIsLoading && book == null -> {
                IReaderLoadingScreen(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            book != null -> {
                if (isTablet) {
                    // Tablet layout with two panels
                    TwoPanelBoxStandalone(
                        modifier = Modifier.padding(paddingValues),
                        isExpandedWidth = true,
                        startContent = {
                            BookDetailInfoPanel(
                                book = book,
                                source = vm.catalogSource?.source,
                                onFavoriteClick = { vm.toggleInLibrary(book) },
                                onWebViewClick = onWebViewClick,
                                onCopyTitle = onCopyTitle,
                                isTogglingFavorite = vm.inLibraryLoading,
                                onMigrateClick = { vm.loadMigrationSources() },
                                onExportClick = { vm.showEpubExportDialog = true },
                                onEditInfoClick = { vm.showDialog = true }
                            )
                        },
                        endContent = {
                            BookDetailChapterPanel(
                                chapters = vm.chapters,
                                searchQuery = vm.query,
                                isSearchMode = vm.searchMode,
                                selectedChapterIds = vm.selection.toSet(),
                                filters = vm.filters.value,
                                sorting = vm.sorting.value,
                                chapterDisplayMode = vm.layout,
                                scrollState = scrollState,
                                onChapterClick = onChapterClick,
                                onChapterLongClick = { 
                                    if (it.id in vm.selection) {
                                        vm.selection.remove(it.id)
                                    } else {
                                        vm.selection.add(it.id)
                                    }
                                },
                                onSearchQueryChange = { vm.query = it },
                                onFilterToggle = { vm.toggleFilter(it) },
                                onSortChange = { vm.toggleSort(it) },
                                focusManager = focusManager,
                                keyboardController = keyboardController
                            )
                        }
                    )
                } else {
                    // Phone layout with single panel
                    BookDetailContent(
                        book = book,
                        chapters = vm.chapters,
                        source = vm.catalogSource?.source,
                        searchQuery = vm.query,
                        isSearchMode = vm.searchMode,
                        selectedChapterIds = vm.selection.toSet(),
                        filters = vm.filters.value,
                        sorting = vm.sorting.value,
                        chapterDisplayMode = vm.layout,
                        scrollState = scrollState,
                        onChapterClick = onChapterClick,
                        onChapterLongClick = { 
                            if (it.id in vm.selection) {
                                vm.selection.remove(it.id)
                            } else {
                                vm.selection.add(it.id)
                            }
                        },
                        onFavoriteClick = { vm.toggleInLibrary(book) },
                        onWebViewClick = onWebViewClick,
                        onCopyTitle = onCopyTitle,
                        onSearchQueryChange = { vm.query = it },
                        onFilterToggle = { vm.toggleFilter(it) },
                        onSortChange = { vm.toggleSort(it) },
                        onMigrateClick = { vm.loadMigrationSources() },
                        onExportClick = { vm.showEpubExportDialog = true },
                        onEditInfoClick = { vm.showDialog = true },
                        isTogglingFavorite = vm.inLibraryLoading,
                        focusManager = focusManager,
                        keyboardController = keyboardController,
                        appbarPadding = appbarPadding
                    )
                }
            }
        }

        // Dialogs
        if (vm.showMigrationDialog) {
            MigrationSourceDialog(
                sources = vm.availableMigrationSources,
                onSourceSelected = { vm.startMigration(it.sourceId) },
                onDismiss = { vm.showMigrationDialog = false }
            )
        }

        if (vm.showEpubExportDialog && book != null) {
            EpubExportDialog(
                book = book,
                chapters = vm.chapters,
                onExport = { vm.exportAsEpub(it) },
                onDismiss = { vm.showEpubExportDialog = false }
            )
        }

        if (vm.showDialog && book != null) {
            EditInfoAlertDialog(
                onStateChange = { vm.showDialog = it },
                book = book,
                onConfirm = { vm.scope.launch { vm.insertUseCases.insertBook(it) } }
            )
        }
    }
}

/**
 * Book detail info panel for tablet layout
 */
@Composable
private fun BookDetailInfoPanel(
    book: Book,
    source: Source?,
    onFavoriteClick: () -> Unit,
    onWebViewClick: () -> Unit,
    onCopyTitle: (String) -> Unit,
    isTogglingFavorite: Boolean,
    onMigrateClick: () -> Unit,
    onExportClick: () -> Unit,
    onEditInfoClick: () -> Unit,
) {
    IReaderFastScrollLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box {
                BookHeaderImage(
                    book = book,
                    scrollProgress = 0f,
                    hideBackdrop = false
                )
            }
        }
        
        item {
            BookHeader(
                book = book,
                onTitle = onCopyTitle,
                source = source,
                appbarPadding = 0.dp,
                onCopyTitle = onCopyTitle
            )
        }
        
        item {
            ActionHeader(
                favorite = book.favorite,
                source = source,
                onFavorite = onFavoriteClick,
                onWebView = onWebViewClick,
                onMigrate = { onMigrateClick() },
                useFab = false
            )
        }
        
        item {
            BookSummaryInfo(
                book = book,
                isSummaryExpanded = true,
                onSummaryExpand = { },
                onCopy = onCopyTitle
            )
        }
        
        item {
            BookReviewsIntegration(
                bookTitle = book.title,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * Book detail chapter panel for tablet layout
 */
@Composable
private fun BookDetailChapterPanel(
    chapters: List<Chapter>,
    searchQuery: String?,
    isSearchMode: Boolean,
    selectedChapterIds: Set<Long>,
    filters: List<ireader.presentation.ui.book.viewmodel.ChaptersFilters>,
    sorting: ireader.presentation.ui.book.viewmodel.ChapterSort,
    chapterDisplayMode: ChapterDisplayMode,
    scrollState: LazyListState,
    onChapterClick: (Chapter) -> Unit,
    onChapterLongClick: (Chapter) -> Unit,
    onSearchQueryChange: (String?) -> Unit,
    onFilterToggle: (ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type) -> Unit,
    onSortChange: (ireader.presentation.ui.book.viewmodel.ChapterSort.Type) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    IReaderFastScrollLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            ChapterListFilterBar(
                filters = filters,
                onToggleFilter = onFilterToggle
            )
        }
        
        if (isSearchMode) {
            item {
                AppTextField(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    query = searchQuery ?: "",
                    onValueChange = onSearchQueryChange,
                    onConfirm = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                )
            }
        }
        
        items(
            items = chapters.reversed(),
            key = { chapter -> chapter.id },
            contentType = { "chapter_item" }
        ) { chapter ->
            ChapterRow(
                modifier = Modifier.animateItem(),
                chapter = chapter,
                onItemClick = { onChapterClick(chapter) },
                isLastRead = false, // TODO: Implement last read tracking
                isSelected = chapter.id in selectedChapterIds,
                onLongClick = { onChapterLongClick(chapter) },
                showNumber = chapterDisplayMode == ChapterDisplayMode.ChapterNumber || chapterDisplayMode == ChapterDisplayMode.Default
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

/**
 * Single panel content for phone layout
 */
@Composable
private fun BookDetailContent(
    book: Book,
    chapters: List<Chapter>,
    source: Source?,
    searchQuery: String?,
    isSearchMode: Boolean,
    selectedChapterIds: Set<Long>,
    filters: List<ireader.presentation.ui.book.viewmodel.ChaptersFilters>,
    sorting: ireader.presentation.ui.book.viewmodel.ChapterSort,
    chapterDisplayMode: ChapterDisplayMode,
    scrollState: LazyListState,
    onChapterClick: (Chapter) -> Unit,
    onChapterLongClick: (Chapter) -> Unit,
    onFavoriteClick: () -> Unit,
    onWebViewClick: () -> Unit,
    onCopyTitle: (String) -> Unit,
    onSearchQueryChange: (String?) -> Unit,
    onFilterToggle: (ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type) -> Unit,
    onSortChange: (ireader.presentation.ui.book.viewmodel.ChapterSort.Type) -> Unit,
    onMigrateClick: () -> Unit,
    onExportClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    isTogglingFavorite: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    appbarPadding: Dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IReaderFastScrollLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Box {
                    val scrollProgress = scrollState.firstVisibleItemScrollOffset.toFloat()
                    BookHeaderImage(
                        book = book,
                        scrollProgress = scrollProgress,
                        hideBackdrop = false
                    )

                    BookHeader(
                        book = book,
                        onTitle = onCopyTitle,
                        source = source,
                        appbarPadding = appbarPadding,
                        onCopyTitle = onCopyTitle
                    )
                }
            }
            
            item {
                ActionHeader(
                    favorite = book.favorite,
                    source = source,
                    onFavorite = onFavoriteClick,
                    onWebView = onWebViewClick,
                    onMigrate = onMigrateClick,
                    useFab = false
                )
            }
            
            item {
                BookSummaryInfo(
                    book = book,
                    isSummaryExpanded = false,
                    onSummaryExpand = { },
                    onCopy = onCopyTitle
                )
            }
            
            item {
                BookReviewsIntegration(
                    bookTitle = book.title,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Chapter bar removed - functionality integrated into filter bar
            
            item {
                ChapterListFilterBar(
                    filters = filters,
                    onToggleFilter = onFilterToggle
                )
            }
            
            if (isSearchMode) {
                item {
                    AppTextField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        query = searchQuery ?: "",
                        onValueChange = onSearchQueryChange,
                        onConfirm = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    )
                }
            }
            
            items(
                items = chapters.reversed(),
                key = { chapter -> chapter.id },
                contentType = { "chapter_item" }
            ) { chapter ->
                ChapterRow(
                    modifier = Modifier.animateItem(),
                    chapter = chapter,
                    onItemClick = { onChapterClick(chapter) },
                    isLastRead = false, // TODO: Implement last read tracking
                    isSelected = chapter.id in selectedChapterIds,
                    onLongClick = { onChapterLongClick(chapter) },
                    showNumber = chapterDisplayMode == ChapterDisplayMode.ChapterNumber || chapterDisplayMode == ChapterDisplayMode.Default
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
        }

        // Bottom bar removed - handled by parent scaffold
    }
}