package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.SortType

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomTabComposable(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    scope: CoroutineScope,
    navController: NavController,
    filters: List<FilterType>,
    addFilters: (FilterType) -> Unit,
    removeFilter: (FilterType)-> Unit,
    sortType: SortType,
    isSortDesc: Boolean,
    onSortSelected:(SortType) -> Unit,
    layoutType: LayoutType,
    onLayoutSelected: (DisplayMode) -> Unit
) {
    val tabs = listOf(TabItem.Filter(
        filters, addFilters, removeFilter
    ),
        TabItem.Sort(
            sortType, isSortDesc, onSortSelected
        ),
        TabItem.Display(
            layoutType, onLayoutSelected
        ))

    /** There is Some issue here were sheet content is not need , not sure why**/
    Column(modifier = modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(
            libraryTabs = tabs,
            pagerState = pagerState,
            filters,
            addFilters,
            removeFilter,
            sortType,
            isSortDesc,
            onSortSelected,
            layoutType,
            onLayoutSelected
        )

    }


}



