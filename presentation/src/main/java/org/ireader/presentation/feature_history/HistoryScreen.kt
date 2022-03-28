package org.ireader.presentation.feature_history

import androidx.compose.animation.Crossfade
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_history.viewmodel.HistoryViewModel
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    vm: HistoryViewModel = hiltViewModel(),
) {

    Scaffold(
        topBar = { HistoryTopAppBar(navController = navController, vm = vm) }
    ) {
        Crossfade(targetState = Pair(vm.isLoading, vm.isEmpty)) { (isLoading, isEmpty) ->
            when {
                isLoading -> LoadingScreen()
                isEmpty -> EmptyScreen(UiText.DynamicString("No History is Available."))
                else -> HistoryContent(
                    state = vm,
                    onClickItem = { history ->
                        navController.navigate(
                            BookDetailScreenSpec.buildRoute(
                                history.sourceId,
                                history.bookId
                            )
                        )
                    },
                    onClickDelete = { history ->
                        vm.deleteHistory(history.chapterId)
                    },
                    onClickPlay = { history ->
                        navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                history.bookId,
                                history.sourceId,
                                history.chapterId
                            )
                        )
                    }
                )
            }
        }
    }
}