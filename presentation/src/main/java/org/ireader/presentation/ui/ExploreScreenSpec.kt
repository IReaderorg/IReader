package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.view_models.explore.ExploreViewModel
import org.ireader.presentation.feature_explore.presentation.browse.ExploreScreen

object ExploreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "explore_route/{sourceId}"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.exploreType,
        NavigationArgs.sourceId
    )


    fun buildRoute(sourceId: Long): String {
        return "explore_route/$sourceId"
    }

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        androidx.paging.ExperimentalPagingApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val viewModel: ExploreViewModel = hiltViewModel()
        ExploreScreen(
            navController = navController,
            viewModel = viewModel,
            onFilterClick = {
                viewModel.toggleFilterMode()
            }
        )
    }

}
