package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.ireader.common_resources.UiText
import org.ireader.components.components.EmptyScreenComposable
import org.ireader.core.R
import org.ireader.core_api.source.HttpSource
import org.ireader.domain.ui.NavigationArgs
import org.ireader.explore.ExploreScreen
import org.ireader.explore.viewmodel.ExploreViewModel

object ExploreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "explore_route/{sourceId}?query={query}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.query,
        NavigationArgs.sourceId
    )

    fun buildRoute(sourceId: Long, query: String? = null): String {
        return if (query != null) {
            "explore_route/$sourceId?query=$query"
        } else {
            "explore_route/$sourceId"
        }
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: ExploreViewModel = hiltViewModel(navBackStackEntry)
        val focusManager = LocalFocusManager.current
        val source = vm.source
        val scope = rememberCoroutineScope()
        if (source != null) {
            ExploreScreen(
                modifier = Modifier.padding(paddingValues = scaffoldPadding),
                vm = vm,
                onFilterClick = {
                    vm.toggleFilterMode()
                },
                source = source,
                onPop = { navController.popBackStack() },
                currentLayout = vm.layout,
                onLayoutTypeSelect = { layout ->
                    vm.saveLayoutType(layout)
                },
                onSearch = {
                    val query = vm.searchQuery
                    if (query != null && query.isNotBlank()) {
                        vm.searchQuery = query
                        vm.loadItems(true)
                    } else {
                        vm.stateListing = source.getListings().first()
                        vm.loadItems()
                        scope.launch {
                            vm.showSnackBar(UiText.StringResource(R.string.query_must_not_be_empty))
                        }
                    }
                    focusManager.clearFocus()
                },
                onSearchDisable = {
                    vm.toggleSearchMode(false)
                    vm.searchQuery = null
                    vm.loadItems(true)
                },
                onSearchEnable = {
                    vm.toggleSearchMode(true)
                },
                onValueChange = {
                    vm.searchQuery = it
                },
                onWebView = {
                    if (source is HttpSource) {
                        navController.navigate(
                            WebViewScreenSpec.buildRoute(
                                url = (source).baseUrl,
                                sourceId = source.id,
                                chapterId = null,
                                bookId = null
                            )
                        )
                    }
                },
                getBooks = { query, listing, filters ->
                    vm.searchQuery = query
                    vm.stateListing = listing
                    vm.stateFilters = filters
                    vm.loadItems()
                },
                loadItems = { reset ->
                    vm.loadItems(reset)
                },
                onBook = { book ->
                    navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(
                            sourceId = book.sourceId,
                            bookId = book.id
                        )
                    )
                },
                onAppbarWebView = {
                    navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = it,
                            sourceId = source.id,
                        )
                    )
                },
                onPopBackStack = {
                    navController.popBackStack()
                }
            )
        } else {
            EmptyScreenComposable(R.string.source_not_available,
                onPopBackStack = {
                    navController.popBackStack()
                })
        }
    }
}