package ireader.presentation.ui.home.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Show icon for smart categories
                            val icon = getSmartCategoryIcon(category.id)
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            
                            Text(
                                category.visibleName + if (!showCount) {
                                    ""
                                } else {
                                    " (${category.bookCount})"
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
