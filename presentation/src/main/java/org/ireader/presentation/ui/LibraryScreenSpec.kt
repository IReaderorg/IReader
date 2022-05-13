package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.app.LibraryScreen
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R

object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    private const val route = "library"

    @OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val vm: LibraryViewModel = hiltViewModel()
        LibraryScreen(
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
        )
    }
}