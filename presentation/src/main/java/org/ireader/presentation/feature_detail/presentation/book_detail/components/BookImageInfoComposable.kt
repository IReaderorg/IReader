package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.presentation.components.BookImageComposable

@Composable
fun BookImageInfoComposable(modifier: Modifier = Modifier, book: Book) {
    Row {
        BookImageComposable(
            image = BookCover.from(book),
            modifier = Modifier
                .height(180.dp)
                .width(150.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(2.dp, MaterialTheme.colors.onBackground.copy(alpha = .1f)),
        )
        Spacer(modifier = modifier.height(8.dp))
        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = modifier.height(8.dp))
            Text(
                text = "Author: ${book.author}",
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = modifier.height(8.dp))
        }
    }
}