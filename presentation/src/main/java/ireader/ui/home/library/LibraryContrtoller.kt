@file:OptIn(ExperimentalPagerApi::class)

package ireader.ui.home.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.toBookCategory
import ireader.ui.component.Controller
import ireader.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryController(
    modifier: Modifier,
    vm: LibraryViewModel,
    controller: Controller,
    goToReader: (BookItem) -> Unit,
    goToDetail: (BookItem) -> Unit,

    ) {

    LibraryScreen(
        modifier = modifier,
        onMarkAsRead = {
            with(vm) {
                viewModelScope.launch(Dispatchers.IO) {
                    markBookAsReadOrNotUseCase.markAsRead(selectedBooks)
                    selectedBooks.clear()
                }
            }
        },
        onDownload = {
            vm.downloadChapters()
        },
        onMarkAsNotRead = {
            with(vm) {
                viewModelScope.launch(Dispatchers.IO) {
                    markBookAsReadOrNotUseCase.markAsNotRead(selectedBooks)
                    selectedBooks.clear()
                }
            }
        },
        onDelete = {
            with(vm) {
                viewModelScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        deleteUseCase.unFavoriteBook(selectedBooks)
                    }
                    selectedBooks.clear()
                }
            }
        },
        goToLatestChapter = { book ->
            goToReader(book)
        },
        onBook = { book ->
            if (vm.selectionMode) {
                if (book.id in vm.selectedBooks) {
                    vm.selectedBooks.remove(book.id)
                } else {
                    vm.selectedBooks.add(book.id)
                }
            } else {
                goToDetail(book)
            }
        },
        onLongBook = {
            if (it.id in vm.selectedBooks) return@LibraryScreen
            vm.selectedBooks.add(it.id)
        },
        vm = vm,
        refreshUpdate = {
            vm.refreshUpdate()
        },
        bottomSheetState = controller.sheetState,
        onClickChangeCategory = {
            vm.showDialog = true
        },
        scaffoldPadding = controller.scaffoldPadding,
        requestHideBottomNav = controller.requestHideNavigator,
        getColumnsForOrientation = { isLandscape ->
            vm.getColumnsForOrientation(isLandscape, this)
        },
        editCategoryDismissDialog = {
            vm.showDialog = false
            vm.selectedBooks.clear()
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
            vm.viewModelScope.launch(Dispatchers.IO) {
                vm.getCategory.insertBookCategory(vm.addQueues)
                vm.getCategory.deleteBookCategory(vm.deleteQueues)
                vm.deleteQueues.clear()
                vm.addQueues.clear()
                vm.selectedBooks.clear()
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
        }
    )
}
