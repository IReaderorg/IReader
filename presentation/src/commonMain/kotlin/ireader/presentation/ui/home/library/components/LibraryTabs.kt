package ireader.presentation.ui.home.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.CategoryWithCount
import ireader.presentation.ui.core.theme.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryTabs(
    state: androidx.compose.foundation.pager.PagerState,
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
            contentColor = AppColors.current.onBars,
            containerColor = AppColors.current.bars,
            edgePadding = 0.dp,

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
