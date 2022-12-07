package ireader.presentation.ui.book

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import ireader.common.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.presentation.R
import ireader.presentation.ui.book.components.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.list.scrollbars.VerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.core.utils.isScrolledToEnd
import ireader.presentation.ui.core.utils.isScrollingUp
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,

    )
@Composable
fun BookDetailScreen(
        modifier: Modifier = Modifier,
        vm: BookDetailViewModel,
        modalBottomSheetState: ModalBottomSheetState,
        onSummaryExpand: () -> Unit,
        onSwipeRefresh: () -> Unit,
        book: Book,
        onTitle: (String) -> Unit,
        snackBarHostState: SnackbarHostState,
        source: Source?,
        isSummaryExpanded: Boolean,
        appbarPadding: Dp,
        onItemClick: (Chapter) -> Unit,
        onLongItemClick: (Chapter) -> Unit,
        onRead: () -> Unit,
        onSortClick: () -> Unit,
        chapters: State<List<Chapter>>,
        scrollState: LazyListState,
        onMap: () -> Unit,
        onFavorite: () -> Unit,
        onWebView: () -> Unit,
        controller: Controller
) {
    val context = LocalContext.current
    val refreshing = remember {
        derivedStateOf { vm.detailIsLoading || vm.chapterIsLoading }
    }
    val swipeRefreshState =
        rememberPullRefreshState(refreshing = refreshing.value, onRefresh = {
            onSwipeRefresh()
        })
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.pullRefresh(swipeRefreshState),
        floatingActionButton = {
            if (!vm.hasSelection) {
                ExtendedFloatingActionButton(
                    text = {
                        val id = if (chapters.value.any { it.read }) {
                            R.string.resume
                        } else {
                            R.string.start
                        }
                        Text(text = stringResource(id))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    onClick = onRead,
                    expanded = scrollState.isScrollingUp() || scrollState.isScrolledToEnd(),
                    modifier = Modifier,
                    shape = CircleShape

                )
            }
        }
    ) {
        Box {

            if (vm.showDialog) {
                EditInfoAlertDialog(onStateChange = {
                    vm.showDialog = it
                }, book, onConfirm = {
                    vm.viewModelScope.launch {
                        vm.insertUseCases.insertBook(it)
                    }
                })
            }
            VerticalFastScroller(listState = scrollState) {

                LazyColumn(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    state = scrollState
                ) {
                    item {
                        Box {

                            BookHeaderImage(book = book)
                            BookHeader(
                                book = book,
                                onTitle = onTitle,
                                source = source,
                                appbarPadding = appbarPadding
                            )
                        }
                    }
                    item {
                        ActionHeader(
                            favorite = book.favorite,
                            source = source,
                            onFavorite = onFavorite,
                            onWebView = onWebView
                        )
                    }
                    item {
                        BookSummaryInfo(
                            book = book,
                            isSummaryExpanded = isSummaryExpanded,
                            onSummaryExpand = onSummaryExpand
                        )
                    }
                    item {
                        ChapterBar(
                            vm = vm,
                            chapters = chapters.value,
                            onMap = onMap,
                            onSortClick = onSortClick
                        )
                    }
                    if (vm.searchMode) {
                        item {
                            AppTextField(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                query = vm.query ?: "",
                                onValueChange = { query ->
                                    vm.query = query
                                },
                                onConfirm = {
                                    vm.searchMode = false
                                    vm.query = null
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                            )
                        }
                    }
                    items(items = vm.chapters.reversed()) { chapter ->
                        ChapterRow(
                            modifier = Modifier,
                            chapter = chapter,
                            onItemClick = { onItemClick(chapter) },
                            isLastRead = chapter.id == vm.lastRead,
                            isSelected = chapter.id in vm.selection,
                            onLongClick = { onLongItemClick(chapter) },
                            showNumber = vm.layout == ChapterDisplayMode.ChapterNumber || vm.layout == ChapterDisplayMode.Default
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ChapterDetailBottomBar(
                        vm,
                        context,
                        onDownload = {
                        },
                        onBookmark = {
                        },
                        onMarkAsRead = {
                        },
                        visible = vm.hasSelection,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

            }
            PullRefreshIndicator(
                refreshing.value,
                swipeRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

