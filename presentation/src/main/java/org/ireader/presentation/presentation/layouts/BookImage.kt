package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.presentation.components.BookImageComposable

@Composable
fun BookImage(
    modifier: Modifier = Modifier,
    onClick: (Book) -> Unit,
    book: Book,
    ratio: Float = 3f / 4f,
    badge: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .clickable(role = Role.Button) { onClick(book) },
    ) {
        BookImageComposable(
            modifier = Modifier
                .aspectRatio(ratio)
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(2.dp,
                    MaterialTheme.colors.onBackground.copy(alpha = .1f))
                .align(Alignment.Center),
            image = BookCover.from(book),
        )

        Box(
            Modifier
                .height(50.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 3f,  // 1/3
                        endY = 80F
                    )
                )
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                text = book.title,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color.White,
            )
        }
        badge()
    }
}