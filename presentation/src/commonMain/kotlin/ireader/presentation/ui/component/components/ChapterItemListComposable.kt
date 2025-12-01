package ireader.presentation.ui.component.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.asRelativeTimeString
import ireader.domain.utils.extensions.toLocalDate
import ireader.i18n.resources.Res
import ireader.i18n.resources.bookmarked
import ireader.i18n.resources.cached
import ireader.i18n.resources.chapter_background
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChapterRow(
        modifier: Modifier = Modifier,
        chapter: Chapter,
        onItemClick: () -> Unit,
        onLongClick: () -> Unit = {},
        isLastRead: Boolean = false,
        isSelected: Boolean = false,
        isLoading: Boolean = false,
        showNumber: Boolean = true,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Use remember to cache computed values and minimize recomposition
    val hasBookmark = remember(chapter.bookmark) { chapter.bookmark }
    val hasTranslator = remember(chapter.translator) { chapter.translator.isNotBlank() }
    val hasDateUpload = remember(chapter.dateUpload) { chapter.dateUpload > 0 }
    
    // Check if chapter has downloaded content
    val isCached = remember(chapter.content) { 
        chapter.content.isNotEmpty()
    }
    
    // Animated background color for chapter rows
    // Use surface color as base to ensure opacity over backdrop images
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                .compositeOver(MaterialTheme.colorScheme.surface)
            isLastRead -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                .compositeOver(MaterialTheme.colorScheme.surface)
            chapter.read -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                .compositeOver(MaterialTheme.colorScheme.surface) // Gray background for read chapters
            else -> MaterialTheme.colorScheme.surface // Use opaque surface color to prevent backdrop bleed-through
        },
        animationSpec = spring(),
        label = localizeHelper.localize(Res.string.chapter_background),
    )
    
    // Cache text color calculations
    val titleColor = remember(isLastRead, chapter.read) {
        when {
            isLastRead -> null // Will use primary color from theme
            chapter.read -> null // Will use disabled alpha
            else -> null // Will use current content color
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(backgroundColor)
            .combinedClickable(onClick = onItemClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bookmark indicator
        if (hasBookmark) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = localizeHelper.localize(Res.string.bookmarked),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Chapter content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Chapter title with number - use remember for text building
            val titleText = remember(chapter.number, chapter.name, showNumber) {
                buildAnnotatedString {
                    if (chapter.number != -1f && showNumber) {
                        append("${chapter.number.toInt()}.  ")
                    }
                    append(chapter.name)
                }
            }
            
            Text(
                titleText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isLastRead) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isLastRead -> MaterialTheme.colorScheme.primary
                    chapter.read -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Gray text for read chapters
                    else -> LocalContentColor.current
                },
                maxLines = 2
            )
            
            // Chapter metadata - use remember for subtitle building
            val subtitleStr = remember(chapter.dateUpload, chapter.translator) {
                buildAnnotatedString {
                    if (hasDateUpload) {
                        append(chapter.dateUpload.toLocalDate().date
                            .asRelativeTimeString(PreferenceValues.RelativeTime.Seconds))
                    }
                    if (hasTranslator) {
                        if (length > 0) append(" • ")
                        append(chapter.translator)
                    }
                }
            }
            
            if (subtitleStr.text.isNotBlank()) {
                Text(
                    subtitleStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (chapter.read) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Gray text for read chapters
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant // Normal subtitle color
                    }
                )
            }
        }
        
        // Status indicators - only render if needed
        if (isLoading || isCached) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (isCached) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = localizeHelper.localize(Res.string.cached),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
