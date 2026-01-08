package ireader.presentation.ui.settings.downloader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller

/**
 * Modern Download Queue Screen with Mihon-style design.
 */
@Composable
fun DownloaderScreen(
    modifier: Modifier = Modifier,
    vm: DownloaderViewModel,
    onNavigateToBook: (bookId: Long) -> Unit,
    padding: PaddingValues,
    scrollState: LazyListState
) {
    val queue by vm.downloadQueue.collectAsState()
    val stats by vm.stats.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val isPaused by vm.isPaused.collectAsState()
    val isPausedDueToNetwork by vm.isPausedDueToNetwork.collectAsState()
    val isPausedDueToDiskSpace by vm.isPausedDueToDiskSpace.collectAsState()
    val currentDownload by vm.currentDownload.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Warning Banners
        AnimatedVisibility(
            visible = isPausedDueToNetwork,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            NetworkWarningBanner(
                onAllowMobileData = { vm.allowMobileDataTemporarily() },
                onDisableWifiOnly = { vm.setWifiOnlyMode(false) }
            )
        }
        
        AnimatedVisibility(
            visible = isPausedDueToDiskSpace,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            DiskSpaceWarningBanner()
        }
        
        // Stats Header
        if (queue.isNotEmpty() || stats.completed > 0 || stats.failed > 0) {
            DownloadStatsHeader(
                stats = stats,
                isRunning = isRunning,
                isPaused = isPaused,
                onStart = { vm.startDownloads() },
                onPauseResume = {
                    if (isPaused) vm.resumeDownloads() else vm.pauseDownloads()
                },
                onCancelAll = { vm.cancelAllDownloads() },
                onRetryFailed = { vm.retryAllFailed() },
                onClearCompleted = { vm.clearCompleted() }
            )
        }
        
        // Content
        if (queue.isEmpty() && stats.total == 0) {
            EmptyDownloadState(modifier = Modifier.weight(1f))
        } else {
            DownloadQueueList(
                queue = queue,
                currentDownload = currentDownload,
                selection = vm.selection.toSet(),
                onItemClick = { download ->
                    if (vm.hasSelection) {
                        vm.toggleSelection(download.chapterId)
                    } else {
                        onNavigateToBook(download.bookId)
                    }
                },
                onItemLongClick = { download ->
                    vm.toggleSelection(download.chapterId)
                },
                onRetry = { vm.retryDownload(it.chapterId) },
                onRemove = { vm.removeDownload(it.chapterId) },
                scrollState = scrollState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NetworkWarningBanner(
    onAllowMobileData: () -> Unit,
    onDisableWifiOnly: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SignalWifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Waiting for WiFi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "WiFi-only mode is enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(
                onClick = onAllowMobileData,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Use Data")
            }
        }
    }
}

