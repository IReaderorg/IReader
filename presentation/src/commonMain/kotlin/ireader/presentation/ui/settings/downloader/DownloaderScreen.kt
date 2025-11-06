package ireader.presentation.ui.settings.downloader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                        text = {
                            MidSizeTextComposable(
                                    text = when (vm.downloadServiceStateImpl.isEnable) {
                                        true -> localize(MR.strings.pause)
                                        else -> localize(MR.strings.resume)
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
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                )
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
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
                }
                val list =
                        listOf<DropDownMenuItem>(
                                DropDownMenuItem(
                                        localize(MR.strings.cancel)
                                ) {
                                    onCancelDownload(item)
                                },
                                DropDownMenuItem(
                                        localize(MR.strings.cancel_all_for_this_series)
                                ) {
                                    onCancelAllFromThisSeries(item)
                                }
                        )
                BuildDropDownMenu(list, onExpand = {
                    isMenuExpanded
                })
            }
        }
    }
}
