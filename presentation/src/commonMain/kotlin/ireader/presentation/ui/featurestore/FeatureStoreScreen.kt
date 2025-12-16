package ireader.presentation.ui.featurestore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginType
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.components.FeaturedPluginsSection
import ireader.presentation.ui.plugins.marketplace.components.FilterBottomSheet
import ireader.presentation.ui.plugins.marketplace.components.PluginCard

/**
 * Feature Store screen - main entry point for plugin monetization
 * Displays available plugins for purchase/download with categories and filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureStoreScreen(
    viewModel: FeatureStoreViewModel,
    onNavigateBack: () -> Unit,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error as snackbar when it occurs
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showSearch) {
                SearchTopBar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onCloseSearch = {
                        showSearch = false
                        viewModel.updateSearchQuery("")
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(localizeHelper.localize(Res.string.feature_store))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = localizeHelper.localize(Res.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = localizeHelper.localize(Res.string.search)
                            )
                        }
                        IconButton(onClick = { showFilters = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = localizeHelper.localize(Res.string.filter)
                            )
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading && state.plugins.isEmpty() -> {
                    LoadingState()
                }
                state.error != null && state.plugins.isEmpty() -> {
                    ErrorState(
                        error = state.error ?: "Unknown error",
                        onRetry = viewModel::loadPlugins
                    )
                }
                else -> {
                    FeatureStoreContent(
                        state = state,
                        onCategorySelected = viewModel::selectCategory,
                        onPluginClick = onPluginClick,
                        onInstall = viewModel::installPlugin,
                        onRefresh = viewModel::refreshPlugins
                    )
                }
            }
        }
    }
    
    if (showFilters) {
        FilterBottomSheet(
            currentSortOrder = state.sortOrder,
            currentPriceFilter = state.priceFilter,
            currentMinRating = state.minRating,
            onSortOrderChange = viewModel::updateSortOrder,
            onPriceFilterChange = viewModel::updatePriceFilter,
            onMinRatingChange = viewModel::updateMinRating,
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun FeatureStoreContent(
    state: FeatureStoreState,
    onCategorySelected: (PluginType?) -> Unit,
    onPluginClick: (String) -> Unit,
    onInstall: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (state.isRefreshing) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Category chips
            item {
                FeatureCategoryTabs(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Featured section when no filters
            if (state.searchQuery.isBlank() && 
                state.selectedCategory == null && 
                state.priceFilter == PriceFilter.ALL &&
                state.minRating == 0f) {
                item {
                    FeaturedPluginsSection(
                        plugins = state.featuredPlugins,
                        onPluginClick = onPluginClick,
                        onInstall = onInstall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.all_plugins),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            if (state.filteredPlugins.isEmpty()) {
                item {
                    EmptyState(
                        message = if (state.searchQuery.isNotBlank()) {
                            "No features found matching \"${state.searchQuery}\""
                        } else {
                            localizeHelper.localize(Res.string.no_plugins_available)
                        }
                    )
                }
            } else {
                items(state.filteredPlugins) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        onClick = { onPluginClick(plugin.id) },
                        onInstall = onInstall
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(localizeHelper.localize(Res.string.search_features)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localizeHelper.localize(Res.string.close_search)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localizeHelper.localize(Res.string.loading_features),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.download_notifier_title_error),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(localizeHelper.localize(Res.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
