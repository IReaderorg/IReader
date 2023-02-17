package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import ireader.domain.models.entities.toSavedDownload
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.downloader.DownloaderScreen
import ireader.presentation.ui.settings.downloader.DownloaderTopAppBar
import ireader.presentation.ui.settings.downloader.DownloaderViewModel
import org.koin.androidx.compose.getViewModel

object DownloaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "downloader_route"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader/downloader_route"
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: DownloaderViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)

        IScaffold(
            topBar = {
                DownloaderTopAppBar(
                    onPopBackStack = {
                        controller.navController.popBackStack()
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
                    scrollBehavior = controller.scrollBehavior
                )
            }
        ) { padding ->
            DownloaderScreen(
                onDownloadItem = { item ->
                    controller.navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            bookId = item.bookId
                        )
                    )
                },
                vm = vm,
                snackBarHostState = controller.snackBarHostState,
                paddingValues = padding
            )
        }

    }
}
