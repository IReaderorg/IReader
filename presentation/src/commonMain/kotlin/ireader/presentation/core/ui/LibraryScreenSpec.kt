package ireader.presentation.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Library screen specification - provides tab metadata and content
 */
object LibraryScreenSpec {
    
    @Composable
    fun getTitle(): String = localize(Res.string.library_screen_label)
    
    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.Book)

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    fun TabContent() {
        val vm: LibraryViewModel = getIViewModel(key = "library")
        LaunchedEffect(key1 = vm.selectionMode) {
            MainStarterScreen.showBottomNav(!vm.selectionMode)
        }
        val sheetState = rememberModalBottomSheetState()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scope = rememberCoroutineScope()

        // Listen for filter sheet requests from double-tap on Library tab
        LaunchedEffect(Unit) {
            MainStarterScreen.libraryFilterSheetFlow().collectLatest { show ->
                if (show) {
                    sheetState.show()
                }
            }
        }

        val swipeRefreshState = rememberPullRefreshState(vm.isBookRefreshing, onRefresh = {
            vm.refreshUpdate()
        })

        IModalSheets(
            bottomSheetState = sheetState,
            sheetContent = {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0f
                ) {
                    3
                }
                BottomTabComposable(
                    modifier = it,
                    pagerState = pagerState,
                    filters = vm.filters.value,
                    toggleFilter = {
                        vm.toggleFilter(it.type)
                    },
                    onSortSelected = {
                        vm.toggleSort(it.type)
                    },
                    sortType = vm.sortType,
                    isSortDesc = vm.desc,
                    onLayoutSelected = { layout ->
                        vm.onLayoutTypeChange(layout)
                    },
                    layoutType = vm.layout,
                    vm = vm,
                    scaffoldPadding = PaddingValues(0.dp)
                )
            }
        ) {
            IScaffold(
                modifier = Modifier.pullRefresh(swipeRefreshState),
                topBar = { scrollBehavior ->
                    LibraryScreenTopBar(
                        state = vm,
                        refreshUpdate = {
                            vm.refreshUpdate()
                        },
                        onClearSelection = {
                            vm.unselectAll()
                        },
                        onClickInvertSelection = {
                            vm.flipAllInCurrentCategory()
                        },
                        onClickSelectAll = {
                            vm.selectAllInCurrentCategory()
                        },
                        scrollBehavior = scrollBehavior,
                        hideModalSheet = {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        showModalSheet = {
                            scope.launch {
                                sheetState.show()
                            }
                        },
                        isModalVisible = sheetState.isVisible,
                        onUpdateLibrary = {
                            vm.updateLibrary()
                        },
                        onUpdateCategory = {
                            vm.showUpdateCategoryDialog()
                        },
                        onImportEpub = {
                            vm.showImportEpubDialog = true
                        },
                        onOpenRandom = {
                            vm.openRandomEntry()?.let { bookId ->
                                navController.navigateTo(
                                    BookDetailScreenSpec(bookId = bookId)
                                )
                            }
                        },
                        onSyncRemote = {
                            vm.syncWithRemote()
                        },
                        onSearchLibrary = {
                            // Search is handled by the search mode in the toolbar
                        }
                    )
                }
            ) { scaffoldPadding ->

                Box(Modifier) {
                    LibraryController(
                        modifier = Modifier,
                        vm = vm,
                        goToReader = { book ->
                            navController.navigateTo(
                                ReaderScreenSpec(
                                    bookId = book.id,
                                    chapterId = LAST_CHAPTER
                                )
                            )
                        },
                        goToDetail = { book ->
                            navController.navigateTo(
                                BookDetailScreenSpec(
                                    bookId = book.id
                                )
                            )
                        },
                        scaffoldPadding = scaffoldPadding,
                        sheetState = sheetState,
                        requestHideNavigator = {
                            scope.launch {
                                MainStarterScreen.showBottomNav(!vm.selectionMode)
                            }
                        }
                    )
                    PullRefreshIndicator(
                        vm.isBookRefreshing,
                        swipeRefreshState,
                        Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
        
        // EPUB Import Dialog
        OnShowImportEpub(
            show = vm.showImportEpubDialog,
            onFileSelected = { uris ->
                vm.showImportEpubDialog = false
                if (uris.isNotEmpty()) {
                    vm.importEpubFiles(uris.map { it.toString() })
                }
            }
        )
    }
}
