package org.ireader.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.entities.BookItem
import org.ireader.common_resources.UiText
import org.ireader.components.list.LayoutComposable
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.ui_library.R

@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
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
    getLibraryBooks: () -> Unit,
    refreshUpdate: () -> Unit,
    onClickChangeCategory: () -> Unit,
    bottomSheetState: ModalBottomSheetState
) {

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()

    val swipeState = rememberSwipeRefreshState(isRefreshing = false)


    SwipeRefresh(
        state = swipeState, onRefresh = { refreshUpdate() },
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
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Crossfade(
                targetState = Pair(
                    vm.isLoading,
                    vm.isEmpty
                )
            ) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty && vm.filters.isEmpty() -> EmptyScreen(
                        text = UiText.StringResource(R.string.empty_library)
                    )
                    else -> {
                        LazyColumnScrollbar(
                            listState = lazyListState,
                        ) {
                            LayoutComposable(
                                books = vm.books.map { it.toBookItem() },
                                layout = vm.layout,
                                isLocal = true,
                                gridState = gridState,
                                scrollState = lazyListState,
                                selection = vm.selection,
                                goToLatestChapter = goToLatestChapter,
                                onClick = onBook,
                                onLongClick = onLongBook,
                            )
                        }
                    }
                }
            }
            LibrarySelectionBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = vm.hasSelection,
                onClickChangeCategory = onClickChangeCategory ,
                onClickDeleteDownloads = onDelete,
                onClickDownload = onDownload,
                onClickMarkAsRead = onMarkAsRead,
                onClickMarkAsUnread = onMarkAsNotRead
            )

        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LibrarySelectionBar(
    visible: Boolean,
    onClickChangeCategory: () -> Unit,
    onClickDownload: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    onClickMarkAsUnread: () -> Unit,
    onClickDeleteDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppIconButton(imageVector = Icons.Outlined.Label, onClick = onClickChangeCategory)
                AppIconButton(imageVector = Icons.Outlined.Download, onClick = onClickDownload)
                AppIconButton(imageVector = Icons.Outlined.Done, onClick = onClickMarkAsRead)
                AppIconButton(
                    imageVector = Icons.Outlined.DoneOutline,
                    onClick = onClickMarkAsUnread
                )
                AppIconButton(imageVector = Icons.Outlined.Delete, onClick = onClickDeleteDownloads)
            }
        }
    }
}
