package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.*
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceKeys
import ireader.presentation.ui.home.sources.extension.SourceUiModel

/**
 * Modern redesigned Remote Sources Screen (Extensions)
 * Features:
 * - Card-based layout
 * - Better grouping and organization
 * - Enhanced visual feedback
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernRemoteSourcesScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginSourceId by remember { mutableStateOf<Long?>(null) }
    var loginSourceName by remember { mutableStateOf("") }
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    
    if (showLoginDialog && loginSourceId != null) {
        SourceLoginDialog(
            sourceName = loginSourceName,
            onDismiss = {
                showLoginDialog = false
                loginSourceId = null
            },
            onLogin = { username, password ->
                loginSourceId?.let { sourceId ->
                    vm.loginToSource(sourceId, username, password)
                }
                showLoginDialog = false
                loginSourceId = null
            }
        )
    }
    
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepositoryDialog = false },
            onAdd = { url ->
                vm.addRepository(url)
                showAddRepositoryDialog = false
            }
        )
    }

    val allCatalogs = remember {
        derivedStateOf { vm.pinnedCatalogs + vm.unpinnedCatalogs }
    }
    
    val remotesCatalogs = remember {
        derivedStateOf { vm.remoteCatalogs }
    }
    
    val installed = remember {
        derivedStateOf {
            listOf(SourceUiModel.Header(SourceKeys.INSTALLED_KEY)) + 
            allCatalogs.value.map { SourceUiModel.Item(it, SourceState.Installed) }
        }
    }
    
    val remotes = remember {
        derivedStateOf {
            val groupedByLanguage = remotesCatalogs.value.groupBy { it.lang }
            val sortedGroups = groupedByLanguage.entries.sortedBy { it.key }
            
            buildList {
                sortedGroups.forEach { (lang, catalogs) ->
                    add(SourceUiModel.Header(lang))
                    addAll(catalogs.sortedBy { it.name }.map { 
                        SourceUiModel.Item(it, SourceState.Remote) 
                    })
                }
            }
        }
    }

    val remoteSources = remember {
        derivedStateOf {
            (installed.value + remotes.value).mapIndexed { index, sourceUiModel -> 
                Pair(index, sourceUiModel) 
            }
        }
    }
    
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = remoteSources.value,
            contentType = {
                return@items when (it.second) {
                    is SourceUiModel.Header -> "header"
                    is SourceUiModel.Item -> "item"
                }
            },
            key = {
                when (val catalog: SourceUiModel = it.second) {
                    is SourceUiModel.Header -> it.second.hashCode()
                    is SourceUiModel.Item -> catalog.source.key(
                        catalog.state, it.first.toLong(), vm.defaultRepo.value
                    )
                }
            },
        ) { catalog ->
            val catalogItem = remember { catalog.second }
            
            when (catalogItem) {
                is SourceUiModel.Header -> {
                    CleanSourceHeader(
                        modifier = Modifier.animateItem(),
                        language = catalogItem.language,
                    )
                }
                is SourceUiModel.Item -> {
                    when (catalogItem.source) {
                        is CatalogLocal -> {
                            CleanCatalogCard(
                                modifier = Modifier.animateItem(),
                                catalog = catalogItem.source,
                                installStep = if (catalogItem.source is CatalogInstalled) 
                                    vm.installSteps[catalogItem.source.pkgName] else null,
                                onInstall = { onClickInstall(catalogItem.source) }
                                    .takeIf { catalogItem.source.hasUpdate },
                                onUninstall = { onClickUninstall(catalogItem.source) }
                                    .takeIf { catalogItem.source is CatalogInstalled },
                                onCancelInstaller = onCancelInstaller?.let { { it(catalogItem.source) } },
                                sourceStatus = vm.getSourceStatus(catalogItem.source.sourceId),
                                onLogin = {
                                    loginSourceId = catalogItem.source.sourceId
                                    loginSourceName = catalogItem.source.name
                                    showLoginDialog = true
                                },
                            )
                        }
                        is CatalogRemote -> {
                            CleanCatalogCard(
                                modifier = Modifier.animateItem(),
                                catalog = catalogItem.source,
                                installStep = vm.installSteps[catalogItem.source.pkgName],
                                onInstall = { onClickInstall(catalogItem.source) },
                                onClick = { onClickInstall(catalogItem.source) },
                                onCancelInstaller = onCancelInstaller?.let { { it(catalogItem.source) } },
                            )
                        }
                    }
                }
            }
        }
    }
}
