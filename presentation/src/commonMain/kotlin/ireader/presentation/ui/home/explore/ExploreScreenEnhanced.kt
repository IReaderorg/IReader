package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.toBook
import ireader.domain.models.entities.toBookItem
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.ModernLayoutComposable
import ireader.presentation.ui.component.components.BookShimmerLoading
import ireader.presentation.ui.component.isLandscape
import ireader.presentation.ui.component.list.isScrolledToTheEnd
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.explore.viewmodel.ExploreScreenState
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhanced ExploreScreen following Mihon's patterns with improved error handling and responsive design.
 * 
 * Key improvements:
 * - Uses IReaderErrorScreen for comprehensive error handling
 * - Uses IReaderLoadingScreen for consistent loading states
 * - Uses IReaderFastScrollLazyColumn for optimized performance
 * - Responsive design with TwoPanelBox for tablets
 * - Enhanced error recovery with contextual actions
 * - Better user feedback and loading states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreenEnhanced(
    modifier: Modifier = Modifier,
    vm: ExploreViewModel,
    source: ireader.core.source.CatalogSource,
    onFilterClick: () -> Unit,
    getBooks: (query: String?, listing: Listing?, filters: List<Filter<*>>) -> Unit,
    loadItems: (Boolean) -> Unit,
    onBook: (BookItem) -> Unit,
    onAppbarWebView: (url: String) -> Unit,
    onPopBackStack: () -> Unit,
    snackBarHostState: SnackbarHostState,
    showmodalSheet: () -> Unit,
    onLongClick: (Book) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
    prevPaddingValues: PaddingValues
) {
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = vm.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
    )
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = vm.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
    )

    val scope = rememberCoroutineScope()

    // Save scroll position when it changes
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = scrollState.firstVisibleItemIndex
        vm.savedScrollOffset = scrollState.firstVisibleItemScrollOffset
    }
    
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = gridState.firstVisibleItemIndex
        vm.savedScrollOffset = gridState.firstVisibleItemScrollOffset
    }

    LaunchedEffect(key1 = true) {
        vm.modifiedFilter = source.getFilters()
    }

    // Enhanced error handling with snackbar
    val (showSnackBar, setShowSnackBar) = remember { mutableStateOf(false) }
    val (snackBarText, setSnackBarText) = remember { mutableStateOf("") }
    val retryLabel = localize(Res.string.retry)

    if (showSnackBar) {
        LaunchedEffect(snackBarHostState.currentSnackbarData) {
            val result = snackBarHostState.showSnackbar(
                message = snackBarText,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    setShowSnackBar(false)
                    vm.endReached = false
                    loadItems(false)
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(key1 = vm.error != null) {
        val errors = vm.error
        if (errors != null && errors.asString(localizeHelper).isNotBlank() && vm.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(errors.asString(localizeHelper))
        }
    }

    // Enhanced scroll-to-end detection
    LaunchedEffect(
        key1 = scrollState.layoutInfo.totalItemsCount > 0,
        key2 = scrollState.isScrolledToTheEnd(),
        key3 = !vm.endReached && !vm.isLoading
    ) {
        if (scrollState.layoutInfo.totalItemsCount > 0 && 
            scrollState.isScrolledToTheEnd() && 
            !vm.endReached && 
            !vm.isLoading) {
            loadItems(false)
        }
    }

    LaunchedEffect(
        key1 = gridState.layoutInfo.totalItemsCount > 0,
        key2 = gridState.isScrolledToTheEnd(),
        key3 = !vm.endReached && !vm.isLoading
    ) {
        if (gridState.layoutInfo.totalItemsCount > 0 && 
            gridState.isScrolledToTheEnd() && 
            !vm.endReached && 
            !vm.isLoading) {
            loadItems(false)
        }
    }

    val columns by if (vm.layout != DisplayMode.List) {
        val isLandscape = isLandscape()
        with(rememberCoroutineScope()) {
            remember(isLandscape, vm.layout) {
                getColumnsForOrientation(isLandscape)
            }.collectAsState(initial = 2)
        }
    } else {
        remember { mutableStateOf(0) }
    }

    // Check if we should use responsive layout
    val isExpandedWidth = false // TODO: Implement proper window size detection

    TwoPanelBoxStandalone(
        isExpandedWidth = isExpandedWidth,
        startContent = {
            if (isExpandedWidth) {
                ExploreFilterPanel(
                    source = source,
                    onFilterClick = onFilterClick,
                    showmodalSheet = showmodalSheet
                )
            }
        },
        endContent = {
            ExploreMainContent(
                modifier = modifier,
                vm = vm,
                source = source,
                onFilterClick = onFilterClick,
                getBooks = getBooks,
                loadItems = loadItems,
                onBook = onBook,
                onAppbarWebView = onAppbarWebView,
                onPopBackStack = onPopBackStack,
                snackBarHostState = snackBarHostState,
                showmodalSheet = showmodalSheet,
                onLongClick = onLongClick,
                headers = headers,
                scrollState = scrollState,
                gridState = gridState,
                columns = columns,
                prevPaddingValues = prevPaddingValues,
                showFilterFab = !isExpandedWidth
            )
        }
    )
}

@Composable
private fun ExploreMainContent(
    modifier: Modifier,
    vm: ExploreViewModel,
    source: ireader.core.source.CatalogSource,
    onFilterClick: () -> Unit,
    getBooks: (query: String?, listing: Listing?, filters: List<Filter<*>>) -> Unit,
    loadItems: (Boolean) -> Unit,
    onBook: (BookItem) -> Unit,
    onAppbarWebView: (url: String) -> Unit,
    onPopBackStack: () -> Unit,
    snackBarHostState: SnackbarHostState,
    showmodalSheet: () -> Unit,
    onLongClick: (Book) -> Unit,
    headers: ((url: String) -> okhttp3.Headers?)?,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    columns: Int,
    prevPaddingValues: PaddingValues,
    showFilterFab: Boolean,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (showFilterFab) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = localize(Res.string.filter),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    onClick = showmodalSheet,
                    icon = {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = localize(Res.string.filter),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                )
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(prevPaddingValues)
        ) {
            when {
                vm.isLoading && vm.page == 1 -> {
                    IReaderLoadingScreen(
                        message = localize(Res.string.loading)
                    )
                }
                
                vm.error != null && vm.page == 1 -> {
                    ExploreScreenErrorEnhanced(
                        error = vm.error!!.asString(LocalLocalizeHelper.current!!),
                        source = source,
                        onRefresh = { 
                            getBooks(null, null, emptyList())
                        },
                        onWebView = {
                            onAppbarWebView(it.baseUrl)
                        }
                    )
                }
                
                vm.booksState.books.isEmpty() && !vm.isLoading -> {
                    // Show error screen with retry option when no books loaded (could be due to error or truly empty)
                    ExploreScreenErrorEnhanced(
                        error = vm.error?.asString(LocalLocalizeHelper.current!!) ?: localize(Res.string.no_results_found),
                        source = source,
                        onRefresh = { 
                            getBooks(null, null, emptyList())
                        },
                        onWebView = {
                            onAppbarWebView(it.baseUrl)
                        }
                    )
                }
                
                else -> {
                    ExploreBooksList(
                        books = vm.booksState.books.mapIndexed { index, book ->  
                            book.toBookItem().copy(column = index.toLong())
                        },
                        layout = vm.layout,
                        scrollState = scrollState,
                        gridState = gridState,
                        onClick = onBook,
                        isLoading = vm.isLoading,
                        onLongClick = { onLongClick(it.toBook()) },
                        headers = headers,
                        columns = columns
                    )
                }
            }
        }
    }
}

