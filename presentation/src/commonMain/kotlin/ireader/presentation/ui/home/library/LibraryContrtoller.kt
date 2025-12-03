package ireader.presentation.ui.home.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.toBookCategory
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.removeIf
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryController(
    modifier: Modifier,
    vm: LibraryViewModel,
    goToReader: (BookItem) -> Unit,
    goToDetail: (BookItem) -> Unit,
    scaffoldPadding: PaddingValues,
    sheetState: SheetState,
    requestHideNavigator: (Boolean) -> Unit,
    showFilterSheet: Boolean = false,
    onShowFilterSheet: () -> Unit = {},
    onHideFilterSheet: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    
    // Local state for category dialog
    var showDialog by remember { mutableStateOf(false) }
    val addQueues = remember { mutableStateListOf<BookCategory>() }
    val deleteQueues = remember { mutableStateListOf<BookCategory>() }

    LibraryScreen(
        modifier = modifier,
        onMarkAsRead = {
            vm.scope.launch(ioDispatcher) {
                vm.markAsReadWithUndo()
            }
        },
        onDownload = {
            vm.downloadChapters()
        },
        onDownloadUnread = {
            vm.scope.launch(ioDispatcher) {
                vm.downloadUnreadChapters()
            }
        },
        onMarkAsNotRead = {
            vm.scope.launch(ioDispatcher) {
                vm.markAsUnreadWithUndo()
            }
        },
        onDelete = {
            vm.scope.launch(ioDispatcher) {
                kotlin.runCatching {
                    vm.deleteUseCase.unFavoriteBook(state.selectedBookIds.toList())
                }
                vm.unselectAll()
            }
        },
        goToLatestChapter = { book ->
            goToReader(book)
        },
        onBook = { book ->
            if (state.selectionMode) {
                vm.toggleSelection(book.id)
            } else {
                goToDetail(book)
            }
        },
        onLongBook = {
            vm.toggleSelection(it.id)
        },
        vm = vm,
        refreshUpdate = {
            vm.refreshUpdate()
        },
        onClickChangeCategory = {
            showDialog = true
        },
        scaffoldPadding = scaffoldPadding,
        requestHideBottomNav = requestHideNavigator,
        getColumnsForOrientation = { isLandscape ->
            vm.getColumnsForOrientation(isLandscape, this)
        },
        showCategoryDialog = showDialog,
        editCategoryDismissDialog = {
            showDialog = false
            vm.unselectAll()
            addQueues.clear()
            deleteQueues.clear()
        },
        editCategoryOnAddDeleteQueue = { category ->
            deleteQueues.addAll(category.toBookCategory(state.selectedBookIds.toList()))
        },
        editCategoryOnAddToInsertQueue = { category ->
            addQueues.addAll(category.toBookCategory(state.selectedBookIds.toList()))
        },
        editCategoryOnConfirm = {
            vm.scope.launch(ioDispatcher) {
                vm.getCategory.insertBookCategory(addQueues)
                vm.getCategory.deleteBookCategory(deleteQueues)
                deleteQueues.clear()
                addQueues.clear()
                vm.unselectAll()
            }
            showDialog = false
        },
        editCategoryOnRemoteInDeleteQueue = { category ->
            deleteQueues.removeIf { it.categoryId == category.id }
        },
        editCategoryOnRemoteInInsertQueue = { category ->
            addQueues.removeIf { it.categoryId == category.id }
        },
        onPagerPageChange = {
            vm.setSelectedPage(it)
        },
        showFilterSheet = showFilterSheet,
        onShowFilterSheet = onShowFilterSheet,
        onHideFilterSheet = onHideFilterSheet
    )
}
