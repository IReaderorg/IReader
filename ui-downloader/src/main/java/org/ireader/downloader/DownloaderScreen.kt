package org.ireader.downloader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_models.entities.SavedDownload
import org.ireader.common_models.entities.SavedDownloadWithInfo
import org.ireader.common_models.entities.toSavedDownload
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.BookListItem
import org.ireader.components.BookListItemColumn
import org.ireader.components.BookListItemSubtitle
import org.ireader.components.BookListItemTitle
import org.ireader.components.reusable_composable.BuildDropDownMenu
import org.ireader.components.reusable_composable.DropDownMenuItem
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.modifier.selectedBackground
import org.ireader.ui_downloader.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    modifier: Modifier = Modifier,
    vm: DownloaderViewModel,
    onDownloadItem: (
        item:
        SavedDownloadWithInfo
    ) -> Unit,
    snackBarHostState:SnackbarHostState
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context = context)
                    )
                }
            }
        }
    }
    val downloads = vm.downloads

    androidx.compose.material3.Scaffold(
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        floatingActionButton = {
            androidx.compose.material.ExtendedFloatingActionButton(
                text = {
                    MidSizeTextComposable(
                        text = when (vm.downloadServiceStateImpl.isEnable) {
                            true -> UiText.StringResource(R.string.pause)
                            else -> UiText.StringResource(R.string.resume)
                        },
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                onClick = {
                    when (vm.downloadServiceStateImpl.isEnable) {
                        false -> vm.startDownloadService(vm.downloads.map { it.chapterId })
                        else -> vm.stopDownloads()
                    }
                },
                icon = {
                    when (vm.downloadServiceStateImpl.isEnable) {
                        true -> Icon(
                            Icons.Filled.Pause,
                            "",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                        else -> Icon(
                            Icons.Filled.PlayArrow,
                            "",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                },
                contentColor = MaterialTheme.colorScheme.onSecondary,
                backgroundColor = MaterialTheme.colorScheme.secondary,
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(count = downloads.size) { index ->
                DownloadScreenItem(
                    downloads[index].toSavedDownload(),
                    onClickItem = {
                        if (vm.selection.isEmpty()) {
                            onDownloadItem(downloads[index])
//                            navController.navigate(
//                                BookDetailScreenSpec.buildRoute(
//                                    sourceId = downloads[index].sourceId,
//                                    bookId = downloads[index].bookId
//                                )
//                            )
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
                        vm.deleteSelectedDownloads(vm.downloads.filter { it.bookId == item.bookId }
                            .map { it.toSavedDownload() })
                    },
                    onCancelDownload = { item ->
                        vm.deleteSelectedDownloads(list = listOf(item))
                    },
                )
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
            .height(80.dp)
            .fillMaxWidth()
            .padding(end = 4.dp),
    ) {
        BookListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            BookListItemTitle(
                text = item.bookName,
                maxLines = 2,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                BookListItemSubtitle(
                    text = item.chapterName
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (inProgress && !isDownloaded) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        if (isDownloaded) {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(imageVector = Icons.Outlined.CheckCircleOutline, contentDescription = "")
            }
        } else {
            Column {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
                }
                val list =
                    listOf<DropDownMenuItem>(
                        DropDownMenuItem(
                            UiText.StringResource(R.string.cancel)
                        ) {
                            onCancelDownload(item)
                        },
                        DropDownMenuItem(
                            UiText.StringResource(R.string.cancel_all_for_this_series)
                        ) {
                            onCancelAllFromThisSeries(item)
                        }
                    )
                BuildDropDownMenu(list, enable = isMenuExpanded, onEnable = { isMenuExpanded = it })
            }
        }
    }
}
