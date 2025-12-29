package ireader.presentation.ui.settings.cloudflare

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.DownloadPhase
import ireader.presentation.ui.component.ExternalResourceDownloadDialog
import ireader.presentation.ui.component.ExternalResourceDownloadProgress
import ireader.presentation.ui.component.ExternalResourceInfo
import ireader.presentation.ui.component.ExternalResourceRequiredBanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder for FlareSolverr download
 */
class FlareSolverrDownloadState {
    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(ExternalResourceDownloadProgress())
    val downloadProgress: StateFlow<ExternalResourceDownloadProgress> = _downloadProgress.asStateFlow()
    
    private val _showDownloadDialog = MutableStateFlow(false)
    val showDownloadDialog: StateFlow<Boolean> = _showDownloadDialog.asStateFlow()
    
    fun setDownloaded(downloaded: Boolean) {
        _isDownloaded.value = downloaded
    }
    
    fun updateProgress(progress: ExternalResourceDownloadProgress) {
        _downloadProgress.value = progress
    }
    
    fun showDialog() {
        _showDownloadDialog.value = true
    }
    
    fun hideDialog() {
        _showDownloadDialog.value = false
    }
    
    fun reset() {
        _downloadProgress.value = ExternalResourceDownloadProgress()
    }
}

/**
 * Get FlareSolverr resource info based on platform
 */
fun getFlareSolverrResourceInfo(): ExternalResourceInfo {
    val os = System.getProperty("os.name")?.lowercase() ?: "unknown"
    val arch = System.getProperty("os.arch")?.lowercase() ?: "unknown"
    
    val (platform, size) = when {
        os.contains("win") -> "Windows x64" to "~600 MB"
        os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm")) -> "macOS ARM64" to "~400 MB"
        os.contains("mac") -> "macOS x64" to "~400 MB"
        os.contains("linux") -> "Linux x64" to "~450 MB"
        else -> "Unknown" to "~500 MB"
    }
    
    return ExternalResourceInfo(
        name = "FlareSolverr",
        description = "FlareSolverr is required to bypass Cloudflare protection on some sources. It runs a browser locally to solve challenges automatically.",
        downloadSize = size,
        sourceUrl = "https://github.com/FlareSolverr/FlareSolverr/releases",
        requiredFor = "Cloudflare-protected sources",
        icon = { Icon(Icons.Outlined.Security, contentDescription = null) }
    )
}

/**
 * Dialog for downloading FlareSolverr
 */
@Composable
fun FlareSolverrDownloadDialog(
    state: FlareSolverrDownloadState,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDialog by state.showDownloadDialog.collectAsState()
    val isDownloaded by state.isDownloaded.collectAsState()
    val progress by state.downloadProgress.collectAsState()
    
    if (showDialog) {
        ExternalResourceDownloadDialog(
            resourceInfo = getFlareSolverrResourceInfo(),
            isDownloaded = isDownloaded,
            downloadProgress = progress,
            onDownload = onDownload,
            onDismiss = {
                state.hideDialog()
                onDismiss()
            },
            modifier = modifier
        )
    }
}

/**
 * Banner shown when FlareSolverr is required but not downloaded
 */
@Composable
fun FlareSolverrRequiredBanner(
    state: FlareSolverrDownloadState,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloaded by state.isDownloaded.collectAsState()
    val progress by state.downloadProgress.collectAsState()
    
    ExternalResourceRequiredBanner(
        resourceInfo = getFlareSolverrResourceInfo(),
        isDownloaded = isDownloaded,
        isDownloading = progress.phase == DownloadPhase.DOWNLOADING || progress.phase == DownloadPhase.EXTRACTING,
        downloadProgress = progress.progress,
        onDownloadClick = {
            state.showDialog()
            onDownloadClick()
        },
        modifier = modifier
    )
}

/**
 * Card showing FlareSolverr status with download option
 */
@Composable
fun FlareSolverrStatusCard(
    state: FlareSolverrDownloadState,
    isServerRunning: Boolean,
    onDownloadClick: () -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloaded by state.isDownloaded.collectAsState()
    val progress by state.downloadProgress.collectAsState()
    val resourceInfo = getFlareSolverrResourceInfo()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isServerRunning -> MaterialTheme.colorScheme.primaryContainer
                isDownloaded -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Security,
                    contentDescription = null,
                    tint = when {
                        isServerRunning -> MaterialTheme.colorScheme.onPrimaryContainer
                        isDownloaded -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FlareSolverr",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = when {
                            isServerRunning -> "Running"
                            isDownloaded -> "Downloaded - Not running"
                            progress.phase == DownloadPhase.DOWNLOADING -> "Downloading... ${progress.progressPercent}%"
                            progress.phase == DownloadPhase.EXTRACTING -> "Extracting..."
                            else -> "Not downloaded"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Progress bar during download
            if (progress.phase == DownloadPhase.DOWNLOADING) {
                LinearProgressIndicator(
                    progress = { progress.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    !isDownloaded && progress.phase == DownloadPhase.IDLE -> {
                        Button(
                            onClick = {
                                state.showDialog()
                                onDownloadClick()
                            },
                            modifier = Modifier.weight(1f)
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
                    isDownloaded && !isServerRunning -> {
                        Button(
                            onClick = onStartServer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Server")
                        }
                    }
                    isServerRunning -> {
                        OutlinedButton(
                            onClick = onStopServer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop Server")
                        }
                    }
                }
            }
        }
    }
}
