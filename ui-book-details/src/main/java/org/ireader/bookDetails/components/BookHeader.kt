package org.ireader.bookDetails.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.Book
import org.ireader.components.components.BookImageCover
import org.ireader.core_api.source.Source
import org.ireader.image_loader.BookCover

@Composable
internal fun BookHeader(
    modifier: Modifier = Modifier,
    book: Book,
    onTitle: (String) -> Unit,
    source: Source?,
    appbarPadding: Dp
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
        )
    }
}