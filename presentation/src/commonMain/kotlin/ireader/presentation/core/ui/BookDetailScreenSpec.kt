package ireader.presentation.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.CatalogSource
import ireader.core.source.HttpSource
import ireader.domain.models.entities.Book
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_chapter_is_available
import ireader.i18n.resources.resume
import ireader.i18n.resources.source_not_available
import ireader.i18n.resources.start
import ireader.i18n.resources.success
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.book.BookDetailScreen
import ireader.presentation.ui.book.BookDetailTopAppBar
import ireader.presentation.ui.book.components.ChapterCommandBottomSheet
import ireader.presentation.ui.book.components.ChapterScreenBottomTabComposable
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.utils.ActivityResultListener
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.TransparentStatusBar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.core.utils.isScrolledToEnd
import ireader.presentation.ui.core.utils.isScrollingUp
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

data class BookDetailScreenSpec constructor(
    val bookId: Long,
) {

    
    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class,
    )
    @Composable
    fun Content(
    ) {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: BookDetailViewModel = getIViewModel(
            key = bookId,
            parameters = { parametersOf(BookDetailViewModel.Param(bookId)) }
        )
        val snackbarHostState = SnackBarListener(vm = vm)
        val state = vm
        val book = state.booksState.book
        val source = state.catalogSource?.source
        val catalog = state.catalogSource
        val scope = rememberCoroutineScope()
        val chapters = vm.getChapters(book?.id)
        val scrollState = rememberLazyListState(
            initialFirstVisibleItemIndex = vm.savedScrollIndex,
            initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
        )
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        
        // Save scroll position when it changes
        androidx.compose.runtime.LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
            vm.saveScrollPosition(
                scrollState.firstVisibleItemIndex,
                scrollState.firstVisibleItemScrollOffset
            )
        }
        
        // Reset scroll position when screen is disposed
        androidx.compose.runtime.DisposableEffect(Unit) {
            onDispose {
                vm.resetScrollPosition()
            }
        }
        val refreshing = vm.detailIsLoading || vm.chapterIsLoading
        val swipeRefreshState =
            rememberPullRefreshState(refreshing = refreshing, onRefresh = {
                scope.launch {
                    vm.getRemoteChapterDetail(book, catalog)
                }
            })
        val topbarState = rememberTopAppBarState()
        val snackBarHostState = SnackBarListener(vm)
        
        // State to control when to trigger epub export
        var triggerEpubExport = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        var triggerEpubExportFromDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        
        // ActivityResultListeners must be created at the top level of the Composable
        val onShare = ActivityResultListener(onSuccess = { uri ->
            vm.booksState.book?.let { book ->
                vm.createEpub(book, uri, currentEvent = {
                    vm.showSnackBar(UiText.DynamicString(it))
                })
            }
            vm.showSnackBar(UiText.MStringResource(Res.string.success))
        }) { e ->
            vm.showSnackBar(UiText.ExceptionString(e))
        }
        
        val onShareEpub = ActivityResultListener(onSuccess = { uri ->
            vm.booksState.book?.let { book ->
                vm.createEpub(book, uri, currentEvent = {
                    vm.showSnackBar(UiText.DynamicString(it))
                })
            }
            vm.showSnackBar(UiText.MStringResource(Res.string.success))
        }) { e ->
            vm.showSnackBar(UiText.ExceptionString(e))
        }
        
        // Trigger epub export when requested from menu
        if (triggerEpubExport.value && book != null) {
            vm.createEpub.onEpubCreateRequested(book) { uri ->
                onShare.launch(uri)
                triggerEpubExport.value = false
            }
        }
        
        // Trigger epub export when requested from End of Life dialog
        if (triggerEpubExportFromDialog.value && book != null) {
            vm.createEpub.onEpubCreateRequested(book) { uri ->
                onShareEpub.launch(uri)
                triggerEpubExportFromDialog.value = false
            }
        }
        
        // Handle back button to close modal sheet instead of closing screen
        // BackHandler removed - Android-specific, implement in androidMain if needed
        
        Box(modifier = Modifier.fillMaxSize()) {
        IModalSheets(
            sheetContent = {
                val detailState = vm.state
                val book = vm.booksState.book
                val catalog = vm.catalogSource

                detailState.source.let { source ->
                    if (vm.chapterMode) {
                        val pagerState = rememberPagerState(
                            initialPage = 0,
                            initialPageOffsetFraction = 0f,
                            pageCount = {
                                3
                            }
                        )
                        ChapterScreenBottomTabComposable(
                            modifier = it,
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
                                modifier = it,
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
            val scrollBehavior = if (isTableUi()) {
                TopAppBarDefaults.pinnedScrollBehavior()
            } else {
                TopAppBarDefaults.enterAlwaysScrollBehavior(
                    state = topbarState,
                    canScroll = { true }
                )
            }
            TransparentStatusBar {
                IScaffold(
                    modifier = Modifier.pullRefresh(swipeRefreshState),
                    topBarScrollBehavior = scrollBehavior,
                    snackbarHostState = snackbarHostState,
                    topBar = { scrollBehavior ->
                        val globalScope = requireNotNull(LocalGlobalCoroutineScope.current) { "LocalGlobalCoroutineScope not provided" }

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
                                navController.popBackStack()
                            },
                            source = vm.source,
                            onCommand = {
                                scope.launch {
                                    sheetState.show()
                                }
                            },
                            onShare = {
                                triggerEpubExport.value = true
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
                            },
                            onArchive = {
                                book?.let { vm.archiveBook(it) }
                            },
                            onUnarchive = {
                                book?.let { vm.unarchiveBook(it) }
                            },
                            isArchived = book?.isArchived ?: false,
                            onShareBook = {
                                vm.shareBook()
                            },
                            onExportEpub = {
                                vm.showEpubExportDialog = true
                            }
                        )
                    },
                    floatingActionButton = {
                        if (!vm.hasSelection) {
                            ExtendedFloatingActionButton(
                                text = {
                                    val id = if (chapters.value.any { it.read }) {
                                        Res.string.resume
                                    } else {
                                        Res.string.start
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
                                            navController.navigateTo(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
                                                    chapterId = LAST_CHAPTER,
                                                )
                                            )
                                        } else if (vm.chapters.isNotEmpty()) {
                                            navController.navigateTo(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
                                                    chapterId = vm.chapters.first().id,
                                                )
                                            )
                                        } else {
                                            scope.launch {
                                                vm.showSnackBar(UiText.MStringResource(Res.string.no_chapter_is_available))
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            vm.showSnackBar(UiText.MStringResource(Res.string.source_not_available))
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
                                navController.navigateTo(
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
                                            navController.navigateTo(
                                                ReaderScreenSpec(
                                                    bookId = book.id,
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
                                navController.navigateTo(
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
        
        // End of Life Options Dialog
        if (vm.showEndOfLifeDialog) {
            ireader.presentation.ui.book.components.EndOfLifeOptionsDialog(
                onDismiss = { vm.hideEndOfLifeOptionsDialog() },
                onArchive = {
                    book?.let { vm.archiveBook(it) }
                },
                onExportEpub = {
                    triggerEpubExportFromDialog.value = true
                }
            )
        }
    }

}
