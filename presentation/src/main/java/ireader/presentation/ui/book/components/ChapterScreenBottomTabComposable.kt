package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.presentation.R
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.book.viewmodel.ChapterSort
import ireader.presentation.ui.book.viewmodel.ChaptersFilters

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun ChapterScreenBottomTabComposable(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    filters: List<ChaptersFilters>,
    toggleFilter: (ChaptersFilters) -> Unit,
    sortType: ChapterSort,
    isSortDesc: Boolean,
    onSortSelected: (ChapterSort) -> Unit,
    layoutType: ChapterDisplayMode,
    onLayoutSelected: (ChapterDisplayMode) -> Unit,
    vm: BookDetailViewModel
) {
    val context = LocalContext.current
    val tabs = remember {
        listOf(
            context.getString(R.string.filter),
            context.getString(R.string.sort),
            context.getString(R.string.display),
        )
    }
    Column(modifier = modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(
            tabs = tabs,
            pagerState = pagerState,
            filters = filters,
            toggleFilter = toggleFilter,
            sortType = sortType,
            isSortDesc,
            onSortSelected,
            layoutType,
            onLayoutSelected,
            vm = vm
        )
    }
}
