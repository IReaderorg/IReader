package ireader.presentation.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Component to display reading time estimation
 */
@Composable
fun ReadingTimeEstimator(
    visible: Boolean,
    estimatedMinutes: Int,
    wordsRemaining: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatReadingTime(estimatedMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$wordsRemaining words left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Format reading time in a human-readable format
 */
private fun formatReadingTime(minutes: Int): String {
    return when {
        minutes < 1 -> "< 1 min left"
        minutes == 1 -> "1 min left"
        minutes < 60 -> "$minutes mins left"
        else -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins == 0) {
                "${hours}h left"
            } else {
                "${hours}h ${mins}m left"
            }
        }
    }
}

/**
 * Calculate reading time based on word count and reading speed
 * @param wordCount Total words in the content
 * @param wordsPerMinute Average reading speed (default: 200-250 WPM for average readers)
 * @return Estimated reading time in minutes
 */
fun calculateReadingTime(wordCount: Int, wordsPerMinute: Int = 225): Int {
    if (wordCount <= 0) return 0
    return (wordCount.toFloat() / wordsPerMinute).toInt().coerceAtLeast(1)
}

/**
 * Count words in text content
 */
fun countWords(text: String): Int {
    if (text.isBlank()) return 0
    return text.trim().split(Regex("\\s+")).size
}
