package org.ireader.presentation.feature_updates.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.ireader.common_models.entities.UpdateWithInfo

private val book
    get() = UpdateWithInfo(
        bookId = -1,
        sourceId = -1,
        chapterLink = "Key",
        bookTitle = "Title",
        cover = "",
        favorite = true,
        chapterDateUpload = -1,
        chapterId = -1,
        chapterTitle = "Name",
        read = false,
        number = 69f,
        date = 46164684548
    )

@Preview(showBackground = true)
@Composable
fun UpdatesItemPreview() {
    UpdatesItem(
        book = book,
        isSelected = false,
        onClickItem = {},
        onLongClickItem = {},
        onClickCover = {},
        onClickDownload = {}
    )
}

@Preview(showBackground = true)
@Composable
fun UpdatesItemReadPreview() {
    UpdatesItem(
        book = book.copy(read = true),
        isSelected = false,
        onClickItem = {},
        onLongClickItem = {},
        onClickCover = {},
        onClickDownload = {}
    )
}

@Preview(showBackground = true)
@Composable
fun UpdatesItemSelectedPreview() {
    UpdatesItem(
        book = book,
        isSelected = true,
        onClickItem = {},
        onLongClickItem = {},
        onClickCover = {},
        onClickDownload = {}
    )
}

@Preview(showBackground = true)
@Composable
fun UpdatesItemSelectedAndReadPreview() {
    UpdatesItem(
        book = book.copy(read = true),
        isSelected = true,
        onClickItem = {},
        onLongClickItem = {},
        onClickCover = {},
        onClickDownload = {},

        )
}
