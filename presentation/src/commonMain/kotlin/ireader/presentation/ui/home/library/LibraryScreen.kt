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
                targetState = Pair(vm.isLoading, vm.isEmpty),
                animationSpec = tween(durationMillis = 300)
            ) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty && vm.filters.value.isEmpty() -> EmptyScreen(
                        text = localize(MR.strings.empty_library)
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
                    onClickMarkAsRead = onMarkAsRead,
                    onClickMarkAsUnread = onMarkAsNotRead
                )
            }
        }
    }
}
