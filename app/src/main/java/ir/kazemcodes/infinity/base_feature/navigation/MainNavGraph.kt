package ir.kazemcodes.infinity.base_feature.navigation

//@ExperimentalMaterialApi
//@Composable
//fun MainNavGraph(
//    navController: NavHostController = rememberNavController(),
//    startDestination: String = BottomNavigationScreens.Library.route,
//) {
//    NavHost(
//        navController = navController,
//        startDestination = startDestination,
//    ) {
//
//        composable(
//            BottomNavigationScreens.Library.route,
//        ) {
//            LibraryScreen(
//                navController = navController
//            )
//        }
//        composable(
//            BottomNavigationScreens.Browse.route
//        ) {
//            BrowseScreen(navController = navController)
//        }
//        composable(
//            BottomNavigationScreens.Setting.route
//        ) {
//            SettingScreen()
//        }
//        //Detail Screen Composable
//        composable(
//            Routes.BookDetailScreen
//        ) {
//            BookDetailScreen(navController = navController)
//        }
//        composable(
//            route = Routes.ReadingScreen.plus("/{$PARAM_BOOK_ID}"),
//            arguments = listOf(
//                navArgument(PARAM_BOOK_ID) {
//                    type = NavType.IntType
//                }
//            )
//        ) { backStackEntry ->
//            ReadingScreen()
//        }
//        composable(
//            route = Routes.WebViewScreen.plus("?url={url}"),
//            arguments = listOf(
//                navArgument("url") {
//                    type = NavType.StringType
//                }
//            )
//        ) {
//            WebPageScreen(it.arguments?.getString("url") ?: "")
//        }
//        composable(
//            route = Routes.ChapterDetailScreen.plus("/{${PARAM_BOOK_ID}}"),
//            arguments = listOf(
//                navArgument(Constants.PARAM_BOOK_ID) {
//                    type = NavType.IntType
//                }
//            )
//        ) { backStackEntry ->
//
//            backStackEntry.arguments?.getString(PARAM_BOOK_ID).let { bookId ->
//
//                ChapterDetailScreen(
//                    navController = navController
//                )
//
//            }
//        }
//    }
//}
//
//
//
