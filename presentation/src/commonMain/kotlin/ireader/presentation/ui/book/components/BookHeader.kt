package ireader.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.presentation.ui.component.components.BookImageCover

@Composable
internal fun BookHeader(
    modifier: Modifier = Modifier,
    book: Book,
    onTitle: (String) -> Unit,
    source: Source?,
    appbarPadding: Dp,
    onCopyTitle:(bookTitle:String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = appbarPadding + 24.dp, end = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /** Enhanced Book Image with elevation **/
        Card(
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .align(Alignment.Top),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            BookImageCover.Book(
                data = BookCover.from(book),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        
        /** Book Info with improved spacing **/
        BookInfo(
            book = book,
            onTitle = onTitle,
            source = source,
            onCopyTitle = onCopyTitle
        )
    }
}
