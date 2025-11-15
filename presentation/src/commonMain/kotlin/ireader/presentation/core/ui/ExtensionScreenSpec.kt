package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.sources.extension.ExtensionScreen
import ireader.presentation.ui.home.sources.extension.ExtensionScreenTopAppBar
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceDetailScreen

object ExtensionScreenSpec : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val title = localize(Res.string.explore_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.Source)
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
        var showMigrationSourceDialog by remember {
            mutableStateOf(false)
        }
        val focusManager = LocalFocusManager.current
        val snackBarHostState = SnackBarListener(vm)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val swipeState = rememberPullRefreshState(vm.isRefreshing, onRefresh = {
            vm.refreshCatalogs()
        })
        // Migration source selection dialog
        if (showMigrationSourceDialog) {
            MigrationSourceSelectionDialog(
                sources = vm.pinnedCatalogs + vm.unpinnedCatalogs,
                onSourceSelected = { sourceId ->
                    showMigrationSourceDialog = false
                    navController.navigateTo(SourceMigrationScreenSpec(sourceId))
                },
                onDismiss = { showMigrationSourceDialog = false }
            )
        }
        
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
                            navController.navigateTo(
                                    GlobalSearchScreenSpec()
                            )
                        },
                        onMigrate = {
                            showMigrationSourceDialog = true
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
                            navController.navigateTo(
                                    ExploreScreenSpec(
                                            sourceId = it.sourceId,
                                            query = ""
                                    )
                            )
                        },
                        onClickInstall = { vm.installCatalog(it) },
                        onClickTogglePinned = { vm.togglePinnedCatalog(it) },
                        onClickUninstall = { vm.uninstallCatalog(it) },
                        snackBarHostState = snackBarHostState,
                        onCancelInstaller = { vm.cancelCatalogJob(it) },
                        onShowDetails = { catalog ->
                            navController.navigateTo(SourceDetailScreen(catalog))
                        },
                        onMigrateFromSource = { sourceId ->
                            navController.navigateTo(SourceMigrationScreenSpec(sourceId))
                        }
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


@Composable
private fun MigrationSourceSelectionDialog(
    sources: List<CatalogLocal>,
    onSourceSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Source to Migrate From") },
        text = {
            LazyColumn {
                items(sources, key = { it.sourceId }) { source ->
                    ListItem(
                        headlineContent = { Text(source.name) },
                        supportingContent = { 
                            Text("${source.source?.lang ?: "unknown"}")
                        },
                        modifier = Modifier.clickable {
                            onSourceSelected(source.sourceId)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.cancel))
            }
        }
    )
}
