package org.ireader.presentation.feature_detail.presentation.book_detail


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.ireader.core.utils.UiEvent
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookDetailScreenBottomBar
import org.ireader.presentation.feature_detail.presentation.book_detail.components.CardTileComposable
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel.BookDetailViewModel
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.components.showLoading


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: BookDetailViewModel,
    onToggleLibrary: () -> Unit,
    onDownload: () -> Unit,
    onRead: () -> Unit,
    onSummaryExpand: () -> Unit,
    onRefresh: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onWebView: () -> Unit,
    onChapterContent: () -> Unit,
    book: Book,
    onTitle:(String) -> Unit
) {

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }

    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = viewModel.detailIsLocalLoading || viewModel.detailIsRemoteLoading || viewModel.chapterIsLoading)

    val source = viewModel.source
    val chapters = viewModel.chapters

    if (viewModel.detailIsLocalLoading) {
        showLoading()
    }
    LaunchedEffect(key1 = true) {
        if (source != null) {
            viewModel.getLocalBookById(bookId = book.id, source = source)
        }
        viewModel.getLocalChaptersByBookId(bookId = book.id)
    }

    TransparentStatusBar {
        Scaffold(
            topBar = {},
            scaffoldState = scaffoldState,
            snackbarHost = { ISnackBarHost(snackBarHostState = it) },
            bottomBar = {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BookDetailScreenBottomBar(
                        onToggleInLibrary = {
                            onToggleLibrary()
                        },
                        isInLibrary = viewModel.inLibrary,
                        onDownload = {
                            onDownload()
                        },
                        isRead = viewModel.chapters.any { it.readAt != 0L },
                        onRead = {
                            onRead()
                        }
                    )
                }
            }) { padding ->

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    onSwipeRefresh()
                },
                indicatorPadding = PaddingValues(vertical = 40.dp),
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        scale = true,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.primaryVariant,
                        elevation = 8.dp,
                    )
                }
            ) {
                /**
                 * I did this because the buttonbar disappear in the
                 * lazy Column
                 */
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    BookDetailScreenLoadedComposable(
                        navController = navController,
                        onWebView = {
                            onWebView()
                        },
                        onTitle = onTitle,
                        onSummaryExpand =  onSummaryExpand,
                        onRefresh = onRefresh,
                        isSummaryExpanded = viewModel.expandedSummary,
                        book = book,
                        source = source,
                    )

                    CardTileComposable(
                        modifier = modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                        onClick = onChapterContent,
                        title = "Contents",
                        subtitle = "${chapters.size} Chapters",
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "",
                                    color = MaterialTheme.colors.onBackground,
                                    style = MaterialTheme.typography.subtitle2
                                )
                                if (viewModel.chapterIsLoading) {
                                    DotsFlashing()
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Contents Detail",
                                    tint = MaterialTheme.colors.onBackground,
                                )
                            }
                        })
                    Spacer(modifier = Modifier.height(60.dp))


                }
            }

        }
    }
}






