package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import org.ireader.core.utils.items
import org.ireader.domain.models.entities.Book


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridLayoutComposable(
    modifier: Modifier = Modifier,
    books: LazyPagingItems<Book>,
    onClick: (book: Book) -> Unit,
    scrollState: LazyGridState,
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit,
) {
    LazyVerticalGrid(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        cells = GridCells.Fixed(3),
        content = {
            items(lazyPagingItems = books) { book ->
                if (book != null) {
                    BookImage(
                        onClick = { onClick(book) }, book = book, ratio = 6f / 10f
                    ) {
                        if (book.lastUpdated > 1 && isLocal && book.lastRead != 0L) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                        }
                    }
                }

//                if (book != null) {
//                    Box(
//                        modifier = modifier
//                            .fillMaxSize()
//                            .padding(8.dp)
//                            .clickable(role = Role.Button) { onClick(book) },
//                    ) {
//                        BookImageComposable(
//                            modifier = modifier
//                                .aspectRatio(6f / 10f)
//                                .clip(RoundedCornerShape(4.dp))
//                                .border(2.5.dp,
//                                    MaterialTheme.colors.onBackground.copy(alpha = .1f))
//                                .align(Alignment.Center),
//                            image = BookCover.from(book),
//                        )
//                        Box(
//                            Modifier
//                                .fillMaxWidth()
//                                .height(50.dp)
//                                .background(
//                                    Brush.verticalGradient(
//                                        colors = listOf(Color.Transparent, Color.Black),
//                                        startY = 3f,  // 1/3
//                                        endY = 80F
//                                    )
//                                )
//                                .align(Alignment.BottomCenter)
//                        ) {
//                            Text(
//                                modifier = modifier
//                                    .align(Alignment.BottomCenter)
//                                    .padding(bottom = 8.dp),
//                                text = book.title,
//                                style = MaterialTheme.typography.caption,
//                                fontWeight = FontWeight.Bold,
//                                overflow = TextOverflow.Ellipsis,
//                                textAlign = TextAlign.Center,
//                                color = Color.White
//                            )
//                        }
//                        /**
//                         * Only show if the latest chapter exist.
//                         */
//                        if (book.lastUpdated > 1 && isLocal && book.lastRead != 0L) {
//                            GoToLastReadComposable(onClick = { goToLatestChapter(book) })
//                        }
//
//                    }
//                }

            }
        })
}