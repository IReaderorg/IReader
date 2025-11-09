package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.usecases.source.ReportBrokenSourceUseCase
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.home.sources.extension.composables.LetterIcon
import ireader.presentation.imageloader.IImageLoader
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.*

data class SourceDetailScreen(
    val catalog: Catalog
) : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        IScaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localize(MR.strings.source)) },
                    navigationIcon = {
                        AppIconButton(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localize(MR.strings.go_back),
                            onClick = { navigator.pop() }
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
        // Source Icon and Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (catalog) {
                is CatalogLocal -> {
                    IImageLoader(
                        model = catalog,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                }
                else -> {
                    LetterIcon(
                        text = catalog.name,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Column {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (catalog is CatalogInstalled) {
                    Text(
                        text = "v${catalog.versionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider()

        // Language
        DetailItem(
            label = localize(MR.strings.language),
            value = when (catalog) {
                is CatalogInstalled -> catalog.source?.lang?.uppercase(Locale.getDefault()) ?: localize(MR.strings.unknown)
                else -> localize(MR.strings.unknown)
            }
        )

        // Description
        val description = when (catalog) {
            is CatalogInstalled -> catalog.source?.let { "Source: ${it.name}" } ?: "No description available"
            else -> "No description available"
        }
        DetailItem(
            label = "Description",
            value = description
        )

        // Status
        DetailItem(
            label = localize(MR.strings.status),
            value = when (catalog) {
                is CatalogInstalled -> localize(MR.strings.installed)
                else -> localize(MR.strings.available)
            }
        )

        // Package Name (for installed sources)
        if (catalog is CatalogInstalled) {
            DetailItem(
                label = "Package Name",
                value = catalog.pkgName
            )
        }

        HorizontalDivider()

        // Report as Broken Button
        Button(
            onClick = { showReportDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isReporting && catalog is CatalogInstalled,
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
            Text("Report as Broken")
        }

        // Statistics Section (if available)
        if (catalog is CatalogInstalled) {
            HorizontalDivider()
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // You can add more statistics here as needed
            DetailItem(
                label = "Version Code",
                value = catalog.versionCode.toString()
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
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun ReportSourceDialog(
    sourceName: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Source as Broken",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "You are about to report \"$sourceName\" as broken or not working properly.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Please describe the issue:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Source not loading, broken search, missing content...") },
                    minLines = 3,
                    maxLines = 5
                )
                
                Text(
                    text = "Your report will be stored locally for review.",
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
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
