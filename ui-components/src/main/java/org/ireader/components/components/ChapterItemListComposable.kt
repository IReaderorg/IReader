package org.ireader.components.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ireader.common_extensions.asRelativeTimeString
import org.ireader.common_models.entities.Chapter
import org.ireader.core_ui.modifier.selectedBackground
import org.ireader.core_ui.preferences.PreferenceValues

private val dateFormat = org.ireader.core_api.util.DateTimeFormatter("dd/MM/yy")

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
                    if (chapter.number != -1f) {
                        append("${chapter.number}   ")
                    }
                    append(chapter.name)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLastRead) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(
                    alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
                )
            )
            val subtitleStr = buildAnnotatedString {
                if (chapter.dateUpload > 0) {
                    val instant = Instant.fromEpochMilliseconds(chapter.dateUpload)
                    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    append(date.date.asRelativeTimeString(range = PreferenceValues.RelativeTime.Seconds))
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
                        alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
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
                    imageVector = Icons.Default.PublishedWithChanges,
                    contentDescription = "Cached",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

    }
}