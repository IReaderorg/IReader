package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.BookDetailScreen
import ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail.ChapterDetailScreen
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreScreen
import ir.kazemcodes.infinity.feature_library.presentation.LibraryScreen
import ir.kazemcodes.infinity.feature_reader.presentation.reader.ReadingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.AboutSettingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.appearance.AppearanceSettingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.SettingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.dns.DnsOverHttpScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.downloader.DownloaderScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorScreen
import ir.kazemcodes.infinity.feature_settings.presentation.webview.WebPageScreen
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalAnimationApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
    androidx.paging.ExperimentalPagingApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun SetupNavHost(navController: NavHostController) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn()
        },
        exitTransition = {
            fadeOut()
        },
        popEnterTransition = {
            fadeIn()
        },
        popExitTransition = {
            fadeOut()
        }
    ) {
        composable(
            route = Screen.Home.route,
        ) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.Library.route,
        ) {
            LibraryScreen(navController = navController)
        }
        composable(Screen.Extension.route) {
            ExtensionScreen(navController = navController)
        }
        composable(Screen.Setting.route) {
            SettingScreen(navController = navController)
        }
        composable(
            Screen.Explore.route,
            listOf(
                NavigationArgs.exploreType,
                NavigationArgs.sourceId,
            )
        ) {
            ExploreScreen(navController = navController)
        }

        composable(
            Screen.BookDetail.route,
            listOf(
                NavigationArgs.bookId,
                NavigationArgs.sourceId,
            )
        ) {
            BookDetailScreen(navController = navController)
        }



        composable(Screen.WebPage.route,
            listOf(
                NavigationArgs.sourceId,
                NavigationArgs.fetchType,
                navArgument("bookId") {
                    type = NavType.IntType
                    defaultValue = -100
                },
                navArgument("chapterId") {
                    type = NavType.IntType
                    defaultValue = -100
                },
                navArgument("url") {
                    type = NavType.StringType
                    nullable = true
                }

            )) {
            WebPageScreen(navController = navController)
        }
        composable(Screen.ChapterDetail.route, listOf(
            NavigationArgs.sourceId,
            NavigationArgs.bookId,
        )) {
            ChapterDetailScreen(navController = navController)
        }
        composable(
            Screen.ReaderScreen.route,
            listOf(
                NavigationArgs.sourceId,
                NavigationArgs.bookId,
                NavigationArgs.chapterId,
            )
        ) {
            ReadingScreen(navController = navController)
        }
        composable(Screen.Downloader.route) {
            DownloaderScreen(navController = navController)
        }
        composable(Screen.ExtensionCreator.route) {
            ExtensionCreatorScreen(navController = navController)
        }
        composable(Screen.AppearanceSetting.route) {
            AppearanceSettingScreen(navController = navController)
        }
        composable(Screen.DnsOverHttpSetting.route) {
            DnsOverHttpScreen(navController = navController)
        }
        composable(Screen.AboutSetting.route) {
            AboutSettingScreen(navController = navController)
        }
    }
}

object NavigationArgs {
    val bookId = navArgument("bookId") {
        type = NavType.IntType
    }
    val sourceId = navArgument("sourceId") {
        type = NavType.LongType
    }
    val exploreType = navArgument("exploreType") {
        type = NavType.IntType
    }
    val url = navArgument("url") {
        type = NavType.StringType
        nullable = true
    }
    val fetchType = navArgument("fetchType") {
        type = NavType.IntType
    }
    val chapterId = navArgument("chapterId") {
        type = NavType.IntType
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home_route")
    object Library : Screen("library_route")
    object Setting : Screen("setting_screen_route")
    object Extension : Screen("extension_route")
    object Explore : Screen("explore_route/{exploreType}/{sourceId}") {
        fun passArgs(sourceId: Long, exploreType: Int): String {
            return "explore_route/$exploreType/$sourceId"
        }
    }

    object BookDetail : Screen("book_detail_route/{bookId}/{sourceId}") {
        fun passArgs(sourceId: Long, bookId: Int): String {
            return "book_detail_route/$bookId/$sourceId"
        }
    }
    object WebPage :
        Screen("web_page_route/{fetchType}/{sourceId}?chapterId={chapterId}&bookId={bookId}&url={url}") {
        fun passArgs(
            sourceId: Long,
            fetchType: Int,
            url: String? = null,
            bookId: Int? = null,
            chapterId: Int? = null,
        ): String {
            return "web_page_route/$fetchType/$sourceId?chapterId=${chapterId?:-300}&bookId=${bookId?:-300}&url=${URLEncoder.encode(url, StandardCharsets.UTF_8.name())}"
        }
    }

    object ChapterDetail : Screen("chapter_detail_route/{bookId}/{sourceId}") {
        fun passArgs(
            bookId: Int,
            sourceId: Long,
        ): String {
            return "chapter_detail_route/$bookId/$sourceId"
        }
    }

    object ReaderScreen : Screen("reader_screen_route/{bookId}/{chapterId}/{sourceId}") {
        fun passArgs(
            bookId: Int,
            sourceId: Long,
            chapterId: Int,
        ): String {
            return "reader_screen_route/$bookId/$chapterId/$sourceId"
        }
    }

    object Downloader : Screen("downloader_route")
    object ExtensionCreator : Screen("extension_creator_route")
    object AppearanceSetting : Screen("appearance_setting_route")
    object DnsOverHttpSetting : Screen("dnh_over_http_route")
    object AboutSetting : Screen("about_screen_route")
}