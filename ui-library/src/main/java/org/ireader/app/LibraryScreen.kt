package org.ireader.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.ireader.app.components.EditCategoriesDialog
import org.ireader.app.components.ScrollableTabs
import org.ireader.app.components.visibleName
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.DisplayMode.Companion.displayMode
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.common_models.entities.toBookCategory
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
    refreshUpdate: () -> Unit,
    onClickChangeCategory: () -> Unit,
    bottomSheetState: ModalBottomSheetState,
    scaffoldPadding: PaddingValues,
    onAddToCategoryConfirm: () -> Unit,
    requestHideBottomNav: (Boolean) -> Unit,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
) {

    LaunchedEffect(vm.selectionMode) {
        requestHideBottomNav(vm.selectionMode)
    }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)



    Box(
        modifier = Modifier
            .padding(scaffoldPadding)
            .fillMaxSize(),
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { refreshUpdate() },
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
                    onPageChanged = {
                        vm.setSelectedPage(it)
                    },
                    getColumnsForOrientation= getColumnsForOrientation
                )

            }
            EditCategoriesDialog(
                vm = vm,
                onConfirm = {
                    onAddToCategoryConfirm()
                },
                dismissDialog = {
                    vm.showDialog = false
                    vm.selectedBooks.clear()
                    vm.addQueues.clear()
                    vm.deleteQueues.clear()
                },
                onAddDeleteQueue = { category ->
                    vm.deleteQueues.addAll(category.toBookCategory(vm.selectedBooks))
                },
                onRemoteInInsertQueue = { category ->
                    vm.addQueues.removeIf { it.categoryId == category.id }
                },
                onAddToInsertQueue = { category ->
                    vm.addQueues.addAll(category.toBookCategory(vm.selectedBooks))
                },
                onRemoteInDeleteQueue = { category ->
                    vm.deleteQueues.removeIf { it.categoryId == category.id }
                },
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

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
private fun LibraryContent(
    vm: LibraryViewModel,
    onBook: (book: BookItem) -> Unit,
    onLongBook: (book: BookItem) -> Unit,
    goToLatestChapter: (book: BookItem) -> Unit,
    onPageChanged: (Int) -> Unit,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
) {
    if (vm.categories.isEmpty()) return
    val horizontalPager = rememberPagerState(initialPage = vm.selectedCategoryIndex)
    LaunchedEffect(horizontalPager) {
        snapshotFlow { horizontalPager.currentPage }.collect {
            onPageChanged(it)
        }
    }
    ScrollableTabs(
        modifier = Modifier.fillMaxWidth(),
        libraryTabs = vm.categories.map { it.visibleName.plus(if (vm.showCountInCategory.value) " (${it.bookCount})" else "") },
        pagerState = horizontalPager,
        visible = vm.showCategoryTabs.value && vm.categories.isNotEmpty()
    )
    LibraryPager(
        pagerState = horizontalPager,
        onClick = onBook,
        onLongClick = onLongBook,
        goToLatestChapter = goToLatestChapter,
        categories = vm.categories,
        pageCount = vm.categories.size,
        layout = vm.layout,
        onPageChange = { page ->
            vm.getLibraryForCategoryIndex(categoryIndex = page)
        },
        selection = vm.selectedBooks,
        currentPage = vm.selectedCategoryIndex,
        showUnreadBadge = vm.unreadBadge.value,
        showReadBadge  = vm.readBadge.value,
        showGoToLastChapterBadge = vm.goToLastChapterBadge.value,
        getColumnsForOrientation = getColumnsForOrientation

    )
}

@Composable
private fun LibrarySelectionBar(
    visible: Boolean,
    onClickChangeCategory: () -> Unit,
    onClickDownload: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    onClickMarkAsUnread: () -> Unit,
    onClickDeleteDownload: () -> Unit,
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
                AppIconButton(imageVector = Icons.Outlined.Delete, onClick = onClickDeleteDownload)
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryPager(
    pagerState: PagerState,
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    goToLatestChapter: (book: BookItem) -> Unit = {},
    categories: List<CategoryWithCount>,
    pageCount: Int,
    layout: DisplayMode,
    selection: List<Long> = emptyList<Long>(),
    currentPage: Int,
    onPageChange: @Composable (page: Int) -> State<List<BookItem>>,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge:Boolean = false,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
) {
    HorizontalPager(
        count = pageCount,
        state = pagerState,
    ) { page ->
        val books by onPageChange(page)
        val gridState = rememberLazyGridState()
        val lazyListState = rememberLazyListState()
        val displayMode =  categories[page].category.displayMode
        val columns by if (displayMode != DisplayMode.List) {
            val window = LocalConfiguration.current
            val isLandscape = window.screenWidthDp > window.screenHeightDp

            with(rememberCoroutineScope()) {
                remember(isLandscape) { getColumnsForOrientation(isLandscape) }.collectAsState()
            }
        } else {
            remember { mutableStateOf(0) }
        }
        LazyColumnScrollbar(
            listState = lazyListState,
        ) {
            LayoutComposable(
                books = books,
                layout = layout,
                isLocal = true,
                gridState = gridState,
                scrollState = lazyListState,
                selection = selection,
                goToLatestChapter = goToLatestChapter,
                onClick = onClick,
                onLongClick = onLongClick,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                showReadBadge = showReadBadge,
                showUnreadBadge = showUnreadBadge,
                columns = columns
            )
        }

    }
}