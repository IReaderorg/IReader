package org.ireader.presentation.ui

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
    override val icon: ImageVector = Icons.Filled.Explore
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
    ) {
        val vm: ExtensionViewModel = hiltViewModel()
        ExtensionScreen(
            viewModel = vm,
            onClickCatalog = {
                if (vm.uiPreferences.incognitoMode().get()) {
                    vm.uiPreferences.lastUsedSource().set(it.sourceId)
                }
                navController.navigate(
                    ExploreScreenSpec.buildRoute(
                        sourceId = it.sourceId,
                    )
                )
            },
            onRefreshCatalogs = { vm.refreshCatalogs() },
            onClickInstall = { vm.installCatalog(it) },
            onClickTogglePinned = { vm.togglePinnedCatalog(it) },
            onClickUninstall = { vm.uninstallCatalog(it) },
            onSearchNavigate = {
                navController.navigate(
                    GlobalSearchScreenSpec.navHostRoute
                )
            }
        )
    }
}