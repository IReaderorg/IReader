package org.ireader.chapterDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ireader.chapterDetails.viewmodel.ChapterDetailViewModel
import org.ireader.ui_chapter_detail.R


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
    vm:ChapterDetailViewModel
) {
    val context = LocalContext.current
    val tabs = remember {
        listOf(
            context.getString( R.string.filter),
            context.getString( R.string.sort),
            context.getString( R.string.display),
        )
    }

    /** There is Some issue here were sheet content is not need , not sure why**/
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