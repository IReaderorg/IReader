package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.downloader.DownloaderScreen
import org.ireader.downloader.DownloaderViewModel

object DownloaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "downloader_route"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader/downloader_route"
        }
    )

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm : DownloaderViewModel = hiltViewModel()
       DownloaderScreen(navController = navController,
       onDownloadItem = { item ->
           navController.navigate(
               BookDetailScreenSpec.buildRoute(
                   sourceId = item.sourceId,
                   bookId = item.bookId
               )
           )
       },
           vm = vm
       )
    }

}