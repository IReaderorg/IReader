package ireader.presentation.ui.home.sources.extension

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.SourceState
import ireader.domain.models.entities.UserSourceCatalog
import ireader.domain.models.entities.key
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.resources.*
import ireader.i18n.resources.available
import ireader.i18n.resources.installed
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.composables.AddRepositoryDialog
import ireader.presentation.ui.home.sources.extension.composables.CleanCatalogCard
import ireader.presentation.ui.home.sources.extension.composables.CleanSourceHeader
import ireader.presentation.ui.home.sources.extension.composables.SourceLoginDialog
import ireader.presentation.ui.home.sources.extension.composables.UserSourcesCreatorCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Unified Source Screen with modern segmented control
 * Merges Browse (Installed Sources) and Extensions (Available) into one screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UnifiedSourceScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    snackBarHostState: SnackbarHostState,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
    onNavigateToBrowseSettings: (() -> Unit)? = null,
    onNavigateToUserSources: (() -> Unit)? = null,
    onNavigateToAddRepository: (() -> Unit)? = null,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(event.uiText.asString(localizeHelper))
                }
                else -> {}
            }
        }
    }

    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Pager state for swipe support
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { 2 }
    
    // Sync pager with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        vm.setCurrentPagerPage(pagerState.currentPage)
    }
    
    // Count badges
    val installedCount by remember(state.pinnedCatalogs, state.unpinnedCatalogs) {
        derivedStateOf { state.pinnedCatalogs.size + state.unpinnedCatalogs.size }
    }
    val availableCount by remember(state.remoteCatalogs) {
        derivedStateOf { state.remoteCatalogs.size }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        // Modern Segmented Control with swipe sync
        ModernSegmentedControl(
            selectedIndex = pagerState.currentPage,
            onSegmentSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            segments = listOf(
                SegmentData(
                    label = localizeHelper.localize(Res.string.installed),
                    icon = Icons.Default.Check,
                    badge = installedCount
                ),
                SegmentData(
                    label = localizeHelper.localize(Res.string.available),
                    icon = Icons.Default.Download,
                    badge = availableCount
                )
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Swipeable content pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> InstalledSourcesContent(
                    vm = vm,
                    onClickCatalog = onClickCatalog,
                    onClickTogglePinned = onClickTogglePinned,
                    onShowDetails = onShowDetails,
                    onMigrateFromSource = onMigrateFromSource,
                    onNavigateToUserSources = onNavigateToUserSources,
                )
                1 -> AvailableSourcesContent(
                    vm = vm,
                    onClickInstall = onClickInstall,
                    onClickUninstall = onClickUninstall,
                    onCancelInstaller = onCancelInstaller,
                    onNavigateToAddRepository = onNavigateToAddRepository,
                    onDeleteUserSource = { sourceUrl -> vm.deleteUserSourceByUrl(sourceUrl) },
                    onShowDetails = onShowDetails,
                )
            }
        }
    }
}

data class SegmentData(
    val label: String,
    val icon: ImageVector,
    val badge: Int = 0
)


