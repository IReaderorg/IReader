

package org.ireader.updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.components.BookListItem
import org.ireader.components.BookListItemColumn
import org.ireader.components.BookListItemImage
import org.ireader.components.BookListItemSubtitle
import org.ireader.components.BookListItemTitle
import org.ireader.core_ui.coil.rememberBookCover
import org.ireader.core_ui.modifier.selectedBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesItem(
    book: UpdateWithInfo,
    isSelected: Boolean,
    onClickItem: (UpdateWithInfo) -> Unit,
    onLongClickItem: (UpdateWithInfo) -> Unit,
    onClickCover: (UpdateWithInfo) -> Unit,
    onClickDownload: (UpdateWithInfo) -> Unit,
    isDownloadable: Boolean = false,
) {
    val alpha = if (book.read) 0.38f else 1f

    BookListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = { onClickItem(book) },
                onLongClick = { onLongClickItem(book) }
            )
            .selectedBackground(isSelected)
            .height(56.dp)
            .fillMaxWidth()
            .padding(end = 4.dp)
    ) {
        BookListItemImage(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onClickCover(book) },
            mangaCover = rememberBookCover(book)
        )
        BookListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .alpha(alpha)
        ) {
            BookListItemTitle(
                text = book.bookTitle,
                fontWeight = FontWeight.SemiBold
            )
            BookListItemSubtitle(
                text = if (book.number == -1F) book.chapterTitle else "${book.number}  ${book.chapterTitle}"
            )
        }

        if (isDownloadable) {
            IconButton(onClick = { onClickDownload(book) }) {
                Icon(imageVector = Icons.Outlined.Download, contentDescription = "")
            }
        } else {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircleOutline,
                    contentDescription = ""
                )
            }
        }
    }
}
