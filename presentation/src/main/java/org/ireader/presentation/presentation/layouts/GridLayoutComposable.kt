package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import org.ireader.core.utils.items
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.presentation.components.BookImageComposable


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
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clickable(role = Role.Button) { onClick(book) },
                    ) {
                        BookImageComposable(
                            modifier = modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(2.5.dp,
                                    MaterialTheme.colors.onBackground.copy(alpha = .1f)),
                            image = book.coverLink ?: "",
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                        startY = 3f,  // 1/3
                                        endY = 80F
                                    )
                                )
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                modifier = modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                text = book.bookName,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                        /**
                         * Only show if the latest chapter exist.
                         */
                        if (book.totalChapters > 1 && isLocal && book.lastRead != 0L) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                        }

                    }
                }

            }
        })
}