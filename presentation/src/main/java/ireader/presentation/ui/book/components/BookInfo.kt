package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.common.models.entities.Book
import ireader.core.source.Source
import ireader.core.source.model.MangaInfo
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.ui.core.modifier.secondaryItemAlpha
import ireader.domain.utils.copyToClipboard

@Composable
internal fun RowScope.BookInfo(
    onTitle: (String) -> Unit,
    book: Book,
    source: Source?,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .weight(0.60f)
            .align(Alignment.Bottom)
    ) {
        Text(
            modifier = Modifier
                .clickableNoIndication(
                    onClick = {
                        if (book.title.isNotBlank()) {
                            onTitle(book.title)
                        }
                    },
                    onLongClick = {
                        if (book.title.isNotBlank()) {
                            context.copyToClipboard(book.title, book.title)
                        }
                    }
                ),
            text = book.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis
        )
        if (book.author.isNotBlank()) {
            Text(
                text = "Author: ${book.author}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.secondaryItemAlpha(),
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .secondaryItemAlpha()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = when (book.status) {
                    MangaInfo.ONGOING -> Icons.Default.Schedule
                    MangaInfo.COMPLETED -> Icons.Default.DoneAll
                    MangaInfo.LICENSED -> Icons.Default.AttachMoney
                    MangaInfo.PUBLISHING_FINISHED -> Icons.Default.Done
                    MangaInfo.CANCELLED -> Icons.Default.Close
                    MangaInfo.ON_HIATUS -> Icons.Default.Pause
                    else -> Icons.Default.Block
                },
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(16.dp),
            )
            Text(
                text = book.getStatusByName(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                overflow = TextOverflow.Ellipsis
            )
            Text("•")
            if (source != null) {
                Text(
                    text = source.name,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}