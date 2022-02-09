package org.ireader.presentation.feature_detail.presentation.book_detail


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.view_models.detail.book_detail.BookDetailViewModel


@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: BookDetailViewModel = hiltViewModel(),
) {

    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = viewModel.state.isLocalLoading || viewModel.state.isRemoteLoading)



    TransparentStatusBar {
        Box(modifier = Modifier.fillMaxSize()) {

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    viewModel.getRemoteBookDetail(viewModel.state.book)
                    viewModel.getRemoteChapterDetail(viewModel.state.book)
                },
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        scale = true,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.primaryVariant,
                    )
                }
            ) {
                LazyColumn(modifier.fillMaxSize()) {
                    item {
                        BookDetailScreenLoadedComposable(
                            modifier = modifier,
                            viewModel = viewModel,
                            navController = navController)
                    }
                }
            }

        }
    }
}


