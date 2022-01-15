package ir.kazemcodes.infinity.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
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
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.presentation.components.BookImageComposable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridLayoutComposable(
    modifier: Modifier = Modifier,
    books: List<Book>,
    onClick: (index: Int) -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
) {
    LazyVerticalGrid(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        cells = GridCells.Fixed(3),
        content = {
            items(books.size) { index ->
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clickable(role = Role.Button) { onClick(index) },
                ) {
                    BookImageComposable(
                        modifier = modifier
                            .width(120.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(2.5.dp,MaterialTheme.colors.onBackground.copy(alpha = .1f)),
                        image = books[index].coverLink ?: "",
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
                            text = books[index].bookName,
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }

                }
            }
        })
}