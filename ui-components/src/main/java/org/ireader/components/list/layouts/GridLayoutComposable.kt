package org.ireader.components.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit,
    isLoading: Boolean = false,
    onEndReach: (itemIndex: Int) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Fixed(3),
            content = {
                items(count = books.size, key = { index ->
                    books[index].id
                }) { index ->
                    onEndReach(index)
                    BookImage(
                        onClick = { onClick(books[index]) },
                        book = books[index],
                        ratio = 6f / 10f,
                        selected = books[index].id in selection,
                        onLongClick = { onLongClick(books[index]) },
                    ) {
                        if (books[index].totalDownload != 0) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(books[index]) })
                        }
                        if (!isLocal && books[index].favorite) {
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
