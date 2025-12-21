package ireader.presentation.ui.home.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import ireader.domain.models.DisplayMode
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.Colour.contentColor
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: androidx.compose.foundation.pager.PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    androidx.compose.material3.TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = tab.title) },
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
fun ScrollableTabs(
    modifier: Modifier = Modifier,
    libraryTabs: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    visible: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        androidx.compose.material3.ScrollableTabRow(
            modifier = modifier,
            selectedTabIndex = pagerState.currentPage,
            containerColor = AppColors.current.bars.toComposeColor(),
            contentColor = AppColors.current.onBars.toComposeColor(),
            edgePadding = 0.dp,
        ) {
            libraryTabs.forEachIndexed { index, tab ->
                Tab(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(
    libraryTabs: List<TabItem>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    filters: List<LibraryFilter>,
    onLayoutSelected: (DisplayMode) -> Unit,
    vm: LibraryViewModel,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val layouts = remember {
        listOf(
            DisplayMode.CompactGrid,
            DisplayMode.ComfortableGrid,
            DisplayMode.List,
            DisplayMode.OnlyCover
        )
    }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        key = null,
        pageContent =  { page->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                contentPadding = scaffoldPadding
            ) {
                when (page) {
                    0 -> FiltersPage(filters, onClick = {
                        vm.toggleFilter(it)
                    })

                    1 -> SortPage(
                        vm.sorting.value,
                        onClick = vm::toggleSort,
                        localizeHelper
                    )

                    2 -> DispalyPage(
                        layouts = layouts,
                        onLayoutSelected = onLayoutSelected,
                        vm = vm
                    )
                }
            }
        }
    )
}

private fun LazyListScope.FiltersPage(
    filters: List<LibraryFilter>,
    onClick: (LibraryFilter.Type) -> Unit
) {
    items(filters, key = { it.first.name }) { (filter, state) ->
        val isActive = state == LibraryFilter.Value.Included
        ClickableRow(onClick = { onClick(filter) }) {
            TriStateCheckbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state.asToggleableState(),
                onClick = { onClick(filter) }
            )
            Text(
                text = filter.name,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = if (isActive) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
            )
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
    onClick: (LibrarySort.Type) -> Unit,
    localizeHelper: LocalizeHelper
) {

    items(LibrarySort.types, key = { it.name }) { type ->
        val isSelected = sorting.type == type
        ClickableRow(onClick = { onClick(type) }) {
            val iconModifier = Modifier.requiredWidth(56.dp)
            if (isSelected) {
                val icon = if (sorting.isAscending) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }
                Icon(
                    icon,
                    contentDescription = if (sorting.isAscending) "Ascending" else "Descending",
                    modifier = iconModifier,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(iconModifier)
            }
            Text(
                text = LibrarySort.Type.name(type).asString(localizeHelper),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = if (isSelected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
            )
        }
    }
}


private fun LazyListScope.DispalyPage(
        layouts: List<DisplayMode>,
        vm: LibraryViewModel,
        onLayoutSelected: (DisplayMode) -> Unit
) {
    item {
        TextSection(
            text = localize(Res.string.display_mode),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    items(layouts, key = { it.name }) { layout ->
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        // Read layout from layouts preference to ensure recomposition
        val currentLayout = vm.layouts.value.let { flags ->
            DisplayMode.getFlag(flags) ?: DisplayMode.CompactGrid
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ClickableRow(onClick = { onLayoutSelected(layout) }) {
                RadioButton(
                    selected = currentLayout == layout,
                    onClick = { onLayoutSelected(layout) },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )

                when (layout) {
                    DisplayMode.CompactGrid -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.compact_layout))
                    }
                    DisplayMode.ComfortableGrid -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.comfortable_layout))
                    }
                    DisplayMode.List -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.list_layout))
                    }
                    DisplayMode.OnlyCover -> {
                        MidSizeTextComposable(text = localizeHelper.localize(Res.string.cover_only_layout))
                    }
                }
            }
        }
    }
    item {
        TextSection(
            text = localize(Res.string.columns),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        Slider(modifier = Modifier.padding(horizontal = 20.dp), value = vm.columnInPortrait.lazyValue.toFloat().coerceIn(1f, 10f), onValueChange = {
            vm.columnInPortrait.lazyValue = it.toInt().coerceIn(1, 10)
        }, valueRange = 1f..10f)
    }
    item {
        TextSection(
            text = localize(Res.string.badge),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.readBadge.value = !vm.readBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.readBadge.value,
                onCheckedChange = {
                    vm.readBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.read_chapters))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.unreadBadge.value = !vm.unreadBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.unreadBadge.value,
                onCheckedChange = {
                    vm.unreadBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.unread_chapters))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.goToLastChapterBadge.value = !vm.goToLastChapterBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.goToLastChapterBadge.value,
                onCheckedChange = {
                    vm.goToLastChapterBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.go_to_last_chapter))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showDownloadedChaptersBadge.value = !vm.showDownloadedChaptersBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showDownloadedChaptersBadge.value,
                onCheckedChange = {
                    vm.showDownloadedChaptersBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.downloaded_chapters))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showUnreadChaptersBadge.value = !vm.showUnreadChaptersBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showUnreadChaptersBadge.value,
                onCheckedChange = {
                    vm.showUnreadChaptersBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.unread_chapters))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showLocalMangaBadge.value = !vm.showLocalMangaBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showLocalMangaBadge.value,
                onCheckedChange = {
                    vm.showLocalMangaBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.local_manga))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showLanguageBadge.value = !vm.showLanguageBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showLanguageBadge.value,
                onCheckedChange = {
                    vm.showLanguageBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.language))
        }
    }
    item {
        TextSection(
            text = localize(Res.string.tabs),
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showCategoryTabs.value = !vm.showCategoryTabs.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCategoryTabs.value,
                onCheckedChange = {
                    vm.showCategoryTabs.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.show_category_tabs))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showAllCategoryTab.value = !vm.showAllCategoryTab.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showAllCategoryTab.value,
                onCheckedChange = {
                    vm.showAllCategoryTab.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.show_all_category_tab))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.showCountInCategory.value = !vm.showCountInCategory.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCountInCategory.value,
                onCheckedChange = {
                    vm.showCountInCategory.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.show_count_in_category_tab))
        }
    }
    item {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        ClickableRow(onClick = { vm.toggleResumeReadingCard(!vm.showResumeReadingCard.value) }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showResumeReadingCard.value,
                onCheckedChange = {
                    vm.toggleResumeReadingCard(it)
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize(Res.string.show_resume_card))
        }
    }
}
