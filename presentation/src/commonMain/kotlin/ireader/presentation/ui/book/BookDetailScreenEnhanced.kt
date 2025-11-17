package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import cafe.adriel.voyager.core.model.rememberScreenModel
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
import ireader.presentation.ui.book.components.*
import ireader.presentation.ui.book.viewmodel.BookDetailScreenModelNew
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.reusable_composable.AppTextField
import org.koin.core.parameter.parametersOf

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
    val screenModel = rememberScreenModel { 
        BookDetailScreenModelNew(
            bookId = bookId,
            getBookUseCases = get(),
            getChapterUseCase = get(),
            insertUseCases = get()
        )
    }
    
    val state by screenModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberLazyListState()
    
    // Check if we're on a tablet for responsive design
    val isTablet = isTableUi()

    IReaderScaffold(
        topBar = { scrollBehavior ->
            BookDetailTopAppBar(
                title = state.book?.title ?: "",
                onNavigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior,
                onSearch = { screenModel.toggleSearchMode() },
                isSearchMode = state.isSearchMode,
                onWebView = onWebViewClick,
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                IReaderLoadingScreen(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            state.error != null -> {
                IReaderErrorScreen(
                    message = state.error!!,
                    modifier = Modifier.padding(paddingValues),
                    actions = listOf(
                        ErrorScreenAction(
                            title = "Retry",
                            icon = Icons.Default.Refresh,
                            onClick = { screenModel.retry() }
                        )
                    )
                )
            }
            state.book != null -> {
                if (isTablet) {
                    // Tablet layout with two panels
                    TwoPanelBoxStandalone(
                        modifier = Modifier.padding(paddingValues),
                        isExpandedWidth = true,
                        startContent = {
                            BookDetailInfoPanel(
                                book = state.book!!,
                                source = state.source,
                                onFavoriteClick = { screenModel.toggleBookFavorite() },
                                onWebViewClick = onWebViewClick,
                                onCopyTitle = onCopyTitle,
                                isTogglingFavorite = state.isTogglingFavorite,
                                onMigrateClick = { screenModel.showMigrationDialog() },
                                onExportClick = { screenModel.showEpubExportDialog() },
                                onEditInfoClick = { screenModel.showEditInfoDialog() }
                            )
                        },
                        endContent = {
                            BookDetailChapterPanel(
                                chapters = state.chapters,
                                searchQuery = state.searchQuery,
                                isSearchMode = state.isSearchMode,
                                selectedChapterIds = state.selectedChapterIds,
                                filters = state.filters,
                                sorting = state.sorting,
                                chapterDisplayMode = state.chapterDisplayMode,
                                scrollState = scrollState,
                                onChapterClick = onChapterClick,
                                onChapterLongClick = { screenModel.toggleChapterSelection(it.id) },
                                onSearchQueryChange = { screenModel.updateSearchQuery(it) },
                                onFilterToggle = { screenModel.toggleFilter(it) },
                                onSortChange = { screenModel.updateSorting(it) },
                                focusManager = focusManager,
                                keyboardController = keyboardController
                            )
                        }
                    )
                } else {
                    // Phone layout with single panel
                    BookDetailContent(
                        book = state.book!!,
                        chapters = state.chapters,
                        source = state.source,
                        searchQuery = state.searchQuery,
                        isSearchMode = state.isSearchMode,
                        selectedChapterIds = state.selectedChapterIds,
                        filters = state.filters,
                        sorting = state.sorting,
                        chapterDisplayMode = state.chapterDisplayMode,
                        scrollState = scrollState,
                        onChapterClick = onChapterClick,
                        onChapterLongClick = { screenModel.toggleChapterSelection(it.id) },
                        onFavoriteClick = { screenModel.toggleBookFavorite() },
                        onWebViewClick = onWebViewClick,
                        onCopyTitle = onCopyTitle,
                        onSearchQueryChange = { screenModel.updateSearchQuery(it) },
                        onFilterToggle = { screenModel.toggleFilter(it) },
                        onSortChange = { screenModel.updateSorting(it) },
                        onMigrateClick = { screenModel.showMigrationDialog() },
                        onExportClick = { screenModel.showEpubExportDialog() },
                        onEditInfoClick = { screenModel.showEditInfoDialog() },
                        isTogglingFavorite = state.isTogglingFavorite,
                        focusManager = focusManager,
                        keyboardController = keyboardController,
                        appbarPadding = appbarPadding
                    )
                }
            }
        }

        // Dialogs
        if (state.showMigrationDialog) {
            MigrationSourceDialog(
                sources = emptyList(), // TODO: Load migration sources
                onSourceSelected = { /* TODO: Handle migration */ },
                onDismiss = { screenModel.hideMigrationDialog() }
            )
        }

        if (state.showEpubExportDialog) {
            EpubExportDialog(
                book = state.book!!,
                chapters = state.chapters,
                onExport = { /* TODO: Handle export */ },
                onDismiss = { screenModel.hideEpubExportDialog() }
            )
        }

        if (state.showEditInfoDialog) {
            EditInfoAlertDialog(
                onStateChange = { screenModel.hideEditInfoDialog() },
                book = state.book!!,
                onConfirm = { /* TODO: Handle edit */ }
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
            BookHeaderImage(
                book = book,
                scrollProgress = 0f,
                hideBackdrop = false
            )
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
                onMigrate = onMigrateClick,
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
    onSortChange: (ireader.presentation.ui.book.viewmodel.ChapterSort) -> Unit,
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
    onSortChange: (ireader.presentation.ui.book.viewmodel.ChapterSort) -> Unit,
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
            
            item {
                ChapterBar(
                    vm = null, // TODO: Adapt to new pattern
                    chapters = chapters,
                    onMap = { },
                    onSortClick = { }
                )
            }
            
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

        // Bottom bar for selected chapters
        if (selectedChapterIds.isNotEmpty()) {
            ChapterDetailBottomBar(
                vm = null, // TODO: Adapt to new pattern
                onDownload = { },
                onBookmark = { },
                onMarkAsRead = { },
                visible = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}