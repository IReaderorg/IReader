package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.list.isScrolledToTheEnd
import ireader.presentation.ui.component.rememberIsGridScrollingFast
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComfortableGridLayout(
    modifier: Modifier = Modifier,
    books: List<BookItem>,
    selection: List<Long> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (book: BookItem) -> Unit = {},
    scrollState: androidx.compose.foundation.lazy.grid.LazyGridState,
    goToLatestChapter: (book: BookItem) -> Unit,
    isLoading: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    showDownloadedChaptersBadge: Boolean = false,
    showUnreadChaptersBadge: Boolean = false,
    showLocalMangaBadge: Boolean = false,
    showLanguageBadge: Boolean = false,
    headers: ((url: String) -> Map<String, String>?)? = null,
    columns: Int = 3,
    keys: ((item: BookItem) -> Any)

) {
    // Cache cells calculation to avoid recreation
    val cells = remember(columns) {
        if (columns > 1) {
            GridCells.Fixed(columns)
        } else {
            GridCells.Adaptive(130.dp)
        }
    }
    
    // Performance optimization: track fast scrolling to defer expensive operations
    val performanceConfig = LocalPerformanceConfig.current
    val isScrollingFast = rememberIsGridScrollingFast(scrollState)
    
    // Pre-compute selection set for O(1) lookup instead of O(n) list contains
    val selectionSet = remember(selection) { selection.toSet() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = cells,
            content = {
                items(
                    items = books,
                    key = keys,
                    contentType = { "books" },
                ) { book ->
                    val height = remember {
                        mutableStateOf(IntSize(0, 0))
                    }
                    BookImage(
                        modifier.onGloballyPositioned {
                            height.value = it.size
                        },
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 2f / 3f,
                        selected = book.id in selectionSet, // Use set for O(1) lookup
                        header = headers,
                        onlyCover = true,
                        comfortableMode = true,
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
                        if (showUnreadBadge || showReadBadge || showDownloadedChaptersBadge || showUnreadChaptersBadge || showLocalMangaBadge || showLanguageBadge || book.isArchived) {
                            LibraryBadges(
                                unread = if (showUnreadBadge || showUnreadChaptersBadge) book.unread else null,
                                downloaded = if (showReadBadge || showDownloadedChaptersBadge) book.downloaded else null,
                                isLocal = showLocalMangaBadge && book.sourceId == -1L,
                                sourceId = book.sourceId,
                                showLanguage = showLanguageBadge,
                                isPinned = false, // Will be implemented in task 5.2
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
