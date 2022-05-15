package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import org.ireader.app.LibraryScreen
import org.ireader.app.LibraryScreenTopBar
import org.ireader.app.components.BottomTabComposable
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.ui.NavigationArgs.showModalSheet
import org.ireader.presentation.R

object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav,
        showModalSheet
    )

    @ExperimentalMaterial3Api
    @ExperimentalMaterialApi
    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BottomModalSheet(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)

        val pagerState = rememberPagerState()
        BottomTabComposable(
            pagerState = pagerState,
            filters = vm.filters,
            addFilters = {
                vm.addFilters(it)
            },
            removeFilter = {
                vm.removeFilters(it)
            },
            onSortSelected = {
                vm.changeSortIndex(it)
            },
            sortType = vm.sortType,
            isSortDesc = vm.desc,
            onLayoutSelected = { layout ->
                vm.onLayoutTypeChange(layout)
            },
            layoutType = vm.layout
        )
    }

    private const val route = "library"

    @OptIn(ExperimentalMaterial3Api::class)
    @ExperimentalMaterialApi
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)
        LibraryScreenTopBar(
            state = vm,
            bottomSheetState = sheetState,
            onSearch = {
                vm.getLibraryBooks()
            },
            refreshUpdate = {
                vm.refreshUpdate()
            },
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)
        LibraryScreen(
            modifier = Modifier.padding(scaffoldPadding),
            addFilters = {
                vm.addFilters(it)
            },
            removeFilter = {
                vm.removeFilters(it)
            },
            onSortSelected = {
                vm.changeSortIndex(it)
            },
            onLayoutSelected = { layout ->
                vm.onLayoutTypeChange(layout)
            },
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
                navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        bookId = book.id,
                        sourceId = book.sourceId,
                        chapterId = LAST_CHAPTER
                    )
                )
            },
            onBook = { book ->
                if (vm.hasSelection) {
                    if (book.id in vm.selection) {
                        vm.selection.remove(book.id)
                    } else {
                        vm.selection.add(book.id)
                    }
                } else {
                    navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(
                            sourceId = book.sourceId,
                            bookId = book.id
                        )
                    )
                }
            },
            onLongBook = {
                vm.selection.add(it.id)
            },
            vm = vm,
            getLibraryBooks = {
                vm.getLibraryBooks()
            },
            refreshUpdate = {
                vm.refreshUpdate()
            },
            bottomSheetState = sheetState
        )
    }
}