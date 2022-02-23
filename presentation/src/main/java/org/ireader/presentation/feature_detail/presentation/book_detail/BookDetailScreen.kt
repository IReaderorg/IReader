package org.ireader.presentation.feature_detail.presentation.book_detail


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import org.ireader.domain.FetchType
import org.ireader.domain.view_models.detail.book_detail.BookDetailEvent
import org.ireader.domain.view_models.detail.book_detail.BookDetailViewModel
import org.ireader.presentation.R
import org.ireader.presentation.feature_detail.presentation.book_detail.components.BookDetailScreenBottomBar
import org.ireader.presentation.feature_detail.presentation.book_detail.components.CardTileComposable
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.presentation.EmptyScreenComposable
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

    val scrollState = rememberLazyListState()
    if (state.isLocalLoading) {
        showLoading()
    }
    if (book != null && source != null) {
        LaunchedEffect(key1 = true) {
            viewModel.getLocalBookById(bookId = book.id, source = source)
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
                                        sourceId = source.id,
                                        chapterId = Constants.LAST_CHAPTER,
                                    ))
                                } else if (viewModel.chapterState.chapters.isNotEmpty()) {
                                    navController.navigate(ReaderScreenSpec.buildRoute(
                                        bookId = book.id,
                                        sourceId = source.id,
                                        chapterId = viewModel.chapterState.chapters.first().id,
                                    ))
                                } else {
                                    scope.launch {
                                        viewModel.showSnackBar(UiText.StringResource(R.string.no_chapter_is_available))
                                    }
                                }
                            }
                        )
                    }
                }) {

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        source.let {
                            scope.launch {
                                viewModel.getRemoteChapterDetail(book, source)

                            }
                        }
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
                                    source.let {
                                        navController.navigate(
                                            WebViewScreenSpec.buildRoute(
                                                url = source.baseUrl + getUrlWithoutDomain(
                                                    book.link),
                                                sourceId = source.id,
                                                fetchType = FetchType.DetailFetchType.index,
                                                bookId = book.id
                                            )
                                        )
                                    }
                                },
                                onSummaryExpand = {
                                    viewModel.onEvent(BookDetailEvent.ToggleSummary)
                                },
                                onRefresh = {
                                    scope.launch {
                                        viewModel.getRemoteBookDetail(book, source = source)
                                        viewModel.getRemoteChapterDetail(book, source)
                                    }
                                },
                                isSummaryExpanded = viewModel.expandedSummary,
                                book = book,
                                source = source,
                            )

                        }
                        item {
                            CardTileComposable(
                                modifier = modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    navController.navigate(ChapterScreenSpec.buildRoute(bookId = book.id,
                                        sourceId = source.id))
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
                                        if (viewModel.chapterState.isLoading) {
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
    } else {
        EmptyScreenComposable(navController = navController,
            errorResId = R.string.something_is_wrong_with_this_book)
    }
}






