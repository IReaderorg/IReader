package ireader.presentation.ui.settings.downloader

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.models.entities.toSavedDownload
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.BookListItem
import ireader.presentation.ui.component.BookListItemColumn
import ireader.presentation.ui.component.BookListItemSubtitle
import ireader.presentation.ui.component.BookListItemTitle
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.modifier.selectedBackground
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
        modifier: Modifier = Modifier,
        vm: DownloaderViewModel,
        onDownloadItem: (
                item:
                SavedDownloadWithInfo
        ) -> Unit,
        snackBarHostState: SnackbarHostState,
        paddingValues: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scrollState = rememberLazyListState()
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                            event.uiText.asString(localizeHelper)
                    )
                }
                else -> {}
            }
        }
    }
    val downloads = vm.downloads

    Scaffold(
            modifier = Modifier,
            topBar = {
                if (vm.hasSelection) {
                    TopAppBar(
                            title = {
                                Text(
                                        text = "${vm.selection.size} ${localize(Res.string.selected)}",
                                        style = MaterialTheme.typography.titleLarge
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { vm.selection.clear() }) {
                                    Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = localize(Res.string.cancel)
                                    )
                                }
                            },
                            actions = {
                                // Select all button
                                IconButton(
                                        onClick = {
                                            if (vm.selection.size == downloads.size) {
                                                vm.selection.clear()
                                            } else {
                                                vm.selection.clear()
                                                vm.selection.addAll(downloads.map { it.chapterId })
                                            }
                                        }
                                ) {
                                    Icon(
                                            imageVector = Icons.Filled.SelectAll,
                                            contentDescription = localize(Res.string.select_all),
                                            tint = if (vm.selection.size == downloads.size) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                    )
                                }
                                
                                // Delete selected button
                                IconButton(
                                        onClick = {
                                            val selectedDownloads = downloads
                                                    .filter { it.chapterId in vm.selection }
                                                    .map { it.toSavedDownload() }
                                            vm.deleteSelectedDownloads(selectedDownloads)
                                            vm.selection.clear()
                                        }
                                ) {
                                    Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = localize(Res.string.delete),
                                            tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            },
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
            floatingActionButton = {
                // Collect state reactively
                val serviceState by vm.downloadServiceState.collectAsState()
                val isRunning = serviceState == ireader.domain.services.common.ServiceState.RUNNING
                val isPaused = serviceState == ireader.domain.services.common.ServiceState.PAUSED
                val hasDownloads = downloads.isNotEmpty()
                
                if (hasDownloads && !vm.hasSelection) {
                    ExtendedFloatingActionButton(
                            text = {
                                MidSizeTextComposable(
                                        text = when {
                                            isRunning && !isPaused -> localize(Res.string.pause)
                                            isPaused -> localize(Res.string.resume)
                                            else -> localize(Res.string.start)
                                        },
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = {
                                when {
                                    isRunning && !isPaused -> vm.pauseDownloads()
                                    isPaused -> vm.resumeDownloads()
                                    else -> vm.startDownloadService(vm.downloads.map { it.chapterId })
                                }
                            },
                            icon = {
                                Icon(
                                        imageVector = when {
                                            isRunning && !isPaused -> Icons.Filled.Pause
                                            else -> Icons.Filled.PlayArrow
                                        },
                                        contentDescription = when {
                                            isRunning && !isPaused -> localize(Res.string.pause)
                                            isPaused -> localize(Res.string.resume)
                                            else -> localize(Res.string.start)
                                        },
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            expanded = scrollState.firstVisibleItemIndex == 0,
                        shape =  RoundedCornerShape(12)
                    )
                }
            },
    ) { padding ->
        // Collect state reactively for the content area
        val serviceState by vm.downloadServiceState.collectAsState()
        val progressMap by vm.downloadServiceProgress.collectAsState()
        val isRunning = serviceState == ireader.domain.services.common.ServiceState.RUNNING
        val isPaused = serviceState == ireader.domain.services.common.ServiceState.PAUSED
        
        Column(modifier = Modifier.padding(padding)) {
            // Download status header
            if (isRunning || isPaused) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    tonalElevation = 4.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = if (isPaused) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeCount = progressMap.values
                            .count { it.status == ireader.domain.services.common.DownloadStatus.DOWNLOADING }
                        val completedCount = progressMap.values
                            .count { it.status == ireader.domain.services.common.DownloadStatus.COMPLETED }
                        val totalCount = progressMap.size
                        
                        Column {
                            Text(
                                text = if (isPaused) {
                                    localize(Res.string.resume)
                                } else {
                                    localize(Res.string.downloading)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaused) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                            
                            Text(
                                text = "$completedCount/$totalCount completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPaused) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                        
                        if (!isPaused && activeCount > 0) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Content
            ActiveDownloadsContent(
                downloads = downloads,
                vm = vm,
                scrollState = scrollState,
                onDownloadItem = onDownloadItem,
                paddingValues = padding
            )
        }
    }
}

@Composable
private fun ActiveDownloadsContent(
    downloads: List<SavedDownloadWithInfo>,
    vm: DownloaderViewModel,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onDownloadItem: (SavedDownloadWithInfo) -> Unit,
    paddingValues: PaddingValues
) {
    // Collect progress map reactively so items update when download status changes
    val progressMap by vm.downloadServiceProgress.collectAsState()
    
    if (downloads.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Text(
                    text = localize(Res.string.no_active_downloads),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = localize(Res.string.no_active_downloads_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
            IVerticalFastScroller(listState = scrollState) {
                LazyColumn(modifier = Modifier.padding(paddingValues), state = scrollState) {
                    items(
                        count = downloads.size,
                        key = { index -> downloads[index].chapterId }
                    ) { index ->
                        val download = downloads[index]
                        val progress = progressMap[download.chapterId]
                        DownloadScreenItem(
                                download.toSavedDownload(),
                                onClickItem = {
                                    if (vm.selection.isEmpty()) {
                                        onDownloadItem(download)
                                    } else {
                                        when (download.chapterId) {
                                            in vm.selection -> {
                                                vm.selection.remove(download.chapterId)
                                            }
                                            else -> {
                                                vm.selection.add(download.chapterId)
                                            }
                                        }
                                    }
                                },
                                onLongClickItem = {
                                    vm.selection.add(download.chapterId)
                                },
                                isSelected = download.chapterId in vm.selection,
                                downloadProgress = progress,
                                onCancelAllFromThisSeries = { item ->
                                    vm.deleteSelectedDownloads(
                                            vm.downloads.filter { it.bookId == item.bookId }
                                                    .map { it.toSavedDownload() }
                                    )
                                },
                                onCancelDownload = { item ->
                                    vm.deleteSelectedDownloads(list = listOf(item))
                                },
                                onRetry = { item ->
                                    vm.retryFailedDownload(item.chapterId)
                                },
                        )
                    }
                }
            }
        }
    }



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadScreenItem(
        item: SavedDownload,
        onClickItem: (SavedDownload) -> Unit,
        onLongClickItem: (SavedDownload) -> Unit,
        isSelected: Boolean = false,
        downloadProgress: ireader.domain.services.common.DownloadProgress? = null,
        onCancelDownload: (SavedDownload) -> Unit,
        onCancelAllFromThisSeries: (SavedDownload) -> Unit,
        onRetry: ((SavedDownload) -> Unit)? = null,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val status = downloadProgress?.status ?: ireader.domain.services.common.DownloadStatus.QUEUED
    val isDownloading = status == ireader.domain.services.common.DownloadStatus.DOWNLOADING
    val isCompleted = status == ireader.domain.services.common.DownloadStatus.COMPLETED
    val isFailed = status == ireader.domain.services.common.DownloadStatus.FAILED
    val isPaused = status == ireader.domain.services.common.DownloadStatus.PAUSED
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }

    Surface(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            tonalElevation = if (isSelected) 12.dp else 2.dp,
            shape = MaterialTheme.shapes.medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
    ) {
        BookListItem(
                modifier = Modifier
                        .combinedClickable(
                                onClick = {
                                    onClickItem(item)
                                },
                                onLongClick = {
                                    onLongClickItem(item)
                                }
                        )
                        .selectedBackground(isSelected)
                        .fillMaxWidth()
                        .padding(16.dp),
        ) {
            BookListItemColumn(
                    modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookListItemTitle(
                        text = item.bookName,
                        maxLines = 2,
                        fontWeight = FontWeight.SemiBold
                )
                BookListItemSubtitle(
                        text = item.chapterName
                )
                
                // Show status information
                when {
                    isFailed && downloadProgress?.errorMessage != null -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Failed: ${downloadProgress.errorMessage}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (downloadProgress.retryCount > 0) {
                                    Text(
                                        text = "Retry attempts: ${downloadProgress.retryCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            if (onRetry != null) {
                                Button(
                                    onClick = { onRetry(item) },
                                    modifier = Modifier.padding(start = 8.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Retry", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    
                    isDownloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = downloadProgress?.progress ?: 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "Downloading... ${(downloadProgress?.progress?.times(100))?.toInt() ?: 0}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    isPaused -> {
                        Text(
                            text = localizeHelper.localize(Res.string.paused),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    isCompleted -> {
                        Text(
                            text = localizeHelper.localize(Res.string.downloaded),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Status indicator and actions
            Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                when {
                    isCompleted -> {
                        Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = localize(Res.string.downloaded),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                    isFailed -> {
                        Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = localizeHelper.localize(Res.string.failed),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                    isDownloading -> {
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                        )
                    }
                    isPaused -> {
                        Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = localizeHelper.localize(Res.string.paused),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                    // Queued - show queue icon (no action, just status)
                    else -> {
                        Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = localizeHelper.localize(Res.string.queued),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Action button with menu
                Box {
                    IconButton(
                            onClick = { isMenuExpanded = !isMenuExpanded },
                            modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = localize(Res.string.hint),
                                tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    ireader.presentation.ui.component.components.IDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                    ) {
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                                onClick = {
                                    onCancelDownload(item)
                                    isMenuExpanded = false
                                },
                                text = { MidSizeTextComposable(text = localize(Res.string.cancel)) }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                                onClick = {
                                    onCancelAllFromThisSeries(item)
                                    isMenuExpanded = false
                                },
                                text = { MidSizeTextComposable(text = localize(Res.string.cancel_all_for_this_series)) }
                        )
                    }
                }
            }
        }
    }
}



