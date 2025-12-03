package ireader.presentation.ui.home.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.HistoryWithRelations
import ireader.presentation.ui.core.coil.rememberBookCover
import ireader.presentation.ui.core.utils.horizontalPadding
import ireader.presentation.ui.component.BookListItem
import ireader.presentation.ui.component.BookListItemColumn
import ireader.presentation.ui.component.BookListItemImage
import ireader.presentation.ui.component.BookListItemSubtitle
import ireader.presentation.ui.component.BookListItemTitle
import ireader.presentation.ui.component.reusable_composable.AppIconButton


val HISTORY_ITEM_HEIGHT = 96.dp

//@Composable
//fun HistoryItem(
//    history: HistoryWithRelations,
//    onBookCover: (HistoryWithRelations) -> Unit,
//    onClickItem: (HistoryWithRelations) -> Unit,
//    onClickDelete: (HistoryWithRelations) -> Unit,
//    onLongClickDelete: (HistoryWithRelations) -> Unit,
//    onClickPlay: (HistoryWithRelations) -> Unit,
//) {
//    BookListItem(
//        modifier = Modifier
//            .clickable { onClickItem(history) }
//            .height(HISTORY_ITEM_HEIGHT)
//            .fillMaxWidth()
//            .padding(end = 4.dp),
//    ) {
//        BookListItemImage(
//            modifier = Modifier
//                .fillMaxHeight()
//                .aspectRatio(3f / 4f)
//                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
//                .clip(MaterialTheme.shapes.medium)
//                .clickable { onBookCover(history) },
//            mangaCover = rememberBookCover(history)
//        )
//        BookListItemColumn(
//            modifier = Modifier
//                .weight(1f)
//                .padding(start = 16.dp, end = 8.dp)
//        ) {
//            BookListItemTitle(
//                text = history.title,
//                maxLines = 2,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            BookListItemSubtitle(
//                text = if (history.chapterNumber != -1f) {
//                    "Ch. ${history.chapterNumber}"
//                } else {
//                    history.chapterName
//                }
//            )
//        }
//        AppIconButton(
//            onClick = {
//                onClickDelete(history)
//            },
//            onLongClick = {
//                onLongClickDelete(history)
//            }
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Delete,
//                contentDescription = "",
//                tint = MaterialTheme.colorScheme.onSurface
//            )
//        }
//        IconButton(onClick = { onClickPlay(history) }) {
//            Icon(
//                imageVector = Icons.Filled.PlayArrow,
//                contentDescription = "",
//                tint = MaterialTheme.colorScheme.onSurface
//            )
//        }
//    }
//}

@Composable
fun HistoryItemShimmer(brush: Brush) {
    Row(
        modifier = Modifier
            .height(HISTORY_ITEM_HEIGHT)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(4.dp))
                .drawBehind {
                    drawRect(brush = brush)
                },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = horizontalPadding, end = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawRect(brush = brush)
                    }
                    .height(14.dp)
                    .fillMaxWidth(0.70f),
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(14.dp)
                    .fillMaxWidth(0.45f)
                    .drawBehind {
                        drawRect(brush = brush)
                    },
            )
        }
    }
}
