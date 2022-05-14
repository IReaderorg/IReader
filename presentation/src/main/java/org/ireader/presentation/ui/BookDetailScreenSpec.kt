package org.ireader.presentation.ui

import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.bookDetails.BookDetailScreen
import org.ireader.bookDetails.viewmodel.BookDetailViewModel
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.common_extensions.getUrlWithoutDomain
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.common_resources.UiText
import org.ireader.components.components.EmptyScreenComposable
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.domain.ui.NavigationArgs

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

    @OptIn(
        ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val modalSheetState: ModalBottomSheetState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val vm: BookDetailViewModel = hiltViewModel()
        val context = LocalContext.current
        val state = vm
        val book = state.book
        val source = state.catalogSource?.source
        val catalog = state.catalogSource
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }

        if (book != null) {
            BookDetailScreen(
                onToggleLibrary = {
                    vm.toggleInLibrary(book = book)
                },
                onDownload = {
                    vm.startDownloadService(book = book)
                },
                onRead = {
                    if (catalog != null) {
                        if (vm.chapters.any { it.read } && vm.chapters.isNotEmpty()) {
                            navController.navigate(
                                ReaderScreenSpec.buildRoute(
                                    bookId = book.id,
                                    sourceId = catalog.sourceId,
                                    chapterId = LAST_CHAPTER,
                                )
                            )
                        } else if (vm.chapters.isNotEmpty()) {
                            navController.navigate(
                                ReaderScreenSpec.buildRoute(
                                    bookId = book.id,
                                    sourceId = catalog.sourceId,
                                    chapterId = vm.chapters.first().id,
                                )
                            )
                        } else {
                            scope.launch {
                                vm.showSnackBar(UiText.StringResource(org.ireader.core.R.string.no_chapter_is_available))
                            }
                        }
                    } else {
                        scope.launch {
                            vm.showSnackBar(UiText.StringResource(org.ireader.core.R.string.source_not_available))
                        }
                    }
                },
                onSummaryExpand = {
                    vm.expandedSummary = !vm.expandedSummary
                },
                onRefresh = {
                    scope.launch {
                        vm.getRemoteBookDetail(book, source = catalog)
                        vm.getRemoteChapterDetail(book, catalog)
                    }
                },
                onWebView = {
                    if (source != null && source is HttpSource)
                        navController.navigate(
                            WebViewScreenSpec.buildRoute(
                                url = (source).baseUrl + getUrlWithoutDomain(
                                    book.link,
                                ),
                                sourceId = book.sourceId,
                                bookId = book.id,
                                chapterId = null
                            )
                        )
                },
                onSwipeRefresh = {
                    scope.launch {
                        vm.getRemoteChapterDetail(book, catalog)
                    }
                },
                onChapterContent = {
                    if (catalog != null) {
                        navController.navigate(
                            ChapterScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = catalog.sourceId
                            )
                        )
                    }
                },
                book = book,
                detailState = vm,
                onTitle = {
                    try {
                        navController.navigate(GlobalSearchScreenSpec.buildRoute(query = it))
                    } catch (e: Throwable) {
                    }
                },
                snackBarHostState = snackBarHostState,
                chapterState = vm,
                onPopBackStack = {
                    navController.popBackStack()
                },
                modalBottomSheetState = modalSheetState,
                onCommand = {
                    scope.launch {
                        modalSheetState.show()
                    }
                },
                onUpdate = {
                    vm.modifiedCommands = it
                },
                onReset = {
                    source.let { source ->
                        if (source is CatalogSource) {
                            vm.modifiedCommands = source.getCommands()
                        }
                    }
                },
                onFetch = {
                    source?.let { source ->
                        vm.viewModelIOCoroutine {
                            vm.getRemoteChapterDetail(
                                book,
                                catalog,
                                vm.modifiedCommands.filter { !it.isDefaultValue() }
                            )
                        }
                    }
                }
            )
        } else {
            EmptyScreenComposable(

                errorResId = org.ireader.core.R.string.something_is_wrong_with_this_book,
                onPopBackStack = {
                    navController.popBackStack()
                }
            )
        }
    }
}