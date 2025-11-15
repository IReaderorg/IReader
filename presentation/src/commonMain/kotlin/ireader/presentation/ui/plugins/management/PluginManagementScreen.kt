package ireader.presentation.ui.plugins.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.plugins.management.components.*

/**
 * Plugin Management screen for managing installed plugins
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 12.1, 12.2, 12.3, 12.4, 12.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagementScreen(
    viewModel: PluginManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installed Plugins") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Update All button (only show if updates available)
                    if (state.updatesAvailable.isNotEmpty()) {
                        IconButton(
                            onClick = viewModel::updateAllPlugins,
                            enabled = !state.isUpdatingAll
                        ) {
                            Icon(
                                imageVector = Icons.Default.Update,
                                contentDescription = "Update All"
                            )
                        }
                    }
                    
                    // Refresh button
                    IconButton(onClick = viewModel::loadInstalledPlugins) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading && state.installedPlugins.isEmpty() -> {
                    LoadingState()
                }
                state.error != null && state.installedPlugins.isEmpty() -> {
                    ErrorState(
                        error = state.error ?: "Unknown error",
                        onRetry = viewModel::loadInstalledPlugins
                    )
                }
                state.installedPlugins.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    PluginManagementContent(
                        state = state,
                        onEnablePlugin = viewModel::enablePlugin,
                        onDisablePlugin = viewModel::disablePlugin,
                        onUninstallPlugin = viewModel::showUninstallConfirmation,
                        onConfigurePlugin = viewModel::openPluginConfiguration,
                        onShowErrorDetails = viewModel::showPluginErrorDetails,
                        onUpdatePlugin = viewModel::updatePlugin,
                        onRefreshResourceUsage = viewModel::refreshResourceUsage
                    )
                }
            }
        }
    }
    
    // Uninstall confirmation dialog
    state.pluginToUninstall?.let { pluginId ->
        val plugin = state.installedPlugins.find { it.id == pluginId }
        if (plugin != null) {
            UninstallConfirmationDialog(
                pluginName = plugin.manifest.name,
                onConfirm = { viewModel.uninstallPlugin(pluginId) },
                onDismiss = viewModel::dismissUninstallConfirmation
            )
        }
    }
    
    // Plugin error details dialog
    state.selectedPluginForError?.let { errorDetails ->
        PluginErrorDetailsDialog(
            errorDetails = errorDetails,
            onDismiss = viewModel::dismissPluginErrorDetails
        )
    }
    
    // Plugin configuration screen
    state.selectedPluginForConfig?.let { pluginId ->
        val plugin = state.installedPlugins.find { it.id == pluginId }
        if (plugin != null) {
            PluginConfigurationScreen(
                plugin = plugin,
                onDismiss = viewModel::closePluginConfiguration
            )
        }
    }
}

/**
 * Main content with installed plugins list
 */
@Composable
private fun PluginManagementContent(
    state: PluginManagementState,
    onEnablePlugin: (String) -> Unit,
    onDisablePlugin: (String) -> Unit,
    onUninstallPlugin: (String) -> Unit,
    onConfigurePlugin: (String) -> Unit,
    onShowErrorDetails: (ireader.domain.plugins.PluginInfo) -> Unit,
    onUpdatePlugin: (String) -> Unit,
    onRefreshResourceUsage: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Update all banner
        if (state.updatesAvailable.isNotEmpty() && !state.isUpdatingAll) {
            item {
                UpdateAllBanner(
                    updateCount = state.updatesAvailable.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        // Updating all indicator
        if (state.isUpdatingAll) {
            item {
                UpdatingAllIndicator(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        // Installed plugins list
        items(state.installedPlugins) { plugin ->
            InstalledPluginItem(
                plugin = plugin,
                hasUpdate = state.updatesAvailable.containsKey(plugin.id),
                newVersion = state.updatesAvailable[plugin.id],
                resourceUsage = state.resourceUsage[plugin.id],
                onEnableToggle = { enabled ->
                    if (enabled) {
                        onEnablePlugin(plugin.id)
                    } else {
                        onDisablePlugin(plugin.id)
                    }
                },
                onConfigure = { onConfigurePlugin(plugin.id) },
                onUninstall = { onUninstallPlugin(plugin.id) },
                onShowErrorDetails = { onShowErrorDetails(plugin) },
                onUpdate = { onUpdatePlugin(plugin.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        
        // Performance metrics section (if any plugins have resource usage data)
        if (state.resourceUsage.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceMetricsSection(
                    plugins = state.installedPlugins,
                    resourceUsage = state.resourceUsage,
                    onRefresh = onRefreshResourceUsage,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
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
                text = "Loading plugins...",
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
                text = "Error",
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
                Text("Retry")
            }
        }
    }
}

/**
 * Empty state when no plugins are installed
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
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
                text = "No Plugins Installed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Visit the Plugin Marketplace to discover and install plugins",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
