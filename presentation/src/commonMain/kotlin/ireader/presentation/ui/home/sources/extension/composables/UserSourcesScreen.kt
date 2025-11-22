package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.key
import ireader.presentation.ui.home.sources.extension.CatalogItem
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceHeader
import ireader.presentation.ui.home.sources.extension.SourceUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserSourcesScreen(
        modifier: Modifier = Modifier,
        vm: ExtensionViewModel,
        onClickCatalog: (Catalog) -> Unit,
        onClickTogglePinned: (Catalog) -> Unit,
        onShowDetails: ((Catalog) -> Unit)? = null,
        onMigrateFromSource: ((Long) -> Unit)? = null,
) {
    // Save scroll state across navigation
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    
    // State for login dialog
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginSourceId by remember { mutableStateOf<Long?>(null) }
    var loginSourceName by remember { mutableStateOf("") }
    
    // Check source health when screen is displayed
    androidx.compose.runtime.LaunchedEffect(Unit) {
        vm.checkAllSourcesHealth()
    }
    
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

    val usersSources = remember {
        derivedStateOf {
            vm.userSources.mapIndexed { index, sourceUiModel ->
                Pair((vm.userSources.size - index).toLong(), sourceUiModel)
            }
        }
    }
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
        item(key = "language_filter") {
            LanguageChipGroup(
                choices = vm.languageChoices,
                selected = vm.selectedUserSourceLanguage,
                onClick = { vm.selectedUserSourceLanguage = it },
                isVisible = vm.showLanguageFilter.value,
                onToggleVisibility = { visible ->
                    vm.uiPreferences.showLanguageFilter().set(visible)
                }
            )
        }

        items(
                items = usersSources.value,
                contentType = {
                    return@items when (val uiModel = it.second) {
                        is SourceUiModel.Header -> "header"
                        is SourceUiModel.Item -> "item"
                    }
                },
                key = {
                    when (val uiModel = it.second) {
                        is SourceUiModel.Header -> it.second.hashCode()
                        is SourceUiModel.Item -> uiModel.source.key(uiModel.state, it.first, vm.defaultRepo.value)
                    }
                },
        ) { catalog ->
            val catalogItem = remember {
                catalog.second
            }
            when (catalogItem) {
                is SourceUiModel.Header -> {
                    SourceHeader(
                            modifier = Modifier.animateItem(),
                            language = catalogItem.language,
                    )
                }
                is SourceUiModel.Item -> CatalogItem(
                        modifier = Modifier.animateItem(),
                        catalog = catalogItem.source,
                        installStep = if (catalogItem.source is CatalogInstalled) vm.installSteps[catalogItem.source.pkgName] else null,
                        onClick = { onClickCatalog(catalogItem.source) },
                        onPinToggle = { onClickTogglePinned(catalogItem.source) },
                        onShowDetails = onShowDetails?.let { { it(catalogItem.source) } },
                        sourceStatus = vm.getSourceStatus(catalogItem.source.sourceId),
                        isLoading = vm.isSourceLoading(catalogItem.source.sourceId),
                        onLogin = {
                            loginSourceId = catalogItem.source.sourceId
                            loginSourceName = catalogItem.source.name
                            showLoginDialog = true
                        },
                        onMigrate = onMigrateFromSource?.let { { it(catalogItem.source.sourceId) } },
                )
            }
        }
    }
}

