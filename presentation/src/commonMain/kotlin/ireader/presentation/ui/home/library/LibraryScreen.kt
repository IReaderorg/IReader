package ireader.presentation.ui.home.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.library.components.EditCategoriesDialog
import ireader.presentation.ui.home.library.components.LibraryFilterBottomSheet
import ireader.presentation.ui.home.library.ui.LibraryContent
import ireader.presentation.ui.home.library.ui.LibrarySelectionBar
import ireader.presentation.ui.home.library.viewmodel.LibraryScreenState
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ExperimentalAnimationApi
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    vm: LibraryViewModel,
    goToLatestChapter: (book: BookItem) -> Unit = {},
    onBook: (book: BookItem) -> Unit,
    onLongBook: (book: BookItem) -> Unit,
    onDownload: () -> Unit,
    onDownloadUnread: () -> Unit,
    onMarkAsRead: () -> Unit,
    onMarkAsNotRead: () -> Unit,
    onDelete: () -> Unit,
    refreshUpdate: () -> Unit,
    onClickChangeCategory: () -> Unit,
    scaffoldPadding: PaddingValues,
    requestHideBottomNav: (Boolean) -> Unit,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
    onPagerPageChange: (page: Int) -> Unit,
    showCategoryDialog: Boolean = false,
    editCategoryOnConfirm: () -> Unit,
    editCategoryDismissDialog: () -> Unit,
    editCategoryOnAddToInsertQueue: (Category) -> Unit,
    editCategoryOnRemoteInInsertQueue: (Category) -> Unit,
    editCategoryOnRemoteInDeleteQueue: (Category) -> Unit,
    editCategoryOnAddDeleteQueue: (Category) -> Unit,
    showFilterSheet: Boolean = false,
    onShowFilterSheet: () -> Unit = {},
    onHideFilterSheet: () -> Unit = {},
) {
    // Collect state from ViewModel
    val state by vm.state.collectAsState()
    
    // Pre-compute modifiers
    val fillMaxSizeModifier = remember { Modifier.fillMaxSize() }
    val selectionBarModifier = remember {
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    }
    
    // Show resume card when not in selection mode
    val showResumeCard = state.isResumeCardVisible && !state.selectionMode
    
    LaunchedEffect(state.selectionMode) {
        requestHideBottomNav(state.selectionMode)
    }
    
    // Refresh last read info when screen becomes visible
    LaunchedEffect(Unit) {
        vm.loadLastReadInfo()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(scaffoldPadding).fillMaxSize(),
        ) {
            Column(modifier = fillMaxSizeModifier) {
                LibraryContent(
                    vm = vm,
                    onBook = onBook,
                    onLongBook = onLongBook,
                    goToLatestChapter = goToLatestChapter,
                    onPageChanged = onPagerPageChange,
                    getColumnsForOrientation = getColumnsForOrientation,
                    onResumeReading = {}
                )
            }
            
            // Resume Reading Card
            ireader.presentation.ui.home.library.components.ResumeReadingCard(
                lastRead = state.lastReadInfo,
                onResume = {
                    state.lastReadInfo?.let { info ->
                        val bookItem = BookItem(
                            id = info.novelId,
                            sourceId = 0,
                            title = info.novelTitle,
                            cover = info.coverUrl,
                            customCover = info.coverUrl
                        )
                        goToLatestChapter(bookItem)
                    }
                },
                onDismiss = { vm.dismissResumeCard() },
                isVisible = showResumeCard,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            
            EditCategoriesDialog(
                vm = vm,
                showDialog = showCategoryDialog,
                onConfirm = editCategoryOnConfirm,
                dismissDialog = editCategoryDismissDialog,
                onAddDeleteQueue = editCategoryOnAddDeleteQueue,
                onRemoteInInsertQueue = editCategoryOnRemoteInInsertQueue,
                onAddToInsertQueue = editCategoryOnAddToInsertQueue,
                onRemoteInDeleteQueue = editCategoryOnRemoteInDeleteQueue,
                categories = state.categories.filter { !it.category.isSystemCategory }
            )
            
            // Update Category Dialog
            if (state.showUpdateCategoryDialog) {
                ireader.presentation.ui.home.library.components.UpdateCategoryDialog(
                    categories = state.categories.map { it.category }.filter { !it.isSystemCategory },
                    onCategorySelected = { category -> vm.updateCategory(category.id) },
                    onDismiss = { vm.hideUpdateCategoryDialog() }
                )
            }
            
            // Show empty screen only when not loading and truly empty
            if (!state.isLoading && state.isEmpty && state.filters.isEmpty()) {
                EmptyScreen(text = localize(Res.string.empty_library))
            }

            // Selection bar
            AnimatedVisibility(
                visible = state.selectionMode,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                LibrarySelectionBar(
                    modifier = selectionBarModifier,
                    visible = true,
                    onClickChangeCategory = onClickChangeCategory,
                    onClickDeleteDownload = onDelete,
                    onClickDownload = onDownload,
                    onClickDownloadUnread = onDownloadUnread,
                    onClickMarkAsRead = onMarkAsRead,
                    onClickMarkAsUnread = onMarkAsNotRead
                )
            }
        }
        
        // Filter Bottom Sheet
        if (showFilterSheet) {
            LibraryFilterBottomSheet(
                filters = state.filters,
                sorting = state.sort,
                columnCount = state.columnsInPortrait,
                displayMode = state.layout,
                showResumeReadingCard = vm.showResumeReadingCard.value,
                showArchivedBooks = vm.showArchivedBooks.value,
                onFilterToggle = { type -> vm.toggleFilterImmediate(type) },
                onSortChange = { type -> vm.toggleSort(type) },
                onSortDirectionToggle = { vm.toggleSortDirection() },
                onColumnCountChange = { count -> vm.updateColumnCount(count) },
                onDisplayModeChange = { mode -> vm.onLayoutTypeChange(mode) },
                onResumeReadingCardToggle = { enabled -> vm.toggleResumeReadingCard(enabled) },
                onArchivedBooksToggle = { enabled -> vm.toggleShowArchivedBooks(enabled) },
                onDismiss = onHideFilterSheet
            )
        }
    }
}
