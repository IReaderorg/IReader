package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.presentation.feature_settings.presentation.webview.WebPageScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WebViewScreenSpec : ScreenSpec {

    override val navHostRoute: String =
        "web_page_route/{url}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument("url") {
            type = NavType.StringType
            defaultValue = "No_Url"
        }
    )

    fun buildRoute(
        url: String? = null,
    ): String {
        return "web_page_route/${
            URLEncoder.encode(url,
                StandardCharsets.UTF_8.name())
        }"
    }


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        WebPageScreen(navController = navController)
    }

}