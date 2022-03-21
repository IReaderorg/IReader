package org.ireader.presentation.feature_history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ireader.core.utils.convertLongToTime
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.feature_services.io.coil.rememberBookCover


@Composable
fun HistoryItem(
  history: HistoryWithRelations,
  onClickItem: (HistoryWithRelations) -> Unit,
  onClickDelete: (HistoryWithRelations) -> Unit,
  onClickPlay: (HistoryWithRelations) -> Unit,
) {
  MangaListItem(
    modifier = Modifier
      .clickable { onClickItem(history) }
      .height(80.dp)
      .fillMaxWidth()
      .padding(end = 4.dp),
  ) {
    MangaListItemImage(
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(3f / 4f)
        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium),
      mangaCover = rememberBookCover(history)
    )
    MangaListItemColumn(
      modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp, end = 8.dp)
    ) {
      MangaListItemTitle(
        text = history.bookTitle,
        maxLines = 2,
        fontWeight = FontWeight.SemiBold
      )

      MangaListItemSubtitle(
        text = "Ch. ${history.chapterTitle} - ${
          convertLongToTime(history.readAt, "HH:mm")
        }"
      )
    }
    IconButton(onClick = { onClickDelete(history) }) {
      Icon(imageVector = Icons.Outlined.Delete, contentDescription = "")
    }
    IconButton(onClick = { onClickPlay(history) }) {
      Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "")
    }
  }
}

//private val formatter = DateTimeFormatter("HH:mm")
//
//private fun Long.toLocalDateTime(): LocalDateTime {
//  return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
//}
