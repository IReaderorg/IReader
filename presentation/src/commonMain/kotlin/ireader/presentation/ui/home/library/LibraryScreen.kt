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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers

import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.library.components.EditCategoriesDialog
import ireader.presentation.ui.home.library.components.LibraryFilterBottomSheet
import ireader.presentation.ui.home.library.components.BatchOperationProgressDialog
import ireader.presentation.ui.home.library.components.BatchOperationResultDialog
import ireader.presentation.ui.home.library.components.BatchOperationDialog
import ireader.presentation.ui.home.library.components.BatchOperation
import ireader.presentation.ui.home.library.ui.LibraryContent
import ireader.presentation.ui.home.library.ui.LibrarySelectionBar
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@OptIn(
    ExperimentalMaterialApi::class
)
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
    bottomSheetState: ModalBottomSheetState,
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
        BoxWithConstraints(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
            
            // Spotify-style Resume Reading Bar at the bottom
            ireader.presentation.ui.home.library.components.ResumeReadingCard(
                lastRead = vm.lastReadInfo,
                onResume = {
                    vm.lastReadInfo?.let { info ->
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
                isVisible = vm.isResumeCardVisible && !vm.selectionMode,
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
            
            Crossfade(
                targetState = Pair(vm.isLoading, vm.isEmpty),
                animationSpec = tween(durationMillis = 300)
            ) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty && vm.filters.value.isEmpty() -> EmptyScreen(
                        text = localize(Res.string.empty_library)
                    )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
        
        // Batch operation dialog
        BatchOperationDialog(
            isVisible = vm.showBatchOperationDialog,
            selectedCount = vm.selectedBooks.size,
            onOperationSelected = { operation ->
                when (operation) {
                    BatchOperation.DELETE -> onDelete()
                    BatchOperation.CHANGE_CATEGORY -> onClickChangeCategory()
                    else -> vm.performBatchOperation(operation)
                }
            },
            onDismiss = { vm.hideBatchOperationDialog() }
        )
        
        // Batch operation progress dialog
        BatchOperationProgressDialog(
            isVisible = vm.batchOperationInProgress,
            message = vm.batchOperationMessage ?: "Processing..."
        )
        
        // Batch operation result dialog
        var showResultDialog by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }
        var showUndoOption by remember { mutableStateOf(false) }
        
        LaunchedEffect(vm.batchOperationMessage, vm.batchOperationInProgress) {
            if (!vm.batchOperationInProgress && vm.batchOperationMessage != null) {
                resultMessage = vm.batchOperationMessage ?: ""
                showResultDialog = true
                // Show undo option if there's a recent undo state (within 10 seconds)
                showUndoOption = vm.lastUndoState != null && 
                    (System.currentTimeMillis() - (vm.lastUndoState?.timestamp ?: 0)) < 10000
            }
        }
        
        BatchOperationResultDialog(
            isVisible = showResultDialog,
            title = "Batch Operation Complete",
            message = resultMessage,
            showUndo = showUndoOption,
            onUndo = {
                vm.scope.launch(Dispatchers.IO) {
                    vm.lastUndoState?.let { undoState ->
                        vm.undoMarkOperation(undoState.previousChapterStates)
                        vm.lastUndoState = null
                    }
                }
            },
            onDismiss = {
                showResultDialog = false
                vm.batchOperationMessage = null
            }
        )
    }
}
