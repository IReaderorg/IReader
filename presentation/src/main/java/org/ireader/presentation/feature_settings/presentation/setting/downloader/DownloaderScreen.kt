package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.utils.toast
import org.ireader.domain.services.downloaderService.DownloadService
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
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
    val downloads = viewModel.savedDownload


    Scaffold(
        topBar = {
            DownloaderTopAppBar(
                navController = navController,
                onStopAllDownload = {
                    WorkManager.getInstance(context)
                        .cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
                    context.toast("Downloads were Stopped Successfully")
                },
                onMenuIcon = {
                    viewModel.toggleExpandMenu(enable = true)
                },
                onDeleteAllDownload = {
                    viewModel.deleteAllDownloads()
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
    ) { padding ->
        LazyColumn {
            items(count = downloads.size) { index ->
                val download = downloads[index]
                if (downloads != null) {
                    Timber.e(downloads[index].progress.toString())
                    Card(
                        modifier = Modifier,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        onClick = {
                            navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    sourceId = downloads[index].sourceId,
                                    bookId = downloads[index].bookId
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
                                    AppIconButton(imageVector = Icons.Default.StopCircle,
                                        title = "StopDownloads",
                                        onClick = {
                                            viewModel.stopDownloads(context = context,
                                                download.bookId,
                                                download.sourceId)
                                            viewModel.showSnackBar(UiText.DynamicString("The Download of ${download.bookName} was stopped"))
                                            viewModel.insertSavedDownload(download.copy(priority = 0))
                                        })
                                } else {
                                    if (download.progress == download.totalChapter) {
                                        viewModel.showSnackBar(UiText.DynamicString("The Download of ${download.bookName} was stopped"))
                                        viewModel.insertSavedDownload(download.copy(priority = 0))
                                    }
                                    AppIconButton(imageVector = Icons.Default.PlayArrow,
                                        title = "Start Download",
                                        onClick = {
                                            viewModel.startDownloadService(context = context,
                                                download.bookId,
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

