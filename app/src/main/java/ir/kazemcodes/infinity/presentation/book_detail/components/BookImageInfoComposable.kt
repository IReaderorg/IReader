package ir.kazemcodes.infinity.presentation.book_detail.components

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
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.presentation.screen.components.BookImageComposable

@Composable
fun BookImageInfoComposable(modifier : Modifier = Modifier , bookDetail : Book) {
    Row {
        BookImageComposable(
            image = bookDetail.coverLink ?: "",
            modifier = Modifier
                .height(180.dp)
                .width(150.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = modifier.height(8.dp))
        Column {
            Text(
                text = bookDetail.bookName,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = modifier.height(8.dp))
            Text(
                text = "Author: ${bookDetail.author ?: "Unknown"}",
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = modifier.height(8.dp))
            Text(
                text = "Translator: ${bookDetail.translator ?: "Unknown"}",
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}