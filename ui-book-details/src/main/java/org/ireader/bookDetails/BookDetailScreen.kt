package org.ireader.bookDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
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
import org.ireader.bookDetails.components.BookDetailScreenBottomBar
import org.ireader.bookDetails.viewmodel.ChapterState
import org.ireader.bookDetails.viewmodel.DetailState
import org.ireader.common_models.entities.Book
import org.ireader.components.components.ISnackBarHost
import org.ireader.components.components.showLoading
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.core_ui.ui_components.CardTile
import org.ireader.core_ui.ui_components.DotsFlashing

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
    onTitle: (String) -> Unit,
    scaffoldState: ScaffoldState,
) {
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = detailState.detailIsLoading)

    val source = detailState.source
    val chapters = chapterState.chapters

    if (detailState.detailIsLoading) {
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
                        isInLibrary = book.favorite,
                        onDownload = {
                            onDownload()
                        },
                        isRead = chapterState.chapters.any { it.readAt != 0L },
                        onRead = {
                            onRead()
                        },
                        isInLibraryInProgress = detailState.inLibraryLoading
                    )
                }
            }
        ) { padding ->

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
                        onSummaryExpand = onSummaryExpand,
                        onRefresh = onRefresh,
                        isSummaryExpanded = detailState.expandedSummary,
                        book = book,
                        source = source,
                    )

                    CardTile(
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
                        }
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
