package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import ir.kazemcodes.infinity.core.presentation.layouts.layouts
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.presentation.theme.Colour.contentColor
import ir.kazemcodes.infinity.core.presentation.theme.Colour.iconColor
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel
import kotlinx.coroutines.launch

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var title: String, var screen: ComposableFun) {
    data class Filter(val viewModel: LibraryViewModel) :
        TabItem("Filter", { FilterScreen(viewModel) })

    data class Sort(val viewModel: LibraryViewModel) : TabItem("Sort", { SortScreen(viewModel) })
    data class Display(val viewModel: LibraryViewModel) :
        TabItem("Display", { DisplayScreen(viewModel) })
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colors.primary
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = { },
                text = {
                    TopAppBarTitle(title = tab.title,
                        color = MaterialTheme.colors.onBackground)
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@Composable
fun FilterScreen(viewModel: LibraryViewModel) {
    Column(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top) {
        CheckBoxWithText("Unread",
            viewModel.state.value.unreadFilter.index == FilterType.Unread.index) {
            if (viewModel.state.value.unreadFilter == FilterType.Unread) {
                viewModel.enableUnreadFilter(FilterType.Disable)
            } else {
                viewModel.enableUnreadFilter(FilterType.Unread)
            }
        }
    }
}

@Composable
fun DisplayScreen(viewModel: LibraryViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        layouts.forEach { layout ->
            RadioButtonWithTitleComposable(
                text = layout.title,
                selected = viewModel.state.value.layout == layout.layout,
                onClick = {
                    viewModel.onEvent(LibraryEvents.UpdateLayoutType(layout))
                }
            )
        }
    }
}

sealed class SortType(val name: String, val index: Int) {
    object DateAdded : SortType("Date Added", 0)
    object Alphabetically : SortType("Alphabetically", 1)
    object LastRead : SortType("Last Read", 2)
    object TotalChapter : SortType("TotalChapter", 3)
}

sealed class FilterType(val name: String, val index: Int) {
    object Disable : FilterType("Disable", 0)
    object Unread : FilterType("Unread", 1)
}

@Composable
fun SortScreen(viewModel: LibraryViewModel) {
    val items = listOf<SortType>(
        SortType.DateAdded,
        SortType.Alphabetically,
        SortType.LastRead,
        SortType.TotalChapter,
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(12.dp)
    ) {
        Column(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top) {
            items.forEach { item ->

                IconWithText(item.name,
                    if (!viewModel.state.value.isSortAcs) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    viewModel.state.value.sortType == item,
                    onClick = {
                        viewModel.changeSortIndex(item)
                    })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@ExperimentalPagerApi
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState,
        count = tabs.size,
        modifier = Modifier.fillMaxSize()) { page ->
        tabs[pagerState.currentPage].screen()
    }
}

@Composable
fun CheckBoxWithText(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        MidTextComposable(title = title)
    }
}

@Composable
fun IconWithText(title: String, icon: ImageVector, isEnable: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon,
            contentDescription = "$title icon",
            tint = if (isEnable) MaterialTheme.colors.iconColor else Color.Transparent)
        Spacer(modifier = Modifier.width(8.dp))
        MidTextComposable(title = title)
    }
}
