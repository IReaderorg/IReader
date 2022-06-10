package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiText
import org.ireader.components.components.EmptyScreenComposable
import org.ireader.components.hideKeyboard
import org.ireader.core.R
import org.ireader.core_api.source.HttpSource
import org.ireader.domain.ui.NavigationArgs
import org.ireader.explore.BrowseTopAppBar
import org.ireader.explore.ExploreScreen
import org.ireader.explore.FilterBottomSheet
import org.ireader.explore.viewmodel.ExploreViewModel
import org.ireader.image_loader.coil.image_loaders.convertToOkHttpRequest

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
)
object ExploreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "explore_route/{sourceId}?query={query}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.query,
        NavigationArgs.sourceId,
        NavigationArgs.showModalSheet,
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
        controller: ScreenSpec.Controller
    ) {
        val vm: ExploreViewModel = hiltViewModel(controller.navBackStackEntry)
        val focusManager = LocalFocusManager.current
        val source = vm.source
        val scope = rememberCoroutineScope()
        val headers = remember {
            mutableStateOf<Headers?>(null)
        }
        if (source != null) {
            ExploreScreen(
                modifier = Modifier.padding(paddingValues = controller.scaffoldPadding),
                vm = vm,
                onFilterClick = {
                    vm.toggleFilterMode()
                },
                source = source,
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
                    controller.navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(
                            sourceId = book.sourceId,
                            bookId = book.id
                        )
                    )
                },
                onAppbarWebView = {
                    controller.navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = it,
                            sourceId = source.id,
                        )
                    )
                },
                onPopBackStack = {
                    controller.navController.popBackStack()
                },
                snackBarHostState = controller.snackBarHostState,
                modalState = controller.sheetState,
                scaffoldPadding = controller.scaffoldPadding,
                headers = {
                    if (headers.value == null) {
                        headers.value =
                            (source as? HttpSource)?.getCoverRequest(it)?.second?.build()
                                ?.convertToOkHttpRequest()?.headers
                        headers.value
                    } else headers.value
                },
                onLongClick = {
                    scope.launchIO {
                        vm.addToFavorite(it)
                    }
                }
            )
        } else {
            EmptyScreenComposable(R.string.source_not_available,
                onPopBackStack = {
                    controller.navController.popBackStack()
                })
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun BottomModalSheet(
        controller: ScreenSpec.Controller
    ) {
        val vm: ExploreViewModel = hiltViewModel(controller.navBackStackEntry)
        val source = vm.source
        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        FilterBottomSheet(
            onApply = {
                val mFilters = vm.modifiedFilter.filterNot { it.isDefaultValue() }
                vm.stateFilters = mFilters
                vm.searchQuery = null
                vm.loadItems(reset = true)
                hideKeyboard(softwareKeyboardController = keyboardController, focusManager)
            },
            filters = vm.modifiedFilter,
            onReset = {
                vm.modifiedFilter = source?.getFilters() ?: emptyList()
            },
            onUpdate = {
                vm.modifiedFilter = it
            }
        )
    }

    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: ExploreViewModel = hiltViewModel(controller.navBackStackEntry)
        val focusManager = LocalFocusManager.current
        val source = vm.source
        val scope = rememberCoroutineScope()
        BrowseTopAppBar(
            state = vm,
            source = source,
            onValueChange = {
                vm.searchQuery = it
            },
            onSearch = {
                val query = vm.searchQuery
                if (query != null && query.isNotBlank()) {
                    vm.searchQuery = query
                    vm.loadItems(true)
                } else {
                    vm.stateListing = source?.getListings()?.first()
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
            onWebView = {
                if (source is HttpSource) {
                    controller.navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = (source).baseUrl,
                            sourceId = source.id,
                            chapterId = null,
                            bookId = null
                        )
                    )
                }
            },
            onPop = { controller.navController.popBackStack() },
            onLayoutTypeSelect = { layout ->
                vm.saveLayoutType(layout)
            },
            currentLayout = vm.layout
        )
    }
}