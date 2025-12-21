package ireader.presentation.ui.update

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Modern full-screen app update dialog
 */
@Composable
fun AppUpdateDialog(
    state: AppUpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onRemindLater: () -> Unit,
    onSkipVersion: () -> Unit,
    onDismiss: () -> Unit,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.shouldShowDialog) return
    
    Dialog(
        onDismissRequest = { if (!state.isDownloading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !state.isDownloading,
            dismissOnClickOutside = !state.isDownloading,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    state.isDownloading -> DownloadingContent(
                        progress = state.downloadProgress,
                        onCancel = onCancelDownload,
                    )
                    state.isDownloaded -> DownloadedContent(
                        onInstall = onInstall,
                        onDismiss = onDismiss,
                    )
                    else -> UpdateAvailableContent(
                        state = state,
                        onDownload = onDownload,
                        onRemindLater = onRemindLater,
                        onSkipVersion = onSkipVersion,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    state: AppUpdateState,
    onDownload: () -> Unit,
    onRemindLater: () -> Unit,
    onSkipVersion: () -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Animated icon
    val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.icon))
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.scale)
    )
    
    // Update icon with gradient background
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size((40 * scale).dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    // Title
    Text(
        text = localizeHelper.localize(Res.string.new_update_available),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Version info
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VersionChip(
            version = state.currentVersion,
            label = localizeHelper.localize(Res.string.current),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        
        VersionChip(
            version = state.newVersion,
            label = localizeHelper.localize(Res.string.add_as_new),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
    }
    
    // Release notes
    if (state.releaseNotes.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NewReleases,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.whats_new_1),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    Text(
                        text = state.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    )
                }
            }
        }
    }
    
    // APK size info
    state.apkAsset?.let { asset ->
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Storage,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Download size: ${formatFileSize(asset.size)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Download button
    Button(
        onClick = onDownload,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizeHelper.localize(Res.string.download_install),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Secondary actions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onRemindLater,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = localizeHelper.localize(Res.string.later),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        
        TextButton(
            onClick = onSkipVersion,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
        ) {
            Text(
                text = localizeHelper.localize(Res.string.skip_version),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DownloadingContent(
    progress: Float,
    onCancel: () -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = localizeHelper.localize(Res.string.progress_1)
    )
    
    // Animated download icon
    val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.download_1))
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.offsety)
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .offset { IntOffset(0, offsetY.dp.roundToPx()) },
            tint = MaterialTheme.colorScheme.primary,
        )
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    Text(
        text = localizeHelper.localize(Res.string.downloading_update_1),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "${(animatedProgress * 100).toInt()}% complete",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Progress bar
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(localizeHelper.localize(Res.string.cancel))
    }
}

@Composable
private fun DownloadedContent(
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Success animation
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF8BC34A),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White,
        )
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    Text(
        text = localizeHelper.localize(Res.string.download_complete),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = localizeHelper.localize(Res.string.ready_to_install_the_new_version),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onInstall,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
        ),
    ) {
        Icon(
            imageVector = Icons.Default.InstallMobile,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizeHelper.localize(Res.string.install_now),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    TextButton(
        onClick = onDismiss,
        modifier = Modifier.height(44.dp),
    ) {
        Text(
            text = localizeHelper.localize(Res.string.install_later),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VersionChip(
    version: String,
    label: String,
    containerColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = version.removePrefix("v"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
