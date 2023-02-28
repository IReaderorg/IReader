package ireader.presentation.ui.home.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.core.ui.LoadingScreen
import ireader.presentation.ui.home.library.components.EditCategoriesDialog
import ireader.presentation.ui.home.library.ui.LibraryContent
import ireader.presentation.ui.home.library.ui.LibrarySelectionBar
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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
    val refresh = remember {
        mutableStateOf(false)
    }
    val swipeRefreshState = rememberPullRefreshState(refresh.value, onRefresh = refreshUpdate)

    BoxWithConstraints(
        modifier = modifier
            .pullRefresh(swipeRefreshState)
            .padding(scaffoldPadding)
            .fillMaxSize(),
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
                    text = localize(MR.strings.empty_library)
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
        PullRefreshIndicator(refresh.value, swipeRefreshState, Modifier.align(Alignment.TopCenter))
    }
}
