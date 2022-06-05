package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NamedNavArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ireader.app.LibraryScreen
import org.ireader.app.LibraryScreenTopBar
import org.ireader.app.components.BottomTabComposable
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.ui.NavigationArgs.showModalSheet
import org.ireader.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav,
        showModalSheet
    )

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BottomModalSheet(
        controller: ScreenSpec.Controller
    ) {
        val vm: LibraryViewModel = hiltViewModel(controller.navBackStackEntry)

        val pagerState = rememberPagerState()
        BottomTabComposable(
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
            vm = vm
        )
    }

    private const val route = "library"

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: LibraryViewModel = hiltViewModel(controller.navBackStackEntry)
        LibraryScreenTopBar(
            state = vm,
            bottomSheetState = controller.sheetState,
            onSearch = {
                //   vm.getBooks()
            },
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
            }
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class
    )
    @Composable
    override fun Content(
        controller:ScreenSpec.Controller
    ) {
        val vm: LibraryViewModel = hiltViewModel(controller.navBackStackEntry)


        LibraryScreen(
            onMarkAsRead = {
                vm.markAsRead()
            },
            onDownload = {
                vm.downloadChapters()
            },
            onMarkAsNotRead = {
                vm.markAsNotRead()
            },
            onDelete = {
                vm.deleteBooks()
            },
            goToLatestChapter = { book ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        bookId = book.id,
                        sourceId = book.sourceId,
                        chapterId = LAST_CHAPTER
                    )
                )
            },
            onBook = { book ->
                if (vm.selectionMode) {
                    if (book.id in vm.selectedBooks) {
                        vm.selectedBooks.remove(book.id)
                    } else {
                        vm.selectedBooks.add(book.id)
                    }
                } else {
                    controller.navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(
                            sourceId = book.sourceId,
                            bookId = book.id
                        )
                    )
                }
            },
            onLongBook = {
                if (it.id in vm.selectedBooks) return@LibraryScreen
                vm.selectedBooks.add(it.id)
            },
            vm = vm,
            refreshUpdate = {
                vm.refreshUpdate()
            },
            bottomSheetState = controller.sheetState,
            onClickChangeCategory = {
                vm.showDialog = true
            },
            scaffoldPadding = controller.scaffoldPadding,
            onAddToCategoryConfirm = {
                vm.viewModelScope.launch(Dispatchers.IO) {
                    vm.getCategory.insertBookCategory(vm.addQueues)
                    vm.getCategory.deleteBookCategory(vm.deleteQueues)
                    vm.deleteQueues.clear()
                    vm.addQueues.clear()
                    vm.selectedBooks.clear()
                    vm.addQueues.clear()
                    vm.deleteQueues.clear()
                }
                vm.showDialog = false
            },
            requestHideBottomNav = controller.requestHideNavigator,
            getColumnsForOrientation = { isLandscape ->
              vm.getColumnsForOrientation(isLandscape,this)
            }
        )
    }
}


