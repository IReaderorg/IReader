package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.bookDetails.BookDetailScreen
import org.ireader.bookDetails.viewmodel.BookDetailViewModel
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.getUrlWithoutDomain
import org.ireader.core_api.source.HttpSource
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.presentation.EmptyScreenComposable

object BookDetailScreenSpec : ScreenSpec {

    override val navHostRoute: String = "book_detail_route/{bookId}/{sourceId}"

    fun buildRoute(sourceId: Long, bookId: Long): String {
        return "book_detail_route/$bookId/$sourceId"
    }

    override val arguments: List<NamedNavArgument> =
        listOf(
            NavigationArgs.bookId,
            NavigationArgs.sourceId,
        )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader.org/book_detail_route/{bookId}/{sourceId}"
            NavigationArgs.bookId
            NavigationArgs.sourceId
        }
    )


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val scaffoldStates = rememberScaffoldState()
        val viewModel: BookDetailViewModel = hiltViewModel()
        val context = LocalContext.current
        val state = viewModel
        val book = state.book
        val source = state.source
        val scope = rememberCoroutineScope()
        if (book != null) {
            BookDetailScreen(
                navController = navController,
                onToggleLibrary = {
                    viewModel.toggleInLibrary(book = book)
                },
                onDownload = {
                    viewModel.startDownloadService(book = book)
                },
                onRead = {
                    if (source != null) {
                        if (viewModel.chapters.any { it.readAt != 0L } && viewModel.chapters.isNotEmpty()) {
                            navController.navigate(ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = source.id,
                                chapterId = LAST_CHAPTER,
                            ))
                        } else if (viewModel.chapters.isNotEmpty()) {
                            navController.navigate(ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = source.id,
                                chapterId = viewModel.chapters.first().id,
                            ))
                        } else {
                            scope.launch {
                                viewModel.showSnackBar(UiText.StringResource(org.ireader.core.R.string.no_chapter_is_available))
                            }
                        }
                    } else {
                        scope.launch {
                            viewModel.showSnackBar(UiText.StringResource(org.ireader.core.R.string.source_not_available))
                        }
                    }
                },
                onSummaryExpand = {
                    viewModel.expandedSummary = !viewModel.expandedSummary
                },
                onRefresh = {
                    if (source != null) {
                        scope.launch {
                            viewModel.getRemoteBookDetail(book, source = source)
                            viewModel.getRemoteChapterDetail(book, source)
                        }
                    }
                },
                onWebView = {
                    if (source != null && source is HttpSource)
                        navController.navigate(
                            WebViewScreenSpec.buildRoute(
                                url = (source).baseUrl + getUrlWithoutDomain(
                                    book.link),
                            )
                        )

                },
                onSwipeRefresh = {
                    source?.let {
                        scope.launch {
                            viewModel.getRemoteChapterDetail(book, source)

                        }
                    }
                },
                onChapterContent = {
                    if (source != null) {
                        navController.navigate(ChapterScreenSpec.buildRoute(bookId = book.id,
                            sourceId = source.id))
                    }
                },
                book = book,
                detailState = viewModel,
                onTitle = {
                    try {
                        navController.navigate(GlobalSearchScreenSpec.buildRoute(query = it))
                    } catch (e: Throwable) {
                    }
                },
                scaffoldState = scaffoldStates,
                chapterState = viewModel
            )
        } else {
            EmptyScreenComposable(navController = navController,
                errorResId = org.ireader.core.R.string.something_is_wrong_with_this_book)
        }
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
//        LaunchedEffect(key1 = true) {
//            book?.let {
//                if (source != null) {
//                    viewModel.getLocalBookById(bookId = book.id, source = source)
//                }
//                viewModel.getLocalChaptersByBookId(bookId = book.id)
//            }
//
//        }
    }

}