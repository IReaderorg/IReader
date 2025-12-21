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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import ireader.presentation.ui.component.rememberIsGridScrollingFast
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * NATIVE-LIKE GRID LAYOUT
 * 
 * Optimizations for 60fps scroll:
 * 1. Stable keys for efficient item recycling
 * 2. contentType for better view recycling
 * 3. Pre-computed selection set for O(1) lookup
 * 4. Minimal recomposition scope
 * 5. No loading indicators during scroll
 */
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
    // Cache cells calculation - stable reference
    val cells = remember(columns) {
        if (columns > 1) GridCells.Fixed(columns) else GridCells.Adaptive(130.dp)
    }
    
    // Get performance config
    val performanceConfig = LocalPerformanceConfig.current
    
    // Track fast scrolling for deferred operations
    val isScrollingFast = rememberIsGridScrollingFast(scrollState)
    
    // Pre-compute selection set for O(1) lookup - CRITICAL for performance
    val selectionSet = remember(selection) { selection.toHashSet() }
    
    // Pre-compute badge visibility flags - avoid recalculation per item
    val showAnyBadge = remember(
        showUnreadBadge, showReadBadge, showDownloadedChaptersBadge,
        showUnreadChaptersBadge, showLocalMangaBadge, showLanguageBadge
    ) {
        showUnreadBadge || showReadBadge || showDownloadedChaptersBadge ||
        showUnreadChaptersBadge || showLocalMangaBadge || showLanguageBadge
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = scrollState,
            modifier = modifier.fillMaxSize(),
            columns = cells,
            content = {
                items(
                    items = books,
                    key = keys,
                    contentType = { "book_item" }, // Same type for all items - better recycling
                ) { book ->
                    // Minimal state per item
                    val height = remember { mutableStateOf(IntSize(0, 0)) }
                    val isSelected = book.id in selectionSet
                    
                    BookImage(
                        modifier = Modifier.onGloballyPositioned { height.value = it.size },
                        onClick = { onClick(book) },
                        book = book,
                        ratio = 2f / 3f,
                        selected = isSelected,
                        header = headers,
                        onlyCover = true,
                        comfortableMode = true,
                        onLongClick = { onLongClick(book) },
                        isScrollingFast = isScrollingFast,
                        performanceConfig = performanceConfig,
                    ) {
                        // Badges - only render if needed
                        if (showGoToLastChapterBadge) {
                            GoToLastReadComposable(
                                onClick = { goToLatestChapter(book) },
                                size = (height.value.height / 20).dp
                            )
                        }
                        
                        if (showAnyBadge || book.isArchived) {
                            LibraryBadges(
                                unread = if (showUnreadBadge || showUnreadChaptersBadge) book.unread else null,
                                downloaded = if (showReadBadge || showDownloadedChaptersBadge) book.downloaded else null,
                                isLocal = showLocalMangaBadge && book.sourceId == -1L,
                                sourceId = book.sourceId,
                                showLanguage = showLanguageBadge,
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
        // NO loading indicator - native apps don't show loading for cached data
    }
}
