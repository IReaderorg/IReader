package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.BookItem
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.library.components.ScrollableTabs
import ireader.presentation.ui.home.library.components.visibleName
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
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
    
    // Early return for empty categories - but remember the check to avoid recomposition
    val hasCategories = remember(categories) { categories.isNotEmpty() }
    if (!hasCategories) return
    
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

    // Trigger data loading for the current category
    val currentCategoryBooks by vm.getLibraryForCategoryIndex(categoryIndex = selectedCategoryIndex)
    
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Show loading screen (hides tabs too)
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen()
            }
        } else {
            // Show tabs only when not loading
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
                    vm.getLibraryForCategoryIndex(categoryIndex = page)
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
                onSaveScrollPosition = { categoryId, index, offset ->
                    vm.saveScrollPosition(categoryId, index, offset)
                },
                getScrollPosition = { categoryId ->
                    vm.getScrollPosition(categoryId)
                }
            )
        }
    }
}
