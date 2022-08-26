package org.ireader.presentation.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.bookDetails.BookDetailScreen
import org.ireader.bookDetails.BookDetailTopAppBar
import org.ireader.bookDetails.components.ChapterCommandBottomSheet
import org.ireader.bookDetails.components.ChapterScreenBottomTabComposable
import org.ireader.bookDetails.viewmodel.BookDetailViewModel
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.Book
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.Controller
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R

object BookDetailScreenSpec : ScreenSpec {

    override val navHostRoute: String = "book_detail_route/{bookId}/{sourceId}"

    fun buildRoute(sourceId: Long, bookId: Long): String {
        return "book_detail_route/$bookId/$sourceId"
    }

    override val arguments: List<NamedNavArgument> =
        listOf(
            NavigationArgs.bookId,
            NavigationArgs.sourceId,
            NavigationArgs.transparentStatusBar,
            NavigationArgs.showModalSheet,
        )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader.org/book_detail_route/{bookId}/{sourceId}"
            NavigationArgs.bookId
            NavigationArgs.sourceId
        }
    )

    @OptIn(ExperimentalMaterialApi::class)
    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: BookDetailViewModel = hiltViewModel(controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        val source = vm.source
        val catalog = vm.catalogSource
        val book = vm.book
        val context = LocalContext.current
        val state = controller.topScrollState
        val decay = rememberSplineBasedDecay<Float>()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decay, state)

        DisposableEffect(key1 = true ) {
            controller.setScrollBehavior(scrollBehavior)
            onDispose {
                val defaultBehavior = controller.scrollBehavior
                controller.setScrollBehavior(defaultBehavior)
            }
        }
        val onShare =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
                if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                    val uri = resultIntent.data!!.data!!
                    context.findComponentActivity()?.lifecycleScope?.launchIO {
                        try {
                            vm.book?.let {
                                vm.showSnackBar(UiText.StringResource(R.string.wait))
                                vm.createEpub(it, uri, context)
                                vm.showSnackBar(UiText.StringResource(R.string.success))
                            }
                        } catch (e: Throwable) {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                    }
                }
            }
        BookDetailTopAppBar(
            scrollBehavior = controller.scrollBehavior,
            onWebView = {
                if (source != null && source is HttpSource && book != null) {
                    controller.navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = book.key,
                            sourceId = book.sourceId,
                            bookId = book.id,
                            chapterId = null,
                            enableChaptersFetch = true,
                            enableBookFetch = true
                        )
                    )
                }

            },
            onRefresh = {
                scope.launch {
                    if (book != null) {
                        vm.getRemoteBookDetail(book, source = catalog)
                        vm.getRemoteChapterDetail(book, catalog)
                    }
                }
            },
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            source = vm.source,
            onCommand = {
                scope.launch {
                    controller.sheetState.show()
                }
            },
            onShare = {
                book?.let { book ->
                    vm.onEpubCreateRequested(book) {
                        onShare.launch(it)
                    }
                }
            },
            state = vm,
            onSelectBetween = {
                val ids: List<Long> =
                    vm.chapters.map { it.id }
                        .filter { it in vm.selection }.distinct().sortedBy { it }.let {
                            val list = mutableListOf<Long>()
                            val min = it.minOrNull() ?: 0
                            val max = it.maxOrNull() ?: 0
                            for (id in min..max) {
                                list.add(id)
                            }
                            list
                        }
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onClickSelectAll = {
                vm.selection.clear()
                vm.selection.addAll(vm.chapters.map { it.id })
                vm.selection.distinct()
            },
            onClickInvertSelection = {
                val ids: List<Long> =
                    vm.chapters.map { it.id }
                        .filterNot { it in vm.selection }.distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onClickCancelSelection = {
                vm.selection.clear()
            },
            paddingValues = controller.scaffoldPadding
        )
    }

