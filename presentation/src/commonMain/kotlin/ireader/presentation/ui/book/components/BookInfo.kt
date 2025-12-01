package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.core.source.model.MangaInfo
import ireader.domain.models.entities.Book
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.ui.core.modifier.secondaryItemAlpha
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
internal fun RowScope.BookInfo(
    onTitle: (String) -> Unit,
    book: Book,
    source: Source?,
    onCopyTitle:(bookTitle:String) -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Enhanced title with better typography
        Text(
            modifier = Modifier
                .clickableNoIndication(
                    onClick = {
                        if (book.title.isNotBlank()) {
                            onTitle(book.title)
                        }
                    },
                    onLongClick = {
                        try {
                            if (book.title.isNotBlank()) {
                                onCopyTitle(book.title)
                            }
                        } catch (e: Exception) {
                            // Silently handle copy errors
                        }
                    }
                ),
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        
        // Author with improved styling
        if (book.author.isNotBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Status and source with improved layout
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status chip
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = book.getStatusByName(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            
            // Archived indicator
            if (book.isArchived) {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.archived),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            
            if (source != null) {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                Text(
                    text = source.name,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}