@Composable
private fun ModernSegmentedControl(
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    segments: List<SegmentData>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(4.dp)
        ) {
            val containerWidth = maxWidth
            val segmentWidth = containerWidth / segments.size
            
            // Animated background indicator
            val indicatorOffset by animateFloatAsState(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = localizeHelper.localize(Res.string.indicator_offset)
            )

            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .height(48.dp)
                    .offset { IntOffset((segmentWidth * indicatorOffset).roundToPx(), 0) }
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor)
            )

            // Segment buttons
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                segments.forEachIndexed { index, segment ->
                    val isSelected = selectedIndex == index
                    
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) onPrimary else onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "content_color_$index"
                    )
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.95f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSegmentSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = segment.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = segment.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = contentColor
                            )
                            
                            // Badge
                            if (segment.badge > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) 
                                        onPrimary.copy(alpha = 0.2f) 
                                    else 
                                        primaryColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (segment.badge > 99) "99+" else segment.badge.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) onPrimary else primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstalledSourcesContent(
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
    onNavigateToUserSources: (() -> Unit)? = null,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
    
    val usersSources = remember(
        state.pinnedCatalogs,
        state.unpinnedCatalogs,
        state.selectedUserSourceLanguage
    ) {
        vm.userSources.mapIndexed { index, sourceUiModel ->
            Pair((vm.userSources.size - index).toLong(), sourceUiModel)
        }
    }

    if (usersSources.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Explore,
            title = localizeHelper.localize(Res.string.no_sources_installed),
            subtitle = localizeHelper.localize(Res.string.switch_to_available_tab_to_install_sources)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
        ) {
            if (onNavigateToUserSources != null) {
                item(key = "user_sources_card") {
                    UserSourcesCreatorCard(
                        onClick = onNavigateToUserSources,
                        modifier = Modifier.animateItem()
                    )
                }
            }
            
            items(
                items = usersSources,
                contentType = {
                    when (it.second) {
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
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AvailableSourcesContent(
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    onNavigateToAddRepository: (() -> Unit)? = null,
    onDeleteUserSource: ((String) -> Unit)? = null,
    onShowDetails: ((Catalog) -> Unit)? = null,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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

    // Check if no repository is configured (defaultRepo == -1)
    val hasNoRepository = vm.defaultRepo.value == -1L
    val hasNoRemoteSources = state.remoteCatalogs.isEmpty()
    
    if (hasNoRepository && hasNoRemoteSources) {
        // No repository configured state
        NoRepositoryStateView(
            onAddRepository = onNavigateToAddRepository
        )
    } else if (hasNoRemoteSources) {
        EmptyStateView(
            icon = Icons.Default.Extension,
            title = localizeHelper.localize(Res.string.no_extensions_available),
            subtitle = localizeHelper.localize(Res.string.pull_to_refresh_or_add_a_repository)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
        ) {
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
                                        state.installSteps[catalogItem.source.pkgName] else null,
                                    onClick = { onShowDetails?.invoke(catalogItem.source) },
                                    onInstall = { onClickInstall(catalogItem.source) }
                                        .takeIf { catalogItem.source.hasUpdate },
                                    onUninstall = { onClickUninstall(catalogItem.source) }
                                        .takeIf { catalogItem.source is CatalogInstalled },
                                    onCancelInstaller = onCancelInstaller?.let { cancel -> { cancel(catalogItem.source) } },
                                    sourceStatus = vm.getSourceStatus(catalogItem.source.sourceId),
                                    onShowDetails = onShowDetails?.let { { it(catalogItem.source) } },
                                    onLogin = {
                                        loginSourceId = catalogItem.source.sourceId
                                        loginSourceName = catalogItem.source.name
                                        showLoginDialog = true
                                    },
                                    onDeleteUserSource = if (catalogItem.source is UserSourceCatalog && onDeleteUserSource != null) {
                                        {
                                            (catalogItem.source as UserSourceCatalog).userSource.sourceUrl.let { url ->
                                                onDeleteUserSource(url)
                                            }
                                        }
                                    } else null,
                                )
                            }
                            is CatalogRemote -> {
                                CleanCatalogCard(
                                    modifier = Modifier.animateItem(),
                                    catalog = catalogItem.source,
                                    installStep = state.installSteps[catalogItem.source.pkgName],
                                    onInstall = { onClickInstall(catalogItem.source) },
                                    onClick = { onShowDetails?.invoke(catalogItem.source) },
                                    onCancelInstaller = onCancelInstaller?.let { cancel -> { cancel(catalogItem.source) } },
                                    onShowDetails = onShowDetails?.let { { it(catalogItem.source) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun NoRepositoryStateView(
    onAddRepository: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title
            Text(
                text = localizeHelper.localize(Res.string.no_repository_configured),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Subtitle
            Text(
                text = localizeHelper.localize(Res.string.add_a_repository_to_browse),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add Repository Button
            if (onAddRepository != null) {
                Button(
                    onClick = onAddRepository,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.add_repository))
                }
            }
            
            // Quick setup hint
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.popular_repositories),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.ireader_extensionsn_lnreader_plugins),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
