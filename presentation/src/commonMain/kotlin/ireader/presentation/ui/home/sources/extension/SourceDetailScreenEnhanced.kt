package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.usecases.source.ReportBrokenSourceUseCase
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.home.sources.extension.composables.LetterIcon
import ireader.presentation.imageloader.IImageLoader
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Enhanced SourceDetailScreen following Mihon's comprehensive information display patterns.
 * 
 * Key improvements:
 * - ListItem layouts for consistent information display
 * - SourceIcon component with proper fallbacks
 * - Version badges and language Pill components
 * - Enhanced error handling and user feedback
 * - Responsive design with TwoPanelBox for tablets
 * - Material Design 3 compliance
 * - Better visual hierarchy and information organization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceDetailScreenEnhanced(
    catalog: Catalog,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Check if we should use responsive layout
    val isExpandedWidth = false // TODO: Implement proper window size detection

    Scaffold(
        modifier = modifier,
        topBar = {
            SourceDetailTopBar(
                title = catalog.name,
                onNavigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        TwoPanelBoxStandalone(
            isExpandedWidth = isExpandedWidth,
            startContent = {
                if (isExpandedWidth) {
                    SourceDetailSidePanel(
                        catalog = catalog,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            },
            endContent = {
                SourceDetailMainContent(
                    catalog = catalog,
                    snackbarHostState = snackbarHostState,
                    showSidePanel = isExpandedWidth,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceDetailTopBar(
    title: String,
    onNavigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localize(Res.string.go_back)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun SourceDetailMainContent(
    catalog: Catalog,
    snackbarHostState: SnackbarHostState,
    showSidePanel: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    
    IReaderFastScrollLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Source header (only show if not in side panel)
        if (!showSidePanel) {
            item(key = "source_header") {
                SourceHeaderSection(
                    catalog = catalog,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
        
        // Source information
        item(key = "source_info") {
            SourceInformationSection(
                catalog = catalog,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Source statistics
        if (catalog is CatalogInstalled) {
            item(key = "source_stats") {
                SourceStatisticsSection(
                    catalog = catalog,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        // Source actions
        item(key = "source_actions") {
            SourceActionsSection(
                catalog = catalog,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SourceDetailSidePanel(
    catalog: Catalog,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SourceHeaderSection(
                catalog = catalog,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Additional side panel content can go here
        }
    }
}

@Composable
private fun SourceHeaderSection(
    catalog: Catalog,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced source icon
            SourceIcon(
                catalog = catalog,
                modifier = Modifier.size(72.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Version badge
                if (catalog is CatalogInstalled) {
                    VersionBadge(
                        version = catalog.versionName,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Language pill
                LanguagePill(
                    language = when (catalog) {
                        is CatalogInstalled -> catalog.source?.lang?.uppercase() ?: "UNKNOWN"
                        else -> "UNKNOWN"
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SourceInformationSection(
    catalog: Catalog,
    modifier: Modifier = Modifier,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localize(Res.string.info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Status
            SourceInfoListItem(
                icon = Icons.Default.Info,
                label = localize(Res.string.status),
                value = when (catalog) {
                    is CatalogInstalled -> localize(Res.string.installed)
                    else -> localize(Res.string.available)
                },
                valueColor = when (catalog) {
                    is CatalogInstalled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // Language
            SourceInfoListItem(
                icon = Icons.Default.Language,
                label = localize(Res.string.language),
                value = when (catalog) {
                    is CatalogInstalled -> catalog.source?.lang?.uppercase() ?: localize(Res.string.unknown)
                    else -> localize(Res.string.unknown)
                }
            )
            
            // Package name (for installed sources)
            if (catalog is CatalogInstalled) {
                SourceInfoListItem(
                    icon = Icons.Default.Apps,
                    label = localizeHelper.localize(Res.string.package_name),
                    value = catalog.pkgName
                )
            }
            
            // Description
            val description = when (catalog) {
                is CatalogInstalled -> catalog.source?.let { "Source: ${it.name}" } ?: "No description available"
                else -> "No description available"
            }
            SourceInfoListItem(
                icon = Icons.Default.Description,
                label = localizeHelper.localize(Res.string.description),
                value = description
            )
        }
    }
}

@Composable
private fun SourceStatisticsSection(
    catalog: CatalogInstalled,
    modifier: Modifier = Modifier,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            SourceInfoListItem(
                icon = Icons.Default.Numbers,
                label = localizeHelper.localize(Res.string.version_code),
                value = catalog.versionCode.toString()
            )
            
            SourceInfoListItem(
                icon = Icons.Default.Update,
                label = localizeHelper.localize(Res.string.version_name),
                value = catalog.versionName
            )
            
            // Add more statistics as needed
            SourceInfoListItem(
                icon = Icons.Default.Source,
                label = localizeHelper.localize(Res.string.source_id),
                value = catalog.sourceId.toString()
            )
        }
    }
}

@Composable
private fun SourceActionsSection(
    catalog: Catalog,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val reportBrokenSourceUseCase: ReportBrokenSourceUseCase = koinInject()
    val scope = rememberCoroutineScope()
    
    var showReportDialog by remember { mutableStateOf(false) }
    var isReporting by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Report as broken button
            ActionButton(
                title = localizeHelper.localize(Res.string.report_as_broken),
                icon = Icons.Default.BugReport,
                onClick = { showReportDialog = true },
                enabled = !isReporting && catalog is CatalogInstalled,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Additional actions can be added here
        }
    }
    
    // Report confirmation dialog
    if (showReportDialog && catalog is CatalogInstalled) {
        EnhancedReportSourceDialog(
            sourceName = catalog.name,
            onDismiss = { showReportDialog = false },
            onConfirm = { reason ->
                showReportDialog = false
                isReporting = true
                
                scope.launch {
                    val result = reportBrokenSourceUseCase(
                        sourceId = catalog.sourceId,
                        packageName = catalog.pkgName,
                        version = catalog.versionName,
                        reason = reason
                    )
                    
                    isReporting = false
                    
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar(
                            message = "Source reported successfully. Thank you for your feedback!",
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            message = "Failed to report source: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun SourceIcon(
    catalog: Catalog,
    modifier: Modifier = Modifier,
) {
    when (catalog) {
        is CatalogLocal -> {
            IImageLoader(
                model = catalog,
                contentDescription = catalog.name,
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
        else -> {
            LetterIcon(
                text = catalog.name,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun VersionBadge(
    version: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "v$version",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LanguagePill(
    language: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = language,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SourceInfoListItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun EnhancedReportSourceDialog(
    sourceName: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var reason by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    
    val categories = listOf(
        "Source not loading",
        "Broken search functionality",
        "Missing or incorrect content",
        "Authentication issues",
        "Performance problems",
        "Other"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = localizeHelper.localize(Res.string.report_source_issue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.help_us_improve)+ "$sourceName\" by reporting the issue you're experiencing.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Category selection
                Text(
                    text = localizeHelper.localize(Res.string.issue_category),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                        Text(
                            text = category,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Description
                Text(
                    text = localizeHelper.localize(Res.string.additional_details),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(localizeHelper.localize(Res.string.please_provide_more_details_about_the_issue)) },
                    minLines = 3,
                    maxLines = 5
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.your_report_will_help_improve),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            ActionButton(
                title = localizeHelper.localize(Res.string.submit_report),
                icon = Icons.Default.Send,
                onClick = { 
                    val fullReason = if (selectedCategory.isNotEmpty()) {
                        "Category: $selectedCategory\n\nDetails: ${reason.ifBlank { "No additional details provided" }}"
                    } else {
                        reason.ifBlank { "No details provided" }
                    }
                    onConfirm(fullReason)
                },
                enabled = selectedCategory.isNotEmpty()
            )
        },
        dismissButton = {
            TextActionButton(
                title = localizeHelper.localize(Res.string.cancel),
                onClick = onDismiss
            )
        }
    )
}