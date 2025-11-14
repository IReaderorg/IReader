package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.domain.models.entities.toSavedDownload
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.downloader.DownloaderScreen
import ireader.presentation.ui.settings.downloader.DownloaderTopAppBar
import ireader.presentation.ui.settings.downloader.DownloaderViewModel


class DownloaderScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(
    ) {
        val vm: DownloaderViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val snackBarHostState = SnackBarListener(vm)
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
            }
        ) { padding ->
            DownloaderScreen(
                onDownloadItem = { item ->
                    navController.navigate(
                        NavigationRoutes.bookDetail(item.bookId)
                    )
                },
                vm = vm,
                snackBarHostState = snackBarHostState,
                paddingValues = padding
            )
        }

    }
}
