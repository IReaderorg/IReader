package ireader.presentation.ui.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ireader.domain.models.download.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Enhanced download queue screen following Mihon's DownloadManager pattern
 */
class DownloadQueueScreen : Screen, KoinComponent {
    
    private val screenModel: DownloadQueueScreenModel by inject()
    
    @Composable
    override fun Content() {
        val state by screenModel.state.collectAsState()
        
        DownloadQueueContent(
            state = state,
            onSelectDownload = screenModel::selectDownload,
            onSelectAll = screenModel::selectAllDownloads,
            onClearSelection = screenModel::clearSelection,
            onPauseDownload = screenModel::pauseDownload,
            onResumeDownload = screenModel::resumeDownload,
            onCancelDownload = screenModel::cancelDownload,
            onRetryDownload = screenModel::retryDownload,
            onPauseAll = screenModel::pauseAllDownloads,
            onResumeAll = screenModel::resumeAllDownloads,
            onCancelAll = screenModel::cancelAllDownloads,
            onClearCompleted = screenModel::clearCompleted,
            onClearFailed = screenModel::clearFailed,
            onReorderQueue = screenModel::reorderQueue,
            onUpdateFilter = screenModel::updateFilterStatus,
            onUpdateSort = screenModel::updateSortOrder,
            onToggleShowCompleted = screenModel::toggleShowCompleted,
            onToggleShowFailed = screenModel::toggleShowFailed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadQueueContent(
    state: DownloadQueueScreenModel.State,
    onSelectDownload: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onPauseDownload: (Long) -> Unit,
    onResumeDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onRetryDownload: (Long) -> Unit,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onCancelAll: () -> Unit,
    onClearCompleted: () -> Unit,
    onClearFailed: () -> Unit,
    onReorderQueue: (List<Long>) -> Unit,
    onUpdateFilter: (DownloadStatus?) -> Unit,
    onUpdateSort: (DownloadQueueScreenModel.DownloadSortOrder) -> Unit,
    onToggleShowCompleted: () -> Unit,
    onToggleShowFailed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                actions = {
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pause All") },
                            onClick = {
                                onPauseAll()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Resume All") },
                            onClick = {
                                onResumeAll()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancel All") },
                            onClick = {
                                onCancelAll()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Clear Completed") },
                            onClick = {
                                onClearCompleted()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.ClearAll, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Failed") },
                            onClick = {
                                onClearFailed()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Download statistics card
            DownloadStatsCard(
                stats = state.downloadStats,
                modifier = Modifier.padding(16.dp)
            )
            
            // Filter and sort bar
            DownloadFilterBar(
                filterStatus = state.filterStatus,
                sortOrder = state.sortOrder,
                showCompleted = state.showCompleted,
                showFailed = state.showFailed,
                onUpdateFilter = onUpdateFilter,
                onUpdateSort = onUpdateSort,
                onToggleShowCompleted = onToggleShowCompleted,
                onToggleShowFailed = onToggleShowFailed
            )
            
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    val filteredDownloads = state.downloadQueue // Apply filtering logic
                    
                    if (filteredDownloads.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No downloads",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredDownloads) { download ->
                                DownloadItem(
                                    download = download,
                                    isSelected = download.chapterId in state.selectedDownloads,
                                    onSelect = { onSelectDownload(download.chapterId) },
                                    onPause = { onPauseDownload(download.chapterId) },
                                    onResume = { onResumeDownload(download.chapterId) },
                                    onCancel = { onCancelDownload(download.chapterId) },
                                    onRetry = { onRetryDownload(download.chapterId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadStatsCard(
    stats: DownloadStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DownloadStatItem(
                label = "Queued",
                value = stats.queuedDownloads.toString(),
                icon = Icons.Default.Queue
            )
            DownloadStatItem(
                label = "Downloading",
                value = stats.downloadingCount.toString(),
                icon = Icons.Default.Download
            )
            DownloadStatItem(
                label = "Completed",
                value = stats.completedDownloads.toString(),
                icon = Icons.Default.CheckCircle
            )
            DownloadStatItem(
                label = "Failed",
                value = stats.failedDownloads.toString(),
                icon = Icons.Default.Error
            )
        }
    }
}

@Composable
private fun DownloadStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DownloadFilterBar(
    filterStatus: DownloadStatus?,
    sortOrder: DownloadQueueScreenModel.DownloadSortOrder,
    showCompleted: Boolean,
    showFailed: Boolean,
    onUpdateFilter: (DownloadStatus?) -> Unit,
    onUpdateSort: (DownloadQueueScreenModel.DownloadSortOrder) -> Unit,
    onToggleShowCompleted: () -> Unit,
    onToggleShowFailed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showCompleted,
                onClick = onToggleShowCompleted,
                label = { Text("Show Completed") }
            )
            
            FilterChip(
                selected = showFailed,
                onClick = onToggleShowFailed,
                label = { Text("Show Failed") }
            )
            
            // Status filter dropdown
            var showStatusMenu by remember { mutableStateOf(false) }
            
            FilterChip(
                selected = filterStatus != null,
                onClick = { showStatusMenu = true },
                label = { 
                    Text(filterStatus?.name ?: "All Status") 
                },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            
            DropdownMenu(
                expanded = showStatusMenu,
                onDismissRequest = { showStatusMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Status") },
                    onClick = {
                        onUpdateFilter(null)
                        showStatusMenu = false
                    }
                )
                DownloadStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.name) },
                        onClick = {
                            onUpdateFilter(status)
                            showStatusMenu = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sort dropdown
        Row {
            var showSortMenu by remember { mutableStateOf(false) }
            
            FilterChip(
                selected = false,
                onClick = { showSortMenu = true },
                label = { Text("Sort: ${sortOrder.name}") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                DownloadQueueScreenModel.DownloadSortOrder.values().forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.name) },
                        onClick = {
                            onUpdateSort(order)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadItem(
    download: DownloadItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = download.bookTitle,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = download.chapterTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Status: ${download.status.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons
                Row {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                            }
                        }
                        DownloadStatus.PAUSED, DownloadStatus.QUEUED -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(onClick = onRetry) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                        else -> {}
                    }
                    
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
            }
            
            // Progress bar for downloading items
            if (download.status == DownloadStatus.DOWNLOADING && download.progress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = download.progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(download.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatSpeed(download.speed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Error message for failed downloads
            if (download.status == DownloadStatus.FAILED && download.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Error: ${download.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatSpeed(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
        else -> "${(bytesPerSecond / (1024 * 1024)).toInt()} MB/s"
    }
}