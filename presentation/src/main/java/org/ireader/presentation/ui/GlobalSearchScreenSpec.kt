package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import org.ireader.core_api.log.Log
import org.ireader.presentation.feature_sources.presentation.global_search.GlobalSearchScreen
import org.ireader.presentation.feature_sources.presentation.global_search.viewmodel.GlobalSearchViewModel

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
        val vm: GlobalSearchViewModel = hiltViewModel()
        GlobalSearchScreen(
            onPopBackStack = {
                navController.popBackStack()
            },
            onSearch = { query->
                vm.searchBooks(query = query)
            },
            vm = vm,
            onBook = {
                try {
                    navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            sourceId = it.sourceId,
                            bookId = it.id,
                        )
                    )

                } catch (e: Exception) {
                    Log.error(e,"")
                }
            },
            onGoToExplore = { index->
                try {
                    if (vm.query.isNotBlank()) {
                        navController.navigate(
                            ExploreScreenSpec.buildRoute(vm.searchItems[index].source.id,
                                query = vm.query)
                        )
                    }
                } catch (e: Exception) {
                    Log.error(e,"")
                }

            }
        )
    }

}

