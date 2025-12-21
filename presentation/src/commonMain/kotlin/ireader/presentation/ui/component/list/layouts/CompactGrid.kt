package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.BookItem
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.rememberIsGridScrollingFast

/**
 * NATIVE-LIKE COMPACT GRID
 * Optimized for 60fps scroll with minimal recomposition
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactGridLayoutComposable(
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
    columns: Int = 3,
    header: ((url: String) -> Map<String, String>?)? = null,
    keys: ((item: BookItem) -> Any)
) {
    val performanceConfig = LocalPerformanceConfig.current
    val isScrollingFast = rememberIsGridScrollingFast(scrollState)
    
    // O(1) selection lookup
    val selectionSet = remember(selection) { selection.toHashSet() }
    
    // Pre-compute badge visibility
    val showAnyBadge = remember(showUnreadBadge, showReadBadge) {
        showUnreadBadge || showReadBadge
    }
    
    // Stable cells reference
    val cells = remember(columns) {
        if (columns > 1) GridCells.Fixed(columns) else GridCells.Adaptive(160.dp)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = cells,
            contentPadding = PaddingValues(8.dp),
            content = {
                items(
                    items = books,
                    key = keys,
                    contentType = { "book_compact" }
                ) { book ->
                    val height = remember { mutableStateOf(IntSize(0, 0)) }
                    val isSelected = book.id in selectionSet

                    BookImage(
                        modifier = Modifier.onGloballyPositioned { height.value = it.size },
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 2f / 3f,
                        selected = isSelected,
                        header = header,
                        onLongClick = { onLongClick(book) },
                        isScrollingFast = isScrollingFast,
                        performanceConfig = performanceConfig,
                    ) {
                        if (showGoToLastChapterBadge) {
                            GoToLastReadComposable(
                                onClick = { goToLatestChapter(book) },
                                size = (height.value.height / 20).dp
                            )
                        }
                        if (showAnyBadge || book.isArchived) {
                            LibraryBadges(
                                unread = if (showUnreadBadge) book.unread else null,
                                downloaded = if (showReadBadge) book.downloaded else null,
                                isPinned = false,
                                isArchived = book.isArchived
                            )
                        }
                        if (showInLibraryBadge && book.favorite) {
                            TextBadge(text = UiText.MStringResource(Res.string.in_library))
                        }
                    }
                }
            }
        )
    }
}
