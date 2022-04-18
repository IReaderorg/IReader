package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import org.ireader.core.utils.UiEvent
import org.ireader.core_ui.modifier.selectedBackground
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.models.entities.toSavedDownload
import org.ireader.domain.utils.toast
import org.ireader.presentation.R
import org.ireader.presentation.feature_history.*
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloaderScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: DownloaderViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context = context)
                    )
                }
            }
        }
    }
    val downloads = vm.downloads


    Scaffold(
        topBar = {
            DownloaderTopAppBar(
                onPopBackStack = { navController.popBackStack() },
                onCancelAll = {
                    vm.stopDownloads()
                    context.toast("Downloads were Stopped Successfully")
                },
                onMenuIcon = {
                    vm.toggleExpandMenu(enable = true)
                },
                onDeleteAllDownload = {
                    vm.deleteAllDownloads()
                },
                state = vm,
                onDelete = {
                    vm.deleteSelectedDownloads(vm.downloads.filter { it.chapterId in vm.selection }.map { it.toSavedDownload() })
                    vm.selection.clear()
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    MidSizeTextComposable(
                        text = when (vm.downloadServiceStateImpl.isEnable) {
                            true -> stringResource(R.string.pause)
                            else -> stringResource(R.string.resume)
                        },
                        color = Color.White
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
                        true -> Icon(Icons.Filled.Pause,
                            "",
                            tint = MaterialTheme.colors.onSecondary)
                        else -> Icon(Icons.Filled.PlayArrow,
                            "",
                            tint = MaterialTheme.colors.onSecondary)
                    }
                }
            )
        },
    ) { padding ->
        LazyColumn {
            items(count = downloads.size) { index ->
                DownloadScreenItem(
                    downloads[index].toSavedDownload(),
                    onClickItem = {
                        if (vm.selection.isEmpty()) {
                            navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    sourceId = downloads[index].sourceId,
                                    bookId = downloads[index].bookId
                                )
                            )
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
                        vm.deleteSelectedDownloads(vm.downloads.filter { it.bookId == item.bookId }.map { it.toSavedDownload() })
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
            Column(modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
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
                            "Cancel"
                        ) {
                            onCancelDownload(item)
                        },
                        DropDownMenuItem(
                            "Cancel all for this series"
                        ) {
                            onCancelAllFromThisSeries(item)
                        })
                BuildDropDownMenu(list, enable = isMenuExpanded, onEnable = { isMenuExpanded = it })
            }
        }


    }
}


@Composable
fun BuildDropDownMenu(
    items: List<DropDownMenuItem>,
    enable: Boolean = false,
    onEnable: (Boolean) -> Unit = {},
) {

    DropdownMenu(
        modifier = Modifier.background(MaterialTheme.colors.surface),
        expanded = enable,
        onDismissRequest = {
            onEnable(false)
        },
    ) {
        items.forEachIndexed { _, item ->
            DropdownMenuItem(onClick = {
                item.onItem()
                onEnable(false)
            }) {
                MidSizeTextComposable(text = item.text)
            }
        }
    }
}

data class DropDownMenuItem(
    val text: String,
    val onItem: () -> Unit,
)
