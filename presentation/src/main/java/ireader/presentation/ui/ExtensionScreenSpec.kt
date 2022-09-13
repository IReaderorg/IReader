package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager

import androidx.navigation.NamedNavArgument
import ireader.ui.component.Controller
import ireader.presentation.ui.util.NavigationArgs
import ireader.presentation.R
import ireader.ui.home.sources.extension.ExtensionScreen
import ireader.ui.home.sources.extension.ExtensionScreenTopAppBar
import ireader.ui.home.sources.extension.ExtensionViewModel
import org.koin.androidx.compose.getViewModel

object ExtensionScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.Explore
    override val label: Int = R.string.explore_screen_label
    override val navHostRoute: String = "explore"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: ExtensionViewModel = getViewModel(owner = controller.navBackStackEntry)
        var searchMode by remember {
            mutableStateOf(false)
        }
        val focusManager = LocalFocusManager.current
        ExtensionScreenTopAppBar(
            searchMode = searchMode,
            query = vm.searchQuery ?: "",
            onValueChange = {
                vm.searchQuery = it
            },
            onConfirm = {
                focusManager.clearFocus()
            },
            currentPage = vm.currentPagerPage,
            onClose = {
                searchMode = false
                vm.searchQuery = ""
            },
            onSearchDisable = {
                searchMode = false
                vm.searchQuery = ""
            },
            onRefresh = {
                vm.refreshCatalogs()
            },
            onSearchEnable = {
                searchMode = true
            },
            onSearchNavigate = {
                controller.navController.navigate(
                    GlobalSearchScreenSpec.navHostRoute
                )
            },
            scrollBehavior = controller.scrollBehavior,
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: ExtensionViewModel = getViewModel(owner = controller.navBackStackEntry)

        ExtensionScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            vm = vm,
            onClickCatalog = {
                if (!vm.incognito.value) {
                    vm.lastUsedSource.value = it.sourceId
                }
                controller.navController.navigate(
                    ExploreScreenSpec.buildRoute(
                        sourceId = it.sourceId,
                    )
                )
            },
            onRefreshCatalogs = { vm.refreshCatalogs() },
            onClickInstall = { vm.installCatalog(it) },
            onClickTogglePinned = { vm.togglePinnedCatalog(it) },
            onClickUninstall = { vm.uninstallCatalog(it) },
            snackBarHostState = controller.snackBarHostState,
            onCancelInstaller = { vm.cancelCatalogJob(it) },
        )
    }
}
