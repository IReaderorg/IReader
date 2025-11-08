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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.asRelativeTimeString
import ireader.domain.utils.extensions.toLocalDate
import ireader.presentation.ui.core.modifier.selectedBackground
import ireader.presentation.ui.core.theme.ContentAlpha

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
    // Animated background color for last read chapter
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            isLastRead -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = spring()
    )
    
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
        if (chapter.bookmark) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Bookmarked",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Chapter content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Chapter title with number
            Text(
                buildAnnotatedString {
                    if (chapter.number != -1f && showNumber) {
                        append("${chapter.number.toInt()}.  ")
                    }
                    append(chapter.name)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isLastRead) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isLastRead -> MaterialTheme.colorScheme.primary
                    chapter.read -> LocalContentColor.current.copy(alpha = ContentAlpha.disabled())
                    else -> LocalContentColor.current
                },
                maxLines = 2
            )
            
            // Chapter metadata
            val subtitleStr = buildAnnotatedString {
                if (chapter.dateUpload > 0) {
                    append(chapter.dateUpload.toLocalDate().date
                        .asRelativeTimeString(PreferenceValues.RelativeTime.Seconds))
                }
                if (chapter.translator.isNotBlank()) {
                    if (length > 0) append(" • ")
                    append(chapter.translator)
                }
            }
            if (subtitleStr.text.isNotBlank()) {
                Text(
                    subtitleStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(
                        alpha = if (chapter.read) ContentAlpha.disabled() else ContentAlpha.medium()
                    )
                )
            }
        }
        
        // Status indicators
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
            if (chapter.content.joinToString(" , ").length > 10) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Cached",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
