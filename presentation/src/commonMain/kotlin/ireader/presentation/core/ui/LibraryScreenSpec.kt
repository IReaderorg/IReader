package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.library.LibraryController
import ireader.presentation.ui.home.library.LibraryScreenTopBar
import ireader.presentation.ui.home.library.components.BottomTabComposable
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

object LibraryScreenSpec : Tab {


    override val options: TabOptions
        @Composable
        get() {
            val title = localize(MR.strings.library_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.Book)
            return remember {
                TabOptions(
                        index = 0u,
                        title = title,
                        icon = icon,
                )
            }

        }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun Content(

    ) {
        val vm: LibraryViewModel = getIViewModel()
        LaunchedEffect(key1 = vm.selectionMode) {
            MainStarterScreen.showBottomNav(!vm.selectionMode)
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        val swipeRefreshState = rememberPullRefreshState(vm.isBookRefreshing, onRefresh = {
            vm.refreshUpdate()
        })

        IModalSheets(
                bottomSheetState = sheetState,
                sheetContent = {
                    val pagerState = rememberPagerState()
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
                                isModalVisible = sheetState.isVisible
                        )
                    }
            ) { scaffoldPadding ->

                Box(Modifier) {
                    LibraryController(
                            modifier = Modifier,
                            vm = vm,
                            goToReader = { book ->
                                navigator.push(
                                        ReaderScreenSpec(
                                                bookId = book.id,
                                                chapterId = LAST_CHAPTER
                                        )
                                )
                            },
                            goToDetail = { book ->
                                navigator.push(
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
    }
}