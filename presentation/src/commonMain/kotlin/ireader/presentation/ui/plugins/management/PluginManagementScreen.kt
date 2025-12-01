package ireader.presentation.ui.plugins.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.plugins.management.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.installed_plugins)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
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
                                contentDescription = localizeHelper.localize(Res.string.update_all)
                            )
                        }
                    }
                    
                    // Refresh button
                    IconButton(onClick = viewModel::loadInstalledPlugins) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = localizeHelper.localize(Res.string.refresh)
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
    
    // Enable JS plugins prompt dialog
    if (state.showEnablePluginPrompt) {
        EnablePluginFeatureDialog(
            onEnableAndContinue = viewModel::enableJSPluginsFeature,
            onDismiss = viewModel::dismissEnablePluginPrompt
        )
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
 * Empty state when no plugins are installed
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
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
                text = localizeHelper.localize(Res.string.no_plugins_installed),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.visit_the_plugin_marketplace_to),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog prompting user to enable JS plugins feature in settings
 */
@Composable
private fun EnablePluginFeatureDialog(
    onEnableAndContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = localizeHelper.localize(Res.string.enable_plugin_feature),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.javascript_plugins_are_currently_disabled),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = localizeHelper.localize(Res.string.to_use_lnreader_compatible_plugins),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizeHelper.localize(Res.string.would_you_like_to_enable_it_now),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(onClick = onEnableAndContinue) {
                Text(localizeHelper.localize(Res.string.enable_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
