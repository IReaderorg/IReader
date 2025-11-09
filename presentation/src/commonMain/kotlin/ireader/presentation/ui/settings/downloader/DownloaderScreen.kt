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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.models.entities.toSavedDownload
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.MR
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
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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
                                        text = "${vm.selection.size} ${localize(MR.strings.selected)}",
                                        style = MaterialTheme.typography.titleLarge
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { vm.selection.clear() }) {
                                    Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = localize(MR.strings.cancel)
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
                                            contentDescription = localize(MR.strings.select_all),
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
                                            contentDescription = localize(MR.strings.delete),
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
                val isDownloading = vm.downloadServiceStateImpl.isEnable
                val hasDownloads = downloads.isNotEmpty()
                
                if (hasDownloads && !vm.hasSelection) {
                    ExtendedFloatingActionButton(
                            text = {
                                MidSizeTextComposable(
                                        text = when (isDownloading) {
                                            true -> localize(MR.strings.pause)
                                            else -> localize(MR.strings.resume)
                                        },
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = {
                                when (isDownloading) {
                                    false -> vm.startDownloadService(vm.downloads.map { it.chapterId })
                                    else -> vm.stopDownloads()
                                }
                            },
                            icon = {
                                Icon(
                                        imageVector = when (isDownloading) {
                                            true -> Icons.Filled.Pause
                                            else -> Icons.Filled.PlayArrow
                                        },
                                        contentDescription = when (isDownloading) {
                                            true -> localize(MR.strings.pause)
                                            else -> localize(MR.strings.resume)
                                        },
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            expanded = scrollState.firstVisibleItemIndex == 0
                    )
                }
            },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab Row
            TabRow(
                selectedTabIndex = vm.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = vm.selectedTab == DownloadTab.ACTIVE,
                    onClick = { vm.selectedTab = DownloadTab.ACTIVE },
                    text = { Text(localize(MR.strings.active_downloads)) }
                )
                Tab(
                    selected = vm.selectedTab == DownloadTab.COMPLETED,
                    onClick = { vm.selectedTab = DownloadTab.COMPLETED },
                    text = { Text(localize(MR.strings.completed_downloads)) }
                )
            }
            
            // Content based on selected tab
            when (vm.selectedTab) {
                DownloadTab.ACTIVE -> ActiveDownloadsContent(
                    downloads = downloads,
                    vm = vm,
                    scrollState = scrollState,
                    onDownloadItem = onDownloadItem,
                    paddingValues = padding
                )
                DownloadTab.COMPLETED -> CompletedDownloadsContent(
                    completedDownloads = vm.downloadServiceStateImpl.completedDownloads,
                    vm = vm,
                    onDownloadItem = onDownloadItem
                )
            }
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
                    text = localize(MR.strings.no_active_downloads),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = localize(MR.strings.no_active_downloads_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
            IVerticalFastScroller(listState = scrollState) {
                LazyColumn(modifier = Modifier.padding(paddingValues), state = scrollState) {
                    // Download speed and ETA header
                    if (vm.downloadServiceStateImpl.isEnable && vm.downloadServiceStateImpl.totalSpeed > 0) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                tonalElevation = 4.dp,
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Download Speed",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = formatSpeed(vm.downloadServiceStateImpl.totalSpeed),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    
                                    // Calculate total ETA from active downloads
                                    val totalEta = vm.downloadServiceStateImpl.downloadProgress.values
                                        .filter { it.estimatedTimeRemaining > 0 }
                                        .maxOfOrNull { it.estimatedTimeRemaining } ?: 0
                                    
                                    if (totalEta > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Estimated Time",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = formatDuration(totalEta),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    items(count = downloads.size) { index ->
                        val progress = vm.downloadServiceStateImpl.downloadProgress[downloads[index].chapterId]
                        val failedDownload = vm.downloadServiceStateImpl.failedDownloads[downloads[index].chapterId]
                        DownloadScreenItem(
                                downloads[index].toSavedDownload(),
                                onClickItem = {
                                    if (vm.selection.isEmpty()) {
                                        onDownloadItem(downloads[index])
                                    } else {
                                        when (vm.downloads[index].chapterId) {
                                            in vm.selection -> {
                                                vm.selection.remove(vm.downloads[index].chapterId)
                                            }
                                            else -> {
                                                vm.selection.add(vm.downloads[index].chapterId)
                                            }
                                        }
                                    }
                                },
                                onLongClickItem = {
                                    vm.selection.add(vm.downloads[index].chapterId)
                                },
                                isSelected = downloads[index].chapterId in vm.selection,
                                inProgress = downloads[index].chapterId in vm.downloadServiceStateImpl.downloads.map { it.chapterId },
                                isDownloaded = downloads[index].isDownloaded,
                                isFailed = failedDownload != null,
                                errorMessage = failedDownload?.errorMessage,
                                retryCount = failedDownload?.retryCount ?: 0,
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
                                onMovePriorityUp = { item ->
                                    vm.moveDownloadUp(downloads[index])
                                },
                                onMovePriorityDown = { item ->
                                    vm.moveDownloadDown(downloads[index])
                                },
                                onRetry = { item ->
                                    vm.retryFailedDownload(item.chapterId)
                                },
                                canMoveUp = index > 0,
                                canMoveDown = index < downloads.size - 1,
                        )
                    }
                }
            }
        }
    }

@Composable
private fun CompletedDownloadsContent(
    completedDownloads: List<ireader.domain.services.downloaderService.CompletedDownload>,
    vm: DownloaderViewModel,
    onDownloadItem: (SavedDownloadWithInfo) -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    if (completedDownloads.isEmpty()) {
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
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Text(
                    text = localize(MR.strings.no_completed_downloads),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = localize(MR.strings.no_completed_downloads_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Clear completed button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${completedDownloads.size} ${localize(MR.strings.completed_downloads)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    androidx.compose.material3.Button(
                        onClick = { vm.clearCompletedDownloads() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(localize(MR.strings.clear_completed))
                    }
                }
            }
            
            // Completed downloads list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = completedDownloads.size) { index ->
                    CompletedDownloadItem(
                        item = completedDownloads[index],
                        onRemove = { vm.removeCompletedDownload(completedDownloads[index].chapterId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedDownloadItem(
    item: ireader.domain.services.downloaderService.CompletedDownload,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        BookListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                
                // Completion timestamp
                Text(
                    text = "${localize(MR.strings.download_completed_at)}: ${formatTimestamp(item.completedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = localize(MR.strings.downloaded),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = localize(MR.strings.delete),
                        tint = MaterialTheme.colorScheme.error
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
        inProgress: Boolean = false,
        isDownloaded: Boolean = false,
        isFailed: Boolean = false,
        errorMessage: String? = null,
        retryCount: Int = 0,
        downloadProgress: ireader.domain.services.downloaderService.DownloadProgress? = null,
        onCancelDownload: (SavedDownload) -> Unit,
        onCancelAllFromThisSeries: (SavedDownload) -> Unit,
        onMovePriorityUp: ((SavedDownload) -> Unit)? = null,
        onMovePriorityDown: ((SavedDownload) -> Unit)? = null,
        onRetry: ((SavedDownload) -> Unit)? = null,
        canMoveUp: Boolean = false,
        canMoveDown: Boolean = false,
) {
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
                
                // Show error message for failed downloads
                if (isFailed && errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Failed: $errorMessage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (retryCount > 0) {
                                Text(
                                    text = "Retry attempts: $retryCount",
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

                if (inProgress && !isDownloaded && !isFailed) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Progress bar
                        val progress = if (downloadProgress != null && downloadProgress.totalBytes > 0) {
                            downloadProgress.bytesDownloaded.toFloat() / downloadProgress.totalBytes.toFloat()
                        } else {
                            0f
                        }
                        
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Speed and ETA info
                        if (downloadProgress != null && downloadProgress.speed > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatSpeed(downloadProgress.speed),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                if (downloadProgress.estimatedTimeRemaining > 0) {
                                    Text(
                                        text = "ETA: ${formatDuration(downloadProgress.estimatedTimeRemaining)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status indicator and actions
            Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority control buttons (only show when not in progress and not downloaded)
                if (!inProgress && !isDownloaded && (onMovePriorityUp != null || onMovePriorityDown != null)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Move up button
                        IconButton(
                            onClick = { onMovePriorityUp?.invoke(item) },
                            enabled = canMoveUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = "Move up in queue",
                                tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Move down button
                        IconButton(
                            onClick = { onMovePriorityDown?.invoke(item) },
                            enabled = canMoveDown,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = "Move down in queue",
                                tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Status icon
                when {
                    isDownloaded -> {
                        Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = localize(MR.strings.downloaded),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                    isFailed -> {
                        Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = "Failed",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                        )
                    }
                    inProgress -> {
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = localize(MR.strings.loading),
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
                                contentDescription = localize(MR.strings.hint),
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
                                text = { MidSizeTextComposable(text = localize(MR.strings.cancel)) }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                                onClick = {
                                    onCancelAllFromThisSeries(item)
                                    isMenuExpanded = false
                                },
                                text = { MidSizeTextComposable(text = localize(MR.strings.cancel_all_for_this_series)) }
                        )
                    }
                }
            }
        }
    }
}


// Helper functions for download screen

private fun formatSpeed(bytesPerSecond: Long): String =
    when {
        bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
        bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
        bytesPerSecond < 1024 * 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSecond / (1024.0 * 1024.0))
        else -> String.format("%.2f GB/s", bytesPerSecond / (1024.0 * 1024.0 * 1024.0))
    }


private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> "${diff / 86_400_000} days ago"
        else -> {
            val date = java.util.Date(timestamp)
            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(date)
        }
    }
}
