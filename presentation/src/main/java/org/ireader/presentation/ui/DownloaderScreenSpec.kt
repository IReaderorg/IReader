package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import org.ireader.Controller
import org.ireader.common_models.entities.toSavedDownload
import org.ireader.downloader.DownloaderScreen
import org.ireader.downloader.DownloaderTopAppBar
import org.ireader.downloader.DownloaderViewModel

object DownloaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "downloader_route"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader/downloader_route"
        }
    )

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: DownloaderViewModel = hiltViewModel(controller.navBackStackEntry)
        DownloaderScreen(
            onDownloadItem = { item ->
                controller.navController.navigate(
                    BookDetailScreenSpec.buildRoute(
                        sourceId = item.sourceId,
                        bookId = item.bookId
                    )
                )
            },
            vm = vm,
            snackBarHostState = controller.snackBarHostState
        )
    }
    @OptIn(ExperimentalMaterialApi::class)
    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: DownloaderViewModel = hiltViewModel(controller.navBackStackEntry)
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
}
