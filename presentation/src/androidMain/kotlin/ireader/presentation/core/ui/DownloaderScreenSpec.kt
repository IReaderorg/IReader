package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.toSavedDownload
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.downloader.DownloaderScreen
import ireader.presentation.ui.settings.downloader.DownloaderTopAppBar
import ireader.presentation.ui.settings.downloader.DownloaderViewModel


class DownloaderScreenSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
    ) {
        val vm: DownloaderViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow
        val snackBarHostState = SnackBarListener(vm)
        IScaffold(
            topBar = { scrollBehavior ->
            DownloaderTopAppBar(
                    onPopBackStack = {
                        popBackStack(navigator)
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
                    navigator.push(
                        BookDetailScreenSpec(
                            bookId = item.bookId
                        )
                    )
                },
                vm = vm,
                snackBarHostState = snackBarHostState,
                paddingValues = padding
            )
        }

    }
}
