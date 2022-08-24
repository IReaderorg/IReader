package org.ireader.bookDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.bookDetails.components.BookHeader
import org.ireader.bookDetails.components.BookHeaderImage
import org.ireader.bookDetails.components.BookSummaryInfo
import org.ireader.bookDetails.components.ChapterDetailBottomBar
import org.ireader.bookDetails.viewmodel.BookDetailViewModel
import org.ireader.common_extensions.isScrolledToEnd
import org.ireader.common_extensions.isScrollingUp
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.ChapterRow
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.core_api.source.Source
import org.ireader.core_ui.preferences.ChapterDisplayMode
import org.ireader.ui_book_details.R

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
    onChapterContent: () -> Unit,
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
    onMap: () -> Unit
) {
    val context = LocalContext.current
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = vm.detailIsLoading)
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
                        modifier = Modifier
                            .padding(
                                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                                    .asPaddingValues()
                            ),
                        shape = CircleShape

                    )
                }
            }
        ) { paddingValues ->
            Box {

                LazyColumnScrollbar(listState = scrollState) {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
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
                            BookSummaryInfo(
                                book = book,
                                isSummaryExpanded = isSummaryExpanded,
                                onSummaryExpand = onSummaryExpand
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "${chapters.value.size.toString()} Chapters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row {
                                    AppIconButton(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search),
                                        onClick = {
                                            vm.searchMode = !vm.searchMode
                                        },
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Filled.Place,
                                        contentDescription = stringResource(R.string.find_current_chapter),
                                        onClick = onMap
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.Sort,
                                        onClick = onSortClick
                                    )
                                }

                            }
                        }
                        if (vm.searchMode) {
                            item {
                                AppTextField(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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




