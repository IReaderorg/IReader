package ir.kazemcodes.infinity.core.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreScreen

object ExploreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "explore_route/{exploreType}/{sourceId}"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.exploreType,
        NavigationArgs.sourceId
    )


    fun buildRoute(sourceId: Long, exploreType: Int): String {
        return "explore_route/$exploreType/$sourceId"
    }

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        androidx.paging.ExperimentalPagingApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState
    ) {
        ExploreScreen(navController = navController)
    }

}
