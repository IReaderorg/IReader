package ir.kazemcodes.infinity.base_feature.navigation



//class Screens(navController: NavHostController) {
//    val bookshelf: () -> Unit = {
//        navController.navigate(route = "${}/${Action.NO_ACTION}") {
//            popUpTo(SPLASH_SCREEN) { inclusive = true }
//        }
//    }
//    val list: (Int) -> Unit = { taskId ->
//        navController.navigate(route = "task/$taskId")
//    }
//    val task: (Action) -> Unit = { action ->
//        navController.navigate(route = "list/${action}") {
//            popUpTo(LIST_SCREEN) { inclusive = true }
//        }
//    }
//}