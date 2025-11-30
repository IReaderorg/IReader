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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.library.components.EditCategoriesDialog
import ireader.presentation.ui.home.library.components.LibraryFilterBottomSheet
import ireader.presentation.ui.home.library.ui.LibraryContent
import ireader.presentation.ui.home.library.ui.LibrarySelectionBar
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
    // Pre-compute modifiers to avoid recreation on each recomposition
    val fillMaxSizeModifier = remember { Modifier.fillMaxSize() }
    val selectionBarModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }
    
    // Memoize callbacks to prevent unnecessary recompositions
    val stableGoToLatestChapter = remember(goToLatestChapter) { goToLatestChapter }
    val stableOnBook = remember(onBook) { onBook }
    val stableOnLongBook = remember(onLongBook) { onLongBook }
    
    // Derive loading/empty state to minimize recompositions
    val screenState by remember {
        derivedStateOf {
            when {
                vm.isLoading -> LibraryScreenState.Loading
                vm.isEmpty && vm.filters.value.isEmpty() -> LibraryScreenState.Empty
                else -> LibraryScreenState.Content
            }
        }
    }
    
    // Derive resume card visibility
    val showResumeCard by remember {
        derivedStateOf { vm.isResumeCardVisible && !vm.selectionMode }
    }
    
    LaunchedEffect(vm.selectionMode) {
        requestHideBottomNav(vm.selectionMode)
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
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize(),
        ) {
            Column(modifier = fillMaxSizeModifier) {
                LibraryContent(
                    vm = vm,
                    onBook = stableOnBook,
                    onLongBook = stableOnLongBook,
                    goToLatestChapter = stableGoToLatestChapter,
                    onPageChanged = onPagerPageChange,
                    getColumnsForOrientation = getColumnsForOrientation,
                    onResumeReading = {}
                )
            }
            
            // Spotify-style Resume Reading Bar at the bottom - memoized click handler
            val resumeClickHandler: () -> Unit = remember(vm.lastReadInfo, stableGoToLatestChapter) {
                {
                    vm.lastReadInfo?.let { info ->
                        val bookItem = BookItem(
                            id = info.novelId,
                            sourceId = 0,
                            title = info.novelTitle,
                            cover = info.coverUrl,
                            customCover = info.coverUrl
                        )
                        stableGoToLatestChapter(bookItem)
                    }
                    Unit
                }
            }
            
            ireader.presentation.ui.home.library.components.ResumeReadingCard(
                lastRead = vm.lastReadInfo,
                onResume = resumeClickHandler,
                onDismiss = { vm.dismissResumeCard() },
                isVisible = showResumeCard,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            
            EditCategoriesDialog(
                vm = vm,
                onConfirm = editCategoryOnConfirm,
                dismissDialog = editCategoryDismissDialog,
                onAddDeleteQueue = editCategoryOnAddDeleteQueue,
                onRemoteInInsertQueue = editCategoryOnRemoteInInsertQueue,
                onAddToInsertQueue = editCategoryOnAddToInsertQueue,
                onRemoteInDeleteQueue = editCategoryOnRemoteInDeleteQueue,
                categories = vm.categories.filter { !it.category.isSystemCategory }
            )
            
            // Update Category Dialog
            if (vm.showUpdateCategoryDialog) {
                ireader.presentation.ui.home.library.components.UpdateCategoryDialog(
                    categories = vm.categories.map { it.category }.filter { !it.isSystemCategory },
                    onCategorySelected = { category ->
                        vm.updateCategory(category.id)
                    },
                    onDismiss = {
                        vm.hideUpdateCategoryDialog()
                    }
                )
            }
            
            // Use derived state for efficient state-based rendering
            Crossfade(
                targetState = screenState,
                animationSpec = tween(durationMillis = 300)
            ) { state ->
                when (state) {
                    LibraryScreenState.Loading -> LoadingScreen()
                    LibraryScreenState.Empty -> EmptyScreen(
                        text = localize(Res.string.empty_library)
                    )
                    LibraryScreenState.Content -> { /* Content is rendered above */ }
                }
            }

            AnimatedVisibility(
                visible = vm.selectionMode,
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
                filters = vm.filters.value,
                sorting = vm.sorting.value,
                columnCount = vm.columnInPortrait.value,
                displayMode = vm.layout,
                showResumeReadingCard = vm.showResumeReadingCard.value,
                showArchivedBooks = vm.showArchivedBooks.value,
                onFilterToggle = { type ->
                    vm.toggleFilterImmediate(type)
                },
                onSortChange = { type ->
                    vm.toggleSort(type)
                },
                onSortDirectionToggle = {
                    vm.toggleSortDirection()
                },
                onColumnCountChange = { count ->
                    vm.updateColumnCount(count)
                },
                onDisplayModeChange = { mode ->
                    vm.onLayoutTypeChange(mode)
                },
                onResumeReadingCardToggle = { enabled ->
                    vm.toggleResumeReadingCard(enabled)
                },
                onArchivedBooksToggle = { enabled ->
                    vm.toggleShowArchivedBooks(enabled)
                },
                onDismiss = onHideFilterSheet
            )
        }
    }
}

/**
 * Enum representing the different states of the library screen
 * Used with derivedStateOf for efficient recomposition
 */
private enum class LibraryScreenState {
    Loading,
    Empty,
    Content
}
