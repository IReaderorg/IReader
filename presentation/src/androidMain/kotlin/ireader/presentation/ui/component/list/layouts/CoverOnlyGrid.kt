package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.BookItem
import ireader.i18n.UiText
import ireader.presentation.ui.component.list.isScrolledToTheEnd
import ireader.presentation.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverOnlyGrid(
    modifier: Modifier = Modifier,
    books: List<BookItem>,
    selection: List<Long> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (book: BookItem) -> Unit = {},
    scrollState: LazyGridState = rememberLazyGridState(),
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit,
    isLoading: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    columns: Int = 2,
    header: ((url: String) -> okhttp3.Headers?)? = null,
    keys: ((item: BookItem) -> Any)
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val cells = if (columns > 1) {
            GridCells.Fixed(columns)
        } else {
            GridCells.Adaptive(160.dp)
        }

        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = cells,
            contentPadding = PaddingValues(8.dp),
            content = {
                items(
                    items = books,
                    key = keys,

                    contentType = { "books" }
                ) { book ->
                    val height = remember {
                        mutableStateOf(IntSize(0, 0))
                    }
                    BookImage(
                        modifier = Modifier.animateItemPlacement().onGloballyPositioned {
                            height.value = it.size
                        },
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 6f / 9f,
                        selected = book.id in selection,
                        header = header,
                        onlyCover = true,
                        onLongClick = { onLongClick(book) },
                    ) {
                        if (showGoToLastChapterBadge) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(book) }, size = (height.value.height / 20).dp)
                        }
                        if (showUnreadBadge || showUnreadBadge) {
                            LibraryBadges(
                                unread = if (showUnreadBadge) book.unread else null,
                                downloaded = if (showReadBadge) book.downloaded else null
                            )
                        }

                        if (showInLibraryBadge && book.favorite) {
                            TextBadge(text = UiText.StringResource(R.string.in_library))
                        }
                    }
                }
            }
        )
        if (isLoading && scrollState.isScrolledToTheEnd()) {
            Spacer(modifier = Modifier.height(45.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
