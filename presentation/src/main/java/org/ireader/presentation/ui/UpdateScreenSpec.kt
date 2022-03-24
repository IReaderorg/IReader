package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.presentation.feature_updates.UpdateScreen


object UpdateScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.NewReleases

    override val label: Int = R.string.updates_screen_label
    override val navHostRoute: String = "updates"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        UpdateScreen(navController)
    }

}
