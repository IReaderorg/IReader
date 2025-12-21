package ireader.presentation.ui.home.explore

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    headers: ((url: String) -> Map<String, String>?)? = null,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
    prevPaddingValues: PaddingValues,
    onListingSelected: ((Listing) -> Unit)? = null,
    onNavigateToBadgeStore: () -> Unit = {},
    onNavigateToDonation: () -> Unit = {}
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
        // Get listings from source
        val listings = remember(source) { source.getListings() }
        
        // Get sort filter options from source filters
        val sortFilter = remember(source) {
            source.getFilters().filterIsInstance<Filter.Sort>().firstOrNull()
        }
        
        // Build unified filter options - merge listings with sort filter options
        val filterOptions = remember(listings, sortFilter) {
            buildList {
                // Add listings first
                listings.forEach { listing ->
                    add(FilterOption.FromListing(listing))
                }
                // Add sort filter options (avoid duplicates by name)
                val listingNames = listings.map { it.name.lowercase() }.toSet()
                sortFilter?.options?.forEachIndexed { index, option ->
                    if (option.lowercase() !in listingNames) {
                        add(FilterOption.FromSort(index, option, sortFilter))
                    }
                }
            }
        }
        
        // Track selected filter option
        var selectedOption by remember(state.currentListing) {
            mutableStateOf<FilterOption?>(
                state.currentListing?.let { FilterOption.FromListing(it) }
                    ?: filterOptions.firstOrNull()
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(prevPaddingValues)
        ) {
            // Show unified filter chips row if there are multiple options and not in search mode
            if (filterOptions.size > 1 && !state.isSearchModeEnabled) {
                UnifiedFilterChips(
                    options = filterOptions,
                    selectedOption = selectedOption,
                    onOptionSelected = { option ->
                        selectedOption = option
                        when (option) {
                            is FilterOption.FromListing -> {
                                onListingSelected?.invoke(option.listing)
                                getBooks(null, option.listing, emptyList())
                            }
                            is FilterOption.FromSort -> {
                                val updatedSortFilter = Filter.Sort(
                                    option.sortFilter.name,
                                    option.sortFilter.options,
                                    Filter.Sort.Selection(option.index, false)
                                )
                                getBooks(null, null, listOf(updatedSortFilter))
                            }
                        }
                    }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // Initial loading state
                    state.isInitialLoading -> {
                        BookShimmerLoading(columns = columns)
                    }
                    // Broken source state (parsing error, not network error)
                    state.isBrokenSourceError -> {
                        BrokenSourceScreen(
                            sourceName = source.name,
                            source = source,
                            onRetry = { getBooks(null, null, emptyList()) },
                            onWebView = { src -> 
                                (src as? HttpSource)?.let { onAppbarWebView(it.baseUrl) }
                            },
                            onNavigateToBadgeStore = onNavigateToBadgeStore,
                            onNavigateToDonation = onNavigateToDonation
                        )
                    }
                    // Error with no content (network error)
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
                    // Empty state in search mode (no results for search query - this is normal)
                    !state.hasContent && !state.isLoading && state.isSearchModeEnabled -> {
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
}

/**
 * Sealed class representing unified filter options from both Listing and Filter.Sort
 */
sealed class FilterOption {
    abstract val displayName: String
    
    data class FromListing(val listing: Listing) : FilterOption() {
        override val displayName: String = listing.name
    }
    
    data class FromSort(
        val index: Int,
        val optionName: String,
        val sortFilter: Filter.Sort
    ) : FilterOption() {
        override val displayName: String = optionName
    }
}

/**
 * Unified horizontal scrollable row of filter chips that combines Listing and Filter.Sort options
 */
@Composable
fun UnifiedFilterChips(
    options: List<FilterOption>,
    selectedOption: FilterOption?,
    onOptionSelected: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = when {
                selectedOption == null -> false
                option is FilterOption.FromListing && selectedOption is FilterOption.FromListing ->
                    option.listing.name == selectedOption.listing.name
                option is FilterOption.FromSort && selectedOption is FilterOption.FromSort ->
                    option.index == selectedOption.index && option.optionName == selectedOption.optionName
                else -> false
            }
            
            FilterChipItem(
                text = option.displayName,
                isSelected = isSelected,
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

/**
 * Reusable filter chip item with animated colors
 */
@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = localizeHelper.localize(Res.string.chipcontainercolor)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = localizeHelper.localize(Res.string.chipcontentcolor)
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
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
