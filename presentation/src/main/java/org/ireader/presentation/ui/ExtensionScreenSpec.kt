package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.ExperimentalPagingApi
import org.ireader.domain.view_models.NavigationArgs
import org.ireader.presentation.R
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionScreen
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel

object ExtensionScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.Explore
    override val label: Int = R.string.explore_screen_label
    override val navHostRoute: String = "explore"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )


    @OptIn(ExperimentalPagingApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val viewModel: ExtensionViewModel = hiltViewModel()
        ExtensionScreen(
            navController = navController,
            viewModel = viewModel,
        )
    }


}