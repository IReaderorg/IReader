package ireader.presentation.ui.plugins.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.all_plugins
import ireader.i18n.resources.back
import ireader.i18n.resources.close_search
import ireader.i18n.resources.download_notifier_title_error
import ireader.i18n.resources.filter
import ireader.i18n.resources.loading_plugins
import ireader.i18n.resources.plugin_marketplace
import ireader.i18n.resources.retry
import ireader.i18n.resources.search
import ireader.i18n.resources.search_plugins
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.plugins.marketplace.components.FeaturedPluginsSection
import ireader.presentation.ui.plugins.marketplace.components.FilterBottomSheet
import ireader.presentation.ui.plugins.marketplace.components.PluginCard
import ireader.presentation.ui.plugins.marketplace.components.PluginCategoryTabs

/**
 * Plugin Marketplace screen for discovering and installing plugins
 * Requirements: 2.1, 2.2, 2.3, 16.1, 16.2, 16.3, 16.4, 16.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginMarketplaceScreen(
    viewModel: PluginMarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    
    Scaffold(
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
                    title = { Text(localizeHelper.localize(Res.string.plugin_marketplace)) },
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
                    PluginMarketplaceContent(
                        state = state,
                        onCategorySelected = viewModel::selectCategory,
                        onPluginClick = onPluginClick,
                        onRefresh = viewModel::refreshPlugins
                    )
                }
            }
        }
    }
    
    // Filter bottom sheet
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

/**
 * Main content with plugins list
 */
@Composable
private fun PluginMarketplaceContent(
    state: PluginMarketplaceState,
    onCategorySelected: (ireader.domain.plugins.PluginType?) -> Unit,
    onPluginClick: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pull-to-refresh indicator at the top
            if (state.isRefreshing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            // Category tabs
            item {
                PluginCategoryTabs(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Featured plugins section (only show when no filters applied)
            if (state.searchQuery.isBlank() && 
                state.selectedCategory == null && 
                state.priceFilter == PriceFilter.ALL &&
                state.minRating == 0f) {
                item {
                    FeaturedPluginsSection(
                        plugins = state.featuredPlugins,
                        onPluginClick = onPluginClick,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                // Section header for all plugins
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.all_plugins),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Plugins list
            if (state.filteredPlugins.isEmpty()) {
                item {
                    EmptyState(
                        message = if (state.searchQuery.isNotBlank()) {
                            "No plugins found matching \"${state.searchQuery}\""
                        } else {
                            "No plugins available"
                        }
                    )
                }
            } else {
                items(state.filteredPlugins) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        onClick = { onPluginClick(plugin.id) }
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Search top bar
 */
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
                placeholder = { Text(localizeHelper.localize(Res.string.search_plugins)) },
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

/**
 * Loading state with skeleton placeholders
 */
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
                text = localizeHelper.localize(Res.string.loading_plugins),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state with retry button
 */
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

/**
 * Empty state when no plugins match filters
 */
@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
