package org.ireader.bookDetails.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.Chapter
import org.ireader.components.text_related.CardTile
import org.ireader.core_ui.ui_components.DotsFlashing

@Composable
fun ChapterContentHeader(
    onChapterContent: () -> Unit,
    isChapterLoading: Boolean,
    chapters: List<Chapter>
) {
    CardTile(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        onClick = onChapterContent,
        title = "Contents",
        subtitle = "${chapters.size} Chapters",
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                DotsFlashing(isChapterLoading)

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Contents Detail",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    )
}
