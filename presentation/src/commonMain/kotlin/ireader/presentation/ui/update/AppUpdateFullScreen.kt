package ireader.presentation.ui.update

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.currentOrThrow

/**
 * Full-screen app update experience similar to RequiredPluginHandler
 * Shows when an app update is available with modern Material Design 3 UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateFullScreen(
    state: AppUpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onRemindLater: () -> Unit,
    onSkipVersion: () -> Unit,
    onDismiss: () -> Unit,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val scrollState = rememberScrollState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = localizeHelper.localize(Res.string.app_update_available),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (!state.isDownloading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = localizeHelper.localize(Res.string.back)
                            )
                        }
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
            
            // Update icon with animation
            UpdateIcon(
                isDownloaded = state.isDownloaded,
                isDownloading = state.isDownloading,
                isConnecting = state.isConnecting,
                progress = state.downloadProgress
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = when {
                    state.isDownloaded -> "Update Ready!"
                    state.isConnecting -> "Connecting..."
                    state.isDownloading -> "Downloading Update..."
                    else -> "New Version Available"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = when {
                    state.isDownloaded -> "Ready to install ${state.newVersion}"
                    state.isConnecting -> "Establishing connection to server..."
                    state.isDownloading -> "Downloading ${state.newVersion}..."
                    else -> "Version ${state.newVersion} is now available"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Version comparison
            if (!state.isDownloading && !state.isDownloaded) {
                VersionComparisonCard(
                    currentVersion = state.currentVersion,
                    newVersion = state.newVersion
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Download progress
            if (state.isDownloading || state.isConnecting) {
                DownloadProgressCard(
                    progress = state.downloadProgress,
                    apkAsset = state.apkAsset,
                    isConnecting = state.isConnecting
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Release notes
            if (state.releaseNotes.isNotEmpty() && !state.isDownloading) {
                ReleaseNotesCard(releaseNotes = state.releaseNotes)
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Download info
            if (!state.isDownloading && !state.isDownloaded) {
                state.apkAsset?.let { asset ->
                    DownloadInfoCard(asset = asset)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Error message
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.error?.let { error ->
                    ErrorCard(
                        message = error,
                        onRetry = onDownload
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            ActionButtons(
                state = state,
                onDownload = onDownload,
                onInstall = onInstall,
                onRemindLater = onRemindLater,
                onSkipVersion = onSkipVersion,
                onDismiss = onDismiss,
                onCancelDownload = onCancelDownload,
                localizeHelper = localizeHelper
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UpdateIcon(
    isDownloaded: Boolean,
    isDownloading: Boolean,
    isConnecting: Boolean,
    progress: Float
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = localizeHelper.localize(Res.string.download_progress_1)
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle with gradient
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDownloaded -> Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF8BC34A)
                            )
                        )
                        isConnecting || isDownloading -> Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                        else -> Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                )
        )
        
        // Progress indicator when downloading or connecting
        if (isConnecting) {
            // Indeterminate progress for connecting state
            CircularProgressIndicator(
                modifier = Modifier.size(120.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )
        } else if (isDownloading) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(120.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )
        }
        
        // Icon
        Icon(
            imageVector = when {
                isDownloaded -> Icons.Default.CheckCircle
                isConnecting -> Icons.Default.CloudSync
                isDownloading -> Icons.Default.CloudDownload
                else -> Icons.Default.SystemUpdate
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun VersionComparisonCard(
    currentVersion: String,
    newVersion: String
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current version
            VersionChip(
                version = currentVersion,
                label = localizeHelper.localize(Res.string.current),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // New version
            VersionChip(
                version = newVersion,
                label = localizeHelper.localize(Res.string.add_as_new),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun VersionChip(
    version: String,
    label: String,
    containerColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = version.removePrefix("v"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DownloadProgressCard(
    progress: Float,
    apkAsset: ireader.domain.models.update_service_models.ReleaseAsset?,
    isConnecting: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isConnecting) "Connecting..." else "Downloading...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isConnecting) "..." else "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isConnecting) {
                // Indeterminate progress bar for connecting state
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            apkAsset?.let { asset ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isConnecting) "Establishing connection..." else "Size: ${formatFileSize(asset.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReleaseNotesCard(releaseNotes: String) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NewReleases,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.whats_new_1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = releaseNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadInfoCard(asset: ireader.domain.models.update_service_models.ReleaseAsset) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    text = localizeHelper.localize(Res.string.download_information),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "File: ${asset.name}\nSize: ${formatFileSize(asset.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(onClick = onRetry) {
                Text(
                    text = localizeHelper.localize(Res.string.notification_retry),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    state: AppUpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onRemindLater: () -> Unit,
    onSkipVersion: () -> Unit,
    onDismiss: () -> Unit,
    onCancelDownload: () -> Unit,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            state.isDownloaded -> {
                // Install button
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.InstallMobile,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.install_now),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Install later button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localizeHelper.localize(Res.string.install_later))
                }
            }
            
            state.isDownloading || state.isConnecting -> {
                // Cancel download button
                OutlinedButton(
                    onClick = onCancelDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isConnecting) "Cancel" else "Cancel Download",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            else -> {
                // Download button
                Button(
                    onClick = onDownload,
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
                        text = localizeHelper.localize(Res.string.download_install),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Secondary actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRemindLater,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(localizeHelper.localize(Res.string.later))
                    }
                    
                    TextButton(
                        onClick = onSkipVersion,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(localizeHelper.localize(Res.string.skip_version))
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> {
            val mb = bytes / (1024.0 * 1024.0)
            val rounded = (mb * 10).toLong() / 10.0
            "$rounded MB"
        }
        else -> {
            val gb = bytes / (1024.0 * 1024.0 * 1024.0)
            val rounded = (gb * 100).toLong() / 100.0
            "$rounded GB"
        }
    }
}