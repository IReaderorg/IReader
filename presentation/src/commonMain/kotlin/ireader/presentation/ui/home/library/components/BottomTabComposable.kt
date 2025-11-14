package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import ireader.domain.models.DisplayMode
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun BottomTabComposable(
    modifier: Modifier = Modifier,
    pagerState: androidx.compose.foundation.pager.PagerState,
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val tabs = listOf(
        TabItem(
            localizeHelper.localize(Res.string.filter)
        ) {
        },
        TabItem(
            localizeHelper.localize(Res.string.sort)
        ) {
        },
        TabItem(
            localizeHelper.localize(Res.string.display)
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
