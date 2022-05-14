package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
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

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding:PaddingValues,
        sheetState: ModalBottomSheetState
    ) {
        val vm: GlobalSearchViewModel = hiltViewModel()
        GlobalSearchScreen(
            onPopBackStack = {
                navController.popBackStack()
            },
            onSearch = { query ->
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
                } catch (e: Throwable) {
                    Log.error(e, "")
                }
            },
            onGoToExplore = { index ->
                try {
                    if (vm.query.isNotBlank()) {
                        navController.navigate(
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