@Composable
private fun DiskSpaceWarningBanner() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Low Disk Space",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Free up space to continue downloads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun DownloadStatsHeader(
    stats: DownloaderViewModel.DownloadStats,
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onCancelAll: () -> Unit,
    onRetryFailed: () -> Unit,
    onClearCompleted: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.Download,
                    count = stats.downloading,
                    label = "Active",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Outlined.Schedule,
                    count = stats.queued,
                    label = "Queued",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    icon = Icons.Outlined.DownloadDone,
                    count = stats.completed,
                    label = "Done",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = Icons.Default.Error,
                    count = stats.failed,
                    label = "Failed",
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start button (when not running)
                if (!isRunning && !isPaused && stats.queued > 0) {
                    FilledTonalButton(
                        onClick = onStart,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Start")
                    }
                }
                
                // Pause/Resume button
                if (isRunning || isPaused) {
                    FilledTonalButton(
                        onClick = onPauseResume,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isPaused) "Resume" else "Pause")
                    }
                }
                
                // Cancel All button
                if (stats.hasActiveDownloads) {
                    OutlinedButton(
                        onClick = onCancelAll,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
                
                // Retry Failed button
                if (stats.failed > 0) {
                    FilledTonalButton(
                        onClick = onRetryFailed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
                
                // Clear Completed button
                if (stats.completed > 0 && !stats.hasActiveDownloads) {
                    OutlinedButton(
                        onClick = onClearCompleted,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyDownloadState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No Downloads",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Downloaded chapters will appear here.\nGo to a book and tap download to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DownloadQueueList(
    queue: List<Download>,
    currentDownload: Download?,
    selection: Set<Long>,
    onItemClick: (Download) -> Unit,
    onItemLongClick: (Download) -> Unit,
    onRetry: (Download) -> Unit,
    onRemove: (Download) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    // Group downloads by book for better organization
    val groupedDownloads by remember(queue) {
        derivedStateOf {
            queue.groupBy { it.bookId }
        }
    }
    
    IVerticalFastScroller(listState = scrollState) {
        LazyColumn(
            state = scrollState,
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            groupedDownloads.forEach { (bookId, downloads) ->
                val bookTitle = downloads.firstOrNull()?.bookTitle ?: "Unknown"
                
                // Book header
                item(key = "header_$bookId") {
                    BookGroupHeader(
                        bookTitle = bookTitle,
                        downloadCount = downloads.size,
                        activeCount = downloads.count { it.status == DownloadStatus.DOWNLOADING }
                    )
                }
                
                // Chapter items
                items(
                    items = downloads,
                    key = { it.chapterId }
                ) { download ->
                    val isCurrentDownload = currentDownload?.chapterId == download.chapterId
                    DownloadItemCard(
                        download = download,
                        isSelected = download.chapterId in selection,
                        isCurrentDownload = isCurrentDownload,
                        onClick = { onItemClick(download) },
                        onLongClick = { onItemLongClick(download) },
                        onRetry = { onRetry(download) },
                        onRemove = { onRemove(download) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookGroupHeader(
    bookTitle: String,
    downloadCount: Int,
    activeCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bookTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (activeCount > 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = if (activeCount > 0) "$activeCount/$downloadCount" else "$downloadCount",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = if (activeCount > 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadItemCard(
    download: Download,
    isSelected: Boolean,
    isCurrentDownload: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRetry: () -> Unit,
    onRemove: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isCurrentDownload -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "backgroundColor"
    )
    
    val progress by animateFloatAsState(
        targetValue = download.progressFloat,
        label = "progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentDownload) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                DownloadStatusIcon(
                    status = download.status,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(Modifier.width(12.dp))
                
                // Chapter info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.chapterName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Status text
                    Text(
                        text = getStatusText(download),
                        style = MaterialTheme.typography.bodySmall,
                        color = getStatusColor(download.status),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Action buttons
                when (download.status) {
                    DownloadStatus.ERROR -> {
                        IconButton(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DownloadStatus.QUEUE, DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = onRemove) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }
            
            // Progress bar for downloading items
            if (download.status == DownloadStatus.DOWNLOADING) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun DownloadStatusIcon(
    status: DownloadStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        DownloadStatus.DOWNLOADING -> {
            CircularProgressIndicator(
                modifier = modifier,
                strokeWidth = 2.dp
            )
        }
        DownloadStatus.DOWNLOADED -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = modifier
            )
        }
        DownloadStatus.ERROR -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Failed",
                tint = MaterialTheme.colorScheme.error,
                modifier = modifier
            )
        }
        DownloadStatus.QUEUE, DownloadStatus.NOT_DOWNLOADED -> {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = "Queued",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }

        else -> {

        }
    }
}

@Composable
private fun getStatusText(download: Download): String {
    return when (download.status) {
        DownloadStatus.DOWNLOADING -> "${download.progress}%"
        DownloadStatus.DOWNLOADED -> "Completed"
        DownloadStatus.ERROR -> download.errorMessage ?: "Failed"
        DownloadStatus.QUEUE -> "Waiting..."
        DownloadStatus.NOT_DOWNLOADED -> "Pending"
        else -> "Pending"
    }
}

@Composable
private fun getStatusColor(status: DownloadStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
        DownloadStatus.DOWNLOADED -> MaterialTheme.colorScheme.tertiary
        DownloadStatus.ERROR -> MaterialTheme.colorScheme.error
        DownloadStatus.QUEUE, DownloadStatus.NOT_DOWNLOADED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