/**
 * Enhanced error screen with better user feedback and recovery options
 */
@Composable
private fun ExploreScreenErrorEnhanced(
    error: String,
    source: ireader.core.source.CatalogSource,
    onRefresh: () -> Unit,
    onWebView: (HttpSource) -> Unit,
) {
    val actions = mutableListOf<ErrorScreenAction>().apply {
        add(
            ErrorScreenAction(
                title = localize(Res.string.retry),
                icon = Icons.Default.Refresh,
                onClick = onRefresh
            )
        )
        
        if (source is HttpSource) {
            add(
                ErrorScreenAction(
                    title = localize(Res.string.open_in_webView),
                    icon = Icons.Default.Public,
                    onClick = { onWebView(source) }
                )
            )
        }
    }

    IReaderErrorScreen(
        message = error,
        actions = actions
    )
}

/**
 * Optimized books list with IReaderFastScrollLazyColumn
 */
@Composable
private fun ExploreBooksList(
    books: List<BookItem>,
    layout: DisplayMode,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onClick: (BookItem) -> Unit,
    isLoading: Boolean,
    onLongClick: (BookItem) -> Unit,
    headers: ((url: String) -> okhttp3.Headers?)?,
    columns: Int,
) {
    ModernLayoutComposable(
        books = books,
        layout = layout,
        scrollState = scrollState,
        gridState = gridState,
        onClick = onClick,
        isLoading = isLoading,
        showInLibraryBadge = true,
        onLongClick = onLongClick,
        headers = headers,
        keys = { it.column },
        columns = columns
    )
}

/**
 * Filter panel for tablet layout
 */
@Composable
private fun ExploreFilterPanel(
    source: ireader.core.source.CatalogSource,
    onFilterClick: () -> Unit,
    showmodalSheet: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = localize(Res.string.filter),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // TODO: Implement filter UI components
            ActionButton(
                title = localize(Res.string.filter),
                icon = Icons.Default.Add,
                onClick = showmodalSheet,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Empty screen component for when no books are found
 */
@Composable
private fun IReaderEmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    actions: List<ErrorScreenAction>? = null,
) {
    IReaderErrorScreen(
        message = message,
        modifier = modifier,
        actions = actions
    )
}