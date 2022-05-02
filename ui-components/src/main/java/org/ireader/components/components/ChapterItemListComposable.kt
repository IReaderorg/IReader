package org.ireader.components.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.common_models.entities.Chapter
import org.ireader.core_ui.modifier.selectedBackground

@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChapterListItemComposable(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isLastRead: Boolean = false,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
) {
    ListItem(
        modifier = modifier
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = { onLongClick() }
            )
            .selectedBackground(isSelected),
        icon = if (chapter.bookmark) {
            {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = MaterialTheme.colors.primary,
                )
            }
        } else null,
        text = {
            Text(
                text = chapter.title,
                color = if (!isLastRead) {
                    if (chapter.read) MaterialTheme.colors.onBackground.copy(
                        alpha = .4f
                    ) else MaterialTheme.colors.onBackground
                } else {
                    MaterialTheme.colors.primary
                },
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = {
            if (isLoading) {
                ShowLoading()
            }
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
                    alpha = .4f
                ) else MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.caption
            )
        }

    )
}
