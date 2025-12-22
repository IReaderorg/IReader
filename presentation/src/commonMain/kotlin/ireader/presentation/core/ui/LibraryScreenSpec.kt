package ireader.presentation.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ireader.core.startup.ScreenProfiler
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.library_screen_label
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.library.LibraryController
import ireader.presentation.ui.home.library.LibraryScreenTopBar
import ireader.presentation.ui.home.library.components.BottomTabComposable
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.settings.advance.OnShowImportEpub
import ireader.presentation.ui.settings.advance.OnShowImportPdf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Library screen specification - provides tab metadata and content
 * Updated to use Mihon-style StateFlow pattern
 */
object LibraryScreenSpec {
    
    @Composable
    fun getTitle(): String = localize(Res.string.library_screen_label)
    
    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.Book)

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    fun TabContent() {
        // Mark when TabContent composable starts
        LaunchedEffect(Unit) {
            ScreenProfiler.mark("Library", "tab_content_composable_start")
        }
        val vm: LibraryViewModel = getIViewModel(key = "library")
        // Mark after VM is obtained
        LaunchedEffect(vm) {
            ScreenProfiler.mark("Library", "vm_obtained")
        }
        val state by vm.state.collectAsState()
        
        // Track screen entry for profiling when returning to library
        LaunchedEffect(Unit) {
            // Check if returning from detail screen
            if (ScreenProfiler.isScreenActive("Navigation_DetailToLibrary")) {
                ScreenProfiler.mark("Navigation_DetailToLibrary", "library_content_entered")
                ScreenProfiler.finishScreen("Navigation_DetailToLibrary")
            }
            // Also track general library return
            if (!ScreenProfiler.isScreenActive("Library")) {
                ScreenProfiler.startScreen("Library_return")
                ScreenProfiler.mark("Library_return", "tab_content_entered")
            }
        }
        
        // Mark when books are loaded (not loading anymore)
        LaunchedEffect(state.isLoading) {
            if (!state.isLoading && ScreenProfiler.isScreenActive("Library_return")) {
                ScreenProfiler.mark("Library_return", "content_ready")
                ScreenProfiler.finishScreen("Library_return")
            }
        }
        
        LaunchedEffect(state.selectionMode) {
            MainStarterScreen.showBottomNav(!state.selectionMode)
        }
        
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scope = rememberCoroutineScope()

        // Listen for filter sheet requests from double-tap on Library tab
        LaunchedEffect(Unit) {
            MainStarterScreen.libraryFilterSheetFlow().collectLatest { show ->
                if (show) {
                    sheetState.partialExpand()
                }
            }
        }

        val pullToRefreshState = rememberPullToRefreshState()

        IModalSheets(
            bottomSheetState = sheetState,
            sheetContent = {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0f
                ) { 3 }
                BottomTabComposable(
                    modifier = it,
                    pagerState = pagerState,
                    filters = vm.filters.value,
                    toggleFilter = { filter -> vm.toggleFilter(filter.type) },
                    onSortSelected = { sort -> vm.toggleSort(sort.type) },
                    sortType = state.sort,
                    isSortDesc = !state.sort.isAscending,
                    onLayoutSelected = { layout -> vm.onLayoutTypeChange(layout) },
                    layoutType = state.layout,
                    vm = vm,
                    scaffoldPadding = PaddingValues(0.dp)
                )
            }
        ) {
            IScaffold(
                topBar = { scrollBehavior ->
                    LibraryScreenTopBar(
                        state = vm,
                        refreshUpdate = { vm.refreshUpdate() },
                        onClearSelection = { vm.unselectAll() },
                        onClickInvertSelection = { vm.flipAllInCurrentCategory() },
                        onClickSelectAll = { vm.selectAllInCurrentCategory() },
                        scrollBehavior = scrollBehavior,
                        hideModalSheet = { scope.launch { sheetState.hide() } },
                        showModalSheet = { scope.launch { sheetState.partialExpand() } },
                        isModalVisible = sheetState.isVisible,
                        onUpdateLibrary = { vm.updateLibrary() },
                        onUpdateCategory = { vm.showUpdateCategoryDialog() },
                        onImportEpub = { vm.setShowImportEpubDialog(true) },
                        onImportPdf = { vm.setShowImportPdfDialog(true) },
                        onOpenRandom = {
                            vm.openRandomEntry()?.let { bookId ->
                                navController.navigateTo(BookDetailScreenSpec(bookId = bookId))
                            }
                        },
                        onSyncRemote = { vm.syncWithRemote() },
                        onSearchLibrary = { }
                    )
                }
            ) { scaffoldPadding ->
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { vm.refreshUpdate() },
                    state = pullToRefreshState
                ) {
                    LibraryController(
                        modifier = Modifier,
                        vm = vm,
                        goToReader = { book ->
                            navController.navigateTo(
                                ReaderScreenSpec(bookId = book.id, chapterId = LAST_CHAPTER)
                            )
                        },
                        goToDetail = { book ->
                            // Start navigation profiling
                            ScreenProfiler.startScreen("Navigation_LibraryToDetail")
                            ScreenProfiler.mark("Navigation_LibraryToDetail", "click_detected")
                            navController.navigateTo(BookDetailScreenSpec(bookId = book.id))
                            ScreenProfiler.mark("Navigation_LibraryToDetail", "navigate_called")
                        },
                        scaffoldPadding = scaffoldPadding,
                        sheetState = sheetState,
                        requestHideNavigator = {
                            scope.launch { MainStarterScreen.showBottomNav(!state.selectionMode) }
                        },
                        showFilterSheet = sheetState.isVisible,
                        onShowFilterSheet = { scope.launch { sheetState.partialExpand() } },
                        onHideFilterSheet = { scope.launch { sheetState.hide() } }
                    )
                }
            }
        }
        
        // EPUB Import Dialog
        OnShowImportEpub(
            show = state.showImportEpubDialog,
            onFileSelected = { uris ->
                vm.setShowImportEpubDialog(false)
                if (uris.isNotEmpty()) {
                    vm.importEpubFiles(uris.map { it.toString() })
                }
            }
        )
        
        // PDF Import Dialog
        OnShowImportPdf(
            show = state.showImportPdfDialog,
            onFileSelected = { uris ->
                vm.setShowImportPdfDialog(false)
                if (uris.isNotEmpty()) {
                    vm.importPdfFiles(uris.map { it.toString() })
                }
            }
        )
    }
}
