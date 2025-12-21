package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.add
import ireader.i18n.resources.add_repository
import ireader.i18n.resources.cancel
import ireader.i18n.resources.enter_the_repository_url
import ireader.i18n.resources.examplesn_ireader_httpsrawgithubusercontentcomireaderorgireader_extensionsrepoindexminjsonn_lnreader
import ireader.i18n.resources.explore_screen_label
import ireader.i18n.resources.migrate
import ireader.i18n.resources.no_sources_available_for_migration
import ireader.i18n.resources.please_enter_a_valid_repository
import ireader.i18n.resources.repository_url
import ireader.i18n.resources.select_a_source_to_migrate_books_from
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceDetailScreen
import ireader.presentation.ui.home.sources.extension.UnifiedSourceScreen
import ireader.presentation.ui.home.sources.extension.UnifiedSourceTopAppBar

/**
 * Extension screen specification - provides tab metadata and content
 */
object ExtensionScreenSpec {

    @Composable
    fun getTitle(): String = localize(Res.string.explore_screen_label)

    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.Source)

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    fun TabContent() {
        val vm: ExtensionViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        var searchMode by remember { mutableStateOf(false) }
        var showMigrationSourceDialog by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        val snackBarHostState = SnackBarListener(vm)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val pullToRefreshState = rememberPullToRefreshState()
        val requiredPluginChecker: ireader.domain.plugins.RequiredPluginChecker = org.koin.compose.koinInject()

        // Migration source selection dialog
        if (showMigrationSourceDialog) {
            MigrationSourceSelectionDialog(
                sources = state.pinnedCatalogs + state.unpinnedCatalogs,
                onSourceSelected = { sourceId ->
                    showMigrationSourceDialog = false
                    navController.navigateTo(SourceMigrationScreenSpec(sourceId))
                },
                onDismiss = { showMigrationSourceDialog = false }
            )
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refreshCatalogs() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            IScaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { scrollBehavior ->
                    UnifiedSourceTopAppBar(
                        searchMode = searchMode,
                        query = state.searchQuery ?: "",
                        onValueChange = { vm.setSearchQuery(it) },
                        onConfirm = { focusManager.clearFocus() },
                        onSearchDisable = {
                            searchMode = false
                            vm.setSearchQuery(null)
                        },
                        onRefresh = { vm.refreshCatalogs() },
                        onSearchEnable = { searchMode = true },
                        onSearchNavigate = {
                            navController.navigateTo(GlobalSearchScreenSpec())
                        },
                        onMigrate = { showMigrationSourceDialog = true },
                        onBrowseSettings = {
                            navController.navigateTo(BrowseSettingsScreenSpec())
                        },
                        scrollBehavior = scrollBehavior,
                        onAddRepository = {
                            navController.navigateTo(RepositoryAddScreenSpec())
                        }
                    )
                }
            ) { scaffoldPadding ->
                UnifiedSourceScreen(
                    vm = vm,
                    onClickCatalog = { catalog ->
                        // Check if this is a JS source that requires JS engine
                        val isJSSource = catalog is ireader.domain.models.entities.JSPluginCatalog
                        val isPendingSource = isJSSource && 
                            (catalog as ireader.domain.models.entities.JSPluginCatalog).source is ireader.domain.js.loader.JSPluginPendingSource
                        val jsEngineAvailable = requiredPluginChecker.isJSEngineAvailable()
                        
                        if (isPendingSource || (isJSSource && !jsEngineAvailable)) {
                            // JS source but no engine - show required plugin dialog
                            requiredPluginChecker.requestJSEngine()
                        } else {
                            // Native source or JS engine available - navigate normally
                            if (!vm.incognito.value) {
                                vm.lastUsedSource.value = catalog.sourceId
                            }
                            navController.navigateTo(
                                ExploreScreenSpec(
                                    sourceId = catalog.sourceId,
                                    query = ""
                                )
                            )
                        }
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
                    },
                    onNavigateToAddRepository = {
                        navController.navigateTo(RepositoryAddScreenSpec())
                    },
                    scaffoldPadding = scaffoldPadding,
                )
            }
        }
    }
}

@Composable
private fun MigrationSourceSelectionDialog(
    sources: List<CatalogLocal>,
    onSourceSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = localize(Res.string.migrate),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.select_a_source_to_migrate_books_from),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (sources.isEmpty()) {
                    Text(
                        text = localizeHelper.localize(Res.string.no_sources_available_for_migration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(sources, key = { it.sourceId }) { source ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSourceSelected(source.sourceId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = source.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${source.source?.lang?.uppercase() ?: "UNKNOWN"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
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

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var url by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        isValidUrl = url.isNotBlank() &&
            (url.startsWith("http://") || url.startsWith("https://")) &&
            (url.contains(".json") || url.contains("index.min.json") || url.contains("v3.json"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.add_repository)) },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.enter_the_repository_url),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(localizeHelper.localize(Res.string.repository_url)) },
                    placeholder = { Text("https://example.com/repo.json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = url.isNotBlank() && !isValidUrl
                )

                if (url.isNotBlank() && !isValidUrl) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.please_enter_a_valid_repository),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = localizeHelper.localize(Res.string.examplesn_ireader_httpsrawgithubusercontentcomireaderorgireader_extensionsrepoindexminjsonn_lnreader),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(url) },
                enabled = isValidUrl
            ) {
                Text(localizeHelper.localize(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
