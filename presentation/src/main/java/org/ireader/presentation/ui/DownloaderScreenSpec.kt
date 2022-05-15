package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
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
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: DownloaderViewModel = hiltViewModel(navBackStackEntry)
        DownloaderScreen(
            onDownloadItem = { item ->
                navController.navigate(
                    BookDetailScreenSpec.buildRoute(
                        sourceId = item.sourceId,
                        bookId = item.bookId
                    )
                )
            },
            vm = vm,
            snackBarHostState = snackBarHostState
        )
    }
    @OptIn(ExperimentalMaterialApi::class)
    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: DownloaderViewModel = hiltViewModel(navBackStackEntry)
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
                vm.deleteSelectedDownloads(vm.downloads.filter { it.chapterId in vm.selection }
                    .map { it.toSavedDownload() })
                vm.selection.clear()
            }
        )
    }

}
