package ireader.presentation.ui.book.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .padding(start = 16.dp, top = appbarPadding + 16.dp, end = 16.dp),
    ) {
        /** Book Image **/
        BookImageCover.Book(
            data = BookCover.from(book),
            modifier = Modifier
                .sizeIn(maxWidth = 100.dp)
                .align(Alignment.Top)
                .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),

        )
        Spacer(modifier = modifier.width(8.dp))
        /** Book Info **/
        BookInfo(
            book = book,
            onTitle = onTitle,
            source = source,
                onCopyTitle = onCopyTitle
        )
    }
}