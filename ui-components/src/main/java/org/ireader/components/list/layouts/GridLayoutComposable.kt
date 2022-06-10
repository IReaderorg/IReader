package org.ireader.components.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.BookItem
import org.ireader.common_resources.UiText
import org.ireader.core_ui.ui_components.isScrolledToTheEnd
import org.ireader.ui_components.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridLayoutComposable(
    modifier: Modifier = Modifier,
    books: List<BookItem>,
    selection: List<Long> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (book: BookItem) -> Unit = {},
    scrollState: androidx.compose.foundation.lazy.grid.LazyGridState,
    goToLatestChapter: (book: BookItem) -> Unit,
    isLoading: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    headers: ((url:String) -> okhttp3.Headers?)? = null,
    columns: Int = 3,
) {
    val cells = if (columns > 1) {
        GridCells.Fixed(columns)
    } else {
        GridCells.Adaptive(130.dp)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = cells,
            content = {
                items(
                    items = books,
                    key = { book ->
                        book.id
                    },
                    contentType = { "books" },
                ) { book ->
                    val height = remember {
                        mutableStateOf(IntSize(0,0))
                    }
                    BookImage(
                        modifier.onGloballyPositioned {
                          height.value = it.size
                        },
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 6f / 10f,
                        selected = book.id in selection,
                        header = headers,
                        onLongClick = { onLongClick(book) },
                    ) {

                        if (showGoToLastChapterBadge) {
                            GoToLastReadComposable(
                                onClick = { goToLatestChapter(book) },
                                size = (height.value.height / 20).dp
                            )
                        }
                        if (showUnreadBadge || showUnreadBadge) {
                            LibraryBadges(
                                unread = if (showUnreadBadge) book.unread else null,
                                downloaded = if (showReadBadge) book.downloaded else null
                            )
                        }

                        if (showInLibraryBadge && book.favorite) {
                            TextBadge(text = UiText.StringResource(R.string.in_library))
                        }
                    }
                }

            }

        )
        if (isLoading && scrollState.isScrolledToTheEnd()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
