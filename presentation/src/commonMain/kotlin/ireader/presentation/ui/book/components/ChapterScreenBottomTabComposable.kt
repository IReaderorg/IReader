package ireader.presentation.ui.book.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.preferences.prefs.ChapterDisplayMode

import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.book.viewmodel.ChapterSort
import ireader.presentation.ui.book.viewmodel.ChaptersFilters
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@ExperimentalMaterialApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChapterScreenBottomTabComposable(
    modifier: Modifier = Modifier,
    pagerState: androidx.compose.foundation.pager.PagerState,
    filters: List<ChaptersFilters>,
    toggleFilter: (ChaptersFilters) -> Unit,
    sortType: ChapterSort,
    isSortDesc: Boolean,
    onSortSelected: (ChapterSort) -> Unit,
    layoutType: ChapterDisplayMode,
    onLayoutSelected: (ChapterDisplayMode) -> Unit,
    vm: BookDetailViewModel
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val tabs = remember {
        listOf(
            localizeHelper.localize { xml ->
                xml.filter
            },
            localizeHelper.localize { xml -> xml.sort },
            localizeHelper.localize { xml -> xml.display },
        )
    }
    Column(modifier = modifier) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(
            tabs = tabs,
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
