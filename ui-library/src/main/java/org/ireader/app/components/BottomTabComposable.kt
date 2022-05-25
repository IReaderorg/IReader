package org.ireader.app.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.LayoutType
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import org.ireader.ui_library.R

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomTabComposable(
    modifier: Modifier = Modifier,
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
    val tabs = listOf(
        TabItem(
            stringResource(id = R.string.filter)
        ) {

        },
        TabItem(
            stringResource(id =  R.string.sort)
        ) {
        },TabItem(
                stringResource(id =   R.string.display)
        ) {

        },
    )

    /** There is Some issue here were sheet content is not need , not sure why**/
    Column(modifier = modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(
            libraryTabs = tabs,
            pagerState = pagerState,
            filters = filters,
            toggleFilter = toggleFilter,
            sortType = sortType,
            isSortDesc,
            onSortSelected,
            layoutType,
            onLayoutSelected,
            vm = vm
        )
    }
}
data class TabItem(
    val title: String,
    val screen: (@Composable BoxScope.() -> Unit)?=null
)

