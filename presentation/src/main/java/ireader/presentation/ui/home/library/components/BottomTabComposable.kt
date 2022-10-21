package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import ireader.common.models.library.LibraryFilter
import ireader.common.models.library.LibrarySort
import ireader.domain.models.DisplayMode
import ireader.presentation.R
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel

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
    layoutType: DisplayMode,
    onLayoutSelected: (DisplayMode) -> Unit,
    vm: LibraryViewModel,
    scaffoldPadding: PaddingValues
) {
    val tabs = listOf(
        TabItem(
            stringResource(id = R.string.filter)
        ) {
        },
        TabItem(
            stringResource(id = R.string.sort)
        ) {
        },
        TabItem(
            stringResource(id = R.string.display)
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
            onLayoutSelected=onLayoutSelected,
            vm = vm,
            scaffoldPadding = scaffoldPadding
        )
    }
}
data class TabItem(
    val title: String,
    val screen: (@Composable BoxScope.() -> Unit)? = null
)
