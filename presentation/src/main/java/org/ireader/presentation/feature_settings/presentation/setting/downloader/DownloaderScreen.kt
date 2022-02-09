package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.ui.BookDetailScreenSpec

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
    val books = viewModel.book.collectAsLazyPagingItems()
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
                    IconButton(
                        onClick = {
                            WorkManager.getInstance(context)
                                .cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
                            context.toast("Downloads were Stopped Successfully")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownloadOff,
                            contentDescription = "Stop Download Icon",
                            tint = MaterialTheme.colors.onBackground
                        )
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
            items(items = books) { book ->
                if (book != null) {
                    Card(
                        modifier = Modifier.padding(vertical = 8.dp),
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        onClick = {
                            navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    sourceId = book.sourceId,
                                    bookId = book.id
                                )
                            )
                        }

                    ) {
                        ListItem(
                            text = { MidSizeTextComposable(title = book.bookName) },
                            trailing = {
                                if (book.beingDownloaded) {
                                    TopAppBarActionButton(imageVector = Icons.Default.StopCircle,
                                        title = "StopDownloads",
                                        onClick = {
                                            viewModel.stopDownloads(context = context,
                                                book.id,
                                                book.sourceId)
                                            viewModel.showSnackBar(UiText.DynamicString("The Download of ${book.bookName} was stopped"))
                                        })
                                } else {
                                    TopAppBarActionButton(imageVector = Icons.Default.PlayArrow,
                                        title = "Start Download",
                                        onClick = {
                                            viewModel.startDownloadService(context = context,
                                                book.id,
                                                book.sourceId)
                                            viewModel.showSnackBar(UiText.DynamicString("The Download of ${book.bookName} was Started"))
                                        })
                                }

                            },
                            secondaryText = {
                                if (book.id == viewModel.state.value.downloadBookId) {
                                    LinearProgressIndicator(modifier = Modifier.padding(top = 8.dp),
                                        progress = viewModel.state.value.progress / 100)
                                }

                            }
                        )
                    }
                }

            }
        }

    }

}