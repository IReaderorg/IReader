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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.core.source.HttpSource
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
 * Stable key generator for explore book items.
 * Uses the column (index) to preserve API order while ensuring stability.
 * The column is set when converting books to BookItems and represents the original order.
 */
@Stable
private fun stableExploreBookKey(book: BookItem): Any = book.column

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    vm: ExploreViewModel,
    state: ExploreScreenState,
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Initialize scroll states with saved positions
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = state.savedScrollOffset
    )
    
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = state.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = state.savedScrollOffset
    )
    
    val scope = rememberCoroutineScope()
    
    // Pre-compute stable key function for book items
    val stableKeyFunction = remember { { book: BookItem -> stableExploreBookKey(book) } }
    
    // Memoize click handlers to prevent unnecessary recompositions
    val stableOnBook = remember(onBook) { onBook }
    val stableOnLongClick = remember(onLongClick) { onLongClick }
    
    // Memoize books with stable keys - only recompute when books list changes
    val booksWithKeys = remember(state.books) {
        state.books.mapIndexed { index, book ->
            book.toBookItem().copy(column = index.toLong())
        }
    }
    
    // Save scroll position when it changes
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = scrollState.firstVisibleItemIndex
        vm.savedScrollOffset = scrollState.firstVisibleItemScrollOffset
    }
    
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = gridState.firstVisibleItemIndex
        vm.savedScrollOffset = gridState.firstVisibleItemScrollOffset
    }
    
    // Initialize filters from source
    LaunchedEffect(source) {
        vm.modifiedFilter = source.getFilters()
    }
    
    // Snackbar state for errors
    val (showSnackBar, setShowSnackBar) = remember { mutableStateOf(false) }
    val (snackBarText, setSnackBarText) = remember { mutableStateOf("") }
    
    // Show snackbar for errors on subsequent pages
    if (showSnackBar) {
        LaunchedEffect(snackBarHostState.currentSnackbarData) {
            val result = snackBarHostState.showSnackbar(
                message = snackBarText,
                actionLabel = "Reload",
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
    
    // Handle errors for pages > 1
    LaunchedEffect(state.error, state.page) {
        val error = state.error
        if (error != null && error.asString(localizeHelper).isNotBlank() && state.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(error.asString(localizeHelper))
        }
    }
    
    // Load more items when scrolled to end (list view)
    LaunchedEffect(
        scrollState.layoutInfo.totalItemsCount,
        scrollState.isScrolledToTheEnd(),
        state.endReached,
        state.isLoading
    ) {
        if (scrollState.layoutInfo.totalItemsCount > 0 &&
            scrollState.isScrolledToTheEnd() &&
            !state.endReached &&
            !state.isLoading
        ) {
            loadItems(false)
        }
    }
    
    // Load more items when scrolled to end (grid view)
    LaunchedEffect(
        gridState.layoutInfo.totalItemsCount,
        gridState.isScrolledToTheEnd(),
        state.endReached,
        state.isLoading
    ) {
        if (gridState.layoutInfo.totalItemsCount > 0 &&
            gridState.isScrolledToTheEnd() &&
            !state.endReached &&
            !state.isLoading
        ) {
            loadItems(false)
        }
    }
    
    // Calculate columns based on layout and orientation
    val columns by if (state.layout != DisplayMode.List) {
        val isLandscape = isLandscape()
        with(rememberCoroutineScope()) {
            remember(isLandscape, state.layout) {
                getColumnsForOrientation(isLandscape)
            }.collectAsState(initial = 2)
        }
    } else {
        remember { mutableStateOf(0) }
    }
    
    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(prevPaddingValues)
        ) {
            when {
                // Initial loading state
                state.isInitialLoading -> {
                    BookShimmerLoading(columns = columns)
                }
                // Error with no content
                state.isErrorWithNoContent -> {
                    ExploreScreenError(
                        error = state.error?.asString(localizeHelper) ?: localize(Res.string.no_results_found),
                        source = source,
                        onRefresh = { getBooks(null, null, emptyList()) },
                        onWebView = { src -> 
                            (src as? HttpSource)?.let { onAppbarWebView(it.baseUrl) }
                        }
                    )
                }
                // Empty state (no books, not loading, no error)
                !state.hasContent && !state.isLoading -> {
                    ExploreScreenError(
                        error = localize(Res.string.no_results_found),
                        source = source,
                        onRefresh = { getBooks(null, null, emptyList()) },
                        onWebView = { src -> 
                            (src as? HttpSource)?.let { onAppbarWebView(it.baseUrl) }
                        }
                    )
                }
                // Content state
                else -> {
                    ModernLayoutComposable(
                        books = booksWithKeys,
                        layout = state.layout,
                        scrollState = scrollState,
                        gridState = gridState,
                        onClick = stableOnBook,
                        isLoading = state.isLoading,
                        showInLibraryBadge = true,
                        onLongClick = { book -> stableOnLongClick(book.toBook()) },
                        headers = headers,
                        keys = stableKeyFunction,
                        columns = columns
                    )
                }
            }
        }
    }
}


/**
 * Error screen for the Explore screen with retry and webview options
 */
@Composable
fun ExploreScreenError(
    error: String,
    source: ireader.core.source.CatalogSource,
    onRefresh: () -> Unit,
    onWebView: (ireader.core.source.CatalogSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Random kaomoji for visual interest
        val kaomoji = remember { ireader.presentation.ui.core.ui.kaomojis.random() }
        
        Text(
            text = kaomoji,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Refresh button
            OutlinedButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = localize(Res.string.retry))
            }
            
            // WebView button (only for HTTP sources)
            if (source is HttpSource) {
                OutlinedButton(onClick = { onWebView(source) }) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = localize(Res.string.open_in_webView))
                }
            }
        }
    }
}
