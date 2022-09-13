package ireader.ui.book

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ireader.ui.book.components.ActionHeader
import ireader.ui.book.components.BookHeader
import ireader.ui.book.components.BookHeaderImage
import ireader.ui.book.components.BookSummaryInfo
import ireader.ui.book.components.ChapterBar
import ireader.ui.book.components.ChapterDetailBottomBar
import ireader.ui.book.viewmodel.BookDetailViewModel
import ireader.common.models.entities.Book
import ireader.common.models.entities.Chapter
import ireader.ui.component.components.ChapterRow
import ireader.ui.component.list.scrollbars.VerticalFastScroller
import ireader.ui.component.reusable_composable.AppTextField
import ireader.core.api.source.Source
import ireader.core.ui.preferences.ChapterDisplayMode
import ireader.core.ui.utils.isScrolledToEnd
import ireader.core.ui.utils.isScrollingUp
import ireader.presentation.R


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
    onWebView: () -> Unit
) {
    val context = LocalContext.current
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = vm.detailIsLoading || vm.chapterIsLoading)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = 8.dp,
            )
        },

        ) {
        Scaffold(
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
            val dialogScrollState = rememberScrollState()
            Box {
                if (vm.showDialog) {
                    AlertDialog(onDismissRequest = { vm.showDialog = false }, confirmButton = {
                    }, title = {
                        Card(
                            modifier = Modifier
                                .heightIn(max = 350.dp, min = 200.dp)
                                .verticalScroll(dialogScrollState)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(text = vm.book.toString())
                            }
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
            }
        }

    }
}

