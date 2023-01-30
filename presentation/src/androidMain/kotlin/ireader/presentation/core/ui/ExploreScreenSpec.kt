package ireader.presentation.core.ui

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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NamedNavArgument
import ireader.domain.models.entities.toBookItem
import ireader.core.source.HttpSource
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.imageloader.coil.image_loaders.convertToOkHttpRequest
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.EmptyScreenComposable
import ireader.presentation.ui.component.hideKeyboard
import ireader.presentation.ui.home.explore.BrowseTopAppBar
import ireader.presentation.ui.home.explore.ExploreScreen
import ireader.presentation.ui.home.explore.FilterBottomSheet
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.koin.androidx.compose.getViewModel

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
    override fun Content(controller: Controller) {
        val vm: ExploreViewModel =
            getViewModel(viewModelStoreOwner = controller.navBackStackEntry, parameters = {
                org.koin.core.parameter.parametersOf(
                    ExploreViewModel.createParam(controller)
                )
            })
        val source = vm.source
        val scope = rememberCoroutineScope()
        val headers = remember {
            mutableStateOf<Headers?>(null)
        }
        if (source != null) {
            ExploreScreen(
                modifier = Modifier.padding(top = controller.scaffoldPadding.calculateTopPadding()),
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

                    vm.viewModelScope.launch {
                        val newBook = vm.booksState.books.getOrNull(book.column.toInt())
                        vm.booksState.book = null
                        val bookId = vm.insertUseCases.insertBook(newBook)

                        if (bookId != 0L) {
                            vm.booksState.replaceBook(newBook?.copy(id = bookId))
                            controller.navController.navigate(
                                route = BookDetailScreenSpec.buildRoute(
                                    bookId = bookId
                                )
                            )
                        }

                    }

                },
                onAppbarWebView = {
                    controller.navController.navigate(
                        WebViewScreenSpec.buildRoute(
                            url = it,
                            sourceId = source.id,
                            enableBookFetch = true,
                            enableChaptersFetch = true
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
                    vm.viewModelScope.launchIO {
                        vm.addToFavorite(it.toBookItem()) { book ->
                            vm.booksState.replaceBook(book)
                        }
                    }
                }
            )
        } else {
            EmptyScreenComposable(
                R.string.source_not_available,
                onPopBackStack = {
                    controller.navController.popBackStack()
                }
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun BottomModalSheet(
        controller: Controller
    ) {
        val vm: ExploreViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ExploreViewModel.createParam(controller)
            )
        })
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
        controller: Controller
    ) {
        val vm: ExploreViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                ExploreViewModel.createParam(controller)
            )
        })
        val focusManager = LocalFocusManager.current
        val source = vm.source
        val scope = rememberCoroutineScope()
        BrowseTopAppBar(
            scrollBehavior = controller.scrollBehavior,
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
                            bookId = null,
                            enableChaptersFetch = true,
                            enableBookFetch = true
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
