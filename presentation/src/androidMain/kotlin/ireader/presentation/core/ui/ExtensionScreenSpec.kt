package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.sources.extension.ExtensionScreen
import ireader.presentation.ui.home.sources.extension.ExtensionScreenTopAppBar
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel

actual object ExtensionScreenSpec : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val title = localize(MR.strings.explore_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.Explore)
            return remember {
                TabOptions(
                    index = 3u,
                    title = title,
                    icon = icon,
                )
            }

        }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(

    ) {
        val vm: ExtensionViewModel =
            getIViewModel()
        var searchMode by remember {
            mutableStateOf(false)
        }
        val focusManager = LocalFocusManager.current
        val snackBarHostState = SnackBarListener(vm)
        val navigator = LocalNavigator.currentOrThrow
        val swipeState = rememberPullRefreshState(vm.isRefreshing, onRefresh = {
            vm.refreshCatalogs()
        })
        Box(modifier = Modifier.fillMaxSize()) {
        IScaffold(
            modifier = Modifier.fillMaxSize().pullRefresh(swipeState, vm.currentPagerPage == 1), topBar = { scrollBehavior ->

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
                        navigator.push(
                            GlobalSearchScreenSpec()
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }) { scaffoldPadding ->



                ExtensionScreen(
                    modifier = Modifier.padding(scaffoldPadding),
                    vm = vm,
                    onClickCatalog = {
                        if (!vm.incognito.value) {
                            vm.lastUsedSource.value = it.sourceId
                        }
                        navigator.push(
                            ExploreScreenSpec(
                                sourceId = it.sourceId,
                            )
                        )
                    },
                    onClickInstall = { vm.installCatalog(it) },
                    onClickTogglePinned = { vm.togglePinnedCatalog(it) },
                    onClickUninstall = { vm.uninstallCatalog(it) },
                    snackBarHostState = snackBarHostState,
                    onCancelInstaller = { vm.cancelCatalogJob(it) },
                )
            }
            PullRefreshIndicator(
                vm.isRefreshing,
                swipeState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
