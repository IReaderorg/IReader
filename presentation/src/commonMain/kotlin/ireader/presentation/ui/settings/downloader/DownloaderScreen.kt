package ireader.presentation.ui.settings.downloader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.reorderable.detectReorder
import ireader.presentation.ui.component.reorderable.draggedItem
import ireader.presentation.ui.component.reorderable.rememberReorderLazyListState
import ireader.presentation.ui.component.reorderable.reorderable

/**
 * Modern Downloader Screen with Spotify-style bottom player bar, borderless cards,
 * drag-and-drop priority reordering, animated download buttons, and real book cover integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val activeDownload by vm.activeDownload.collectAsState()
    val bookCovers by vm.bookCovers.collectAsState()
    val isPausedDueToNetwork by vm.isPausedDueToNetwork.collectAsState()
    val isPausedDueToDiskSpace by vm.isPausedDueToDiskSpace.collectAsState()
    val isWifiOnly by vm.isWifiOnlyMode.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val showPlayerBar = queue.isNotEmpty() || activeDownload != null || isRunning || isPaused

    val reorderState = rememberReorderLazyListState(
        onMove = { from, to ->
            vm.reorder(from.index, to.index)
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Warning Banners (Network / Disk)
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

            // Main Content: Borderless Reorderable Queue List or Clean Empty State
            if (queue.isEmpty() && !isRunning && activeDownload == null) {
                EmptyDownloadState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    state = reorderState.listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .reorderable(reorderState),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 10.dp,
                        bottom = if (showPlayerBar) 100.dp else 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = queue,
                        key = { it.chapterId }
                    ) { item ->
                        val isSelected = item.chapterId in vm.selection
                        val isCurrentlyDownloading = item.chapterId == activeDownload?.chapterId && isRunning && !isPaused
                        val cover = bookCovers[item.bookId]?.cover ?: item.coverUrl

                        DownloadItemCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggedItem(reorderState.offsetByKey(item.chapterId)),
                            download = item,
                            coverUrl = cover,
                            isSelected = isSelected,
                            isCurrentlyDownloading = isCurrentlyDownloading,
                            reorderModifier = Modifier.detectReorder(reorderState),
                            onClick = {
                                if (vm.hasSelection) {
                                    vm.toggleSelection(item.chapterId)
                                } else {
                                    onNavigateToBook(item.bookId)
                                }
                            },
                            onLongClick = {
                                vm.toggleSelection(item.chapterId)
                            },
                            onDownloadImmediately = {
                                vm.downloadImmediately(item.chapterId)
                            },
                            onRetry = { vm.retryDownload(item.chapterId) },
                            onRemove = { vm.removeDownload(item.chapterId) }
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // Spotify-Like Bottom Player Bar (Fixed at bottom)
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showPlayerBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val activeCover = activeDownload?.let { bookCovers[it.bookId]?.cover ?: it.coverUrl }
            SpotifyDownloadPlayerBar(
                activeDownload = activeDownload,
                coverUrl = activeCover,
                isRunning = isRunning,
                isPaused = isPaused,
                stats = stats,
                onTogglePlayPause = { vm.togglePlayPause() },
                onSkip = { vm.skipCurrent() },
                onOpenFullPlayer = { showFullPlayer = true }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Expandable Full-Screen Player Sheet
    // ═══════════════════════════════════════════════════════════════
    if (showFullPlayer) {
        val activeCover = activeDownload?.let { bookCovers[it.bookId]?.cover ?: it.coverUrl }
        ModalBottomSheet(
            onDismissRequest = { showFullPlayer = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            FullPlayerSheetContent(
                activeDownload = activeDownload,
                coverUrl = activeCover,
                isRunning = isRunning,
                isPaused = isPaused,
                stats = stats,
                isWifiOnly = isWifiOnly,
                onTogglePlayPause = { vm.togglePlayPause() },
                onSkip = { vm.skipCurrent() },
                onRetryAll = { vm.retryAllFailed() },
                onCancelAll = { vm.cancelAllDownloads() },
                onClearCompleted = { vm.clearCompleted() },
                onToggleWifiOnly = { vm.setWifiOnlyMode(!isWifiOnly) },
                onDismiss = { showFullPlayer = false }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Spotify-Style Mini-Player Bar
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun SpotifyDownloadPlayerBar(
    activeDownload: Download?,
    coverUrl: String?,
    isRunning: Boolean,
    isPaused: Boolean,
    stats: DownloaderViewModel.DownloadStats,
    onTogglePlayPause: () -> Unit,
    onSkip: () -> Unit,
    onOpenFullPlayer: () -> Unit
) {
    val progressFloat = (activeDownload?.progress ?: 0) / 100f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenFullPlayer() },
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hairline Live Progress Indicator along the top edge
            if (isRunning && !isPaused) {
                if (progressFloat > 0f) {
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Square
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            } else if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }

            // Player Bar Content Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Book Cover Thumbnail
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (!coverUrl.isNullOrBlank()) {
                        IImageLoader(
                            model = coverUrl,
                            contentDescription = activeDownload?.bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Title and Chapter Subtitle (Track Info)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = activeDownload?.bookTitle?.ifEmpty { "Downloads" } ?: "Downloads",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val statusSubtext = when {
                        isPaused -> "Paused"
                        isRunning && activeDownload != null -> {
                            val pr = if (activeDownload.progress > 0) "${activeDownload.progress}% • " else ""
                            "$pr${stats.queued + 1} remaining"
                        }
                        stats.queued > 0 -> "${stats.queued} queued"
                        stats.completed > 0 -> "${stats.completed} downloaded"
                        else -> "Idle"
                    }

                    Text(
                        text = if (activeDownload != null && activeDownload.chapterName.isNotEmpty()) {
                            "${activeDownload.chapterName} • $statusSubtext"
                        } else {
                            statusSubtext
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaused) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Queue Count Pill
                if (stats.queued > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = "${stats.queued}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Skip / Cancel Current Button
                if (isRunning || isPaused || stats.queued > 0) {
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Chapter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Spotify-Style Play / Pause Circular Button
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isPaused) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (isPaused) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning && !isPaused) "Pause" else "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Full Player Sheet (Expandable Dialog / Sheet)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun FullPlayerSheetContent(
    activeDownload: Download?,
    coverUrl: String?,
    isRunning: Boolean,
    isPaused: Boolean,
    stats: DownloaderViewModel.DownloadStats,
    isWifiOnly: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkip: () -> Unit,
    onRetryAll: () -> Unit,
    onCancelAll: () -> Unit,
    onClearCompleted: () -> Unit,
    onToggleWifiOnly: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
            }
            Text(
                text = "NOW DOWNLOADING",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onCancelAll) {
                Icon(Icons.Outlined.DeleteSweep, contentDescription = "Cancel All", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Large Book Cover Card with real artwork
        Surface(
            modifier = Modifier
                .size(width = 150.dp, height = 210.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            if (!coverUrl.isNullOrBlank()) {
                IImageLoader(
                    model = coverUrl,
                    contentDescription = activeDownload?.bookTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Book & Chapter Titles
        Text(
            text = activeDownload?.bookTitle?.ifEmpty { "Download Queue" } ?: "Download Queue",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = activeDownload?.chapterName?.ifEmpty { "No active chapter" } ?: "No active chapter",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp))

        // Live Progress Indicator & Stats Text
        val progressVal = (activeDownload?.progress ?: 0) / 100f
        LinearProgressIndicator(
            progress = { progressVal },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${activeDownload?.progress ?: 0}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${stats.completed} done • ${stats.queued} queued",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        // Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Retry Failed
            IconButton(
                onClick = onRetryAll,
                enabled = stats.failed > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry Failed",
                    tint = if (stats.failed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            // Big Play/Pause Main Button
            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning && !isPaused) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip to Next
            IconButton(
                onClick = onSkip,
                enabled = stats.queued > 0
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip to Next",
                    tint = if (stats.queued > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(12.dp))

        // Quick Settings: WiFi Only & Clear Finished
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Download only on Wi-Fi", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = isWifiOnly,
                onCheckedChange = { onToggleWifiOnly() }
            )
        }

        if (stats.completed > 0) {
            TextButton(
                onClick = onClearCompleted,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear completed (${stats.completed})")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Modern Borderless Download Item Card with Drag Handle
// ═══════════════════════════════════════════════════════════════════════════
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DownloadItemCard(
    modifier: Modifier = Modifier,
    download: Download,
    coverUrl: String?,
    isSelected: Boolean,
    isCurrentlyDownloading: Boolean,
    reorderModifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDownloadImmediately: () -> Unit,
    onRetry: () -> Unit,
    onRemove: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            isCurrentlyDownloading -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200)
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = animatedBg,
        tonalElevation = if (isCurrentlyDownloading) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag Handle for Priority Reordering
            Box(
                modifier = reorderModifier
                    .size(28.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder priority",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Book Cover Thumbnail (46x62dp)
            Surface(
                modifier = Modifier
                    .size(width = 46.dp, height = 62.dp)
                    .clip(RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                if (!coverUrl.isNullOrBlank()) {
                    IImageLoader(
                        model = coverUrl,
                        contentDescription = download.bookTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Chapter & Book Information Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = download.chapterName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = download.bookTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Status or Progress Display
                if (download.status == DownloadStatus.DOWNLOADING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { download.progress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${download.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val (statusText, statusColor) = when (download.status) {
                        DownloadStatus.DOWNLOADED -> "Downloaded" to MaterialTheme.colorScheme.primary
                        DownloadStatus.ERROR -> (download.errorMessage ?: "Failed") to MaterialTheme.colorScheme.error
                        DownloadStatus.QUEUE -> "In Queue (Priority #${download.priority})" to MaterialTheme.colorScheme.onSurfaceVariant
                        else -> "Queued" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right-side Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Animated Download Action Button
                AnimatedDownloadActionButton(
                    status = download.status,
                    progress = download.progress,
                    onManualDownload = onDownloadImmediately,
                    onRetry = onRetry
                )

                // Cancel / Remove Button
                if (download.status == DownloadStatus.QUEUE || download.status == DownloadStatus.DOWNLOADING) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Animated Download Button with State Transitions
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun AnimatedDownloadActionButton(
    status: DownloadStatus,
    progress: Int,
    onManualDownload: () -> Unit,
    onRetry: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Smooth subtle bounce animation for active download arrow
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    when (status) {
        DownloadStatus.DOWNLOADING -> {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Downloading",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(14.dp)
                        .offset(y = arrowOffset.dp)
                )
            }
        }

        DownloadStatus.DOWNLOADED -> {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DownloadStatus.ERROR -> {
            IconButton(
                onClick = onRetry,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry Download",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DownloadStatus.QUEUE, DownloadStatus.NOT_DOWNLOADED -> {
            // Interactive Manual Download Button
            IconButton(
                onClick = onManualDownload,
                modifier = Modifier.size(36.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download Now",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        else -> {
            IconButton(
                onClick = onManualDownload,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Empty State
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun EmptyDownloadState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Downloads in Queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Chapters queued for offline reading will appear here with live progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Network & Disk Warning Banners
// ═══════════════════════════════════════════════════════════════════════════
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    text = "Waiting for Wi-Fi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Wi-Fi only mode is active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(
                onClick = onAllowMobileData,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    text = "Free up storage to continue downloading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
