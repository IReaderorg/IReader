package ireader.presentation.ui.book


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.presentation.ui.book.components.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppTextField
import kotlinx.coroutines.launch

@OptIn(
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class,

        )
@Composable
fun BookDetailScreen(
    vm: BookDetailViewModel,
    onSummaryExpand: () -> Unit,
    book: Book,
    onTitle: (String) -> Unit,
    source: Source?,
    isSummaryExpanded: Boolean,
    appbarPadding: Dp,
    onItemClick: (Chapter) -> Unit,
    onLongItemClick: (Chapter) -> Unit,
    onSortClick: () -> Unit,
    chapters: State<List<Chapter>>,
    scrollState: LazyListState,
    onMap: () -> Unit,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    onCopyTitle: (bookTitle: String) -> Unit,
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current



            if (vm.showDialog) {
                EditInfoAlertDialog(onStateChange = {
                    vm.showDialog = it
                }, book, onConfirm = {
                    vm.scope.launch {
                        vm.insertUseCases.insertBook(it)
                    }
                })
            }
            IVerticalFastScroller(listState = scrollState) {

                LazyColumn(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Top,
                        state = scrollState
                ) {
                    item {
                        Box {
                           BookHeaderImage(book = book,)

                            BookHeader(
                                    book = book,
                                    onTitle = onTitle,
                                    source = source,
                                    appbarPadding = appbarPadding,
                                    onCopyTitle = onCopyTitle
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
                                onSummaryExpand = onSummaryExpand,
                                onCopy = onCopyTitle
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

                Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                    ChapterDetailBottomBar(
                            vm,
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

