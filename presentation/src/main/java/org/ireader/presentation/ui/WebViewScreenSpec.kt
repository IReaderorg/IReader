package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.core.utils.Constants
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.feature_settings.presentation.webview.WebPageScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WebViewScreenSpec : ScreenSpec {

    override val navHostRoute: String =
        "web_page_route/{fetchType}/{sourceId}/{chapterId}/{bookId}/{url}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.sourceId,
        NavigationArgs.fetchType,
        navArgument("bookId") {
            type = NavType.IntType
            defaultValue = Constants.NULL_VALUE
        },
        navArgument("chapterId") {
            type = NavType.IntType
            defaultValue = Constants.NULL_VALUE
        },
        navArgument("url") {
            type = NavType.StringType
            defaultValue = "No_Url"
        }
    )

    fun buildRoute(
        sourceId: Long,
        fetchType: Int,
        url: String? = null,
        bookId: Int? = null,
        chapterId: Int? = null,
    ): String {
        return "web_page_route/$fetchType/$sourceId/${chapterId ?: Constants.NULL_VALUE}/${bookId ?: Constants.NULL_VALUE}/${
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