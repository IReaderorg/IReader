package ireader.presentation.ui.book.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.chapter_number
import ireader.i18n.resources.source_title
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.book.viewmodel.ChapterSort
import ireader.presentation.ui.book.viewmodel.ChaptersFilters
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.Colour.contentColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(libraryTabs: List<String>, pagerState: androidx.compose.foundation.pager.PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    androidx.compose.material3.TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(
    tabs: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    filters: List<ChaptersFilters>,
    toggleFilter: (ChaptersFilters) -> Unit,
    sortType: ChapterSort,
    isSortDesc: Boolean,
    onSortSelected: (ChapterSort) -> Unit,
    layoutType: ChapterDisplayMode,
    onLayoutSelected: (ChapterDisplayMode) -> Unit,
    vm: BookDetailViewModel
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageContent =  { page ->
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {

                when (page) {
                    0 -> FiltersPage(filters = filters, onClick = {
                        vm.toggleFilter(it)
                    })

                    1 -> SortPage(
                        vm.sorting.value,
                        onClick = vm::toggleSort
                    )

                    2 -> DispalyPage(
                        layouts = listOf(
                            ChapterDisplayMode.SourceTitle,
                            ChapterDisplayMode.ChapterNumber,
                        ),
                        onLayoutSelected = onLayoutSelected,
                        selectedLayout = layoutType
                    )
                }
            }
        }
    )
}

private fun LazyListScope.FiltersPage(
    filters: List<ChaptersFilters>,
    onClick: (ChaptersFilters.Type) -> Unit
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
private fun ChaptersFilters.Value.asToggleableState(): ToggleableState {
    return when (this) {
        ChaptersFilters.Value.Included -> ToggleableState.On
        ChaptersFilters.Value.Excluded -> ToggleableState.Indeterminate
        ChaptersFilters.Value.Missing -> ToggleableState.Off
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

private fun LazyListScope.SortPage(
    sorting: ChapterSort,
    onClick: (ChapterSort.Type) -> Unit
) {

    items(ChapterSort.types.toList()) { type ->
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
            Text(ChapterSort.Type.name(type).asString(localizeHelper))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.DispalyPage(
    layouts: List<ChapterDisplayMode>,
    onLayoutSelected: (ChapterDisplayMode) -> Unit,
    selectedLayout: ChapterDisplayMode
) {
    items(layouts) { layout ->
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ClickableRow(onClick = { onLayoutSelected(layout) }) {
                RadioButton(
                    selected = selectedLayout == layout,
                    onClick = { onLayoutSelected(layout) },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )

                when (layout) {
                    ChapterDisplayMode.SourceTitle -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.source_title))
                    }
                    ChapterDisplayMode.ChapterNumber -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.chapter_number))
                    }
                    else -> {}
                }
            }
        }
    }
}
