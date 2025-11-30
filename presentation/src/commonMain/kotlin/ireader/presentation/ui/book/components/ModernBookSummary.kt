package ireader.presentation.ui.book.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Book
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

private val whitespaceLineRegex = Regex("[\\r\\n]{2,}", setOf(RegexOption.MULTILINE))

@Composable
fun ModernBookSummary(
    book: Book,
    isSummaryExpanded: Boolean,
    onSummaryExpand: () -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.synopsis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            
            IconButton(
                onClick = onSummaryExpand,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isSummaryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isSummaryExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Description
        val desc = book.description.takeIf { it.isNotBlank() }
            ?: localize(Res.string.description_placeholder)
        
        val trimmedDescription = remember(desc) {
            desc.replace(whitespaceLineRegex, "\n").trimEnd()
        }
        
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = trimmedDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = if (isSummaryExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(12.dp)
                    .clickableNoIndication(
                        onClick = { onSummaryExpand() },
                        onLongClick = {
                            try {
                                onCopy(desc)
                            } catch (_: Exception) {}
                        }
                    )
            )
        }
        
        // Genres
        if (book.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.genres),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            if (isSummaryExpanded) {
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    book.genres.filter { it.isNotBlank() }.forEach { genre ->
                        ModernGenreChip(genre)
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    book.genres.filter { it.isNotBlank() }.take(3).forEach { genre ->
                        ModernGenreChip(genre)
                    }
                    if (book.genres.size > 3) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "+${book.genres.size - 3}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernGenreChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 12.sp
        )
    }
}
