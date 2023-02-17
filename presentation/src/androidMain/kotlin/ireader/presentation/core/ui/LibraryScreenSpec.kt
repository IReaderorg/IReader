package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.navigation.NamedNavArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ireader.presentation.ui.home.library.LibraryScreenTopBar
import ireader.presentation.ui.home.library.components.BottomTabComposable
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.i18n.LAST_CHAPTER
import ireader.presentation.ui.component.Controller
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.core.ui.util.NavigationArgs.showModalSheet
import ireader.presentation.R
import ireader.presentation.core.IModalSheets
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.library.LibraryController
import org.koin.androidx.compose.getViewModel

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
    override fun Content(
        controller: Controller
    ) {
        val vm: LibraryViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)

        LaunchedEffect(key1 = vm.selectionMode) {
            controller.requestHideNavigator(vm.selectionMode)
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

        IModalSheets(
            bottomSheetState = sheetState,
            sheetContent = {
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
                    vm = vm,
                    scaffoldPadding = PaddingValues(0.dp)
                )
            }
        ) {
            IScaffold(
                topBar = {
                    LibraryScreenTopBar(
                        state = vm,
                        bottomSheetState = sheetState,
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
            ) { scaffoldPadding ->

                LibraryController(
                    modifier = Modifier,
                    vm = vm,
                    controller = controller,
                    goToReader = { book ->
                        controller.navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                chapterId = LAST_CHAPTER
                            )
                        )
                    },
                    goToDetail = { book ->
                        controller.navController.navigate(
                            route = BookDetailScreenSpec.buildRoute(
                                bookId = book.id
                            )
                        )
                    },
                    scaffoldPadding = scaffoldPadding,
                    sheetState = sheetState
                )
            }
        }
    }
}
