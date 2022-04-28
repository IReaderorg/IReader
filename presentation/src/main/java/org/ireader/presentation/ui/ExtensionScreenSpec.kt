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
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.sources.extension.ExtensionScreen
import org.ireader.sources.extension.ExtensionViewModel

object ExtensionScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.Explore
    override val label: Int = R.string.explore_screen_label
    override val navHostRoute: String = "explore"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
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
            onClickCatalog = {
                navController.navigate(
                    ExploreScreenSpec.buildRoute(
                        sourceId = it.sourceId,
                    )
                )
            },
            onRefreshCatalogs = { viewModel.refreshCatalogs() },
            onClickInstall = { viewModel.installCatalog(it) },
            onClickTogglePinned = { viewModel.togglePinnedCatalog(it) },
            onClickUninstall = { viewModel.uninstallCatalog(it) },
            onSearchNavigate = {
                navController.navigate(
                    GlobalSearchScreenSpec.navHostRoute
                )
            }
        )
    }
}
