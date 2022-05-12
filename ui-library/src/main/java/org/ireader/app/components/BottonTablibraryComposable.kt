package org.ireader.app.components


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.FilterType
import org.ireader.common_models.LayoutType
import org.ireader.common_models.SortType
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.ui.Colour.contentColor

typealias ComposableFun = @Composable () -> Unit

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,

            )
        }
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = UiText.StringResource( tab.title)) },
                selected = pagerState.currentPage == index,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabsContent(
    libraryTabs: List<TabItem>,
    pagerState: PagerState,
    filters: List<FilterType>,
    addFilters: (FilterType) -> Unit,
    removeFilter: (FilterType) -> Unit,
    sortType: SortType,
    isSortDesc: Boolean,
    onSortSelected: (SortType) -> Unit,
    layoutType: LayoutType,
    onLayoutSelected: (DisplayMode) -> Unit
) {
    HorizontalPager(
        count = libraryTabs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> FilterScreen(
                filters,
                addFilters,
                removeFilter
            )
            1 -> SortScreen(
                sortType,
                isSortDesc,
                onSortSelected
            )
            2 -> DisplayScreen(
                layoutType,
                onLayoutSelected
            )
        }
    }
}
