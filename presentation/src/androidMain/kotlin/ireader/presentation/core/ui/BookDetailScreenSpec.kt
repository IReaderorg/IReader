package ireader.presentation.core.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ireader.core.source.HttpSource
import ireader.core.source.model.ChapterInfo
import ireader.domain.models.entities.Book
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.book.BookDetailScreen
import ireader.presentation.ui.book.BookDetailTopAppBar
import ireader.presentation.ui.book.components.ChapterCommandBottomSheet
import ireader.presentation.ui.book.components.ChapterScreenBottomTabComposable
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.TransparentStatusBar
import ireader.presentation.ui.core.ui.SnackBarListener
import kotlinx.coroutines.launch
import ireader.i18n.resources.MR
data class BookDetailScreenSpec(
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


        val topbarState = rememberTopAppBarState()
        val snackBarHostState = SnackBarListener(vm)
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
                        if (source is ireader.core.source.CatalogSource) {
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
                    topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                        topbarState
                    ),
                    snackbarHostState = snackbarHostState,
                    topBar = { scrollBehavior ->
                        val globalScope = LocalGlobalCoroutineScope.currentOrThrow
                        val onShare =
                            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
                                if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                                    val uri = resultIntent.data!!.data!!
                                    globalScope.launchIO {
                                        try {
                                            vm.booksState.book?.let {
                                                vm.showSnackBar(UiText.MStringResource(MR.strings.wait))
                                                vm.createEpub(it, uri)
                                                vm.showSnackBar(UiText.MStringResource(MR.strings.success))
                                            }
                                        } catch (e: Throwable) {
                                            vm.showSnackBar(UiText.ExceptionString(e))
                                        }
                                    }
                                }
                            }
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
                                    vm.onEpubCreateRequested(book) {
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
                    }
                ) { scaffoldPadding ->
                    BookDetailScreen(
                        modifier = Modifier,
                        onSummaryExpand = {
                            vm.expandedSummary = !vm.expandedSummary
                        },
                        onSwipeRefresh = {
                            scope.launch {
                                vm.getRemoteChapterDetail(book, catalog)
                            }
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
                        snackBarHostState = snackBarHostState,
                        modalBottomSheetState = sheetState,
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
                        onRead = {
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
                                        enableBookFetch = true
                                    )
                                )
                            }
                        },
                        onFavorite = {
                            if (book != null) {
                                vm.toggleInLibrary(book = book)
                            }
                        },

                    )
                }
            }
        }
    }

}
