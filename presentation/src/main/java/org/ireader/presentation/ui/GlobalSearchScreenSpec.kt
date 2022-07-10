package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.ireader.Controller
import org.ireader.core_api.log.Log
import org.ireader.sources.global_search.GlobalSearchScreen
import org.ireader.sources.global_search.viewmodel.GlobalSearchViewModel

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

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: GlobalSearchViewModel = hiltViewModel(   controller.navBackStackEntry)
        GlobalSearchScreen(
            scrollBehavior = controller.scrollBehavior,
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            onSearch = { query ->
                vm.searchBooks(query = query)
            },
            vm = vm,
            onBook = {
                try {
                    controller.navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            sourceId = it.sourceId,
                            bookId = it.id,
                        )
                    )
                } catch (e: Throwable) {
                    Log.error(e, "")
                }
            },
            onGoToExplore = { index ->
                try {
                    if (vm.query.isNotBlank()) {
                        controller.navController.navigate(
                            ExploreScreenSpec.buildRoute(
                                vm.searchItems[index].source.id,
                                query = vm.query
                            )
                        )
                    }
                } catch (e: Throwable) {
                    Log.error(e, "")
                }
            },
        )
    }
}