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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    useDefaultImageLoader: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge:Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Fixed(3),
            content = {
                items(
                    items = books,
                    key = { book ->
                        book.id
                    },
                    contentType = { "books" },
                ) { book ->

                    BookImage(
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 6f / 10f,
                        selected = book.id in selection,
                        useDefaultImageLoader = useDefaultImageLoader,
                        onLongClick = { onLongClick(book) },
                    ) {
                        if (showGoToLastChapterBadge) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(book) })
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
