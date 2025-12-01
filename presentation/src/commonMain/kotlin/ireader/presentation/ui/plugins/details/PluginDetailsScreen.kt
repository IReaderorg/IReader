package ireader.presentation.ui.plugins.details

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.back
import ireader.i18n.resources.cancel
import ireader.i18n.resources.download_notifier_title_error
import ireader.i18n.resources.enable_continue
import ireader.i18n.resources.enable_plugin_feature
import ireader.i18n.resources.javascript_plugins_are_currently_disabled
import ireader.i18n.resources.loading_plugin_details
import ireader.i18n.resources.retry
import ireader.i18n.resources.to_use_lnreader_compatible_plugins
import ireader.i18n.resources.would_you_like_to_enable_it_now
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.plugins.details.components.DescriptionSection
import ireader.presentation.ui.plugins.details.components.DeveloperInfoSection
import ireader.presentation.ui.plugins.details.components.EmptyReviewsState
import ireader.presentation.ui.plugins.details.components.InstallButton
import ireader.presentation.ui.plugins.details.components.PermissionsSection
import ireader.presentation.ui.plugins.details.components.PluginHeader
import ireader.presentation.ui.plugins.details.components.PluginScreenshots
import ireader.presentation.ui.plugins.details.components.PurchaseDialog
import ireader.presentation.ui.plugins.details.components.ResourceUsageHistoryGraph
import ireader.presentation.ui.plugins.details.components.ResourceUsageSection
import ireader.presentation.ui.plugins.details.components.ReviewItem
import ireader.presentation.ui.plugins.details.components.ReviewsSectionHeader
import ireader.presentation.ui.plugins.details.components.SuccessMessageDialog
import ireader.presentation.ui.plugins.details.components.WriteReviewDialog

/**
 * Plugin Details screen showing comprehensive plugin information
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 13.1, 13.2, 13.3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailsScreen(
    viewModel: PluginDetailsViewModel,
    onNavigateBack: () -> Unit,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.plugin?.manifest?.name ?: "Plugin Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (state.plugin != null) {
                InstallButton(
                    plugin = state.plugin!!,
                    installationState = state.installationState,
                    installProgress = state.installProgress,
                    onInstall = viewModel::installPlugin,
                    onPurchase = viewModel::installPlugin,
                    onOpen = viewModel::openPlugin,
                    onRetry = viewModel::retryInstallation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading && state.plugin == null -> {
                    LoadingState()
                }
                state.error != null && state.plugin == null -> {
                    ErrorState(
                        error = state.error ?: "Unknown error",
                        onRetry = viewModel::loadPluginDetails
                    )
                }
                state.plugin != null -> {
                    PluginDetailsContent(
                        state = state,
                        onWriteReview = viewModel::showWriteReviewDialog,
                        onMarkReviewHelpful = viewModel::markReviewHelpful,
                        onPluginClick = onPluginClick
                    )
                }
            }
        }
    }
    
    // Purchase dialog
    if (state.showPurchaseDialog && state.plugin != null) {
        PurchaseDialog(
            plugin = state.plugin!!,
            onPurchase = viewModel::purchasePlugin,
            onStartTrial = viewModel::startTrial,
            onDismiss = viewModel::dismissPurchaseDialog
        )
    }
    
    // Review dialog
    if (state.showReviewDialog) {
        WriteReviewDialog(
            onSubmit = viewModel::submitReview,
            onDismiss = viewModel::dismissReviewDialog
        )
    }
    
    // Success message
    if (state.showSuccessMessage) {
        SuccessMessageDialog(
            onOpen = {
                viewModel.dismissSuccessMessage()
                viewModel.openPlugin()
            },
            onDismiss = viewModel::dismissSuccessMessage
        )
    }
    
    // Enable JS plugins prompt dialog
    if (state.showEnablePluginPrompt) {
        EnablePluginFeatureDialog(
            onEnableAndContinue = viewModel::enableJSPluginsFeature,
            onGoToSettings = {
                viewModel.dismissEnablePluginPrompt()
                // TODO: Navigate to general settings screen
            },
            onDismiss = viewModel::dismissEnablePluginPrompt
        )
    }
}

/**
 * Main content with plugin details
 */
@Composable
private fun PluginDetailsContent(
    state: PluginDetailsState,
    onWriteReview: () -> Unit,
    onMarkReviewHelpful: (String) -> Unit,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val plugin = state.plugin ?: return
    
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Plugin header
        item {
            PluginHeader(
                plugin = plugin,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Screenshots
        if (plugin.manifest.screenshotUrls.isNotEmpty()) {
            item {
                PluginScreenshots(
                    screenshots = plugin.manifest.screenshotUrls,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
        
        // Description
        item {
            DescriptionSection(
                description = plugin.manifest.description,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Developer info
        item {
            DeveloperInfoSection(
                author = plugin.manifest.author,
                otherPlugins = state.otherPluginsByDeveloper,
                onPluginClick = onPluginClick,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Permissions
        if (plugin.manifest.permissions.isNotEmpty()) {
            item {
                PermissionsSection(
                    permissions = plugin.manifest.permissions,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Resource Usage (if plugin is installed and running)
        if (state.resourceUsage != null && state.resourcePercentages != null) {
            item {
                ResourceUsageSection(
                    usage = state.resourceUsage!!,
                    percentages = state.resourcePercentages!!,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Resource Usage History
        if (state.resourceHistory.isNotEmpty()) {
            item {
                ResourceUsageHistoryGraph(
                    history = state.resourceHistory,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Reviews section header
        item {
            ReviewsSectionHeader(
                reviewCount = state.reviews.size,
                averageRating = state.reviews.map { it.rating }.average().toFloat(),
                onWriteReview = onWriteReview,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Reviews list
        items(state.reviews) { review ->
            ReviewItem(
                review = review,
                onMarkHelpful = { onMarkReviewHelpful(review.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Empty reviews state
        if (state.reviews.isEmpty()) {
            item {
                EmptyReviewsState(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Bottom spacing for button
        item {
            Spacer(modifier = Modifier.height(80.dp))
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
                text = localizeHelper.localize(Res.string.loading_plugin_details),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state
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
 * Dialog prompting user to enable JS plugins feature in settings
 */
@Composable
private fun EnablePluginFeatureDialog(
    onEnableAndContinue: () -> Unit,
    onGoToSettings: () -> Unit,
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
