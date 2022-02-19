package org.ireader.presentation.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.domain.models.entities.Chapter


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterListItemComposable(modifier: Modifier = Modifier, chapter: Chapter, goTo: () -> Unit) {
    ListItem(
        modifier = modifier.clickable {
            goTo()
        },
        text = {
            Text(
                text = chapter.title,
                color = if (chapter.read) MaterialTheme.colors.onBackground.copy(
                    alpha = .4f) else MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = {
            Icon(
                imageVector = Icons.Default.PublishedWithChanges,
                contentDescription = "Cached",
                tint = if (chapter.content.joinToString(" , ").length > 10) MaterialTheme.colors.onBackground else MaterialTheme.colors.background,
            )
        },
        secondaryText = {

        Text(
                text = if (chapter.dateUploaded != 0L) chapter.dateUploaded.toString() else "",
                fontStyle = FontStyle.Italic,
                color = if (chapter.read) MaterialTheme.colors.onBackground.copy(
                    alpha = .4f) else MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.caption
            )
        }

    )
}