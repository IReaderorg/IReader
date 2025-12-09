//package ireader.presentation.ui.home.sources.extension.composables
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import ireader.domain.models.entities.Catalog
//import ireader.domain.models.entities.CatalogInstalled
//import ireader.domain.models.entities.CatalogLocal
//import ireader.domain.models.entities.CatalogRemote
//import ireader.domain.models.entities.SourceState
//import ireader.domain.models.entities.key
//import ireader.presentation.ui.home.sources.extension.CatalogItem
//import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
//import ireader.presentation.ui.home.sources.extension.SourceHeader
//import ireader.presentation.ui.home.sources.extension.SourceKeys
//import ireader.presentation.ui.home.sources.extension.SourceUiModel
//
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun RemoteSourcesScreen(
//        modifier: Modifier = Modifier,
//        vm: ExtensionViewModel,
//        onClickInstall: (Catalog) -> Unit,
//        onClickUninstall: (Catalog) -> Unit,
//        onCancelInstaller: ((Catalog) -> Unit)? = null,
//
//) {
//    // State for login dialog
//    var showLoginDialog by remember { mutableStateOf(false) }
//    var loginSourceId by remember { mutableStateOf<Long?>(null) }
//    var loginSourceName by remember { mutableStateOf("") }
//
//    // State for add repository dialog
//    var showAddRepositoryDialog by remember { mutableStateOf(false) }
//
//    // Show login dialog when needed
//    if (showLoginDialog && loginSourceId != null) {
//        SourceLoginDialog(
//            sourceName = loginSourceName,
//            onDismiss = {
//                showLoginDialog = false
//                loginSourceId = null
//            },
//            onLogin = { username, password ->
//                loginSourceId?.let { sourceId ->
//                    vm.loginToSource(sourceId, username, password)
//                }
//                showLoginDialog = false
//                loginSourceId = null
//            }
//        )
//    }
//
//    // Show add repository dialog when needed
//    if (showAddRepositoryDialog) {
//        AddRepositoryDialog(
//            onDismiss = {
//                showAddRepositoryDialog = false
//            },
//            onAdd = { url ->
//                vm.addRepository(url)
//                showAddRepositoryDialog = false
//            }
//        )
//    }
//    val state by vm.state.collectAsState()
//
//    // Optimize derived state chain to avoid unnecessary recompositions
//    val remoteSources = remember(state.pinnedCatalogs, state.unpinnedCatalogs, state.remoteCatalogs) {
//        val allCatalogs = state.pinnedCatalogs + state.unpinnedCatalogs
//
//        val installed = listOf(SourceUiModel.Header(SourceKeys.INSTALLED_KEY)) +
//            allCatalogs.map { SourceUiModel.Item(it, SourceState.Installed) }
//
//        // Group remote catalogs by language and sort
//        val groupedByLanguage = state.remoteCatalogs.groupBy { it.lang }
//        val sortedGroups = groupedByLanguage.entries.sortedBy { it.key }
//
//        // Build the list with headers for each language
//        val remotes = buildList {
//            sortedGroups.forEach { (lang, catalogs) ->
//                add(SourceUiModel.Header(lang))
//                addAll(catalogs.sortedBy { it.name }.map { SourceUiModel.Item(it, SourceState.Remote) })
//            }
//        }
//
//        (installed + remotes).mapIndexed { index, sourceUiModel -> Pair(index, sourceUiModel) }
//    }
//
//    // Save scroll state across navigation with proper key
//    val scrollState = rememberSaveable(
//        key = "remote_sources_scroll",
//        saver = LazyListState.Saver
//    ) {
//        LazyListState()
//    }
//
//    LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            state = scrollState,
//            verticalArrangement = Arrangement.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
//    ) {
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
//        items(
//                items = remoteSources,
//                contentType = {
//                    return@items when (it.second) {
//                        is SourceUiModel.Header -> "header"
//                        is SourceUiModel.Item -> "item"
//                    }
//                },
//                key = {
//                    when (val catalog: SourceUiModel = it.second) {
//                        is SourceUiModel.Header -> it.second.hashCode()
//                        is SourceUiModel.Item -> catalog.source.key(catalog.state, it.first.toLong(), vm.defaultRepo.value)
//                    }
//                },
//        ) { catalog ->
//            val catalogItem = remember {
//                catalog.second
//            }
//            when (catalogItem) {
//                is SourceUiModel.Header -> {
//                    SourceHeader(
//                            modifier = Modifier.animateItem(),
//                            language = catalogItem.language,
//                    )
//                }
//                is SourceUiModel.Item -> {
//                    when (catalogItem.source) {
//                        is CatalogLocal -> {
//                            CatalogItem(
//                                    modifier = Modifier.fillMaxSize(),
//                                    catalog = catalogItem.source,
//                                    installStep = if (catalogItem.source is CatalogInstalled) state.installSteps[catalogItem.source.pkgName] else null,
//                                    onInstall = { onClickInstall(catalogItem.source) }.takeIf { catalogItem.source.hasUpdate },
//                                    onUninstall = { onClickUninstall(catalogItem.source) }.takeIf { catalogItem.source is CatalogInstalled },
//                                    onCancelInstaller = {
//                                        if (onCancelInstaller != null) {
//                                            onCancelInstaller(it)
//                                        }
//                                    },
//                                    sourceStatus = vm.getSourceStatus(catalogItem.source.sourceId),
//                                    onLogin = {
//                                        loginSourceId = catalogItem.source.sourceId
//                                        loginSourceName = catalogItem.source.name
//                                        showLoginDialog = true
//                                    },
//                            )
//                        }
//                        is CatalogRemote -> {
//                            CatalogItem(
//                                    modifier = Modifier.fillMaxSize(),
//                                    catalog = catalogItem.source,
//                                    installStep = state.installSteps[catalogItem.source.pkgName],
//                                    onInstall = { onClickInstall(catalogItem.source) },
//                                    onClick = { onClickInstall(catalogItem.source) },
//                                    onCancelInstaller = {
//                                        if (onCancelInstaller != null) {
//                                            onCancelInstaller(it)
//                                        }
//                                    },
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}
