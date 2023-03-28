package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ireader.core.source.CatalogSource
import ireader.core.source.HttpSource
import ireader.core.source.model.ChapterInfo
import ireader.domain.models.entities.Book
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.book.BookDetailScreen
import ireader.presentation.ui.book.BookDetailTopAppBar
import ireader.presentation.ui.book.components.ChapterCommandBottomSheet
import ireader.presentation.ui.book.components.ChapterScreenBottomTabComposable
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.utils.ActivityResultListener
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.TransparentStatusBar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.core.utils.isScrolledToEnd
import ireader.presentation.ui.core.utils.isScrollingUp
import kotlinx.coroutines.launch

data class BookDetailScreenSpec constructor(
    val bookId: Long,
) : VoyagerScreen() {


    override val key: ScreenKey = "Detail_Screen#$bookId"

    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class
    )
    @Composable
    override fun Content(
    ) {
        val navigator = LocalNavigator.currentOrThrow
        val vm: BookDetailViewModel =
            getIViewModel(parameters = BookDetailViewModel.Param(bookId))
        val snackbarHostState = SnackBarListener(vm = vm)
        val state = vm
        val book = state.booksState.book
        val source = state.catalogSource?.source
        val catalog = state.catalogSource
        val scope = rememberCoroutineScope()
        val chapters = vm.getChapters(book?.id)
        val scrollState = rememberLazyListState();
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val refreshing = vm.detailIsLoading || vm.chapterIsLoading
        val swipeRefreshState =
            rememberPullRefreshState(refreshing = refreshing, onRefresh = {
                scope.launch {
                    vm.getRemoteChapterDetail(book, catalog)
                }
            })
        val topbarState = rememberTopAppBarState()
        val snackBarHostState = SnackBarListener(vm)
        Box(modifier = Modifier.fillMaxSize()) {
        IModalSheets(
            sheetContent = {
                val detailState = vm.state
                val book = vm.booksState.book
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
                                        vm.scope.launch {
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
            },
            bottomSheetState = sheetState
        ) {

            TransparentStatusBar {
                IScaffold(
                    modifier = Modifier.pullRefresh(swipeRefreshState),
                    topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                        topbarState
                    ),
                    snackbarHostState = snackbarHostState,
                    topBar = { scrollBehavior ->
                        val onShare = ActivityResultListener(onSuccess = { uri ->
                            vm.booksState.book?.let { book ->
                                vm.createEpub(book, uri, currentEvent = {
                                    vm.showSnackBar(UiText.DynamicString(it))
                                })
                            }
                            vm.showSnackBar(UiText.MStringResource(MR.strings.success))
                        }) { e ->
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                        val globalScope = LocalGlobalCoroutineScope.currentOrThrow

                        BookDetailTopAppBar(
                            scrollBehavior = scrollBehavior,
                            onRefresh = {
                                globalScope.launch {
                                    if (book != null) {
                                        vm.getRemoteBookDetail(book, source = catalog)
                                        vm.getRemoteChapterDetail(book, catalog)
                                    }
                                }
                            },
                            onPopBackStack = {
                                popBackStack(navigator)
                            },
                            source = vm.source,
                            onCommand = {
                                globalScope.launch {
                                    sheetState.show()
                                }
                            },
                            onShare = {
                                book?.let { book ->
                                    vm.createEpub.onEpubCreateRequested(book) {
                                        onShare.launch(it)
                                    }
                                }
                            },
                            state = vm,
                            onSelectBetween = {
                                val ids: List<Long> =
                                    vm.chapters.map { it.id }
                                        .filter { it in vm.selection }.distinct().sortedBy { it }
                                        .let {
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
                            paddingValues = PaddingValues(0.dp),
                            onDownload = {
                                if (book != null) {
                                    vm.startDownloadService(book = book)
                                }
                            },
                            onInfo = {
                                vm.showDialog = true
                            }
                        )
                    },
                    floatingActionButton = {
                        if (!vm.hasSelection) {
                            ExtendedFloatingActionButton(
                                text = {
                                    val id = if (chapters.value.any { it.read }) {
                                        MR.strings.resume
                                    } else {
                                        MR.strings.start
                                    }
                                    Text(text = localize(id))
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    if (catalog != null && book != null) {
                                        if (vm.chapters.any { it.read } && vm.chapters.isNotEmpty()) {
                                            navigator.push(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
                                                    chapterId = LAST_CHAPTER,
                                                )
                                            )
                                        } else if (vm.chapters.isNotEmpty()) {
                                            navigator.push(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
                                                    chapterId = vm.chapters.first().id,
                                                )
                                            )
                                        } else {
                                            scope.launch {
                                                vm.showSnackBar(UiText.MStringResource(MR.strings.no_chapter_is_available))
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            vm.showSnackBar(UiText.MStringResource(MR.strings.source_not_available))
                                        }
                                    }
                                },
                                expanded = scrollState.isScrollingUp() || scrollState.isScrolledToEnd(),
                                modifier = Modifier,
                                shape = CircleShape

                            )
                        }
                    }
                ) { scaffoldPadding ->

                    BookDetailScreen(
                        onSummaryExpand = {
                            vm.expandedSummary = !vm.expandedSummary
                        },
                        book = book ?: Book(key = "", sourceId = 0, title = ""),
                        vm = vm,
                        onTitle = {
                            try {
                                navigator.push(
                                    GlobalSearchScreenSpec(
                                        query = it
                                    )
                                )
                            } catch (e: Throwable) {
                            }
                        },
                        isSummaryExpanded = vm.expandedSummary,
                        source = vm.source,
                        appbarPadding = scaffoldPadding.calculateTopPadding(),
                        onItemClick = { chapter ->
                            if (vm.selection.isEmpty()) {
                                if (book != null) {
                                    when (chapter.type) {
                                        ChapterInfo.MOVIE -> {
                                            navigator.push(
                                                VideoScreenSpec(
                                                    chapterId = chapter.id,
                                                )
                                            )

                                        }
                                        else -> {
                                            navigator.push(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
                                                    chapterId = chapter.id,
                                                )
                                            )
                                        }
                                    }

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
                        onSortClick = {
                            scope.launch {
                                vm.chapterMode = true
                                sheetState.show()
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
                                navigator.push(
                                    WebViewScreenSpec(
                                        url = book.key,
                                        sourceId = book.sourceId,
                                        bookId = book.id,
                                        chapterId = null,
                                        enableChaptersFetch = true,
                                        enableBookFetch = true,
                                        enableChapterFetch = false
                                    )
                                )
                            }
                        },
                        onFavorite = {
                            if (book != null) {
                                vm.toggleInLibrary(book = book)
                            }
                        },
                        onCopyTitle = {
                            vm.platformHelper.copyToClipboard(it, it)
                        },
                        topAppBarState = topbarState

                    )

                }
                }
            }
            if (refreshing) {
                PullRefreshIndicator(
                    refreshing,
                    swipeRefreshState,
                    Modifier.align(Alignment.TopCenter),

                    )
            }

        }
    }

}
