package ireader.presentation.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Download progress state for external resources
 */
data class ExternalResourceDownloadProgress(
    val downloaded: Long = 0,
    val total: Long = 0,
    val status: String = "",
    val phase: DownloadPhase = DownloadPhase.IDLE
) {
    val progress: Float
        get() = if (total > 0) downloaded.toFloat() / total else 0f
    
    val progressPercent: Int
        get() = (progress * 100).toInt()
}

enum class DownloadPhase {
    IDLE,
    CHECKING,
    DOWNLOADING,
    EXTRACTING,
    COMPLETE,
    ERROR
}

/**
 * Information about an external resource that needs to be downloaded
 */
data class ExternalResourceInfo(
    val name: String,
    val description: String,
    val downloadSize: String,
    val sourceUrl: String,
    val requiredFor: String,
    val icon: @Composable () -> Unit = { Icon(Icons.Default.CloudDownload, null) }
)

/**
 * A reusable dialog for downloading external resources.
 * Shows download progress, handles errors, and provides a clean UX.
 */
@Composable
fun ExternalResourceDownloadDialog(
    resourceInfo: ExternalResourceInfo,
    isDownloaded: Boolean,
    downloadProgress: ExternalResourceDownloadProgress,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = onDownload,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = {
            if (downloadProgress.phase != DownloadPhase.DOWNLOADING && 
                downloadProgress.phase != DownloadPhase.EXTRACTING) {
                onDismiss()
            }
        },
        modifier = modifier,
        icon = {
            when (downloadProgress.phase) {
                DownloadPhase.COMPLETE -> Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                DownloadPhase.ERROR -> Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                else -> resourceInfo.icon()
            }
        },
        title = {
            Text(
                text = when (downloadProgress.phase) {
                    DownloadPhase.IDLE -> "Download Required"
                    DownloadPhase.CHECKING -> "Checking..."
                    DownloadPhase.DOWNLOADING -> "Downloading..."
                    DownloadPhase.EXTRACTING -> "Extracting..."
                    DownloadPhase.COMPLETE -> "Download Complete"
                    DownloadPhase.ERROR -> "Download Failed"
                },
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (downloadProgress.phase) {
                    DownloadPhase.IDLE -> {
                        // Show resource info
                        ResourceInfoSection(resourceInfo)
                    }
                    
                    DownloadPhase.CHECKING, DownloadPhase.DOWNLOADING, DownloadPhase.EXTRACTING -> {
                        // Show progress
                        DownloadProgressSection(
                            resourceName = resourceInfo.name,
                            progress = downloadProgress
                        )
                    }
                    
                    DownloadPhase.COMPLETE -> {
                        Text(
                            text = "${resourceInfo.name} has been downloaded successfully. You can now use this feature.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    DownloadPhase.ERROR -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Failed to download ${resourceInfo.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (downloadProgress.status.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = downloadProgress.status,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (downloadProgress.phase) {
                DownloadPhase.IDLE -> {
                    Button(onClick = onDownload) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Download")
                    }
                }
                DownloadPhase.COMPLETE -> {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
                DownloadPhase.ERROR -> {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
                else -> {
                    // No confirm button during download
                }
            }
        },
        dismissButton = {
            when (downloadProgress.phase) {
                DownloadPhase.IDLE -> {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                DownloadPhase.ERROR -> {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
                DownloadPhase.COMPLETE -> {
                    // No dismiss button when complete
                }
                else -> {
                    // Can't dismiss during download
                }
            }
        }
    )
}

@Composable
private fun ResourceInfoSection(resourceInfo: ExternalResourceInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = resourceInfo.description,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "Resource", value = resourceInfo.name)
                InfoRow(label = "Download Size", value = resourceInfo.downloadSize)
                InfoRow(label = "Required For", value = resourceInfo.requiredFor)
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Downloaded from official source",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DownloadProgressSection(
    resourceName: String,
    progress: ExternalResourceDownloadProgress
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = resourceName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        when (progress.phase) {
            DownloadPhase.CHECKING -> {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text(
                    text = "Checking download...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DownloadPhase.DOWNLOADING -> {
                LinearProgressIndicator(
                    progress = { progress.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = progress.status.ifEmpty { "Downloading..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${progress.progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (progress.total > 0) {
                    Text(
                        text = "${formatFileSize(progress.downloaded)} / ${formatFileSize(progress.total)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DownloadPhase.EXTRACTING -> {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text(
                    text = "Extracting files...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {}
        }
    }
}

/**
 * A banner that shows when an external resource is required but not downloaded.
 * Can be placed at the top of a screen to prompt the user to download.
 */
@Composable
fun ExternalResourceRequiredBanner(
    resourceInfo: ExternalResourceInfo,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isDownloaded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${resourceInfo.name} Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = resourceInfo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Download (${resourceInfo.downloadSize})")
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> "${(gb * 100).toLong() / 100.0} GB"
        mb >= 1.0 -> "${(mb * 100).toLong() / 100.0} MB"
        kb >= 1.0 -> "${(kb * 100).toLong() / 100.0} KB"
        else -> "$bytes B"
    }
}
