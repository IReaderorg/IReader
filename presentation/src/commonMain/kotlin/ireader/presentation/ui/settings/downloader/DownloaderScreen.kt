package ireader.presentation.ui.settings.downloader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import ireader.presentation.ui.component.reusable_composable.BuildDropDownMenu
import ireader.presentation.ui.component.reusable_composable.DropDownMenuItem
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
        IVerticalFastScroller(listState = scrollState) {
            LazyColumn(modifier = Modifier.padding(padding), state = scrollState) {
                items(count = downloads.size) { index ->
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
                            onCancelAllFromThisSeries = { item ->
                                vm.deleteSelectedDownloads(
                                        vm.downloads.filter { it.bookId == item.bookId }
                                                .map { it.toSavedDownload() }
                                )
                            },
                            onCancelDownload = { item ->
                                vm.deleteSelectedDownloads(list = listOf(item))
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
        inProgress: Boolean = false,
        isDownloaded: Boolean = false,
        onCancelDownload: (SavedDownload) -> Unit,
        onCancelAllFromThisSeries: (SavedDownload) -> Unit,
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

                if (inProgress && !isDownloaded) {
                    LinearProgressIndicator(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Status indicator and actions
            Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
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
