package ireader.ui.home.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import ireader.ui.home.library.viewmodel.LibraryViewModel
import ireader.domain.models.DisplayMode
import ireader.common.models.library.LibraryFilter
import ireader.common.models.library.LibrarySort
import ireader.common.resources.asString
import ireader.ui.component.components.component.pagerTabIndicatorOffset
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.ui.component.text_related.TextSection
import ireader.core.ui.theme.AppColors
import ireader.core.ui.ui.Colour.contentColor
import ireader.presentation.R

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
fun ScrollableTabs(
    modifier: Modifier = Modifier,
    libraryTabs: List<String>,
    pagerState: PagerState,
    visible: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        ScrollableTabRow(
            modifier = modifier,
            selectedTabIndex = pagerState.currentPage,
            containerColor = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .fillMaxWidth()
                        .pagerTabIndicatorOffset(pagerState, tabPositions),
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
    layoutType: DisplayMode,
    onLayoutSelected: (DisplayMode) -> Unit,
    vm: LibraryViewModel,
    scaffoldPadding: PaddingValues
) {
    val layouts = remember {
        listOf(
            DisplayMode.CompactGrid,
            DisplayMode.ComfortableGrid,
            DisplayMode.List,
            DisplayMode.OnlyCover
        )
    }
    HorizontalPager(
        count = libraryTabs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, contentPadding = scaffoldPadding) {
            when (page) {
                0 -> FiltersPage(filters, onClick = {
                    vm.toggleFilter(it)
                })
                1 -> SortPage(
                    vm.sorting.value,
                    onClick = vm::toggleSort
                )
                2 -> DispalyPage(
                    layouts = layouts,
                    onLayoutSelected = onLayoutSelected,
                    vm = vm
                )
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
            TriStateCheckbox(
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
        val context = LocalContext.current
        ClickableRow(onClick = { onClick(type) }) {
            val iconModifier = Modifier.requiredWidth(56.dp)
            if (sorting.type == type) {
                val icon = if (sorting.isAscending) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }
                Icon(
                    icon,
                    null,
                    iconModifier,
                    MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(iconModifier)
            }
            Text(LibrarySort.Type.name(type).asString(context = context))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.DispalyPage(
    layouts: List<DisplayMode>,
    vm: LibraryViewModel,
    onLayoutSelected: (DisplayMode) -> Unit
) {
    item {
        TextSection(
            text = stringResource(R.string.display_mode),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    items(layouts) { layout ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ClickableRow(onClick = { onLayoutSelected(layout) }) {
                RadioButton(
                    selected = vm.layout == layout,
                    onClick = { onLayoutSelected(layout) },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )

                when (layout) {
                    DisplayMode.CompactGrid -> {
                        MidSizeTextComposable(text = stringResource(id = R.string.compact_layout))
                    }
                    DisplayMode.ComfortableGrid -> {
                        MidSizeTextComposable(text = stringResource(id = R.string.comfortable_layout))
                    }
                    DisplayMode.List -> {
                        MidSizeTextComposable(text = stringResource(id = R.string.list_layout))
                    }
                    DisplayMode.OnlyCover -> {
                        MidSizeTextComposable(text = stringResource(id = R.string.cover_only_layout))
                    }
                }
            }
        }
    }
    item {
        TextSection(
            text = stringResource(R.string.columns),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        Slider(modifier = Modifier.padding(horizontal = 20.dp), value = vm.columnInPortrait.lazyValue.toFloat(), onValueChange = {
            vm.columnInPortrait.lazyValue = it.toInt()
        }, valueRange = 0f..10f)
    }
    item {
        TextSection(
            text = stringResource(R.string.badge),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        ClickableRow(onClick = { vm.readBadge.value = !vm.readBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.readBadge.value,
                onCheckedChange = {
                    vm.readBadge.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.read_chapters))
        }
    }
    item {
        ClickableRow(onClick = { vm.unreadBadge.value = !vm.unreadBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.unreadBadge.value,
                onCheckedChange = {
                    vm.unreadBadge.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.unread_chapters))
        }
    }
    item {
        ClickableRow(onClick = { vm.goToLastChapterBadge.value = !vm.goToLastChapterBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.goToLastChapterBadge.value,
                onCheckedChange = {
                    vm.goToLastChapterBadge.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.go_to_last_chapter))
        }
    }
    item {
        TextSection(
            text = stringResource(R.string.tabs),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        ClickableRow(onClick = { vm.showCategoryTabs.value = !vm.showCategoryTabs.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCategoryTabs.value,
                onCheckedChange = {
                    vm.showCategoryTabs.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.show_category_tabs))
        }
    }
    item {
        ClickableRow(onClick = { vm.showAllCategoryTab.value = !vm.showAllCategoryTab.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showAllCategoryTab.value,
                onCheckedChange = {
                    vm.showAllCategoryTab.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.show_all_category_tab))
        }
    }
    item {
        ClickableRow(onClick = { vm.showCountInCategory.value = !vm.showCountInCategory.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCountInCategory.value,
                onCheckedChange = {
                    vm.showCountInCategory.value = it
                }
            )
            MidSizeTextComposable(text = stringResource(id = R.string.show_count_in_category_tab))
        }
    }
}
