package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.*
import org.ireader.presentation.feature_sources.presentation.global_search.GlobalSearchScreen

object GlobalSearchScreenSpec : ScreenSpec {

    override val navHostRoute: String = "global?query={query}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument("query") {
            type = NavType.StringType
            defaultValue = ""
        }
    )

    fun buildRoute(query: String): String {
        return "global?query=$query"
    }

    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        GlobalSearchScreen(navController = navController)
    }

}

