package ireader.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.core.source.Source
import ireader.core.source.model.MangaInfo
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.presentation.ui.component.components.BookImageCover
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Modern book header component displaying cover and book info.
 * 
 * The cover displays customCover if set by user, otherwise shows the default cover.
 * Custom covers are preserved when updating book details from remote source.
 * 
 * Tap on cover opens the cover preview dialog with options to:
 * - Pick local image
 * - Edit cover URL
 * - Share cover
 * - Reset to original
 * 
 * @param book The book to display
 * @param source The source of the book
 * @param onTitle Callback when title is clicked
 * @param onCopyTitle Callback when title is long-pressed (copy)
 * @param onCoverClick Callback when cover is clicked (opens cover preview dialog)
 * @param modifier Modifier for the component
 */
@Composable
fun ModernBookHeader(
    book: Book,
    source: Source?,
    onTitle: (String) -> Unit,
    onCopyTitle: (String) -> Unit,
    onCoverClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enhanced cover with shadow - uses BookCover.from() which prioritizes customCover
        Card(
            modifier = Modifier
                .width(120.dp)
                .height(170.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = { onCoverClick?.invoke() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BookImageCover.Book(
                    data = BookCover.from(book),
                    modifier = Modifier.fillMaxSize()
                )
                
                // Status badge overlay (top-right)
                if (book.status != MangaInfo.UNKNOWN) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = when (book.status) {
                            MangaInfo.ONGOING -> MaterialTheme.colorScheme.primary
                            MangaInfo.COMPLETED -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }.copy(alpha = 0.92f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = book.getStatusByName(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
        
        // Book info
        Column(
            modifier = Modifier
                .weight(1f)
                .height(170.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title
            Text(
                modifier = Modifier.clickableNoIndication(
                    onClick = { if (book.title.isNotBlank()) onTitle(book.title) },
                    onLongClick = { 
                        try {
                            if (book.title.isNotBlank()) onCopyTitle(book.title)
                        } catch (_: Exception) {}
                    }
                ),
                text = book.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp
            )
            
            // Author
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
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Status and Source row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (book.status) {
                            MangaInfo.ONGOING -> Icons.Default.Schedule
                            MangaInfo.COMPLETED -> Icons.Default.DoneAll
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = book.getStatusByName(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                
                if (source != null) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Archived badge
            if (book.isArchived) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.archived),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
