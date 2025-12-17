package ireader.presentation.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import ireader.core.startup.ScreenProfiler
import androidx.compose.ui.unit.dp
import ireader.core.source.CatalogSource
import ireader.core.source.HttpSource
import ireader.domain.models.entities.Chapter
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
import ireader.presentation.ui.book.viewmodel.BookDetailEvent
import ireader.presentation.ui.book.viewmodel.BookDetailState
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.core.theme.TransparentStatusBar
import ireader.presentation.ui.core.utils.isScrolledToEnd
import ireader.presentation.ui.core.utils.isScrollingUp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
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
    val onPopBackStack: () -> Unit,
    val onNavigateToCharacterArtGallery: () -> Unit,
    val onNavigateToCharacterArtDetail: (artId: String) -> Unit
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
    fun Content() {
        // Mark navigation arrival - finish the navigation profiling
        LaunchedEffect(Unit) {
            if (ScreenProfiler.isScreenActive("Navigation_LibraryToDetail")) {
                ScreenProfiler.mark("Navigation_LibraryToDetail", "detail_content_started")
            }
        }
        
        val vm: BookDetailViewModel = getIViewModel(
            key = bookId,
            parameters = { parametersOf(BookDetailViewModel.Param(bookId)) }
        )
        
        // Mark after ViewModel obtained
        LaunchedEffect(Unit) {
            if (ScreenProfiler.isScreenActive("Navigation_LibraryToDetail")) {
                ScreenProfiler.mark("Navigation_LibraryToDetail", "detail_vm_obtained")
            }
        }
        
        val state by vm.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scope = rememberCoroutineScope()
        
        // Finish navigation profiling when state is ready
        LaunchedEffect(state) {
            if (ScreenProfiler.isScreenActive("Navigation_LibraryToDetail") && state is BookDetailState.Success) {
                ScreenProfiler.mark("Navigation_LibraryToDetail", "detail_state_ready")
                ScreenProfiler.finishScreen("Navigation_LibraryToDetail")
            }
        }
        
        // Handle events
        LaunchedEffect(vm) {
            vm.events.collectLatest { event ->
                when (event) {
                    is BookDetailEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    is BookDetailEvent.NavigateToReader -> {
                        navController.navigateTo(ReaderScreenSpec(event.bookId, event.chapterId))
                    }
                    is BookDetailEvent.NavigateToWebView -> {
                        navController.navigateTo(
                            WebViewScreenSpec(
                                url = event.url,
                                sourceId = event.sourceId,
                                bookId = event.bookId,
                                chapterId = null,
                                enableChaptersFetch = true,
                                enableBookFetch = true,
                                enableChapterFetch = false
                            )
                        )
                    }
                    BookDetailEvent.NavigateBack -> {
                        navController.popBackStack()
                    }
                }
            }
        }
        
        // Memoize navigation callbacks
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
                onPopBackStack = { 
                    // Start navigation profiling for back navigation
                    ScreenProfiler.startScreen("Navigation_DetailToLibrary")
                    ScreenProfiler.mark("Navigation_DetailToLibrary", "back_pressed")
                    navController.popBackStack() 
                },
                onNavigateToCharacterArtGallery = {
                    navController.navigate(ireader.presentation.core.NavigationRoutes.characterArtGallery)
                },
                onNavigateToCharacterArtDetail = { artId ->
                    navController.navigate("${ireader.presentation.core.NavigationRoutes.characterArtDetail}/$artId")
                }
            )
        }
        
        // Wrap entire screen with TransparentStatusBar to prevent UI jump during transition
        // Use graphicsLayer to promote to separate layer for smoother animation
        TransparentStatusBar {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Promote to separate layer for GPU-accelerated animation
                        // This prevents recomposition from affecting the animation
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                when (val s = state) {
                    BookDetailState.Loading -> {
                        // Brief loading state - ViewModel transitions to Success.empty() immediately
                        // No shimmer, no placeholder - just empty box during brief transition
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    
                    is BookDetailState.Success -> {
                        // Mark UI composition for profiling
                        LaunchedEffect(Unit) {
                            ScreenProfiler.mark("BookDetail_$bookId", "ui_composition_started")
                        }
                        
                        BookDetailContent(
                            vm = vm,
                            state = s,
                            snackbarHostState = snackbarHostState,
                            navigationCallbacks = navigationCallbacks,
                        )
                    }
                
                    is BookDetailState.Error -> {
                        ErrorContent(
                            message = s.message,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
    
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class,
        FlowPreview::class
    )
    @Composable
    private fun BookDetailContent(
        vm: BookDetailViewModel,
        state: BookDetailState.Success,
        snackbarHostState: SnackbarHostState,
        navigationCallbacks: NavigationCallbacks,
    ) {
        val scope = rememberCoroutineScope()
        
        // Scroll state with saved position
        val scrollState = rememberLazyListState(
            initialFirstVisibleItemIndex = vm.savedScrollIndex,
            initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
        )
        
        // Sheet state
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        
        // Debounced scroll position saving
        LaunchedEffect(scrollState) {
            snapshotFlow { 
                scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset 
            }
            .debounce(300L)
            .distinctUntilChanged()
            .collect { (index, offset) ->
                vm.saveScrollPosition(index, offset)
            }
        }
        
        // Chapter mode for bottom sheet
        val chapterMode = remember { mutableStateOf(true) }
        
        // Refreshing state - directly use state.isRefreshing (no derivedStateOf needed)
        val refreshing = state.isRefreshing
        val pullToRefreshState = rememberPullToRefreshState()
        val topbarState = rememberTopAppBarState()
        
        // Memoize refresh callback - refreshes both book details and chapters
        val onRefresh = remember(state.book, state.catalogSource) {
            {
                scope.launch {
                    // Refresh book details first, then chapters
                    vm.getRemoteBookDetail(state.book, state.catalogSource)
                    vm.getRemoteChapterDetail(state.book, state.catalogSource)
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
                    if (chapterMode.value) {
                        val pagerState = rememberPagerState(
                            initialPage = 0,
                            initialPageOffsetFraction = 0f,
                            pageCount = { 3 }
                        )
                        
                        ChapterScreenBottomTabComposable(
                            modifier = sheetModifier,
                            pagerState = pagerState,
                            filters = vm.filters.value,
                            toggleFilter = { filter -> vm.toggleFilter(filter.type) },
                            onSortSelected = { sort -> vm.toggleSort(sort.type) },
                            sortType = vm.sorting.value,
                            isSortDesc = vm.sorting.value.isAscending,
                            onLayoutSelected = { layout -> vm.layout = layout },
                            layoutType = vm.layout,
                            vm = vm
                        )
                    } else {
                        val source = state.source
                        if (source is CatalogSource) {
                            ChapterCommandBottomSheet(
                                modifier = sheetModifier,
                                onFetch = {
                                    scope.launch {
                                        vm.getRemoteChapterDetail(
                                            state.book,
                                            state.catalogSource,
                                            vm.modifiedCommands.filter { !it.isDefaultValue() }
                                        )
                                    }
                                },
                                onReset = { vm.resetCommands() },
                                onUpdate = { commands -> vm.updateModifiedCommands(commands) },
                                commandList = state.modifiedCommands,
                            )
                        }
                    }
                },
                bottomSheetState = sheetState
            ) {
                val isTablet = isTableUi()
                val scrollBehavior = if (isTablet) {
                    TopAppBarDefaults.pinnedScrollBehavior(topbarState)
                } else {
                    TopAppBarDefaults.enterAlwaysScrollBehavior(
                        state = topbarState,
                        canScroll = { true }
                    )
                }
                
                IScaffold(
                        topBarScrollBehavior = scrollBehavior,
                        snackbarHostState = snackbarHostState,
                        topBar = { scrollBehavior ->
                            BookDetailTopAppBar(
                                scrollBehavior = scrollBehavior,
                                onRefresh = {
                                    scope.launch {
                                        vm.getRemoteBookDetail(state.book, state.catalogSource)
                                        vm.getRemoteChapterDetail(state.book, state.catalogSource)
                                    }
                                },
                                onPopBackStack = navigationCallbacks.onPopBackStack,
                                source = state.source,
                                onCommand = {
                                    chapterMode.value = false
                                    scope.launch { sheetState.partialExpand() }
                                },
                                hasSelection = vm.hasSelection,
                                selectionSize = vm.selection.size,
                                onSelectBetween = { vm.selectBetween() },
                                onClickSelectAll = { vm.selectAllChapters() },
                                onClickInvertSelection = { vm.invertSelection() },
                                onClickCancelSelection = { vm.clearSelection() },
                                paddingValues = PaddingValues(0.dp),
                                onDownload = { vm.startDownloadService(state.book) },
                                onInfo = { vm.showDialog = true },
                                onArchive = { vm.archiveBook(state.book) },
                                onUnarchive = { vm.unarchiveBook(state.book) },
                                isArchived = state.isArchived,
                                onShareBook = { vm.shareBook() },
                                onExportEpub = { 
                                    vm.checkTranslationsForExport()
                                    vm.showEpubExportDialog = true 
                                }
                            )
                        },
                        floatingActionButton = {
                            if (!vm.hasSelection) {
                                val fabTextRes by remember(state.chapters) {
                                    derivedStateOf {
                                        if (state.hasReadChapters) Res.string.resume else Res.string.start
                                    }
                                }
                                
                                val isScrollingUp = scrollState.isScrollingUp()
                                val isScrolledToEnd = scrollState.isScrolledToEnd()
                                val isExpanded = isScrollingUp || isScrolledToEnd
                                
                                ExtendedFloatingActionButton(
                                    text = { Text(text = localize(fabTextRes)) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        if (state.catalogSource != null) {
                                            when {
                                                state.hasReadChapters && state.hasChapters -> {
                                                    navigationCallbacks.onNavigateToReader(state.book.id, LAST_CHAPTER)
                                                }
                                                state.hasChapters -> {
                                                    navigationCallbacks.onNavigateToReader(state.book.id, state.chapters.first().id)
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
                                    },
                                    expanded = isExpanded,
                                    modifier = Modifier,
                                    shape = CircleShape
                                )
                            }
                        }
                    ) { scaffoldPadding ->
                        val appbarPadding = scaffoldPadding.calculateTopPadding()
                        
                        // Apply filtering and sorting based on vm.query, vm.filters, vm.sorting, and read status
                        // Note: Use vm.sorting (State object) not vm.sorting.value to properly track changes
                        val currentSorting = vm.sorting.value
                        val currentFilters = vm.filters.value
                        val filteredChapters by remember(state.chapters, vm.query, currentFilters, currentSorting, state.hasReadChapters) {
                            derivedStateOf {
                                var result = state.chapters.toList()
                                
                                // Apply search filter
                                val query = vm.query
                                if (!query.isNullOrBlank()) {
                                    result = result.filter { it.name.contains(query, ignoreCase = true) }
                                }
                                
                                // Apply chapter filters
                                for (filter in currentFilters) {
                                    if (filter.value == ireader.presentation.ui.book.viewmodel.ChaptersFilters.Value.Missing) continue
                                    
                                    val predicate: (Chapter) -> Boolean = when (filter.type) {
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type.Unread -> { ch -> !ch.read }
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type.Read -> { ch -> ch.read }
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type.Bookmarked -> { ch -> ch.bookmark }
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type.Downloaded -> { ch -> ch.content.joinToString("").isNotBlank() }
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type.Duplicate -> { ch ->
                                            state.chapters.any { other ->
                                                other.id != ch.id && other.name.trim().equals(ch.name.trim(), ignoreCase = true)
                                            }
                                        }
                                    }
                                    
                                    result = when (filter.value) {
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Value.Included -> result.filter(predicate)
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Value.Excluded -> result.filterNot(predicate)
                                        ireader.presentation.ui.book.viewmodel.ChaptersFilters.Value.Missing -> result
                                    }
                                }
                                
                                // Apply sorting based on captured sorting preference
                                // For Default sort: just reverse the original order when isAscending is false
                                // For other sorts: use the appropriate comparator
                                if (currentSorting.type == ireader.presentation.ui.book.viewmodel.ChapterSort.Type.Default) {
                                    // Default sort: keep original order, reverse if not ascending
                                    result = if (currentSorting.isAscending) {
                                        result
                                    } else {
                                        result.reversed()
                                    }
                                } else {
                                    val comparator: Comparator<Chapter> = when (currentSorting.type) {
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.Default -> compareBy { it.sourceOrder } // Won't be used
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.ByName -> compareBy { it.name }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.BySource -> compareBy { it.sourceOrder }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.ByChapterNumber -> compareBy { it.number }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.DateFetched -> compareBy { it.dateFetch }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.DateUpload -> compareBy { it.dateUpload }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.Bookmark -> compareByDescending { it.bookmark }
                                        ireader.presentation.ui.book.viewmodel.ChapterSort.Type.Read -> compareByDescending { it.read }
                                    }
                                    
                                    result = if (currentSorting.isAscending) {
                                        result.sortedWith(comparator)
                                    } else {
                                        result.sortedWith(comparator.reversed())
                                    }
                                }
                                
                                result
                            }
                        }
                        
                        // Create chapters state for BookDetailScreen
                        val chaptersState = remember { mutableStateOf<List<Chapter>>(emptyList()) }
                        LaunchedEffect(filteredChapters) {
                            chaptersState.value = filteredChapters
                        }
                        
                        BookDetailScreen(
                            onSummaryExpand = { vm.toggleSummaryExpansion() },
                            book = state.book,
                            vm = vm,
                            onTitle = { title -> navigationCallbacks.onNavigateToGlobalSearch(title) },
                            isSummaryExpanded = state.isSummaryExpanded,
                            source = state.source,
                            appbarPadding = appbarPadding,
                            onItemClick = { chapter ->
                                if (vm.selection.isEmpty()) {
                                    navigationCallbacks.onNavigateToReader(state.book.id, chapter.id)
                                } else {
                                    if (chapter.id in vm.selection) {
                                        vm.selection.remove(chapter.id)
                                    } else {
                                        vm.selection.add(chapter.id)
                                    }
                                }
                            },
                            onLongItemClick = { chapter ->
                                if (chapter.id in vm.selection) {
                                    vm.selection.remove(chapter.id)
                                } else {
                                    vm.selection.add(chapter.id)
                                }
                            },
                            onSortClick = {
                                chapterMode.value = true
                                scope.launch { sheetState.partialExpand() }
                            },
                            chapters = chaptersState,
                            scrollState = scrollState,
                            onMap = {
                                scope.launch {
                                    try {
                                        val index = vm.getLastChapterIndex()
                                        if (index >= 0) {
                                            val offset = -scrollState.layoutInfo.viewportEndOffset / 2
                                            scrollState.scrollToItem(index, offset)
                                        }
                                    } catch (_: Throwable) {}
                                }
                            },
                            onWebView = {
                                val source = state.source
                                if (source is HttpSource) {
                                    val absoluteUrl = ensureAbsoluteUrlForWebView(state.book.key, source)
                                    navigationCallbacks.onNavigateToWebView(absoluteUrl, state.book.sourceId, state.book.id)
                                }
                            },
                            onFavorite = { vm.toggleInLibrary(state.book) },
                            onCopyTitle = { title -> vm.copyToClipboard("Title", title) },
                            onEditInfo = { vm.showDialog = true },
                            onPickLocalCover = { vm.showImagePickerDialog = true },
                            onShowCoverPreview = { vm.showCoverPreviewDialog = true },
                            onCharacterArtGallery = navigationCallbacks.onNavigateToCharacterArtGallery,
                            onCharacterArtDetail = navigationCallbacks.onNavigateToCharacterArtDetail,
                        )
                    }
                }
            }
        
        // Dialogs
        if (vm.showMigrationDialog) {
            ireader.presentation.ui.book.components.MigrationSourceDialog(
                sources = vm.availableMigrationSources,
                onSourceSelected = { targetSource ->
                    vm.showMigrationDialog = false
                    vm.startMigration(targetSource.sourceId)
                },
                onDismiss = { vm.showMigrationDialog = false }
            )
        }
        
        if (vm.sourceSwitchingState.showMigrationDialog) {
            vm.sourceSwitchingState.migrationProgress?.let { progress ->
                ireader.presentation.ui.component.MigrationProgressDialog(
                    currentStep = progress.currentStep,
                    progress = progress.progress,
                    onDismiss = {
                        if (progress.isComplete) {
                            vm.sourceSwitchingState.showMigrationDialog = false
                        }
                    }
                )
            }
        }
        
        if (vm.showEpubExportDialog) {
            ireader.presentation.ui.book.components.EpubExportDialog(
                book = state.book,
                chapters = state.chapters,
                hasTranslations = vm.hasTranslationsForExport,
                translationTargetLanguage = vm.translationExportTargetLanguage,
                onExport = { options ->
                    vm.showEpubExportDialog = false
                    vm.exportAsEpub(options)
                },
                onDismiss = { vm.showEpubExportDialog = false }
            )
        }
        
        if (vm.showDialog) {
            ireader.presentation.ui.book.components.EditInfoAlertDialog(
                onStateChange = { vm.showDialog = it },
                book = state.book,
                onConfirm = { updatedBook ->
                    vm.scope.launch {
                        vm.insertUseCases.insertBook(updatedBook)
                    }
                }
            )
        }
        
        // Image picker for custom cover
        ireader.presentation.ui.book.components.ImagePickerDialog(
            show = vm.showImagePickerDialog,
            onImageSelected = { uri ->
                vm.showImagePickerDialog = false
                vm.handleImageSelected(uri)
            },
            onDismiss = { vm.showImagePickerDialog = false }
        )
        
        // Cover preview dialog with options
        if (vm.showCoverPreviewDialog) {
            ireader.presentation.ui.book.components.CoverPreviewDialog(
                book = state.book,
                onDismiss = { vm.showCoverPreviewDialog = false },
                onPickLocalImage = { 
                    vm.showCoverPreviewDialog = false
                    vm.showImagePickerDialog = true 
                },
                onEditCoverUrl = { 
                    vm.showCoverPreviewDialog = false
                    vm.showDialog = true 
                },
                onShareCover = {
                    // Share the cover URL
                    val coverUrl = if (state.book.customCover.isNotBlank()) {
                        state.book.customCover
                    } else {
                        state.book.cover
                    }
                    vm.shareText(coverUrl, state.book.title)
                },
                onResetCover = {
                    vm.showCoverPreviewDialog = false
                    vm.resetCustomCover()
                }
            )
        }
    }
    
    @Composable
    private fun ErrorContent(
        message: String,
        onBack: () -> Unit,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "Error: $message",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
                androidx.compose.material3.OutlinedButton(onClick = onBack) {
                    androidx.compose.material3.Text("Go Back")
                }
            }
        }
    }
}
