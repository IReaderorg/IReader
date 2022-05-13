package org.ireader.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.app.components.BottomTabComposable
import org.ireader.app.viewmodel.LibraryState
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.FilterType
import org.ireader.common_models.SortType
import org.ireader.common_models.entities.BookItem
import org.ireader.common_resources.UiText
import org.ireader.components.list.LayoutComposable
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.ui_library.R

@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    vm: LibraryState,
    goToLatestChapter: (book: BookItem) -> Unit = {},
    onBook: (book: BookItem) -> Unit,
    onLongBook: (book: BookItem) -> Unit,
    onDownload: () -> Unit,
    onMarkAsRead: () -> Unit,
    onMarkAsNotRead: () -> Unit,
    onDelete: () -> Unit,
    addFilters: (FilterType) -> Unit,
    removeFilter: (FilterType) -> Unit,
    onSortSelected: (SortType) -> Unit,
    onLayoutSelected: (DisplayMode) -> Unit,
    getLibraryBooks: () -> Unit,
    refreshUpdate: () -> Unit,
) {


    val pagerState = rememberPagerState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()

    val swipeState = rememberSwipeRefreshState(isRefreshing = false)

    ModalBottomSheetLayout(
        modifier = if (bottomSheetState.targetValue == ModalBottomSheetValue.Expanded) Modifier.statusBarsPadding() else Modifier,
        sheetContent = {

                Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                    BottomTabComposable(
                        pagerState = pagerState,
                        filters = vm.filters,
                        addFilters = addFilters,
                        removeFilter = removeFilter,
                        onSortSelected = onSortSelected,
                        sortType = vm.sortType,
                        isSortDesc = vm.desc,
                        onLayoutSelected = onLayoutSelected,
                        layoutType = vm.layout
                    )
                }
        },
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
    ) {

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
            androidx.compose.material3.Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                topBar = {
                    LibraryScreenTopBar(
                        state = vm,
                        bottomSheetState = bottomSheetState,
                        onSearch = getLibraryBooks,
                        refreshUpdate = refreshUpdate
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
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
                                            books = vm.books,
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
                    when {
                        vm.hasSelection -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp)
                                    .height(60.dp)
                                    .align(Alignment.BottomCenter)
                                    .border(
                                        width = 0.dp, color = MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable(enabled = false) {},
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIconButton(
                                        imageVector = Icons.Default.GetApp,
                                       contentDescription = stringResource(R.string.download),
                                        onClick = onDownload
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.Done,
                                       contentDescription = stringResource(R.string.mark_as_read),
                                        onClick = onMarkAsRead
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.DoneOutline,
                                       contentDescription = stringResource(R.string.mark_as_not_read),
                                        onClick = onMarkAsNotRead
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.Delete,
                                       contentDescription = stringResource(R.string.mark_previous_as_read),
                                        onClick = onDelete
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
