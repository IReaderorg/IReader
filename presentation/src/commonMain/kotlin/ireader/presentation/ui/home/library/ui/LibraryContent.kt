package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.BookItem
import ireader.presentation.ui.home.library.components.ScrollableTabs
import ireader.presentation.ui.home.library.components.visibleName
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.home.library.viewmodel.PaginationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryContent(
        vm: LibraryViewModel,
        onBook: (book: BookItem) -> Unit,
        onLongBook: (book: BookItem) -> Unit,
        goToLatestChapter: (book: BookItem) -> Unit,
        onPageChanged: (Int) -> Unit,
        getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
        onResumeReading: () -> Unit,
) {
    // Collect state reactively
    val state by vm.state.collectAsState()
    val categories = state.categories
    val selectedCategoryIndex = state.selectedCategoryIndex
    val selectedBooks = state.selectedBookIds
    val layout = state.layout
    val isLoading = state.isLoading
    val inSearchMode = state.inSearchMode
    val searchBooks = state.books // This contains search results when in search mode
    
    // Show search results when in search mode
    if (inSearchMode) {
        val searchBookItems = remember(searchBooks) {
            searchBooks.map { it.toBookItem() }
        }
        
        LibrarySearchResults(
            books = searchBookItems,
            layout = layout,
            selection = selectedBooks.toList(),
            onClick = onBook,
            onLongClick = onLongBook,
            goToLatestChapter = goToLatestChapter,
            showUnreadBadge = vm.unreadBadge.value,
            showReadBadge = vm.readBadge.value,
            showGoToLastChapterBadge = vm.goToLastChapterBadge.value,
            getColumnsForOrientation = getColumnsForOrientation,
            columnsInPortrait = state.columnsInPortrait,
            columnsInLandscape = state.columnsInLandscape
        )
        return
    }
    
    // If categories not ready yet, return calmly without jarring placeholders
    val horizontalPager =
        rememberPagerState(
            initialPage = selectedCategoryIndex,
            initialPageOffsetFraction = 0f
        ) {
            categories.size
        }
    LaunchedEffect(horizontalPager) {
        snapshotFlow { horizontalPager.currentPage }.collect {
            onPageChanged(it)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScrollableTabs(
                modifier = Modifier.fillMaxWidth(),
                libraryTabs = categories
                    .map { it.visibleName.plus(if (vm.showCountInCategory.value) " (${it.bookCount})" else "") },
                pagerState = horizontalPager,
                visible = vm.showCategoryTabs.value && categories.isNotEmpty()
            )
            
            LibraryPager(
                pagerState = horizontalPager,
                onClick = onBook,
                onLongClick = onLongBook,
                goToLatestChapter = goToLatestChapter,
                categories = categories,
                pageCount = categories.size,
                layout = layout,
                onPageChange = { page ->
                    vm.getLibraryForCategoryIndexAsState(categoryIndex = page)
                },
                selection = selectedBooks.toList(),
                currentPage = selectedCategoryIndex,
                showUnreadBadge = vm.unreadBadge.value,
                showReadBadge = vm.readBadge.value,
                showGoToLastChapterBadge = vm.goToLastChapterBadge.value,
                showDownloadedChaptersBadge = vm.showDownloadedChaptersBadge.value,
                showUnreadChaptersBadge = vm.showUnreadChaptersBadge.value,
                showLocalMangaBadge = vm.showLocalMangaBadge.value,
                showLanguageBadge = vm.showLanguageBadge.value,
                getColumnsForOrientation = getColumnsForOrientation,
                columnsInPortrait = state.columnsInPortrait,
                columnsInLandscape = state.columnsInLandscape,
                onSaveScrollPosition = { categoryId, index, offset ->
                    vm.saveScrollPosition(categoryId, index, offset)
                },
                getScrollPosition = { categoryId ->
                    vm.getScrollPosition(categoryId)
                },
                onLoadMore = { categoryId ->
                    vm.loadMoreBooks(categoryId)
                },
                getPaginationState = { categoryId ->
                    state.categoryPaginationState[categoryId] ?: PaginationState()
                }
            )
        }

        // Subtle top progress line overlay - zero layout shifts!
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}
