package ireader.presentation.ui.featurestore

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.plugin.api.PluginMonetization
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.components.FilterBottomSheet

/**
 * Modern Feature Store screen with hero section and improved UX
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
    val pullRefreshState = rememberPullToRefreshState()
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedContent(targetState = showSearch, label = "search_bar") { isSearching ->
                if (isSearching) {
                    ModernSearchBar(
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onCloseSearch = {
                            showSearch = false
                            viewModel.updateSearchQuery("")
                        }
                    )
                } else {
                    ModernTopBar(
                        onNavigateBack = onNavigateBack,
                        onSearchClick = { showSearch = true },
                        onFilterClick = { showFilters = true }
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshPlugins,
            state = pullRefreshState,
            modifier = Modifier.padding(padding)
        ) {
            when {
                state.isLoading && state.plugins.isEmpty() -> ModernLoadingState()
                state.error != null && state.plugins.isEmpty() -> {
                    ModernErrorState(error = state.error ?: "Unknown error", onRetry = { viewModel.loadPlugins(true) })
                }
                else -> {
                    ModernFeatureStoreContent(
                        state = state,
                        onCategorySelected = viewModel::selectCategory,
                        onPluginClick = onPluginClick,
                        onInstall = viewModel::installPlugin,
                        onUninstall = viewModel::uninstallPlugin,
                        onCancelDownload = viewModel::cancelDownload,
                        onUpdate = viewModel::updatePlugin
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.feature_store),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Discover plugins to enhance your reading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = localizeHelper.localize(Res.string.search))
            }
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.Tune, contentDescription = localizeHelper.localize(Res.string.filter))
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(localizeHelper.localize(Res.string.search_features)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.close_search))
            }
        }
    )
}


@Composable
private fun ModernFeatureStoreContent(
    state: FeatureStoreState,
    onCategorySelected: (PluginType?) -> Unit,
    onPluginClick: (String) -> Unit,
    onInstall: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Category chips with icons
        item {
            ModernCategoryChips(
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        
        // Featured section (only when no filters active)
        if (state.searchQuery.isBlank() && state.selectedCategory == null && 
            state.priceFilter == PriceFilter.ALL && state.minRating == 0f && 
            state.featuredPlugins.isNotEmpty()) {
            item {
                ModernFeaturedSection(
                    plugins = state.featuredPlugins,
                    onPluginClick = onPluginClick,
                    onInstall = onInstall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
        
        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.selectedCategory != null) {
                        getCategoryDisplayName(state.selectedCategory)
                    } else {
                        localizeHelper.localize(Res.string.all_plugins)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.filteredPlugins.size} plugins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (state.filteredPlugins.isEmpty()) {
            item { ModernEmptyState(searchQuery = state.searchQuery) }
        } else {
            items(state.filteredPlugins, key = { it.id }) { plugin ->
                ModernPluginCard(
                    plugin = plugin,
                    onClick = { onPluginClick(plugin.id) },
                    onInstall = { onInstall(plugin.id) },
                    onUninstall = { onUninstall(plugin.id) },
                    onCancelDownload = { onCancelDownload(plugin.id) },
                    onUpdate = { onUpdate(plugin.id) },
                    downloadProgress = state.downloadProgress[plugin.id],
                    updateInfo = state.availableUpdates[plugin.id],
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun ModernCategoryChips(
    selectedCategory: PluginType?,
    onCategorySelected: (PluginType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to (Icons.Outlined.Apps to "All"),
        PluginType.JS_ENGINE to (Icons.Outlined.Code to "JS Engines"),
        PluginType.TTS to (Icons.Outlined.RecordVoiceOver to "Text-to-Speech"),
        PluginType.THEME to (Icons.Outlined.Palette to "Themes"),
        PluginType.FEATURE to (Icons.Outlined.Extension to "Features"),
        PluginType.GRADIO_TTS to (Icons.Outlined.Mic to "Gradio TTS")
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(categories, key = { it.first?.name ?: "all" }) { (type, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            val isSelected = selectedCategory == type
            
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun ModernFeaturedSection(
    plugins: List<PluginInfo>,
    onPluginClick: (String) -> Unit,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.featured_plugins),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(plugins.take(5), key = { it.id }) { plugin ->
                ModernFeaturedCard(
                    plugin = plugin,
                    onClick = { onPluginClick(plugin.id) },
                    onInstall = { onInstall(plugin.id) }
                )
            }
        }
    }
}

@Composable
private fun ModernFeaturedCard(
    plugin: PluginInfo,
    onClick: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            ) {
                AsyncImage(
                    model = plugin.manifest.iconUrl,
                    contentDescription = plugin.manifest.name,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            
            // Type badge
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = getCategoryDisplayName(plugin.manifest.type),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plugin.manifest.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = plugin.manifest.author.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = plugin.manifest.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stats
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    plugin.rating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = ireader.presentation.ui.core.utils.toDecimalString(rating.toDouble(), 1),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatDownloadCount(plugin.downloadCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Install button
                when (plugin.status) {
                    PluginStatus.NOT_INSTALLED -> {
                        FilledTonalButton(
                            onClick = onInstall,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Get", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    PluginStatus.ENABLED, PluginStatus.DISABLED -> {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Installed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    else -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}


@Composable
private fun ModernPluginCard(
    plugin: PluginInfo,
    onClick: () -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onCancelDownload: () -> Unit,
    onUpdate: () -> Unit,
    downloadProgress: DownloadProgress?,
    updateInfo: PluginUpdateInfo?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val hasUpdate = updateInfo != null
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plugin icon with type indicator
            Box {
                Card(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = plugin.manifest.iconUrl,
                        contentDescription = plugin.manifest.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Type indicator dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .background(
                            color = getCategoryColor(plugin.manifest.type),
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Plugin info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plugin.manifest.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Price badge
                    PriceBadge(monetization = plugin.manifest.monetization)
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = plugin.manifest.author.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    plugin.rating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = ireader.presentation.ui.core.utils.toDecimalString(rating.toDouble(), 1),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatDownloadCount(plugin.downloadCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "v${plugin.manifest.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Action button
            if (downloadProgress != null && downloadProgress.status != DownloadStatus.COMPLETED) {
                DownloadProgressButton(progress = downloadProgress, onCancel = onCancelDownload)
            } else if (hasUpdate) {
                UpdateButton(updateInfo = updateInfo!!, onUpdate = onUpdate)
            } else {
                PluginActionButton(
                    status = plugin.status,
                    onInstall = onInstall,
                    onUninstall = onUninstall
                )
            }
        }
    }
}

@Composable
private fun DownloadProgressButton(
    progress: DownloadProgress,
    onCancel: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { if (progress.progress > 0f) progress.progress else 0f },
            modifier = Modifier.size(44.dp),
            strokeWidth = 3.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun UpdateButton(
    updateInfo: PluginUpdateInfo,
    onUpdate: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalButton(
            onClick = onUpdate,
            modifier = Modifier.widthIn(min = 72.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Update", style = MaterialTheme.typography.labelMedium)
        }
        Text(
            text = "v${updateInfo.newVersion}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PluginActionButton(
    status: PluginStatus,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    when (status) {
        PluginStatus.NOT_INSTALLED -> {
            FilledTonalButton(
                onClick = onInstall,
                modifier = Modifier.widthIn(min = 72.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get", style = MaterialTheme.typography.labelLarge)
            }
        }
        PluginStatus.ENABLED, PluginStatus.DISABLED -> {
            OutlinedButton(
                onClick = onUninstall,
                modifier = Modifier.widthIn(min = 72.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open", style = MaterialTheme.typography.labelMedium)
            }
        }
        PluginStatus.UPDATING -> {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
        }
        PluginStatus.ERROR -> {
            FilledTonalButton(
                onClick = onInstall,
                modifier = Modifier.widthIn(min = 72.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Retry", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PriceBadge(monetization: PluginMonetization?, modifier: Modifier = Modifier) {
    val (text, containerColor, contentColor) = when (monetization) {
        is PluginMonetization.Premium -> Triple(
            formatPrice(monetization.price, monetization.currency),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        is PluginMonetization.Freemium -> Triple(
            "Freemium",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Triple(
            "Free",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ModernLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading plugins...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun ModernEmptyState(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No plugins available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
private fun getCategoryDisplayName(type: PluginType?): String = when (type) {
    PluginType.JS_ENGINE -> "JS Engines"
    PluginType.TTS -> "Text-to-Speech"
    PluginType.THEME -> "Themes"
    PluginType.FEATURE -> "Features"
    PluginType.GRADIO_TTS -> "Gradio TTS"
    PluginType.TRANSLATION -> "Translation"
    PluginType.AI -> "AI"
    PluginType.CATALOG -> "Catalog"
    PluginType.IMAGE_PROCESSING -> "Image Processing"
    PluginType.SYNC -> "Sync"
    PluginType.COMMUNITY_SCREEN -> "Community"
    PluginType.GLOSSARY -> "Glossary"
    PluginType.TACHI_SOURCE_LOADER -> "Tachi Sources"
    PluginType.READER_SCREEN -> "Reader Screens"
    PluginType.SOURCE_LOADER -> "Source Loaders"
    null -> "All"
}

@Composable
private fun getCategoryColor(type: PluginType): Color = when (type) {
    PluginType.JS_ENGINE -> Color(0xFF4CAF50)
    PluginType.TTS -> Color(0xFF2196F3)
    PluginType.THEME -> Color(0xFFE91E63)
    PluginType.FEATURE -> Color(0xFFFF9800)
    PluginType.GRADIO_TTS -> Color(0xFF9C27B0)
    PluginType.TRANSLATION -> Color(0xFF00BCD4)
    PluginType.AI -> Color(0xFF673AB7)
    PluginType.CATALOG -> Color(0xFF795548)
    PluginType.IMAGE_PROCESSING -> Color(0xFF607D8B)
    PluginType.SYNC -> Color(0xFF009688)
    PluginType.COMMUNITY_SCREEN -> Color(0xFFFF5722)
    PluginType.GLOSSARY -> Color(0xFF3F51B5)
    PluginType.TACHI_SOURCE_LOADER -> Color(0xFFE65100)
    PluginType.READER_SCREEN -> Color(0xFF1565C0)
    PluginType.SOURCE_LOADER -> Color(0xFF558B2F)
}

private fun formatDownloadCount(count: Int): String = when {
    count >= 1_000_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000_000.0, 1)}M"
    count >= 1_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000.0, 1)}K"
    else -> count.toString()
}

private fun formatPrice(price: Double, currency: String): String {
    val symbol = when (currency.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> currency
    }
    return "$symbol${ireader.presentation.ui.core.utils.toDecimalString(price, 2)}"
}
