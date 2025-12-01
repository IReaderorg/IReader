package ireader.presentation.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.CatalogSource
import ireader.core.source.HttpSource
import ireader.domain.models.entities.Book
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_chapter_is_available
import ireader.i18n.resources.resume
import ireader.i18n.resources.source_not_available
import ireader.i18n.resources.start
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.ensureAbsoluteUrlForWebView
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.book.BookDetailScreen
import ireader.presentation.ui.book.BookDetailTopAppBar
import ireader.presentation.ui.book.components.ChapterCommandBottomSheet
import ireader.presentation.ui.book.components.ChapterScreenBottomTabComposable
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.core.theme.TransparentStatusBar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.core.utils.isScrolledToEnd
import ireader.presentation.ui.core.utils.isScrollingUp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

/**
 * Stable holder for navigation callbacks to prevent recomposition
 */
@Stable
private class NavigationCallbacks(
    val onNavigateToReader: (bookId: Long, chapterId: Long) -> Unit,
    val onNavigateToGlobalSearch: (query: String) -> Unit,
    val onNavigateToWebView: (url: String, sourceId: Long, bookId: Long) -> Unit,
    val onPopBackStack: () -> Unit
)

data class BookDetailScreenSpec constructor(
    val bookId: Long,
) {

    
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class,
        FlowPreview::class
    )
    @Composable
    fun Content(
    ) {
        
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: BookDetailViewModel = getIViewModel(
            key = bookId,
            parameters = { parametersOf(BookDetailViewModel.Param(bookId)) }
        )
        
        // Memoize navigation callbacks to prevent recomposition
        val navigationCallbacks = remember(navController) {
            NavigationCallbacks(
                onNavigateToReader = { bookId, chapterId ->
                    navController.navigateTo(ReaderScreenSpec(bookId = bookId, chapterId = chapterId))
                },
                onNavigateToGlobalSearch = { query ->
                    try {
                        navController.navigateTo(GlobalSearchScreenSpec(query = query))
                    } catch (_: Throwable) {}
                },
                onNavigateToWebView = { url, sourceId, bookId ->
                    navController.navigateTo(
                        WebViewScreenSpec(
                            url = url,
                            sourceId = sourceId,
                            bookId = bookId,
                            chapterId = null,
                            enableChaptersFetch = true,
                            enableBookFetch = true,
                            enableChapterFetch = false
                        )
                    )
                },
                onPopBackStack = { navController.popBackStack() }
            )
        }
        
        val snackbarHostState = SnackBarListener(vm = vm)
        
        // Use derivedStateOf for computed values to reduce recomposition
        val book by remember { derivedStateOf { vm.booksState.book } }
        val source by remember { derivedStateOf { vm.catalogSource?.source } }
        val catalog by remember { derivedStateOf { vm.catalogSource } }
        
        val scope = rememberCoroutineScope()
        val chapters = vm.getChapters(book?.id)
        
        // Lazy scroll state initialization with saved position
        val scrollState = rememberLazyListState(
            initialFirstVisibleItemIndex = vm.savedScrollIndex,
            initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
        )
        
        // Lazy sheet state - only created when needed
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        // Debounced scroll position saving - reduces frequent state updates on low-end devices
        androidx.compose.runtime.LaunchedEffect(scrollState) {
            snapshotFlow { 
                scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset 
            }
            .debounce(300L) // Debounce to reduce frequent updates
            .distinctUntilChanged()
            .collect { (index, offset) ->
                vm.saveScrollPosition(index, offset)
            }
        }
        
        // Reset scroll position when screen is disposed
        androidx.compose.runtime.DisposableEffect(Unit) {
            onDispose {
                vm.resetScrollPosition()
            }
        }
        
        // Derive refreshing state to avoid multiple property accesses
        val refreshing by remember { derivedStateOf { vm.detailIsLoading || vm.chapterIsLoading } }
        val pullToRefreshState = rememberPullToRefreshState()
        val topbarState = rememberTopAppBarState()
        val snackBarHostState = SnackBarListener(vm)
        
        // Memoize refresh callback
        val onRefresh = remember(book, catalog) {
            {
                scope.launch {
                    vm.getRemoteChapterDetail(book, catalog)
                }
                Unit
            }
        }
        
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
        IModalSheets(
            sheetContent = { sheetModifier ->
                // Use local variables to avoid repeated property access
                val detailState = vm.state
                val currentBook = vm.booksState.book
                val currentCatalog = vm.catalogSource
                val currentSource = detailState.source

                if (vm.chapterMode) {
                    // Lazy pager state - only created when sheet is shown
                    val pagerState = rememberPagerState(
                        initialPage = 0,
                        initialPageOffsetFraction = 0f,
                        pageCount = { 3 }
                    )
                    
                    // Memoize callbacks to prevent recomposition
                    val toggleFilterCallback = remember { { filter: ireader.presentation.ui.book.viewmodel.ChaptersFilters -> vm.toggleFilter(filter.type) } }
                    val onSortCallback = remember { { sort: ireader.presentation.ui.book.viewmodel.ChapterSort -> vm.toggleSort(sort.type) } }
                    val onLayoutCallback = remember { { layout: ireader.domain.preferences.prefs.ChapterDisplayMode -> vm.layout = layout } }
                    
                    ChapterScreenBottomTabComposable(
                        modifier = sheetModifier,
                        pagerState = pagerState,
                        filters = vm.filters.value,
                        toggleFilter = toggleFilterCallback,
                        onSortSelected = onSortCallback,
                        sortType = vm.sorting.value,
                        isSortDesc = vm.isAsc,
                        onLayoutSelected = onLayoutCallback,
                        layoutType = vm.layout,
                        vm = vm
                    )
                } else {
                    if (currentSource is CatalogSource) {
                        // Memoize callbacks
                        val onFetchCallback = remember(currentBook, currentCatalog) {
                            {
                                vm.scope.launch {
                                    if (currentBook != null) {
                                        vm.getRemoteChapterDetail(
                                            currentBook,
                                            currentCatalog,
                                            vm.modifiedCommands.filter { !it.isDefaultValue() }
                                        )
                                    }
                                }
                                Unit
                            }
                        }
                        val onResetCallback = remember(currentSource) {
                            { vm.modifiedCommands = currentSource.getCommands() }
                        }
                        val onUpdateCallback = remember { { commands: ireader.core.source.model.CommandList -> vm.modifiedCommands = commands } }
                        
                        ChapterCommandBottomSheet(
                            modifier = sheetModifier,
                            onFetch = onFetchCallback,
                            onReset = onResetCallback,
                            onUpdate = onUpdateCallback,
                            detailState.modifiedCommands
                        )
                    }
                }
            },
            bottomSheetState = sheetState
        ) {
            // Cache tablet check to avoid repeated computation
            val isTablet = isTableUi()
            
            // Use simpler scroll behavior on low-end devices (pinned is less expensive)
            // Note: These are @Composable functions, so we can't use remember here
            val scrollBehavior = if (isTablet) {
                TopAppBarDefaults.pinnedScrollBehavior(topbarState)
            } else {
                TopAppBarDefaults.enterAlwaysScrollBehavior(
                    state = topbarState,
                    canScroll = { true }
                )
            }
            
            // Pre-compute stable values for top bar
            val isArchived by remember { derivedStateOf { book?.isArchived ?: false } }
            
            TransparentStatusBar {
                IScaffold(
                    topBarScrollBehavior = scrollBehavior,
                    snackbarHostState = snackbarHostState,
                    topBar = { scrollBehavior ->
                        // Memoize all top bar callbacks to prevent recomposition
                        val onRefreshCallback = remember(book, catalog) {
                            {
                                scope.launch {
                                    if (book != null) {
                                        vm.getRemoteBookDetail(book, source = catalog)
                                        vm.getRemoteChapterDetail(book, catalog)
                                    }
                                }
                                Unit
                            }
                        }
                        
                        val onCommandCallback = remember(sheetState) {
                            { scope.launch { sheetState.show() }; Unit }
                        }
                        
                        // Optimized selection callbacks - avoid creating new lists on each call
                        val onSelectBetweenCallback = remember {
                            {
                                val selectedIds = vm.selection.toSet()
                                val chapterIds = vm.chapters.map { it.id }
                                val filteredIds = chapterIds.filter { it in selectedIds }.sorted()
                                if (filteredIds.isNotEmpty()) {
                                    val min = filteredIds.first()
                                    val max = filteredIds.last()
                                    vm.selection.clear()
                                    vm.selection.addAll((min..max).toList())
                                }
                                Unit
                            }
                        }
                        
                        val onSelectAllCallback = remember {
                            {
                                vm.selection.clear()
                                vm.selection.addAll(vm.chapters.map { it.id }.distinct())
                                Unit
                            }
                        }
                        
                        val onInvertSelectionCallback = remember {
                            {
                                val selectedIds = vm.selection.toSet()
                                val invertedIds = vm.chapters.map { it.id }.filterNot { it in selectedIds }.distinct()
                                vm.selection.clear()
                                vm.selection.addAll(invertedIds)
                                Unit
                            }
                        }
                        
                        val onCancelSelectionCallback: () -> Unit = remember { { vm.selection.clear() } }
                        val onDownloadCallback: () -> Unit = remember(book) { { book?.let { vm.startDownloadService(book = it) }; Unit } }
                        val onInfoCallback: () -> Unit = remember { { vm.showDialog = true } }
                        val onArchiveCallback: () -> Unit = remember(book) { { book?.let { vm.archiveBook(it) }; Unit } }
                        val onUnarchiveCallback: () -> Unit = remember(book) { { book?.let { vm.unarchiveBook(it) }; Unit } }
                        val onShareCallback: () -> Unit = remember { { vm.shareBook() } }
                        val onExportCallback: () -> Unit = remember { { vm.showEpubExportDialog = true } }

                        BookDetailTopAppBar(
                            scrollBehavior = scrollBehavior,
                            onRefresh = onRefreshCallback,
                            onPopBackStack = navigationCallbacks.onPopBackStack,
                            source = vm.source,
                            onCommand = onCommandCallback,
                            state = vm,
                            onSelectBetween = onSelectBetweenCallback,
                            onClickSelectAll = onSelectAllCallback,
                            onClickInvertSelection = onInvertSelectionCallback,
                            onClickCancelSelection = onCancelSelectionCallback,
                            paddingValues = PaddingValues(0.dp),
                            onDownload = onDownloadCallback,
                            onInfo = onInfoCallback,
                            onArchive = onArchiveCallback,
                            onUnarchive = onUnarchiveCallback,
                            isArchived = isArchived,
                            onShareBook = onShareCallback,
                            onExportEpub = onExportCallback
                        )
                    },
                    floatingActionButton = {
                        // Derive hasSelection to avoid repeated property access
                        val hasSelection by remember { derivedStateOf { vm.hasSelection } }
                        
                        if (!hasSelection) {
                            // Derive FAB text resource to avoid recomputation
                            val fabTextRes by remember(chapters) {
                                derivedStateOf {
                                    if (chapters.value.any { it.read }) Res.string.resume else Res.string.start
                                }
                            }
                            
                            // FAB expanded state - these are @Composable functions
                            val isScrollingUp = scrollState.isScrollingUp()
                            val isScrolledToEnd = scrollState.isScrolledToEnd()
                            val isExpanded = isScrollingUp || isScrolledToEnd
                            
                            // Capture book value for use in callback
                            val currentBook = book
                            
                            // Memoize FAB click handler
                            val onFabClick: () -> Unit = remember(catalog, currentBook, vm.chapters) {
                                {
                                    if (catalog != null && currentBook != null) {
                                        val vmChapters = vm.chapters
                                        when {
                                            vmChapters.any { it.read } && vmChapters.isNotEmpty() -> {
                                                navigationCallbacks.onNavigateToReader(currentBook.id, LAST_CHAPTER)
                                            }
                                            vmChapters.isNotEmpty() -> {
                                                navigationCallbacks.onNavigateToReader(currentBook.id, vmChapters.first().id)
                                            }
                                            else -> {
                                                scope.launch {
                                                    vm.showSnackBar(UiText.MStringResource(Res.string.no_chapter_is_available))
                                                }
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            vm.showSnackBar(UiText.MStringResource(Res.string.source_not_available))
                                        }
                                    }
                                }
                            }
                            
                            ExtendedFloatingActionButton(
                                text = { Text(text = localize(fabTextRes)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                },
                                onClick = onFabClick,
                                expanded = isExpanded,
                                modifier = Modifier,
                                shape = CircleShape
                            )
                        }
                    }
                ) { scaffoldPadding ->
                    // Pre-compute stable values to avoid recomputation
                    val appbarPadding = scaffoldPadding.calculateTopPadding()
                    val defaultBook = remember { Book(key = "", sourceId = 0, title = "") }
                    val displayBook = book ?: defaultBook
                    
                    // Capture book for callbacks
                    val currentBookForCallbacks = book
                    val currentSourceForCallbacks = source
                    
                    // Memoize all callbacks to prevent recomposition in BookDetailScreen
                    val onSummaryExpandCallback: () -> Unit = remember { { vm.expandedSummary = !vm.expandedSummary } }
                    
                    val onTitleCallback: (String) -> Unit = remember(navigationCallbacks) {
                        { title: String -> navigationCallbacks.onNavigateToGlobalSearch(title) }
                    }
                    
                    val onItemClickCallback: (ireader.domain.models.entities.Chapter) -> Unit = remember(currentBookForCallbacks, navigationCallbacks) {
                        { chapter: ireader.domain.models.entities.Chapter ->
                            if (vm.selection.isEmpty()) {
                                currentBookForCallbacks?.let { b ->
                                    navigationCallbacks.onNavigateToReader(b.id, chapter.id)
                                }
                            } else {
                                if (chapter.id in vm.selection) {
                                    vm.selection.remove(chapter.id)
                                } else {
                                    vm.selection.add(chapter.id)
                                }
                            }
                        }
                    }
                    
                    val onLongItemClickCallback: (ireader.domain.models.entities.Chapter) -> Unit = remember {
                        { chapter: ireader.domain.models.entities.Chapter ->
                            if (chapter.id in vm.selection) {
                                vm.selection.remove(chapter.id)
                            } else {
                                vm.selection.add(chapter.id)
                            }
                            Unit
                        }
                    }
                    
                    val onSortClickCallback: () -> Unit = remember(sheetState) {
                        {
                            scope.launch {
                                vm.chapterMode = true
                                sheetState.show()
                            }
                            Unit
                        }
                    }
                    
                    val onMapCallback: () -> Unit = remember(scrollState) {
                        {
                            scope.launch {
                                try {
                                    val index = vm.getLastChapterIndex()
                                    val offset = -scrollState.layoutInfo.viewportEndOffset / 2
                                    scrollState.scrollToItem(index, offset)
                                } catch (_: Throwable) {}
                            }
                            Unit
                        }
                    }
                    
                    val onWebViewCallback: () -> Unit = remember(currentSourceForCallbacks, currentBookForCallbacks, navigationCallbacks) {
                        {
                            val s = currentSourceForCallbacks
                            val b = currentBookForCallbacks
                            if (s != null && s is HttpSource && b != null) {
                                val absoluteUrl = ensureAbsoluteUrlForWebView(b.key, s)
                                navigationCallbacks.onNavigateToWebView(absoluteUrl, b.sourceId, b.id)
                            }
                        }
                    }
                    
                    val onFavoriteCallback: () -> Unit = remember(currentBookForCallbacks) {
                        { currentBookForCallbacks?.let { vm.toggleInLibrary(book = it) }; Unit }
                    }
                    
                    val onCopyTitleCallback: (String) -> Unit = remember {
                        { title: String -> vm.copyToClipboard(title, title) }
                    }

                    BookDetailScreen(
                        onSummaryExpand = onSummaryExpandCallback,
                        book = displayBook,
                        vm = vm,
                        onTitle = onTitleCallback,
                        isSummaryExpanded = vm.expandedSummary,
                        source = vm.source,
                        appbarPadding = appbarPadding,
                        onItemClick = onItemClickCallback,
                        onLongItemClick = onLongItemClickCallback,
                        onSortClick = onSortClickCallback,
                        chapters = chapters,
                        scrollState = scrollState,
                        onMap = onMapCallback,
                        onWebView = onWebViewCallback,
                        onFavorite = onFavoriteCallback,
                        onCopyTitle = onCopyTitleCallback,
                    )
                }
                }
            }
        }
        
        // End of Life Options Dialog - only compose when needed
        val showEndOfLifeDialog by remember { derivedStateOf { vm.showEndOfLifeDialog } }
        if (showEndOfLifeDialog) {
            // Capture book for callback
            val currentBookForDialog = book
            
            // Memoize dialog callbacks
            val onDismissDialog: () -> Unit = remember { { vm.hideEndOfLifeOptionsDialog() } }
            val onArchiveDialog: () -> Unit = remember(currentBookForDialog) { { currentBookForDialog?.let { vm.archiveBook(it) }; Unit } }
            val onExportDialog: () -> Unit = remember { { vm.showEpubExportDialog = true } }
            
            ireader.presentation.ui.book.components.EndOfLifeOptionsDialog(
                onDismiss = onDismissDialog,
                onArchive = onArchiveDialog,
                onExportEpub = onExportDialog
            )
        }
    }

}
