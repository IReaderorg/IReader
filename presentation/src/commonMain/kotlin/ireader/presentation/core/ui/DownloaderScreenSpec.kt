package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.toSavedDownload
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.pause
import ireader.i18n.resources.resume
import ireader.i18n.resources.start
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.downloader.DownloaderScreen
import ireader.presentation.ui.settings.downloader.DownloaderTopAppBar
import ireader.presentation.ui.settings.downloader.DownloaderViewModel


object DownloaderScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
    ) {
        val vm: DownloaderViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val snackBarHostState = SnackBarListener(vm)
        // Collect state at the top level so it's always observed regardless of UI structure
        val serviceState by vm.downloadServiceState.collectAsState()
        val progressMap by vm.downloadServiceProgress.collectAsState()
        val isRunning = serviceState == ireader.domain.services.common.ServiceState.RUNNING
        val isPaused = serviceState == ireader.domain.services.common.ServiceState.PAUSED
        val downloads = vm.downloads
        val scrollState = rememberLazyListState()
        IScaffold(
            topBar = { scrollBehavior ->
            DownloaderTopAppBar(
                    onPopBackStack = {
                        navController.popBackStack()
                    },
                    onCancelAll = {
                        vm.deleteSelectedDownloads(vm.downloads.map { it.toSavedDownload() })
                    },
                    onMenuIcon = {
                        vm.toggleExpandMenu(enable = true)
                    },
                    onDeleteAllDownload = {
                        vm.deleteAllDownloads()
                    },
                    state = vm,
                    onDelete = {
                        vm.deleteSelectedDownloads(
                            vm.downloads.filter { it.chapterId in vm.selection }
                                .map { it.toSavedDownload() }
                        )
                        vm.selection.clear()
                    },
                    scrollBehavior = scrollBehavior
                )
            },floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
            floatingActionButton = {
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
            Box(modifier = Modifier.padding(padding)) {
                DownloaderScreen(
                    onDownloadItem = { item ->
                        navController.navigate(
                            NavigationRoutes.bookDetail(item.bookId)
                        )
                    },
                    vm = vm,
                    snackBarHostState = snackBarHostState,
                    padding = padding,
                    downloads = downloads,
                    scrollState = scrollState
                )
            }

        }

    }
}
