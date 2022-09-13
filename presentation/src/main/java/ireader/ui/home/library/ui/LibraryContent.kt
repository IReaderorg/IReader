package ireader.ui.home.library.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ireader.ui.home.library.components.ScrollableTabs
import ireader.ui.home.library.components.visibleName
import ireader.ui.home.library.viewmodel.LibraryViewModel
import ireader.common.models.entities.BookItem

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun LibraryContent(
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
        libraryTabs = vm.categories
            .map { it.visibleName.plus(if (vm.showCountInCategory.value) " (${it.bookCount})" else "") },
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
        showReadBadge = vm.readBadge.value,
        showGoToLastChapterBadge = vm.goToLastChapterBadge.value,
        getColumnsForOrientation = getColumnsForOrientation,

    )
}
