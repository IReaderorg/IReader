package ireader.presentation.ui.plugins.required

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Required plugin types that can be requested
 */
enum class RequiredPluginType {
    /** No plugin required (e.g., iOS which doesn't support plugins) */
    NONE,
    /** J2V8 JavaScript Engine for Android */
    JS_ENGINE,
    /** GraalVM JavaScript Engine for Desktop */
    GRAALVM_ENGINE,
    /** Piper TTS for Desktop */
    PIPER_TTS
}

/**
 * State for the required plugin screen
 */
@Stable
data class RequiredPluginState(
    val pluginType: RequiredPluginType,
    val isLoading: Boolean = false,
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val error: String? = null,
    val pluginInfo: PluginDisplayInfo? = null,
    /** True if JS sources were successfully reloaded after enabling JS engine plugin */
    val jsSourcesReloaded: Boolean = false,
    /** True if app restart is recommended (JS engine couldn't initialize without restart) */
    val restartRecommended: Boolean = false
)

/**
 * Plugin display information
 */
data class PluginDisplayInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val fileSize: Long,
    val author: String
)

/**
 * Modern UI screen for required plugin installation
 * Shows when a feature needs a plugin to work
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredPluginScreen(
    state: RequiredPluginState,
    featureName: String,
    onInstall: () -> Unit,
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pluginConfig = remember(state.pluginType) {
        getPluginConfig(state.pluginType)
    }
    val scrollState = rememberScrollState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Required Plugin") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Plugin icon
            PluginIcon(
                isInstalled = state.isInstalled && state.isEnabled,
                isDownloading = state.isDownloading,
                progress = state.downloadProgress
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = if (state.isInstalled && state.isEnabled) {
                    "Plugin Ready!"
                } else if (state.isInstalled) {
                    "Plugin Installed"
                } else {
                    "Plugin Required"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = when {
                    state.restartRecommended -> "Please restart the app to use $featureName"
                    state.isInstalled && state.isEnabled && state.jsSourcesReloaded -> "You can now use $featureName"
                    state.isInstalled && state.isEnabled -> "Sources are being loaded..."
                    state.isInstalled -> "Enable the plugin to use $featureName"
                    else -> "$featureName requires ${pluginConfig.name}"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (state.restartRecommended) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Plugin info card
            PluginInfoCard(
                config = pluginConfig,
                pluginInfo = state.pluginInfo,
                isInstalled = state.isInstalled
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Why needed section
            WhyNeededSection(
                featureName = featureName,
                pluginConfig = pluginConfig
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.error?.let { error ->
                    ErrorMessage(
                        message = error,
                        onRetry = onRetry
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button
            ActionButton(
                state = state,
                onInstall = onInstall,
                onEnable = onEnable,
                onDismiss = onDismiss
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PluginIcon(
    isInstalled: Boolean,
    isDownloading: Boolean,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "download_progress"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = if (isInstalled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {}
        
        // Progress indicator when downloading
        if (isDownloading) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(120.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        // Icon
        Icon(
            imageVector = if (isInstalled) Icons.Default.Check else Icons.Default.Extension,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = if (isInstalled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun PluginInfoCard(
    config: PluginConfig,
    pluginInfo: PluginDisplayInfo?,
    isInstalled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pluginInfo?.name ?: config.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (isInstalled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Installed",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = pluginInfo?.description ?: config.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    label = "Version",
                    value = pluginInfo?.version ?: config.version
                )
                InfoChip(
                    label = "Size",
                    value = formatFileSize(pluginInfo?.fileSize ?: config.estimatedSize)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WhyNeededSection(
    featureName: String,
    pluginConfig: PluginConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Why is this needed?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = pluginConfig.whyNeeded,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun ActionButton(
    state: RequiredPluginState,
    onInstall: () -> Unit,
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    when {
        state.restartRecommended -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Restart info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "The plugin was installed successfully. Please restart the app to load JavaScript sources.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "OK, I'll Restart Later",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        state.isInstalled && state.isEnabled -> {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        state.isInstalled && !state.isEnabled -> {
            Button(
                onClick = onEnable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Enable Plugin",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        state.isDownloading -> {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = false
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Downloading... ${(state.downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        state.isLoading -> {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = false
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Installing...",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        else -> {
            Button(
                onClick = onInstall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download & Install",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Plugin configuration for display
 */
data class PluginConfig(
    val name: String,
    val description: String,
    val version: String,
    val estimatedSize: Long,
    val whyNeeded: String,
    val pluginId: String
)

private fun getPluginConfig(type: RequiredPluginType): PluginConfig {
    return when (type) {
        RequiredPluginType.NONE -> PluginConfig(
            name = "Not Available",
            description = "Plugins are not supported on this platform",
            version = "",
            estimatedSize = 0L,
            whyNeeded = "This feature is not available on this platform.",
            pluginId = ""
        )
        RequiredPluginType.JS_ENGINE -> PluginConfig(
            name = "J2V8 JavaScript Engine",
            description = "V8 JavaScript engine for running LNReader-compatible sources",
            version = "6.2.1",
            estimatedSize = 33 * 1024 * 1024L, // ~33 MB
            whyNeeded = "This plugin provides the JavaScript engine needed to run web-based novel sources. Without it, sources that use JavaScript for content loading won't work.",
            pluginId = "io.github.ireaderorg.plugins.j2v8-engine"
        )
        RequiredPluginType.GRAALVM_ENGINE -> PluginConfig(
            name = "GraalVM JavaScript Engine",
            description = "GraalVM JavaScript engine for running LNReader-compatible sources on Desktop",
            version = "25.0.1",
            estimatedSize = 8 * 1024 * 1024L, // ~8 MB
            whyNeeded = "This plugin provides the JavaScript engine needed to run web-based novel sources on Desktop. Without it, sources that use JavaScript for content loading won't work.",
            pluginId = "io.github.ireaderorg.plugins.graalvm-engine"
        )
        RequiredPluginType.PIPER_TTS -> PluginConfig(
            name = "Piper TTS",
            description = "High-quality neural text-to-speech for offline reading",
            version = "1.2.0",
            estimatedSize = 22 * 1024 * 1024L, // ~22 MB
            whyNeeded = "This plugin provides neural text-to-speech capabilities for reading novels aloud. It works completely offline after installation.",
            pluginId = "io.github.ireaderorg.plugins.piper-tts"
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return ireader.presentation.ui.core.utils.formatBytesKmp(bytes)
}
