package org.ireader.app.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.LayoutType
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import org.ireader.components.components.component.pagerTabIndicatorOffset
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.ui.Colour.contentColor



@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    androidx.compose.material3.TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,

            )
        }
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            androidx.compose.material3.Tab(
                text = { MidSizeTextComposable(text = tab.title) },
                selected = pagerState.currentPage == index,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }
}
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun ScrollableTabs(modifier : Modifier = Modifier,libraryTabs: List<String>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    ScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = pagerState.currentPage,
        containerColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,

                )
        }
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            androidx.compose.material3.Tab(
                text = { MidSizeTextComposable(text = tab) },
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
    filters: List<LibraryFilter>,
    toggleFilter: (LibraryFilter) -> Unit,
    sortType: LibrarySort,
    isSortDesc: Boolean,
    onSortSelected: (LibrarySort) -> Unit,
    layoutType: LayoutType,
    onLayoutSelected: (DisplayMode) -> Unit,
    vm:LibraryViewModel
) {
    HorizontalPager(
        count = libraryTabs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {


        when (page) {
            0 -> FiltersPage(filters, onClick = {
                vm.toggleFilter(it)
            })
            1 -> SortPage(
                vm.sorting,
                onClick = vm::toggleSort
            )
            2 -> item {
                DisplayScreen(
                    layoutType,
                    onLayoutSelected
                )
            }
        }
    }
    }
}


private fun LazyListScope.FiltersPage(
    filters: List<LibraryFilter>,
    onClick: (LibraryFilter.Type) -> Unit
) {
    items(filters) { (filter, state) ->
        ClickableRow(onClick = { onClick(filter) }) {
            androidx.compose.material3.TriStateCheckbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state.asToggleableState(),
                onClick = { onClick(filter) }
            )
            Text(filter.name)
        }
    }
}

@Composable
private fun ClickableRow(onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        content = { content() }
    )
}
private fun LibraryFilter.Value.asToggleableState(): ToggleableState {
    return when (this) {
        LibraryFilter.Value.Included -> ToggleableState.On
        LibraryFilter.Value.Excluded -> ToggleableState.Indeterminate
        LibraryFilter.Value.Missing -> ToggleableState.Off
    }
}

private fun LazyListScope.SortPage(
    sorting: LibrarySort,
    onClick: (LibrarySort.Type) -> Unit
) {
    items(LibrarySort.types) { type ->
        ClickableRow(onClick = { onClick(type) }) {
            val iconModifier = Modifier.requiredWidth(56.dp)
            if (sorting.type == type) {
                val icon = if (sorting.isAscending) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }
                androidx.compose.material3.Icon(icon, null, iconModifier, MaterialTheme.colorScheme.primary)
            } else {
                Spacer(iconModifier)
            }
            Text(type.name)
        }
    }
}