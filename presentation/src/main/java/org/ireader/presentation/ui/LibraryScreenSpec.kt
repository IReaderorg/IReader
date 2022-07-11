package org.ireader.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import org.ireader.Controller
import org.ireader.app.LibraryController
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
        controller: Controller
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

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: LibraryViewModel = hiltViewModel(controller.navBackStackEntry)
        LibraryScreenTopBar(
            state = vm,
            bottomSheetState = controller.sheetState,
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
            scrollBehavior = controller.scrollBehavior

        )
    }
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: LibraryViewModel = hiltViewModel(controller.navBackStackEntry)

        LaunchedEffect(key1 = vm.selectionMode) {
            controller.requestHideNavigator(vm.selectionMode)
        }

        LibraryController(
            modifier = Modifier,
            vm = vm,
            controller = controller,
            goToReader = { book ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        bookId = book.id,
                        sourceId = book.sourceId,
                        chapterId = LAST_CHAPTER
                    )
                )
            },
            goToDetail = { book ->
                controller.navController.navigate(
                    route = BookDetailScreenSpec.buildRoute(
                        sourceId = book.sourceId,
                        bookId = book.id
                    )
                )
            }
        )
    }
}
