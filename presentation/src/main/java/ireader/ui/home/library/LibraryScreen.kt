package ireader.ui.home.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ireader.ui.home.library.components.EditCategoriesDialog
import ireader.ui.home.library.ui.LibraryContent
import ireader.ui.home.library.ui.LibrarySelectionBar
import ireader.ui.home.library.viewmodel.LibraryViewModel
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.Category
import ireader.core.ui.ui.EmptyScreen
import ireader.core.ui.ui.LoadingScreen
import ireader.presentation.R

@ExperimentalPagerApi
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

) {

    LaunchedEffect(vm.selectionMode) {
        requestHideBottomNav(vm.selectionMode)
    }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    BoxWithConstraints(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize(),
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = refreshUpdate,
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = 8.dp,
                )
            }
        ) {
            Column {
                LibraryContent(
                    vm = vm,
                    onBook = onBook,
                    onLongBook = onLongBook,
                    goToLatestChapter = goToLatestChapter,
                    onPageChanged = onPagerPageChange,
                    getColumnsForOrientation = getColumnsForOrientation,

                )
            }
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
            Crossfade(
                targetState = Pair(
                    vm.isLoading,
                    vm.isEmpty
                )
            ) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty && vm.filters.value.isEmpty() -> EmptyScreen(
                        text = stringResource(R.string.empty_library)
                    )
                }
            }

            LibrarySelectionBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = vm.selectionMode,
                onClickChangeCategory = onClickChangeCategory,
                onClickDeleteDownload = onDelete,
                onClickDownload = onDownload,
                onClickMarkAsRead = onMarkAsRead,
                onClickMarkAsUnread = onMarkAsNotRead
            )
        }
    }
}