//    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
//    @Composable
//    override fun BottomAppBar(
//        controller: Controller
//    ) {
//        val vm: BookDetailViewModel = hiltViewModel(controller.navBackStackEntry)
//        val detailState = vm.state
//        val chapterState = vm.chapterState
//        val book = vm.book
//        val catalog = vm.catalogSource
//        val scope = rememberCoroutineScope()
//        val context = LocalContext.current
//
//        AnimatedVisibility(
//            visible = true,
//            enter = slideInVertically(initialOffsetY = { it }),
//            exit = slideOutVertically(targetOffsetY = { it })
//        ) {
//            if (book != null) {
//                BookDetailScreenBottomBar(
//                    onToggleInLibrary = {
//                        vm.toggleInLibrary(book = book)
//                    },
//                    isInLibrary = book.favorite,
//                    onDownload = {
//                        vm.startDownloadService(book = book)
//                    },
//                    isRead = chapterState.haveBeenRead,
//                    onRead = {
//                        if (catalog != null) {
//                            if (vm.chapters.any { it.read } && vm.chapters.isNotEmpty()) {
//                                controller.navController.navigate(
//                                    ReaderScreenSpec.buildRoute(
//                                        bookId = book.id,
//                                        sourceId = catalog.sourceId,
//                                        chapterId = LAST_CHAPTER,
//                                    )
//                                )
//                            } else if (vm.chapters.isNotEmpty()) {
//                                controller.navController.navigate(
//                                    ReaderScreenSpec.buildRoute(
//                                        bookId = book.id,
//                                        sourceId = catalog.sourceId,
//                                        chapterId = vm.chapters.first().id,
//                                    )
//                                )
//                            } else {
//                                scope.launch {
//                                    vm.showSnackBar(UiText.StringResource(R.string.no_chapter_is_available))
//                                }
//                            }
//                        } else {
//                            scope.launch {
//                                vm.showSnackBar(UiText.StringResource(R.string.source_not_available))
//                            }
//                        }
//                    },
//                    isInLibraryInProgress = detailState.inLibraryLoading
//                )
//            }
//        }
//    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
    @ExperimentalMaterial3Api
    @Composable
    override fun BottomModalSheet(
        controller: Controller
    ) {
        val vm: BookDetailViewModel = hiltViewModel(controller.navBackStackEntry)
        val detailState = vm.state
        val book = vm.book
        val catalog = vm.catalogSource

        detailState.source.let { source ->
            if (vm.chapterMode) {

                val pagerState = rememberPagerState()
                ChapterScreenBottomTabComposable(
                    pagerState = pagerState,
                    filters = vm.filters.value,
                    toggleFilter = {
                        vm.toggleFilter(it.type)
                    },
                    onSortSelected = {
                        vm.toggleSort(it.type)
                    },
                    sortType = vm.sorting.value,
                    isSortDesc = vm.isAsc,
                    onLayoutSelected = { layout ->
                        vm.layout = layout
                    },
                    layoutType = vm.layout,
                    vm = vm
                )
            } else {
                if (source is CatalogSource) {
                    ChapterCommandBottomSheet(
                        onFetch = {
                            source.let { source ->
                                vm.viewModelIOCoroutine {
                                    if (book != null) {
                                        vm.getRemoteChapterDetail(
                                            book,
                                            catalog,
                                            vm.modifiedCommands.filter { !it.isDefaultValue() }
                                        )
                                    }
                                }
                            }
                        },
                        onReset = {
                            source.let { source ->
                                vm.modifiedCommands = source.getCommands()
                            }
                        },
                        onUpdate = {
                            vm.modifiedCommands = it
                        },
                        detailState.modifiedCommands
                    )
                }
            }

        }
    }

    @OptIn(
        ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: BookDetailViewModel = hiltViewModel(controller.navBackStackEntry)
        val context = LocalContext.current
        LaunchedEffect(key1 = true) {
            vm.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        controller.snackBarHostState.showSnackbar(
                            event.uiText.asString(context)
                        )
                    }
                    else -> {}
                }
            }
        }
        val state = vm
        val book = state.book
        val source = state.catalogSource?.source
        val catalog = state.catalogSource
        val scope = rememberCoroutineScope()
        val chapters = vm.getChapters(book?.id)
        val scrollState = rememberLazyListState();

        DisposableEffect(key1 = true) {
            controller.requestedHideSystemStatusBar(true)
            onDispose {
                controller.requestedHideSystemStatusBar(false)
            }
        }
        LaunchedEffect(vm.hasSelection) {
            controller.requestedHideSystemStatusBar(!vm.hasSelection)
        }

        BookDetailScreen(
            modifier = Modifier.padding(bottom = controller.scaffoldPadding.calculateBottomPadding()),
            onSummaryExpand = {
                vm.expandedSummary = !vm.expandedSummary
            },
            onSwipeRefresh = {
                scope.launch {
                    vm.getRemoteChapterDetail(book, catalog)
                }
            },
            onChapterContent = {
                if (catalog != null && book != null) {
                    controller.navController.navigate(
                        ChapterScreenSpec.buildRoute(
                            bookId = book.id,
                            sourceId = catalog.sourceId
                        )
                    )
                }
            },
            book = book ?: Book(key = "", sourceId = 0, title = ""),
            vm = vm,
            onTitle = {
                try {
                    controller.navController.navigate(GlobalSearchScreenSpec.buildRoute(query = it))
                } catch (e: Throwable) {
                }
            },
            snackBarHostState = controller.snackBarHostState,
            modalBottomSheetState = controller.sheetState,
            isSummaryExpanded = vm.expandedSummary,
            source = vm.source,
            appbarPadding = controller.scaffoldPadding.calculateTopPadding(),
            onItemClick = { chapter ->
                if (vm.selection.isEmpty()) {
                    if (book != null) {
                        controller.navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = book.sourceId,
                                chapterId = chapter.id,
                            )
                        )
                    }
                } else {
                    when (chapter.id) {
                        in vm.selection -> {
                            vm.selection.remove(chapter.id)
                        }
                        else -> {
                            vm.selection.add(chapter.id)
                        }
                    }
                }
            },
            onLongItemClick = { chapter ->
                when (chapter.id) {
                    in vm.selection -> {
                        vm.selection.remove(chapter.id)
                    }
                    else -> {
                        vm.selection.add(chapter.id)
                    }
                }
            },
            onRead = {
                if (catalog != null && book != null) {
                    if (vm.chapters.any { it.read } && vm.chapters.isNotEmpty()) {
                        controller.navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = catalog.sourceId,
                                chapterId = LAST_CHAPTER,
                            )
                        )
                    } else if (vm.chapters.isNotEmpty()) {
                        controller.navController.navigate(
                            ReaderScreenSpec.buildRoute(
                                bookId = book.id,
                                sourceId = catalog.sourceId,
                                chapterId = vm.chapters.first().id,
                            )
                        )
                    } else {
                        scope.launch {
                            vm.showSnackBar(UiText.StringResource(R.string.no_chapter_is_available))
                        }
                    }
                } else {
                    scope.launch {
                        vm.showSnackBar(UiText.StringResource(R.string.source_not_available))
                    }
                }
            },
            onSortClick = {
                scope.launch {
                    vm.chapterMode = true
                    controller.sheetState.show()
                }
            },
            chapters = chapters,
            scrollState = scrollState,
            onMap = {
                scope.launch {
                    try {
                        scrollState?.scrollToItem(
                            vm.getLastChapterIndex(),
                            -scrollState.layoutInfo.viewportEndOffset / 2
                        )
                    } catch (e: Throwable) {
                    }
                }
            },
            onWebView = {
                if (source != null && source is HttpSource && book != null) {
                    controller.navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = book.key,
                            sourceId = book.sourceId,
                            bookId = book.id,
                            chapterId = null,
                            enableChaptersFetch = true,
                            enableBookFetch = true
                        )
                    )
                }
            },
            onFavorite = {
                if (book != null) {
                    vm.toggleInLibrary(book = book)
                }
            }

        )
    }


}
