package ireader.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.services.common.DownloadProgress
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.DownloadStatus
import ireader.domain.services.common.ServiceState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf

/**
 * Desktop Download Notification Panel
 * 
 * A beautiful floating notification panel that shows download progress
 * when downloads are active. Designed specifically for desktop where
 * system notifications aren't available like on Android.
 */
@Composable
fun DesktopDownloadNotification(
    downloadService: DownloadService,
    modifier: Modifier = Modifier,
    onNavigateToDownloads: () -> Unit = {}
) {
    val serviceState by downloadService.state.collectAsState()
    val downloadProgress by downloadService.downloadProgress.collectAsState()
    val scope = rememberCoroutineScope()
    
    var isExpanded by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }
    
    // Reset dismissed state when new downloads start
    LaunchedEffect(downloadProgress.size) {
        if (downloadProgress.isNotEmpty() && isDismissed) {
            isDismissed = false
        }
    }
    
    val hasActiveDownloads = downloadProgress.isNotEmpty() && 
        downloadProgress.values.any { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
    
    val showNotification = hasActiveDownloads && !isDismissed
    
    AnimatedVisibility(
        visible = showNotification,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        DownloadNotificationPanel(
            serviceState = serviceState,
            downloadProgress = downloadProgress,
            isExpanded = isExpanded,
            onExpandToggle = { isExpanded = !isExpanded },
            onDismiss = { isDismissed = true },
            onNavigateToDownloads = onNavigateToDownloads,
            onPauseAll = { scope.launch { downloadService.pause() } },
            onResumeAll = { scope.launch { downloadService.resume() } }
        )
    }
}


@Composable
private fun DownloadNotificationPanel(
    serviceState: ServiceState,
    downloadProgress: Map<Long, DownloadProgress>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit
) {
    val activeDownloads = downloadProgress.values.filter { 
        it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED 
    }
    val completedCount = downloadProgress.values.count { it.status == DownloadStatus.COMPLETED }
    val failedCount = downloadProgress.values.count { it.status == DownloadStatus.FAILED }
    
    val overallProgress = if (activeDownloads.isNotEmpty()) {
        activeDownloads.map { it.progress }.average().toFloat()
    } else 0f
    
    val currentDownload = activeDownloads.firstOrNull { it.status == DownloadStatus.DOWNLOADING }
        ?: activeDownloads.firstOrNull()
    
    Card(
        modifier = Modifier
            .width(if (isExpanded) 400.dp else 320.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header
            DownloadNotificationHeader(
                activeCount = activeDownloads.size,
                completedCount = completedCount,
                failedCount = failedCount,
                overallProgress = overallProgress,
                isPaused = serviceState == ServiceState.PAUSED,
                isExpanded = isExpanded,
                onExpandToggle = onExpandToggle,
                onDismiss = onDismiss,
                onNavigateToDownloads = onNavigateToDownloads
            )
            
            // Current download info (collapsed view)
            if (!isExpanded && currentDownload != null) {
                CurrentDownloadInfo(
                    download = currentDownload,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Expanded list view
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    DownloadList(
                        downloads = activeDownloads,
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .padding(vertical = 8.dp)
                    )
                    
                    // Action buttons
                    DownloadActions(
                        isPaused = serviceState == ServiceState.PAUSED,
                        onPauseAll = onPauseAll,
                        onResumeAll = onResumeAll,
                        onViewAll = onNavigateToDownloads
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadNotificationHeader(
    activeCount: Int,
    completedCount: Int,
    failedCount: Int,
    overallProgress: Float,
    isPaused: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToDownloads: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated download icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPaused) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Animated download icon
                val infiniteTransition = rememberInfiniteTransition(label = "download")
                val animatedOffset by infiniteTransition.animateFloat(
                    initialValue = -8f,
                    targetValue = 8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "downloadArrow"
                )
                
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Downloading",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = animatedOffset.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isPaused) "Downloads Paused" else "Downloading...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    icon = Icons.Outlined.Download,
                    count = activeCount,
                    color = MaterialTheme.colorScheme.primary
                )
                if (completedCount > 0) {
                    StatusChip(
                        icon = Icons.Outlined.CheckCircle,
                        count = completedCount,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (failedCount > 0) {
                    StatusChip(
                        icon = Icons.Outlined.Error,
                        count = failedCount,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Progress bar
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "${(overallProgress * 100).toInt()}% complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Expand/collapse button
        IconButton(onClick = onExpandToggle) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Close button
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
private fun CurrentDownloadInfo(
    download: DownloadProgress,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = download.bookName.ifEmpty { "Unknown Book" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = download.chapterName.ifEmpty { "Chapter" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Individual progress
        Box(
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { download.progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${(download.progress * 100).toInt()}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DownloadList(
    downloads: List<DownloadProgress>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(downloads, key = { it.chapterId }) { download ->
            DownloadListItem(
                download = download,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun DownloadListItem(
    download: DownloadProgress,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primaryContainer
                        DownloadStatus.QUEUED -> MaterialTheme.colorScheme.surfaceVariant
                        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (download.status) {
                DownloadStatus.DOWNLOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                DownloadStatus.QUEUED -> {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Queued",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DownloadStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                DownloadStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Failed",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = download.bookName.ifEmpty { "Unknown Book" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = download.chapterName.ifEmpty { "Chapter" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (download.status == DownloadStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { download.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            if (download.status == DownloadStatus.FAILED && download.errorMessage != null) {
                Text(
                    text = download.errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        // Progress percentage for downloading items
        if (download.status == DownloadStatus.DOWNLOADING) {
            Text(
                text = "${(download.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DownloadActions(
    isPaused: Boolean,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pause/Resume button
        OutlinedButton(
            onClick = if (isPaused) onResumeAll else onPauseAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPaused) "Resume" else "Pause")
        }
        
        // View all button
        Button(
            onClick = onViewAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = "View All",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View All")
        }
    }
}

/**
 * Wrapper composable that positions the download notification at the bottom-right of the screen
 */
@Composable
fun DesktopDownloadNotificationOverlay(
    downloadService: DownloadService,
    onNavigateToDownloads: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val downloadProgress by downloadService.downloadProgress.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Track completed downloads to show completion toast
    var previousCompletedCount by remember { mutableIntStateOf(0) }
    val currentCompletedCount = downloadProgress.values.count { it.status == DownloadStatus.COMPLETED }
    
    LaunchedEffect(currentCompletedCount) {
        if (currentCompletedCount > previousCompletedCount && previousCompletedCount > 0) {
            val newlyCompleted = currentCompletedCount - previousCompletedCount
            snackbarHostState.showSnackbar(
                message = if (newlyCompleted == 1) "Download completed!" else "$newlyCompleted downloads completed!",
                duration = SnackbarDuration.Short
            )
        }
        previousCompletedCount = currentCompletedCount
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        // Snackbar for completion notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        )
        
        DesktopDownloadNotification(
            downloadService = downloadService,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onNavigateToDownloads = onNavigateToDownloads
        )
    }
}
