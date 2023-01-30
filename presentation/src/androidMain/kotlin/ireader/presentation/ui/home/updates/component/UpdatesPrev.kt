package ireader.presentation.ui.home.updates.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.models.BookCover

private val book
    get() = UpdatesWithRelations(
        bookId = -1,
        sourceId = -1,
        bookTitle = "",
        read = true,
        chapterId = 1,
        downloaded = true,
        scanlator = "",
        dateFetch = 0,
        bookmark = true,
        chapterName = "",
        coverData = BookCover(0,0,"",false,0)
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
