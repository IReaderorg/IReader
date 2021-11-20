package ir.kazemcodes.infinity.base_feature.navigation.home_screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.kazemcodes.infinity.base_feature.navigation.BottomNavigationScreens
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.domain.util.decodeUrl
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.ChapterDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_BOOK_TITLE
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_BOOK_URL
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_CHAPTERS_DETAIL
import ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen.BrowseScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.ReadingScreen
import ir.kazemcodes.infinity.library_feature.presentation.screen.library_screen.LibraryScreen
import ir.kazemcodes.infinity.presentation.screen.BookDetailScreen
import ir.kazemcodes.infinity.presentation.screen.components.WebPageScreen
import ir.kazemcodes.infinity.presentation.screen.setting_screen.SettingScreen
import java.lang.reflect.Type

@ExperimentalMaterialApi
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = BottomNavigationScreens.Library.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {


        composable(
            BottomNavigationScreens.Library.route,
        ) {
            LibraryScreen(
                navController = navController
            )
        }
        composable(
            BottomNavigationScreens.Browse.route
        ) {
            BrowseScreen(navController = navController)
        }
        composable(
            BottomNavigationScreens.Setting.route
        ) {
            SettingScreen()
        }
        composable(
            Routes.BookDetailScreen + "?bookTitle={bookTitle}&bookUrl={bookUrl}",
            arguments = listOf(navArgument("bookUrl") {
                type = NavType.StringType
                defaultValue = ""
            },
                navArgument("bookTitle") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            BookDetailScreen(book = Book.create().apply {
                name = backStackEntry.arguments?.getString(PARAM_BOOK_TITLE) ?: ""
                link = backStackEntry.arguments?.getString(PARAM_BOOK_URL) ?: ""
            }, navController = navController)
        }
        composable(
            route = Routes.ReadingScreen.plus("?url={url}&name={name}&chapterNumber={chapterNumber}"),
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                },
                navArgument("name") {
                    type = NavType.StringType
                },
                navArgument("chapterNumber") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val url = backStackEntry.arguments?.getString("url")?: ""
            val name = backStackEntry.arguments?.getString("name")?: ""
            val chapterNumber = backStackEntry.arguments?.getString("chapterNumber")?: ""
            ReadingScreen(decodeUrl(url) , name ,chapterNumber )
        }
        composable(
            route = Routes.WebViewScreen.plus("?url={url}"),
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) {
            WebPageScreen(it.arguments?.getString("url") ?: "")
        }
        composable(
            route = Routes.ChapterDetailScreen.plus("/{${PARAM_CHAPTERS_DETAIL}}"),
            arguments = listOf(
                navArgument(Constants.PARAM_CHAPTERS_DETAIL) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            backStackEntry.arguments?.getString(PARAM_CHAPTERS_DETAIL).let { json ->

                val listType: Type =
                    object : TypeToken<List<Chapter>>() {}.type
                val chapters = Gson().fromJson<List<Chapter>>(decodeUrl(json?:""), listType)

                ChapterDetailScreen(chapters = chapters, navController = navController)

            }
        }
    }
}



