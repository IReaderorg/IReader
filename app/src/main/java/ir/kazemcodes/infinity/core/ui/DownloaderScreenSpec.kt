package ir.kazemcodes.infinity.core.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.feature_settings.presentation.setting.downloader.DownloaderScreen

object DownloaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "downloader_route"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader.com/downloader_route"
        }
    )

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState
    ) {
        DownloaderScreen(navController=navController)
    }

}