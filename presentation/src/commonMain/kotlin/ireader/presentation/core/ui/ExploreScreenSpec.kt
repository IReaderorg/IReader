package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ireader.core.source.HttpSource
import ireader.domain.models.entities.toBookItem
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.query_must_not_be_empty
import ireader.i18n.resources.source_not_available
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.ensureAbsoluteUrlForWebView
import ireader.presentation.core.navigateTo
import ireader.presentation.imageloader.getHeadersMap
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.EmptyScreenComposable
import ireader.presentation.ui.component.hideKeyboard
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.explore.BrowseTopAppBar
import ireader.presentation.ui.home.explore.ExploreScreen
import ireader.presentation.ui.home.explore.FilterBottomSheet
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.launch

import org.koin.core.parameter.parametersOf

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
data class ExploreScreenSpec(
    val sourceId: Long,
    val query: String?
) {
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class
    )
    @Composable
    fun Content() {
        val vm: ExploreViewModel = getIViewModel(
            parameters = { parametersOf(ExploreViewModel.Param(sourceId, query)) }
        )
        
        // Collect state as Compose state for efficient recomposition
        val state by vm.state.collectAsState()
        
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val source = state.source
        val scope = rememberCoroutineScope()
        
        val headers = remember { mutableStateOf<Map<String, String>?>(null) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val snackBarHostState = SnackBarListener(vm)
        
        IModalSheets(
            sheetContent = { modifier ->
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                
                FilterBottomSheet(
                    modifier = modifier,
                    onApply = {
                        val mFilters = state.modifiedFilters.filterNot { it.isDefaultValue() }
                        vm.stateFilters = mFilters
                        vm.searchQuery = null
                        vm.loadItems(reset = true)
                        hideKeyboard(
                            softwareKeyboardController = keyboardController,
                            focusManager
                        )
                    },
                    filters = state.modifiedFilters,
                    onReset = {
                        vm.modifiedFilter = source?.getFilters() ?: emptyList()
                    },
                    onUpdate = { filters ->
                        vm.modifiedFilter = filters
                    },
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                    }
                )
            },
            bottomSheetState = sheetState
        ) {
            IScaffold(
                topBar = { scrollBehavior ->
                    val focusManager = LocalFocusManager.current
                    
                    BrowseTopAppBar(
                        scrollBehavior = scrollBehavior,
                        state = vm,
                        source = source,
                        searchQuery = state.searchQuery ?: "",
                        isSearchMode = state.isSearchModeEnabled,
                        onValueChange = { vm.searchQuery = it },
                        onSearch = {
                            val searchQuery = state.searchQuery
                            if (!searchQuery.isNullOrBlank()) {
                                vm.loadItems(true)
                            } else {
                                vm.stateListing = source?.getListings()?.first()
                                vm.loadItems()
                                scope.launch {
                                    vm.showSnackBar(UiText.MStringResource(Res.string.query_must_not_be_empty))
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
                                val absoluteUrl = ensureAbsoluteUrlForWebView(source.baseUrl, source)
                                navController.navigateTo(
                                    WebViewScreenSpec(
                                        url = absoluteUrl,
                                        sourceId = source.id,
                                        chapterId = null,
                                        bookId = null,
                                        enableChaptersFetch = true,
                                        enableBookFetch = true,
                                        enableChapterFetch = false
                                    )
                                )
                            }
                        },
                        onPop = { navController.popBackStack() },
                        onLayoutTypeSelect = { layout -> vm.saveLayoutType(layout) },
                        currentLayout = state.layout,
                        onOpenLocalFolder = {
                            val success = vm.openLocalFolderAction()
                            scope.launch {
                                if (success) {
                                    vm.showSnackBar(UiText.DynamicString("Opening local folder..."))
                                } else {
                                    vm.showSnackBar(UiText.DynamicString("Could not open folder: ${vm.getLocalFolderPath()}"))
                                }
                            }
                        }
                    )
                }
            ) { scaffoldPadding ->
                if (source != null) {
                    ExploreScreen(
                        vm = vm,
                        state = state,
                        onFilterClick = { vm.toggleFilterMode() },
                        prevPaddingValues = scaffoldPadding,
                        source = source,
                        getBooks = { searchQuery, listing, filters ->
                            vm.searchQuery = searchQuery
                            vm.stateListing = listing ?: vm.stateListing
                            vm.stateFilters = filters
                            vm.loadItems(reset = true)
                        },
                        loadItems = { reset -> vm.loadItems(reset) },
                        onBook = { book ->
                            scope.launch {
                                handleBookClick(vm, book, navController)
                            }
                        },
                        onAppbarWebView = { url ->
                            val absoluteUrl = ensureAbsoluteUrlForWebView(url, source)
                            navController.navigateTo(
                                WebViewScreenSpec(
                                    url = absoluteUrl,
                                    sourceId = source.id,
                                    enableBookFetch = true,
                                    enableChaptersFetch = true,
                                    bookId = null,
                                    chapterId = null,
                                    enableChapterFetch = false
                                )
                            )
                        },
                        onPopBackStack = { navController.popBackStack() },
                        snackBarHostState = snackBarHostState,
                        showmodalSheet = {
                            scope.launchIO { sheetState.partialExpand() }
                        },
                        headers = { url ->
                            if (headers.value == null) {
                                headers.value = (source as? HttpSource)
                                    ?.getCoverRequest(url)?.second
                                    ?.getHeadersMap()
                            }
                            headers.value
                        },
                        onLongClick = { book ->
                            scope.launchIO {
                                vm.addToFavorite(book.toBookItem()) { updatedBook ->
                                    vm.booksState.replaceBook(updatedBook)
                                }
                            }
                        },
                        getColumnsForOrientation = { isLandscape ->
                            vm.getColumnsForOrientation(isLandscape, this)
                        },
                        onListingSelected = { listing ->
                            vm.stateListing = listing
                        }
                    )
                } else {
                    EmptyScreenComposable(
                        UiText.MStringResource(Res.string.source_not_available),
                        onPopBackStack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
    
    /**
     * Handle book click with proper error handling and navigation.
     * Inserts book to database first, then navigates to detail screen.
     */
    private suspend fun handleBookClick(
        vm: ExploreViewModel,
        book: ireader.domain.models.entities.BookItem,
        navController: androidx.navigation.NavController
    ) {
        try {
            val state = vm.state.value
            val bookIndex = book.column.toInt()
            val selectedBook = state.books.getOrNull(bookIndex)
            
            if (selectedBook == null) {
                vm.showSnackBar(UiText.DynamicString("Book not found"))
                return
            }
            
            // Insert book and get ID
            val bookId = vm.insertUseCases.insertBook(selectedBook)
            
            if (bookId > 0L) {
                // Update local state with the new ID
                val updatedBook = selectedBook.copy(id = bookId)
                vm.booksState.replaceBook(updatedBook)
                
                // Navigate to book detail
                navController.navigate(NavigationRoutes.bookDetail(bookId))
            } else {
                vm.showSnackBar(UiText.DynamicString("Failed to save book"))
            }
        } catch (e: Exception) {
            ireader.core.log.Log.error("Error navigating to book", e)
            vm.showSnackBar(UiText.DynamicString("Error: ${e.message}"))
        }
    }
}
