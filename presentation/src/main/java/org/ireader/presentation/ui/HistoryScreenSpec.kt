package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.domain.ui.NavigationArgs
import org.ireader.history.HistoryScreen
import org.ireader.history.viewmodel.HistoryViewModel
import org.ireader.presentation.R


object HistoryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.History
    override val label: Int = R.string.history_screen_label
    override val navHostRoute: String = "history"


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
        val vm: HistoryViewModel = hiltViewModel()
        HistoryScreen(
            onHistory = { history ->
                navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.sourceId,
                        history.chapterId
                    )
                )
            },
            onHistoryPlay = { history ->
                navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.sourceId,
                        history.chapterId
                    )
                )
            },
            onHistoryDelete = { history ->
                vm.viewModelIOCoroutine {
                    vm.historyUseCase.deleteHistory(history.chapterId)
                }
            },
            onAppbarDeleteAll = {
                vm.viewModelIOCoroutine {
                    vm.historyUseCase.deleteAllHistories()
                }
            },
            getHistories = {
                vm.getHistoryBooks()
            },
            state = vm,
            onBookCover = { history ->
                navController.navigate(
                    BookDetailScreenSpec.buildRoute(
                        history.sourceId,
                        history.bookId
                    )
                )
            }
        )
    }

}
