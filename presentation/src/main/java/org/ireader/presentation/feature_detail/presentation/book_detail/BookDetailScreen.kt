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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookDetailScreenBottomBar
import org.ireader.presentation.feature_detail.presentation.book_detail.components.CardTileComposable
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel.ChapterState
import org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel.DetailState
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.components.showLoading


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    detailState: DetailState,
    chapterState: ChapterState,
    onToggleLibrary: () -> Unit,
    onDownload: () -> Unit,
    onRead: () -> Unit,
    onSummaryExpand: () -> Unit,
    onRefresh: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onWebView: () -> Unit,
    onChapterContent: () -> Unit,
    book: Book,
    onTitle:(String) -> Unit,
    scaffoldState:ScaffoldState,
) {
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = detailState.detailIsLocalLoading || detailState.detailIsRemoteLoading || chapterState.chapterIsLoading)

    val source = detailState.source
    val chapters = chapterState.chapters

    if (detailState.detailIsLocalLoading) {
        showLoading()
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
                        isInLibrary = detailState.inLibrary,
                        onDownload = {
                            onDownload()
                        },
                        isRead = chapterState.chapters.any { it.readAt != 0L },
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
                        isSummaryExpanded = detailState.expandedSummary,
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
                                DotsFlashing(chapterState.chapterIsLoading)

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






