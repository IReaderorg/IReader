package org.ireader.presentation.feature_detail.presentation.book_detail


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.getUrlWithoutDomain
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.models.source.FetchType
import org.ireader.domain.view_models.detail.book_detail.BookDetailEvent
import org.ireader.domain.view_models.detail.book_detail.BookDetailViewModel
import org.ireader.presentation.R
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookDetailScreenBottomBar
import org.ireader.presentation.feature_detail.presentation.book_detail.components.CardTileComposable
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.components.showLoading
import org.ireader.presentation.ui.ChapterScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec
import org.ireader.presentation.ui.WebViewScreenSpec


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val book = viewModel.state.book//viewModel.book
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


    val scope = rememberCoroutineScope()
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = viewModel.state.isLocalLoading || viewModel.state.isRemoteLoading)

    val source = viewModel.state.source
    val state = viewModel.state
    val chapters = viewModel.chapterState.chapters


    val webview = viewModel.webView
    val scrollState = rememberLazyListState()
    if (state.isLocalLoading || state.isRemoteLoading) {
        showLoading()
    }
    if (book != null && state.isLocalLoaded) {
        val isWebViewEnable by remember {
            mutableStateOf(webview.originalUrl == book.link)
        }
        TransparentStatusBar {
            Scaffold(
                topBar = {},
                scaffoldState = scaffoldState,
                snackbarHost = { ISnackBarHost(snackBarHostState = it) },
                bottomBar = {
                    BookDetailScreenBottomBar(
                        onToggleInLibrary = {
                            if (!state.inLibrary) {
                                viewModel.toggleInLibrary(true, book = book)
                            } else {
                                viewModel.toggleInLibrary(false, book)
                            }
                        },
                        isInLibrary = state.inLibrary,
                        onDownload = {
                            viewModel.startDownloadService(context, book = book)
                        },
                        isRead = book.lastRead != 0L,
                        onRead = {
                            if (book.lastRead != 0L && viewModel.chapterState.chapters.isNotEmpty()) {
                                navController.navigate(ReaderScreenSpec.buildRoute(
                                    bookId = book.id,
                                    sourceId = source.sourceId,
                                    chapterId = Constants.LAST_CHAPTER,
                                ))
                            } else if (viewModel.chapterState.chapters.isNotEmpty()) {
                                navController.navigate(ReaderScreenSpec.buildRoute(
                                    bookId = book.id,
                                    sourceId = source.sourceId,
                                    chapterId = viewModel.chapterState.chapters.first().id,
                                ))
                            } else {
                                scope.launch {
                                    viewModel.showSnackBar(UiText.StringResource(R.string.no_chapter_is_available))
                                }
                            }
                        }
                    )
                }) {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        viewModel.getRemoteChapterDetail(book)
                    },
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
                    LazyColumn(state = scrollState) {
                        item {
                            BookDetailScreenLoadedComposable(
                                navController = navController,
                                onWebView = {
                                    navController.navigate(
                                        WebViewScreenSpec.buildRoute(
                                            url = viewModel.state.source.baseUrl + getUrlWithoutDomain(
                                                book.link),
                                            sourceId = viewModel.state.source.sourceId,
                                            fetchType = FetchType.DetailFetchType.index,
                                            bookId = book.id
                                        )
                                    )
                                },
                                onSummaryExpand = {
                                    viewModel.onEvent(BookDetailEvent.ToggleSummary)
                                },
                                onRefresh = {
                                    viewModel.getRemoteBookDetail(book)
                                    viewModel.getRemoteChapterDetail(book)
                                },
                                onFetch = {
                                    viewModel.getWebViewData()
                                },
                                isSummaryExpanded = viewModel.expandedSummary,
                                book = book,
                                source = source,
                                isFetchModeEnable = isWebViewEnable
                            )

                        }
                        item {
                            CardTileComposable(
                                modifier = modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    navController.navigate(ChapterScreenSpec.buildRoute(bookId = book.id,
                                        sourceId = source.sourceId))
                                },
                                title = "Contents",
                                subtitle = "${chapters.size} Chapters",
                                trailing = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "",
                                            color = MaterialTheme.colors.onBackground,
                                            style = MaterialTheme.typography.subtitle2
                                        )
                                        if (viewModel.chapterState.isLocalLoading || viewModel.chapterState.isRemoteLoading) {
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
    }
}





