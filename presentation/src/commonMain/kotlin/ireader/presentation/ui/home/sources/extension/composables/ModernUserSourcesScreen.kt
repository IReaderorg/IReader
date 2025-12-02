package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.key
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceUiModel

/**
 * Modern redesigned User Sources Screen
 * Features:
 * - Card-based layout with elevation
 * - Smooth animations
 * - Better spacing and visual hierarchy
 * - Enhanced language filter UI
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernUserSourcesScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
) {
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginSourceId by remember { mutableStateOf<Long?>(null) }
    var loginSourceName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        vm.checkAllSourcesHealth()
    }
    
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

    val state by vm.state.collectAsState()
    
    // Recompute usersSources when state changes (pinnedCatalogs, unpinnedCatalogs, or language filter)
    val usersSources = remember(
        state.pinnedCatalogs,
        state.unpinnedCatalogs,
        state.selectedUserSourceLanguage
    ) {
        vm.userSources.mapIndexed { index, sourceUiModel ->
            Pair((vm.userSources.size - index).toLong(), sourceUiModel)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = usersSources,
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
            val catalogItem = remember { catalog.second }
            
            when (catalogItem) {
                is SourceUiModel.Header -> {
                    CleanSourceHeader(
                        modifier = Modifier.animateItem(),
                        language = catalogItem.language,
                    )
                }
                is SourceUiModel.Item -> CleanCatalogCard(
                    modifier = Modifier.animateItem(),
                    catalog = catalogItem.source,
                    installStep = if (catalogItem.source is CatalogInstalled) 
                        state.installSteps[catalogItem.source.pkgName] else null,
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
