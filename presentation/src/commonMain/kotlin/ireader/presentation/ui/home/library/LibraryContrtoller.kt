package ireader.presentation.ui.home.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.toBookCategory
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryController(
        modifier: Modifier,
        vm: LibraryViewModel,
        goToReader: (BookItem) -> Unit,
        goToDetail: (BookItem) -> Unit,
        scaffoldPadding: PaddingValues,
        sheetState: SheetState,
        requestHideNavigator : (Boolean) -> Unit,
        showFilterSheet: Boolean = false,
        onShowFilterSheet: () -> Unit = {},
        onHideFilterSheet: () -> Unit = {}
    ) {

    LibraryScreen(
        modifier = modifier,
        onMarkAsRead = {
            with(vm) {
                scope.launch(Dispatchers.IO) {
                    batchOperationInProgress = true
                    batchOperationMessage = "Marking chapters as read..."
                    
                    val result = markAsReadWithUndo()
                    
                    batchOperationInProgress = false
                    batchOperationMessage = when (result) {
                        is ireader.domain.usecases.local.book_usecases.MarkResult.Success -> {
                            // Store undo state
                            lastUndoState = ireader.presentation.ui.home.library.viewmodel.UndoState(
                                previousChapterStates = result.previousStates,
                                operationType = ireader.presentation.ui.home.library.viewmodel.UndoOperationType.MARK_AS_READ,
                                timestamp = System.currentTimeMillis()
                            )
                            
                            "Marked ${result.totalChapters} chapters as read in ${result.totalBooks} books"
                        }
                        is ireader.domain.usecases.local.book_usecases.MarkResult.Failure -> {
                            "Failed to mark as read: ${result.message}"
                        }
                    }
                }
            }
        },
        onDownload = {
            vm.downloadChapters()
        },
        onDownloadUnread = {
            with(vm) {
                scope.launch(Dispatchers.IO) {
                    batchOperationInProgress = true
                    batchOperationMessage = "Downloading unread chapters..."
                    
                    val result = downloadUnreadChapters()
                    
                    batchOperationInProgress = false
                    batchOperationMessage = when (result) {
                        is ireader.domain.usecases.local.book_usecases.DownloadResult.Success -> {
                            if (result.failedBooks.isEmpty()) {
                                "Successfully queued ${result.totalChapters} chapters from ${result.totalBooks} books for download"
                            } else {
                                "Queued ${result.totalChapters} chapters from ${result.totalBooks} books. ${result.failedBooks.size} books failed"
                            }
                        }
                        is ireader.domain.usecases.local.book_usecases.DownloadResult.NoUnreadChapters -> {
                            "No unread chapters found in ${result.totalBooks} selected books"
                        }
                        is ireader.domain.usecases.local.book_usecases.DownloadResult.Failure -> {
                            "Failed to download: ${result.message}"
                        }
                    }
                }
            }
        },
        onMarkAsNotRead = {
            with(vm) {
                scope.launch(Dispatchers.IO) {
                    batchOperationInProgress = true
                    batchOperationMessage = "Marking chapters as unread..."
                    
                    val result = markAsUnreadWithUndo()
                    
                    batchOperationInProgress = false
                    batchOperationMessage = when (result) {
                        is ireader.domain.usecases.local.book_usecases.MarkResult.Success -> {
                            // Store undo state
                            lastUndoState = ireader.presentation.ui.home.library.viewmodel.UndoState(
                                previousChapterStates = result.previousStates,
                                operationType = ireader.presentation.ui.home.library.viewmodel.UndoOperationType.MARK_AS_UNREAD,
                                timestamp = System.currentTimeMillis()
                            )
                            
                            "Marked ${result.totalChapters} chapters as unread in ${result.totalBooks} books"
                        }
                        is ireader.domain.usecases.local.book_usecases.MarkResult.Failure -> {
                            "Failed to mark as unread: ${result.message}"
                        }
                    }
                }
            }
        },
        onDelete = {
            with(vm) {
                scope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        deleteUseCase.unFavoriteBook(selectedBooks.toList())
                    }
                    unselectAll()
                }
            }
        },
        goToLatestChapter = { book ->
            goToReader(book)
        },
        onBook = { book ->
            if (vm.selectionMode) {
                vm.toggleSelection(book.id)
            } else {
                goToDetail(book)
            }
        },
        onLongBook = {
            // Use toggleSelection to properly manage selection state
            vm.toggleSelection(it.id)
        },
        vm = vm,
        refreshUpdate = {
            vm.refreshUpdate()
        },
        onClickChangeCategory = {
            vm.showDialog = true
        },
        scaffoldPadding = scaffoldPadding,
        requestHideBottomNav = requestHideNavigator,
        getColumnsForOrientation = { isLandscape ->
            vm.getColumnsForOrientation(isLandscape, this)
        },
        editCategoryDismissDialog = {
            vm.showDialog = false
            vm.unselectAll()
            vm.addQueues.clear()
            vm.deleteQueues.clear()
        },
        editCategoryOnAddDeleteQueue = { category ->
            vm.deleteQueues.addAll(category.toBookCategory(vm.selectedBooks))
        },
        editCategoryOnAddToInsertQueue = { category ->
            vm.addQueues.addAll(category.toBookCategory(vm.selectedBooks))
        },
        editCategoryOnConfirm = {
            vm.scope.launch(Dispatchers.IO) {
                vm.getCategory.insertBookCategory(vm.addQueues)
                vm.getCategory.deleteBookCategory(vm.deleteQueues)
                vm.deleteQueues.clear()
                vm.addQueues.clear()
                vm.unselectAll()
                vm.addQueues.clear()
                vm.deleteQueues.clear()
            }
            vm.showDialog = false
        },
        editCategoryOnRemoteInDeleteQueue = { category ->
            vm.deleteQueues.removeIf { it.categoryId == category.id }
        },
        editCategoryOnRemoteInInsertQueue = { category ->
            vm.addQueues.removeIf { it.categoryId == category.id }
        },
        onPagerPageChange = {
            vm.setSelectedPage(it)
        },
        showFilterSheet = showFilterSheet,
        onShowFilterSheet = onShowFilterSheet,
        onHideFilterSheet = onHideFilterSheet
    )
}
