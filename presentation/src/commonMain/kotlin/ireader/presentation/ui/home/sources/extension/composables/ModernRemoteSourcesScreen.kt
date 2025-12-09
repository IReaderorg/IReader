package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.SourceState
import ireader.domain.models.entities.key
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

    val state by vm.state.collectAsState()
    
    val allCatalogs = remember(state.pinnedCatalogs, state.unpinnedCatalogs) {
        state.pinnedCatalogs + state.unpinnedCatalogs
    }
    
    val installed = remember(allCatalogs) {
        listOf(SourceUiModel.Header(SourceKeys.INSTALLED_KEY)) + 
        allCatalogs.map { SourceUiModel.Item(it, SourceState.Installed) }
    }
    
    val remotes = remember(state.remoteCatalogs) {
        val groupedByLanguage = state.remoteCatalogs.groupBy { it.lang }
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

    val remoteSources = remember(installed, remotes) {
        (installed + remotes).mapIndexed { index, sourceUiModel -> 
            Pair(index, sourceUiModel) 
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
        // Language filter for remote sources
//        item(key = "language_filter") {
//            LanguageChipGroup(
//                choices = state.languageChoices,
//                selected = state.selectedLanguage,
//                onClick = { vm.setSelectedLanguage(it) },
//                isVisible = vm.showLanguageFilter.value,
//                onToggleVisibility = { visible ->
//                    vm.uiPreferences.showLanguageFilter().set(visible)
//                }
//            )
//        }
//
        items(
            items = remoteSources,
            contentType = { pair ->
                when (pair.second) {
                    is SourceUiModel.Header -> "header"
                    is SourceUiModel.Item -> "item"
                }
            },
            key = { pair ->
                when (val catalog: SourceUiModel = pair.second) {
                    is SourceUiModel.Header -> pair.second.hashCode()
                    is SourceUiModel.Item -> catalog.source.key(
                        catalog.state, pair.first.toLong(), vm.defaultRepo.value
                    )
                }
            },
        ) { pair ->
            val catalogItem = pair.second
            
            when (catalogItem) {
                is SourceUiModel.Header -> {
                    CleanSourceHeader(
                        modifier = Modifier,
                        language = catalogItem.language,
                    )
                }
                is SourceUiModel.Item -> {
                    when (catalogItem.source) {
                        is CatalogLocal -> {
                            CleanCatalogCard(
                                modifier = Modifier,
                                catalog = catalogItem.source,
                                installStep = if (catalogItem.source is CatalogInstalled) 
                                    state.installSteps[catalogItem.source.pkgName] else null,
                                onInstall = { onClickInstall(catalogItem.source) }
                                    .takeIf { catalogItem.source.hasUpdate },
                                onUninstall = { onClickUninstall(catalogItem.source) }
                                    .takeIf { catalogItem.source is CatalogInstalled },
                                onCancelInstaller = onCancelInstaller?.let { cancel -> { cancel(catalogItem.source) } },
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
                                modifier = Modifier,
                                catalog = catalogItem.source,
                                installStep = state.installSteps[catalogItem.source.pkgName],
                                onInstall = { onClickInstall(catalogItem.source) },
                                onClick = { onClickInstall(catalogItem.source) },
                                onCancelInstaller = onCancelInstaller?.let { cancel -> { cancel(catalogItem.source) } },
                            )
                        }
                    }
                }
            }
        }
    }
}
