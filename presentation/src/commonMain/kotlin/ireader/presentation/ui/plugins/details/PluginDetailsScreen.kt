package ireader.presentation.ui.plugins.details

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import ireader.presentation.ui.featurestore.PluginUpdateInfo
import ireader.presentation.ui.plugins.details.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailsScreen(
    viewModel: PluginDetailsViewModel,
    onNavigateBack: () -> Unit,
    onPluginClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ModernDetailsTopBar(
                plugin = state.plugin,
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            if (state.plugin != null) {
                ModernInstallBar(
                    plugin = state.plugin!!,
                    installationState = state.installationState,
                    installProgress = state.installProgress,
                    updateAvailable = state.updateAvailable,
                    updateInfo = state.updateInfo,
                    onInstall = viewModel::installPlugin,
                    onEnable = viewModel::enablePlugin,
                    onDisable = viewModel::disablePlugin,
                    onUninstall = viewModel::uninstallPlugin,
                    onUpdate = viewModel::updatePlugin,
                    onRetry = viewModel::retryInstallation
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading && state.plugin == null -> ModernLoadingState()
                state.error != null && state.plugin == null -> {
                    ModernErrorState(error = state.error ?: "Unknown error", onRetry = viewModel::loadPluginDetails)
                }
                state.plugin != null -> {
                    ModernPluginDetailsContent(
                        state = state,
                        onWriteReview = viewModel::showWriteReviewDialog,
                        onMarkReviewHelpful = viewModel::markReviewHelpful,
                        onPluginClick = onPluginClick
                    )
                }
            }
        }
    }
    
    if (state.showPurchaseDialog && state.plugin != null) {
        PurchaseDialog(
            plugin = state.plugin!!,
            onPurchase = viewModel::purchasePlugin,
            onStartTrial = viewModel::startTrial,
            onDismiss = viewModel::dismissPurchaseDialog
        )
    }
    
    if (state.showReviewDialog) {
        WriteReviewDialog(onSubmit = viewModel::submitReview, onDismiss = viewModel::dismissReviewDialog)
    }
    
    if (state.showSuccessMessage) {
        SuccessMessageDialog(
            onOpen = { viewModel.dismissSuccessMessage(); viewModel.openPlugin() },
            onDismiss = viewModel::dismissSuccessMessage
        )
    }
    
    if (state.showEnablePluginPrompt) {
        EnablePluginFeatureDialog(
            onEnableAndContinue = viewModel::enableJSPluginsFeature,
            onGoToSettings = { viewModel.dismissEnablePluginPrompt(); onNavigateToSettings() },
            onDismiss = viewModel::dismissEnablePluginPrompt
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDetailsTopBar(
    plugin: PluginInfo?,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = plugin?.manifest?.name ?: "Plugin Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (plugin != null) {
                    Text(
                        text = plugin.manifest.author.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
            }
        },
        actions = {
            if (plugin != null) {
                IconButton(onClick = { }) { Icon(Icons.Default.Share, contentDescription = "Share") }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}


@Composable
private fun ModernInstallBar(
    plugin: PluginInfo,
    installationState: InstallationState,
    installProgress: Float,
    updateAvailable: Boolean,
    updateInfo: PluginUpdateInfo?,
    onInstall: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onUninstall: () -> Unit,
    onUpdate: () -> Unit,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Price and type info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceBadge(monetization = plugin.manifest.monetization)
                    TypeBadge(type = plugin.manifest.type)
                }
                
                Text(
                    text = "v${plugin.manifest.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons based on state
            when (installationState) {
                is InstallationState.NotInstalled -> {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.install), style = MaterialTheme.typography.titleMedium)
                    }
                }
                is InstallationState.Downloading -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { installProgress },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Downloading... ${(installProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                is InstallationState.Installing -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(localizeHelper.localize(Res.string.installing_1), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is InstallationState.Installed -> {
                    // Show update button if update is available
                    if (updateAvailable && updateInfo != null) {
                        Button(
                            onClick = onUpdate,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update to v${updateInfo.newVersion}", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current: v${updateInfo.currentVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    when (plugin.status) {
                        PluginStatus.ENABLED -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onDisable,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(localizeHelper.localize(Res.string.disable))
                                }
                                OutlinedButton(
                                    onClick = onUninstall,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(localizeHelper.localize(Res.string.uninstall))
                                }
                            }
                            if (!updateAvailable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(localizeHelper.localize(Res.string.enabled), style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                        PluginStatus.DISABLED -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onEnable,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(localizeHelper.localize(Res.string.enable))
                                }
                                OutlinedButton(
                                    onClick = onUninstall,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(localizeHelper.localize(Res.string.uninstall))
                                }
                            }
                            if (!updateAvailable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = localizeHelper.localize(Res.string.disabled),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        else -> {
                            Button(onClick = onEnable, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(localizeHelper.localize(Res.string.enable))
                            }
                        }
                    }
                }
                is InstallationState.Error -> {
                    Column {
                        Text(
                            text = installationState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizeHelper.localize(Res.string.retry))
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ModernPluginDetailsContent(
    state: PluginDetailsState,
    onWriteReview: () -> Unit,
    onMarkReviewHelpful: (String) -> Unit,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val plugin = state.plugin ?: return
    
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        // Hero section with icon and stats
        item {
            ModernHeroSection(plugin = plugin)
        }
        
        // Quick stats cards
        item {
            QuickStatsRow(plugin = plugin, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        
        // Screenshots
        if (plugin.manifest.screenshotUrls.isNotEmpty()) {
            item {
                PluginScreenshots(screenshots = plugin.manifest.screenshotUrls, modifier = Modifier.padding(vertical = 16.dp))
            }
        }
        
        // About section
        item {
            ModernSection(title = "About", icon = Icons.Outlined.Info) {
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // What's New section (if available)
        item {
            ModernSection(title = "What's New", icon = Icons.Outlined.NewReleases) {
                Text(
                    text = "Version ${plugin.manifest.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bug fixes and performance improvements",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Developer section
        item {
            ModernSection(title = "Developer", icon = Icons.Outlined.Person) {
                DeveloperCard(
                    author = plugin.manifest.author,
                    otherPlugins = state.otherPluginsByDeveloper,
                    onPluginClick = onPluginClick
                )
            }
        }
        
        // Permissions section
        if (plugin.manifest.permissions.isNotEmpty()) {
            item {
                ModernSection(title = "Permissions", icon = Icons.Outlined.Security) {
                    plugin.manifest.permissions.forEach { permission ->
                        PermissionItem(permission = permission)
                    }
                }
            }
        }
        
        // Resource usage (if installed)
        if (state.resourceUsage != null && state.resourcePercentages != null) {
            item {
                ModernSection(title = "Resource Usage", icon = Icons.Outlined.Memory) {
                    ResourceUsageSection(usage = state.resourceUsage!!, percentages = state.resourcePercentages!!)
                }
            }
        }
        
        // Reviews section
        item {
            ModernSection(title = "Reviews", icon = Icons.Outlined.RateReview, action = {
                TextButton(onClick = onWriteReview) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Review")
                }
            }) {
                if (state.reviews.isEmpty()) {
                    EmptyReviewsState()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.reviews.take(3).forEach { review ->
                            ReviewItem(review = review, onMarkHelpful = { onMarkReviewHelpful(review.id) })
                        }
                    }
                }
            }
        }
        
        // Additional info
        item {
            ModernSection(title = "Additional Information", icon = Icons.Outlined.Info) {
                InfoGrid(plugin = plugin)
            }
        }
    }
}

@Composable
private fun ModernHeroSection(plugin: PluginInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // Plugin icon
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = plugin.manifest.iconUrl,
                    contentDescription = plugin.manifest.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rating stars
            plugin.rating?.let { rating ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (index < rating.toInt()) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ireader.presentation.ui.core.utils.toDecimalString(rating.toDouble(), 1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(plugin: PluginInfo, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            icon = Icons.Default.Download,
            value = formatDownloadCount(plugin.downloadCount),
            label = "Downloads",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Star,
            value = plugin.rating?.let { ireader.presentation.ui.core.utils.toDecimalString(it.toDouble(), 1) } ?: "N/A",
            label = "Rating",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Update,
            value = plugin.manifest.version,
            label = "Version",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModernSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            action?.invoke()
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}


@Composable
private fun DeveloperCard(
    author: ireader.plugin.api.PluginAuthor,
    otherPlugins: List<PluginInfo>,
    onPluginClick: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = author.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = author.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                author.email?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        if (otherPlugins.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "More by this developer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(otherPlugins.take(5)) { plugin ->
                    Card(
                        modifier = Modifier.size(60.dp).clickable { onPluginClick(plugin.id) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(model = plugin.manifest.iconUrl, contentDescription = plugin.manifest.name, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(permission: ireader.plugin.api.PluginPermission) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getPermissionIcon(permission),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = permission.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = getPermissionDescription(permission),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoGrid(plugin: PluginInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow(label = "Type", value = getCategoryDisplayName(plugin.manifest.type))
        InfoRow(label = "Version", value = plugin.manifest.version)
        InfoRow(label = "Min IReader Version", value = plugin.manifest.minIReaderVersion)
        InfoRow(label = "Platforms", value = plugin.manifest.platforms.joinToString(", ") { it.name })
        plugin.fileSize?.let { InfoRow(label = "Size", value = formatFileSize(it)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
        is PluginMonetization.Freemium -> Triple("Freemium", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        else -> Triple("Free", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = containerColor) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = contentColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun TypeBadge(type: PluginType, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Text(text = getCategoryDisplayName(type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun ModernLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Loading plugin details...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModernErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Something went wrong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
private fun EnablePluginFeatureDialog(onEnableAndContinue: () -> Unit, onGoToSettings: () -> Unit, onDismiss: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(text = localizeHelper.localize(Res.string.enable_plugin_feature), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = localizeHelper.localize(Res.string.javascript_plugins_are_currently_disabled), style = MaterialTheme.typography.bodyMedium)
                Text(text = localizeHelper.localize(Res.string.to_use_lnreader_compatible_plugins), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = localizeHelper.localize(Res.string.would_you_like_to_enable_it_now), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = { Button(onClick = onEnableAndContinue) { Text(localizeHelper.localize(Res.string.enable_continue)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(localizeHelper.localize(Res.string.cancel)) } }
    )
}

// Helper functions
private fun getCategoryDisplayName(type: PluginType): String = when (type) {
    PluginType.JS_ENGINE -> "JS Engine"
    PluginType.TTS -> "Text-to-Speech"
    PluginType.THEME -> "Theme"
    PluginType.FEATURE -> "Feature"
    PluginType.GRADIO_TTS -> "Gradio TTS"
    PluginType.TRANSLATION -> "Translation"
    PluginType.AI -> "AI"
    PluginType.CATALOG -> "Catalog"
    PluginType.IMAGE_PROCESSING -> "Image Processing"
    PluginType.SYNC -> "Sync"
    PluginType.COMMUNITY_SCREEN -> "Community"
    PluginType.GLOSSARY -> "Glossary"
    PluginType.TACHI_SOURCE_LOADER -> "Tachi Sources"
    PluginType.READER_SCREEN -> "Reader Screen"
    PluginType.SOURCE_LOADER -> "Source Loader"
}

private fun getPermissionIcon(permission: ireader.plugin.api.PluginPermission): ImageVector = when (permission) {
    ireader.plugin.api.PluginPermission.NETWORK -> Icons.Default.Wifi
    ireader.plugin.api.PluginPermission.STORAGE -> Icons.Default.Storage
    ireader.plugin.api.PluginPermission.NOTIFICATIONS -> Icons.Default.Notifications
    else -> Icons.Default.Security
}

private fun getPermissionDescription(permission: ireader.plugin.api.PluginPermission): String = when (permission) {
    ireader.plugin.api.PluginPermission.NETWORK -> "Access to network for fetching content"
    ireader.plugin.api.PluginPermission.STORAGE -> "Access to device storage"
    ireader.plugin.api.PluginPermission.NOTIFICATIONS -> "Show notifications"
    else -> "Additional permission required"
}

private fun formatDownloadCount(count: Int): String = when {
    count >= 1_000_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000_000.0, 1)}M"
    count >= 1_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000.0, 1)}K"
    else -> count.toString()
}

private fun formatPrice(price: Double, currency: String): String {
    val symbol = when (currency.uppercase()) { "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY" -> "¥"; else -> currency }
    return "$symbol${ireader.presentation.ui.core.utils.toDecimalString(price, 2)}"
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(bytes / 1_000_000.0, 1)} MB"
    bytes >= 1_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(bytes / 1_000.0, 1)} KB"
    else -> "$bytes B"
}
