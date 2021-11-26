package ir.kazemcodes.infinity.base_feature.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.ChapterDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.PARAM_BOOK_ID
import ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen.BrowseScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.ReadingScreen
import ir.kazemcodes.infinity.library_feature.presentation.screen.library_screen.LibraryScreen
import ir.kazemcodes.infinity.presentation.screen.components.WebPageScreen
import ir.kazemcodes.infinity.presentation.screen.setting_screen.SettingScreen

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
        //Detail Screen Composable
        composable(
            Routes.BookDetailScreen
        ) {
            BookDetailScreen(navController = navController)
        }
        composable(
            route = Routes.ReadingScreen.plus("/{$PARAM_BOOK_ID}"),
            arguments = listOf(
                navArgument(PARAM_BOOK_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            ReadingScreen()
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
            route = Routes.ChapterDetailScreen.plus("/{${PARAM_BOOK_ID}}"),
            arguments = listOf(
                navArgument(Constants.PARAM_BOOK_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            backStackEntry.arguments?.getString(PARAM_BOOK_ID).let { bookId ->

                ChapterDetailScreen(
                    navController = navController
                )

            }
        }
    }
}



