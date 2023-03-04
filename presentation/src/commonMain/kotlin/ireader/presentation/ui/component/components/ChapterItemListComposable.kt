package ireader.presentation.ui.component.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.core.modifier.selectedBackground
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.asRelativeTimeString
import ireader.domain.utils.extensions.toLocalDate
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
    Row(
        modifier = Modifier
            .height(64.dp)
            .combinedClickable(onClick = onItemClick, onLongClick = onLongClick)
            .selectedBackground(isSelected)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (chapter.bookmark) {
            Icon(
                modifier = Modifier
                    .padding(end = 18.dp)
                    .align(Alignment.CenterVertically),
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Bookmarked",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {

            Text(
                buildAnnotatedString {
                    if (chapter.number != -1f && showNumber) {
                        append("${chapter.number.toInt()}   ")
                    }
                    append(chapter.name)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLastRead) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                    alpha = if (chapter.read) ContentAlpha.disabled() else ContentAlpha.high()
                )
            )
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
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(
                        alpha = if (chapter.read) ContentAlpha.disabled() else ContentAlpha.medium()
                    )
                )
            }
        }
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (isLoading) {
                ShowLoading()
            }
            if (chapter.content.joinToString(" , ").length > 10) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Cached",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
