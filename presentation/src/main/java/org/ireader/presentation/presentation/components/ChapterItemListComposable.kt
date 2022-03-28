package org.ireader.presentation.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.core_ui.modifier.selectedBackground
import org.ireader.domain.models.entities.Chapter


@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChapterListItemComposable(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isLastRead: Boolean = false,
    isSelected: Boolean = false,
) {
    ListItem(
        modifier = modifier
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = { onLongClick() }
            )
            .selectedBackground(isSelected),
        text = {
            Text(
                text = chapter.title,
                color = if (!isLastRead) {
                    if (chapter.read) MaterialTheme.colors.onBackground.copy(
                        alpha = .4f) else MaterialTheme.colors.onBackground
                } else {
                    MaterialTheme.colors.primary
                },
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = {
            if (chapter.content.joinToString(" , ").length > 10) {
                Icon(
                    imageVector = Icons.Default.PublishedWithChanges,
                    contentDescription = "Cached",
                    tint = MaterialTheme.colors.onBackground,
                )
            }

        },
        secondaryText = {

            Text(
                text = if (chapter.dateUpload != 0L) chapter.dateUpload.toString() else "",
                fontStyle = FontStyle.Italic,
                color = if (chapter.read) MaterialTheme.colors.onBackground.copy(
                    alpha = .4f) else MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.caption
            )
        }

    )
}