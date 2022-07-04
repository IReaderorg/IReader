package org.ireader.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.HistoryWithRelations
import org.ireader.components.BookListItem
import org.ireader.components.BookListItemColumn
import org.ireader.components.BookListItemImage
import org.ireader.components.BookListItemSubtitle
import org.ireader.components.BookListItemTitle
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.coil.rememberBookCover

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    history: HistoryWithRelations,
    onBookCover: (HistoryWithRelations) -> Unit,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
) {
    BookListItem(
        modifier = Modifier
            .clickable { onClickItem(history) }
            .height(80.dp)
            .fillMaxWidth()
            .padding(end = 4.dp),
    ) {
        BookListItemImage(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(3f / 4f)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onBookCover(history) },
            mangaCover = rememberBookCover(history)
        )
        BookListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp)
        ) {
            BookListItemTitle(
                text = history.bookTitle,
                maxLines = 2,
                fontWeight = FontWeight.SemiBold
            )

            BookListItemSubtitle(
                text = if (history.chapterNumber != -1) {
                    "Ch. ${history.chapterNumber}"
                } else {
                    history.chapterTitle
                }
            )
        }
        AppIconButton(
            onClick = {
                onClickDelete(history)
            },
            onLongClick = {
                onLongClickDelete(history)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
//        AppIconButton(
//                modifier = Modifier.combinedClickable(
//                    onClick = { onClickDelete(history) },
//                onLongClick = { onLongClickDelete(history) },
//                    role = Role.Button,
//                ),
//            onLongClick = ,
//                imageVector = Icons.Outlined.Delete,
//                contentDescription = "",
//                tint = MaterialTheme.colorScheme.onSurface
//            )
        IconButton(onClick = { onClickPlay(history) }) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// private val formatter = DateTimeFormatter("HH:mm")
//
// private fun Long.toLocalDateTime(): LocalDateTime {
//  return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
// }
