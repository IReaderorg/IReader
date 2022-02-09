package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import org.ireader.core_ui.ui.Colour.contentColor
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable

typealias ComposableFun = @Composable () -> Unit


@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colors.primary,

                )
        }) {
        libraryTabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = { },
                text = {
                    MidSizeTextComposable(title = tab.title,
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


@ExperimentalPagerApi
@Composable
fun TabsContent(libraryTabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState,
        count = libraryTabs.size,
        modifier = Modifier.fillMaxSize()) { page ->
        libraryTabs[pagerState.currentPage].screen()
    }
}
