package ireader.ui.home.history

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
import ireader.common.models.entities.HistoryWithRelations
import ireader.ui.component.BookListItem
import ireader.ui.component.BookListItemColumn
import ireader.ui.component.BookListItemImage
import ireader.ui.component.BookListItemSubtitle
import ireader.ui.component.BookListItemTitle
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.core.ui.coil.rememberBookCover

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
                text = history.title,
                maxLines = 2,
                fontWeight = FontWeight.SemiBold
            )

            BookListItemSubtitle(
                text = if (history.chapterNumber != -1f) {
                    "Ch. ${history.chapterNumber}"
                } else {
                    history.title
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
        IconButton(onClick = { onClickPlay(history) }) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}