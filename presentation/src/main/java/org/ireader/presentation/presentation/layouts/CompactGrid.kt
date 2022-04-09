package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.domain.models.entities.BookItem


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactGridLayoutComposable(
    modifier: Modifier = Modifier,
    books: List<BookItem>,
    selection: List<Long> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (book: BookItem) -> Unit = {},
    scrollState: LazyGridState = rememberLazyGridState(),
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit,
    onEndReach: (itemIndex: Int) -> Unit = {},
    isLoading: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {


        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            content = {
                    items(count = books.size) { index ->
                        onEndReach(index)
                        BookImage(
                            onClick = { onClick(books[index]) },
                            book = books[index],
                            ratio = 6f / 9f,
                            selected = books[index].id in selection,
                            onLongClick = { onLongClick(books[index]) },
                        ) {
                            if (books[index].totalDownload != 0) {
                                GoToLastReadComposable(onClick = { goToLatestChapter(books[index]) })
                            }
                            if (!isLocal && books[index].favorite) {
                                TextBadge(text = "in Library")
                            }
                        }
                    }
            })
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp))
        }

    }
}