package org.ireader.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.components.components.component.pagerTabIndicatorOffset
import org.ireader.core_ui.theme.AppColors

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun LibraryTabs(
    state: PagerState,
    visible: Boolean,
    categories: List<CategoryWithCount>,
    showCount: Boolean,
    onClickTab: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        androidx.compose.material3.ScrollableTabRow(
            selectedTabIndex = state.currentPage,
            containerColor = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            edgePadding = 0.dp,
            indicator = { androidx.compose.material3.TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(state, it)) }
        ) {
            categories.forEachIndexed { i, category ->
                Tab(
                    selected = state.currentPage == i,
                    onClick = { onClickTab(i) },
                    text = {
                        Text(
                            category.visibleName + if (!showCount) {
                                ""
                            } else {
                                " (${category.bookCount})"
                            }
                        )
                    }
                )
            }
        }
    }
}
