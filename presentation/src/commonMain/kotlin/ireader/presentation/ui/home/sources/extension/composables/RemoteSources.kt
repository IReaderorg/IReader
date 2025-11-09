package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.SourceState
import ireader.domain.models.entities.key
import ireader.presentation.ui.home.sources.extension.CatalogItem
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceHeader
import ireader.presentation.ui.home.sources.extension.SourceKeys
import ireader.presentation.ui.home.sources.extension.SourceUiModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteSourcesScreen(
        modifier: Modifier = Modifier,
        vm: ExtensionViewModel,
        onClickInstall: (Catalog) -> Unit,
        onClickUninstall: (Catalog) -> Unit,
        onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    // State for login dialog
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginSourceId by remember { mutableStateOf<Long?>(null) }
    var loginSourceName by remember { mutableStateOf("") }
    
    // State for add repository dialog
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    
    // Show login dialog when needed
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
    
    // Show add repository dialog when needed
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = {
                showAddRepositoryDialog = false
            },
            onAdd = { url ->
                vm.addRepository(url)
                showAddRepositoryDialog = false
            }
        )
    }
    val allCatalogs = remember {
        derivedStateOf {
            vm.pinnedCatalogs + vm.unpinnedCatalogs
        }
    }
    val remotesCatalogs = remember {
        derivedStateOf {

            vm.remoteCatalogs
        }
    }
    val installed = remember {
        derivedStateOf {
            listOf(SourceUiModel.Header(SourceKeys.INSTALLED_KEY)) + allCatalogs.value.map {
                SourceUiModel.Item(it, SourceState.Installed)
            }
        }
    }
    val remotes = remember {
        derivedStateOf {
            // Group remote catalogs by language and sort
            val groupedByLanguage = remotesCatalogs.value.groupBy { it.lang }
            
            val sortedGroups = groupedByLanguage.entries.sortedBy { it.key }
            
            // Build the list with headers for each language
            buildList {
                sortedGroups.forEach { (lang, catalogs) ->
                    add(SourceUiModel.Header(lang))
                    addAll(catalogs.sortedBy { it.name }.map { SourceUiModel.Item(it, SourceState.Remote) })
                }
            }
        }
    }

    val remoteSources = remember {
        derivedStateOf {
            (installed.value + remotes.value).mapIndexed { index, sourceUiModel -> Pair(index, sourceUiModel) }
        }
    }
    val scrollState = rememberLazyListState()

    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "language_filter") {
            LanguageChipGroup(
                choices = vm.languageChoices,
                selected = vm.selectedLanguage,
                onClick = { vm.selectedLanguage = it },
                isVisible = vm.showLanguageFilter.value,
                onToggleVisibility = { visible ->
                    vm.uiPreferences.showLanguageFilter().set(visible)
                }
            )
        }

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
                        is SourceUiModel.Item -> catalog.source.key(catalog.state, it.first.toLong(), vm.defaultRepo.value)
                    }
                },
        ) { catalog ->
            val catalog = remember {
                catalog.second
            }
            when (catalog) {
                is SourceUiModel.Header -> {
                    SourceHeader(
                            modifier = Modifier.animateItem(),
                            language = catalog.language,
                    )
                }
                is SourceUiModel.Item -> {
                    when (catalog.source) {
                        is CatalogLocal -> {
                            CatalogItem(
                                    modifier = Modifier.fillMaxSize(),
                                    catalog = catalog.source,
                                    installStep = if (catalog.source is CatalogInstalled) vm.installSteps[catalog.source.pkgName] else null,
                                    onInstall = { onClickInstall(catalog.source) }.takeIf { catalog.source.hasUpdate },
                                    onUninstall = { onClickUninstall(catalog.source) }.takeIf { catalog.source is CatalogInstalled },
                                    onCancelInstaller = {
                                        if (onCancelInstaller != null) {
                                            onCancelInstaller(it)
                                        }
                                    },
                                    sourceStatus = vm.getSourceStatus(catalog.source.sourceId),
                                    onLogin = {
                                        loginSourceId = catalog.source.sourceId
                                        loginSourceName = catalog.source.name
                                        showLoginDialog = true
                                    },
                            )
                        }
                        is CatalogRemote -> {
                            CatalogItem(
                                    modifier = Modifier.fillMaxSize(),
                                    catalog = catalog.source,
                                    installStep = vm.installSteps[catalog.source.pkgName],
                                    onInstall = { onClickInstall(catalog.source) },
                                    onClick = { onClickInstall(catalog.source) },
                                    onCancelInstaller = {
                                        if (onCancelInstaller != null) {
                                            onCancelInstaller(it)
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

}
