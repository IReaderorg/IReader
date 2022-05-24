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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import org.ireader.app.components.EditCategoriesDialog
import org.ireader.app.components.ScrollableTabs
import org.ireader.app.components.visibleName
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.LayoutType
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.common_models.entities.toBookCategory
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
    refreshUpdate: () -> Unit,
    onClickChangeCategory: () -> Unit,
    bottomSheetState: ModalBottomSheetState,
    pagerState: PagerState,
    scaffoldPadding: PaddingValues,
    onAddToCategoryConfirm: () -> Unit,
    requestHideBottomNav: (Boolean) -> Unit,
) {

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    val categoriesWithCount = remember {
        derivedStateOf { vm.categories.map { CategoryWithCount(it,vm.books.size) }  }
    }
    LaunchedEffect(vm.hasSelection) {
        requestHideBottomNav(vm.hasSelection)
    }

        val horizontalPager = rememberPagerState()

        Box(modifier = Modifier
            .padding(scaffoldPadding)
            .fillMaxSize(),) {
            Column {
                if (vm.categories.isNotEmpty()) {
                    ScrollableTabs(
                        modifier = Modifier.fillMaxWidth(),
                        libraryTabs = vm.categories.map { it.visibleName },
                        pagerState = horizontalPager
                    )
                }
                LibraryPager(
                    pagerState = horizontalPager,
                    lazyListState = lazyListState,
                    onClick = onBook,
                    onLongClick = onLongBook,
                    goToLatestChapter = goToLatestChapter,
                    gridState = gridState,
                    categories = categoriesWithCount.value,
                    pageCount = vm.categories.size,
                    layout = vm.layout,
                    onPageChange = { page ->
                                   vm.getLibraryForCategoryIndex(categoryIndex = page)
                    },
                    selection = vm.selection

                )

            }
            EditCategoriesDialog(
                vm = vm,
                onConfirm = onAddToCategoryConfirm,
                dismissDialog = {
                    vm.showDialog = false
                },
                onAddDeleteQueue = { category ->
                    vm.deleteQueues.addAll(category.toBookCategory(vm.selection))
                },
                onRemoteInInsertQueue = { category ->
                    vm.addQueues.removeIf { it.categoryId == category.id }
                },
                onAddToInsertQueue = { category ->
                    vm.addQueues.addAll(category.toBookCategory(vm.selection))
                },
                onRemoteInDeleteQueue = { category ->
                    vm.deleteQueues.removeIf { it.categoryId == category.id }
                }
            )
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
                }
            }
            LibrarySelectionBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = vm.hasSelection,
                onClickChangeCategory = onClickChangeCategory,
                onClickDeleteDownloads = onDelete,
                onClickDownload = onDownload,
                onClickMarkAsRead = onMarkAsRead,
                onClickMarkAsUnread = onMarkAsNotRead
            )

    }
}

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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryPager(
    pagerState: PagerState,
    lazyListState:LazyListState,
    gridState:LazyGridState,
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    goToLatestChapter: (book: BookItem) -> Unit = {},
    categories: List<CategoryWithCount>,
    pageCount: Int,
    layout: LayoutType,
    selection: List<Long> = emptyList<Long>(),
    onPageChange: @Composable (page:Int) -> State<List<BookItem>>
) {
    if (categories.isEmpty()) return


    HorizontalPager(
        count = pageCount,
        state = pagerState,
    ) { page ->
        val books by onPageChange(page)
        val gridStatea = rememberLazyGridState()
        val lazyListStatea = rememberLazyListState()
        LazyColumnScrollbar(
            listState = lazyListState,
        ) {
            LayoutComposable(
                books = books,
                layout = layout,
                isLocal = true,
                gridState = gridStatea,
                scrollState = lazyListStatea,
                selection = selection,
                goToLatestChapter = goToLatestChapter,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }

    }
}