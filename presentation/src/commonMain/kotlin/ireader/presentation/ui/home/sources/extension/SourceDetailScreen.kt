package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.usecases.source.ReportBrokenSourceUseCase
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.actions
import ireader.i18n.resources.available
import ireader.i18n.resources.cancel
import ireader.i18n.resources.description
import ireader.i18n.resources.eg_source_not_loading_broken
import ireader.i18n.resources.eighteen
import ireader.i18n.resources.go_back
import ireader.i18n.resources.icon_url
import ireader.i18n.resources.info
import ireader.i18n.resources.installed
import ireader.i18n.resources.language
import ireader.i18n.resources.package_name
import ireader.i18n.resources.package_url
import ireader.i18n.resources.please_describe_the_issue
import ireader.i18n.resources.report_as_broken
import ireader.i18n.resources.report_source_as_broken
import ireader.i18n.resources.repository
import ireader.i18n.resources.source_id
import ireader.i18n.resources.status
import ireader.i18n.resources.submit_report
import ireader.i18n.resources.technical_details
import ireader.i18n.resources.unknown
import ireader.i18n.resources.version_code
import ireader.i18n.resources.version_name
import ireader.i18n.resources.you_are_about_to_report
import ireader.i18n.resources.your_report_will_be_stored_locally_for_review
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.composables.LetterIcon
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class SourceDetailScreen(
    val catalog: Catalog
) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        IScaffold(
            topBar = {
                TopAppBar(
                    title = { Text(catalog.name) },
                    navigationIcon = {
                        AppIconButton(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localize(Res.string.go_back),
                            onClick = { navController.safePopBackStack() }
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            SourceDetailContent(
                catalog = catalog,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun SourceDetailContent(
    catalog: Catalog,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scrollState = rememberScrollState()
    val reportBrokenSourceUseCase: ReportBrokenSourceUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showReportDialog by remember { mutableStateOf(false) }
    var isReporting by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card with Icon and Name
            SourceHeaderCard(catalog = catalog)
            
            // Source Information Card
            SourceInfoCard(catalog = catalog)
            
            // Description Card (if available)
            if (catalog.description.isNotBlank()) {
                DescriptionCard(description = catalog.description)
            }
            
            // Technical Details Card
            TechnicalDetailsCard(catalog = catalog)
            
            // Actions Card (only for installed sources)
            if (catalog is CatalogInstalled) {
                ActionsCard(
                    catalog = catalog,
                    isReporting = isReporting,
                    onReportClick = { showReportDialog = true }
                )
            }
        }
        
        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Report confirmation dialog
    if (showReportDialog && catalog is CatalogInstalled) {
        ReportSourceDialog(
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
private fun SourceHeaderCard(catalog: Catalog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Source Icon
            SourceIcon(
                catalog = catalog,
                modifier = Modifier.size(72.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Version badge
                    val version = when (catalog) {
                        is CatalogInstalled -> catalog.versionName
                        is CatalogRemote -> catalog.versionName
                        else -> null
                    }
                    if (version != null) {
                        VersionBadge(version = version)
                    }
                    
                    // Language pill
                    val language = when (catalog) {
                        is CatalogInstalled -> catalog.source?.lang?.uppercase()
                        is CatalogRemote -> catalog.lang.uppercase()
                        else -> null
                    }
                    if (language != null) {
                        LanguagePill(language = language)
                    }
                    
                    // NSFW badge
                    val isNsfw = when (catalog) {
                        is CatalogLocal -> catalog.nsfw
                        is CatalogRemote -> catalog.nsfw
                        else -> false
                    }
                    if (isNsfw) {
                        NsfwBadge()
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceInfoCard(catalog: Catalog) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
            InfoRow(
                icon = Icons.Default.Info,
                label = localize(Res.string.status),
                value = when (catalog) {
                    is CatalogInstalled -> localize(Res.string.installed)
                    is CatalogRemote -> localize(Res.string.available)
                    else -> localize(Res.string.unknown)
                },
                valueColor = when (catalog) {
                    is CatalogInstalled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // Language
            val language = when (catalog) {
                is CatalogInstalled -> catalog.source?.lang?.uppercase() ?: localize(Res.string.unknown)
                is CatalogRemote -> catalog.lang.uppercase()
                else -> localize(Res.string.unknown)
            }
            InfoRow(
                icon = Icons.Default.Language,
                label = localize(Res.string.language),
                value = language
            )
            
            // Repository type (for remote sources)
            if (catalog is CatalogRemote) {
                InfoRow(
                    icon = Icons.Default.Storage,
                    label = localizeHelper.localize(Res.string.repository),
                    value = catalog.repositoryType
                )
            }
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = localizeHelper.localize(Res.string.description),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TechnicalDetailsCard(catalog: Catalog) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.technical_details),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Package Name
            val pkgName = when (catalog) {
                is CatalogInstalled -> catalog.pkgName
                is CatalogRemote -> catalog.pkgName
                else -> null
            }
            if (pkgName != null) {
                InfoRow(
                    icon = Icons.Default.Apps,
                    label = localizeHelper.localize(Res.string.package_name),
                    value = pkgName
                )
            }
            
            // Version Code
            val versionCode = when (catalog) {
                is CatalogInstalled -> catalog.versionCode.toString()
                is CatalogRemote -> catalog.versionCode.toString()
                else -> null
            }
            if (versionCode != null) {
                InfoRow(
                    icon = Icons.Default.Numbers,
                    label = localizeHelper.localize(Res.string.version_code),
                    value = versionCode
                )
            }
            
            // Version Name
            val versionName = when (catalog) {
                is CatalogInstalled -> catalog.versionName
                is CatalogRemote -> catalog.versionName
                else -> null
            }
            if (versionName != null) {
                InfoRow(
                    icon = Icons.Default.Update,
                    label = localizeHelper.localize(Res.string.version_name),
                    value = versionName
                )
            }
            
            // Source ID
            InfoRow(
                icon = Icons.Default.Source,
                label = localizeHelper.localize(Res.string.source_id),
                value = catalog.sourceId.toString()
            )
            
            // Download URLs (for remote sources)
            if (catalog is CatalogRemote) {
                if (catalog.pkgUrl.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Default.Download,
                        label = localizeHelper.localize(Res.string.package_url),
                        value = catalog.pkgUrl
                    )
                }
                if (catalog.iconUrl.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Default.Image,
                        label = localizeHelper.localize(Res.string.icon_url),
                        value = catalog.iconUrl
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsCard(
    catalog: CatalogInstalled,
    isReporting: Boolean,
    onReportClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
            
            Button(
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isReporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                if (isReporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.report_as_broken))
            }
        }
    }
}

@Composable
private fun SourceIcon(
    catalog: Catalog,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (catalog) {
            is CatalogLocal -> {
                IImageLoader(
                    model = catalog,
                    contentDescription = catalog.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            }
            is CatalogRemote -> {
                IImageLoader(
                    model = catalog,
                    contentDescription = catalog.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            }
            else -> {
                LetterIcon(
                    text = catalog.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun VersionBadge(version: String) {
    Surface(
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
private fun LanguagePill(language: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = language,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NsfwBadge() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = localizeHelper.localize(Res.string.eighteen),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
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
private fun ReportSourceDialog(
    sourceName: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = localizeHelper.localize(Res.string.report_source_as_broken),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.you_are_about_to_report)+ "$sourceName\" as broken or not working properly.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.please_describe_the_issue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(localizeHelper.localize(Res.string.eg_source_not_loading_broken)) },
                    minLines = 3,
                    maxLines = 5
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.your_report_will_be_stored_locally_for_review),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (reason.isNotBlank()) {
                        onConfirm(reason)
                    }
                },
                enabled = reason.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.submit_report))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
