package ireader.presentation.ui.component.enhanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.accessibility.AccessibilityUtils
import ireader.presentation.ui.component.accessibility.AccessibilityUtils.accessibleClickable
import ireader.core.log.IReaderLog

/**
 * Accessibility-enhanced book list item following Mihon's patterns
 * Features:
 * - Proper content descriptions for screen readers
 * - Minimum touch target sizes (48dp)
 * - Semantic roles for better navigation
 * - Enhanced keyboard navigation support
 */
@Composable
fun AccessibleBookListItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
) {
    val contentDescription = buildString {
        append("Book: ${book.title}")
        if (showAuthor && book.author.isNotBlank()) {
            append(", by ${book.author}")
        }
        if (book.favorite) {
            append(", favorited")
        }
        if (book.status != 0L) {
            append(", status: ${getStatusDescription(book.status)}")
        }
    }
    
    IReaderLog.accessibility("Creating accessible book list item: ${book.title}")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .accessibleClickable(
                contentDescription = contentDescription,
                role = Role.Button,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover with accessibility support
            IImageLoader(
                model = book.cover,
                contentDescription = "Cover image for ${book.title}",
                modifier = Modifier
                    .size(width = 56.dp, height = 80.dp)
                    .semantics {
                        role = Role.Image
                        contentDescription = "Cover image for ${book.title}"
                    }
            )
            
            // Book information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title with heading semantics
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics {
                        role = Role.Text
                        contentDescription = "Book title: ${book.title}"
                    }
                )
                
                // Author if shown
                if (showAuthor && book.author.isNotBlank()) {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = "Author: ${book.author}"
                        }
                    )
                }
                
                // Description if shown
                if (showDescription && book.description.isNotBlank()) {
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = "Description: ${book.description}"
                        }
                    )
                }
                
                // Status indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (book.favorite) {
                        Text(
                            text = "★",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics {
                                contentDescription = "Favorited"
                            }
                        )
                    }
                    
                    if (book.status != 0L) {
                        Text(
                            text = getStatusText(book.status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.semantics {
                                contentDescription = "Status: ${getStatusDescription(book.status)}"
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get human-readable status text
 */
private fun getStatusText(status: Long): String {
    return when (status) {
        1L -> "Ongoing"
        2L -> "Completed"
        3L -> "Hiatus"
        4L -> "Cancelled"
        else -> "Unknown"
    }
}

/**
 * Get accessibility-friendly status description
 */
private fun getStatusDescription(status: Long): String {
    return when (status) {
        1L -> "ongoing series"
        2L -> "completed series"
        3L -> "series on hiatus"
        4L -> "cancelled series"
        else -> "unknown status"
    }
}