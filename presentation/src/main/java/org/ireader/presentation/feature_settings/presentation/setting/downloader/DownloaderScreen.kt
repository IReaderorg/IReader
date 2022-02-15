package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.utils.toast
import org.ireader.presentation.R
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.ui.BookDetailScreenSpec
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloaderScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: DownloaderViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context = context)
                    )
                }
            }
        }
    }
    val downloads = viewModel.savedDownload.collectAsLazyPagingItems()

    val chapters = viewModel.chapters.collectAsLazyPagingItems()
    viewModel.updateChapters(chapters.itemSnapshotList.items)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                actions = {
                    TopAppBarActionButton(
                        imageVector = Icons.Default.FileDownloadOff,
                        title = "Stop Download Icon",
                        onClick = {
                            WorkManager.getInstance(context)
                                .cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
                            context.toast("Downloads were Stopped Successfully")
                        },
                    )
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Menu,
                        title = "Menu Icon",
                        onClick = {
                            viewModel.toggleExpandMenu(enable = true)
                        },
                    )
                    DropdownMenu(
                        modifier = Modifier.background(MaterialTheme.colors.background),
                        expanded = viewModel.state.isMenuExpanded,
                        onDismissRequest = { viewModel.toggleExpandMenu(false) },
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.toggleExpandMenu(false)
                            viewModel.deleteAllDownloads()
                        }) {
                            MidSizeTextComposable(text = stringResource(R.string.delete_all_downloads))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "ArrowBack Icon",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
    ) {
        LazyColumn {
            items(items = downloads) { download ->
                if (download != null) {
                    Timber.e(download.progress.toString())
                    Card(
                        modifier = Modifier,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        onClick = {
                            navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    sourceId = download.sourceId,
                                    bookId = download.id
                                )
                            )
                        }

                    ) {
                        ListItem(
                            modifier = Modifier.height(80.dp),
                            text = { MidSizeTextComposable(text = download.bookName) },
                            trailing = {
                                if (download.totalChapter == download.progress) {
                                    Box {}
                                } else if (download.priority != 0) {
                                    TopAppBarActionButton(imageVector = Icons.Default.StopCircle,
                                        title = "StopDownloads",
                                        onClick = {
                                            viewModel.stopDownloads(context = context,
                                                download.id,
                                                download.sourceId)
                                            viewModel.showSnackBar(UiText.DynamicString("The Download of ${download.bookName} was stopped"))
                                            viewModel.insertSavedDownload(download.copy(priority = 0))
                                        })
                                } else {
                                    if (download.progress == download.totalChapter) {
                                        viewModel.showSnackBar(UiText.DynamicString("The Download of ${download.bookName} was stopped"))
                                        viewModel.insertSavedDownload(download.copy(priority = 0))
                                    }
                                    TopAppBarActionButton(imageVector = Icons.Default.PlayArrow,
                                        title = "Start Download",
                                        onClick = {
                                            viewModel.startDownloadService(context = context,
                                                download.id,
                                                download.sourceId)
                                            viewModel.showSnackBar(UiText.DynamicString("The Download of ${download.bookName} was Started"))
                                            viewModel.insertSavedDownload(download.copy(priority = 1))
                                        })
                                }

                            },
                            secondaryText = {
                                LinearProgressIndicator(modifier = Modifier.padding(top = 8.dp),
                                    progress = ((download.progress * 100) / download.totalChapter).toFloat() / 100)
                            }
                        )
                    }
                }

            }
        }

    }

}

