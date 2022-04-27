package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.core.utils.Constants
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.presentation.feature_library.presentation.LibraryScreen
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel

object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm : LibraryViewModel = hiltViewModel()
        val context = LocalContext.current
        LibraryScreen(
            navController = navController,
            addFilters = {
                vm.addFilters(it)
            },
            removeFilter = {
                vm.removeFilters(it)
            },
            onSortSelected = {
                vm.changeSortIndex(it)
            },
            onLayoutSelected = { layout->
                vm.onLayoutTypeChange(layout)
            } ,
            onMarkAsRead = {
                vm.markAsRead()
            },
            onDownload =  {
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
                        chapterId = Constants.LAST_CHAPTER
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
                            bookId = book.id)
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
