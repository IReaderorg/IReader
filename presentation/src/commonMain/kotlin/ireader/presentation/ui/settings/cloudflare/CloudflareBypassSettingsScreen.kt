package ireader.presentation.ui.settings.cloudflare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.http.cloudflare.BypassManagerStatus
import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.http.cloudflare.CloudflareBypassProvider
import ireader.presentation.ui.component.DownloadPhase
import ireader.presentation.ui.component.ExternalResourceDownloadProgress
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.launch

/**
 * Settings screen for Cloudflare bypass configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudflareBypassSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    bypassManager: CloudflareBypassPluginManager,
    flareSolverrUrl: String,
    onFlareSolverrUrlChange: (String) -> Unit,
    // FlareSolverr download state (optional - for on-demand download feature)
    flareSolverrDownloadState: FlareSolverrDownloadState? = null,
    onDownloadFlareSolverr: (() -> Unit)? = null,
    onStartFlareSolverr: (() -> Unit)? = null,
    onStopFlareSolverr: (() -> Unit)? = null,
    isFlareSolverrRunning: Boolean = false
) {
    val localizeHelper = LocalLocalizeHelper.current
    val scope = rememberCoroutineScope()
    
    val providers by bypassManager.providers.collectAsState()
    val status by bypassManager.status.collectAsState()
    
    var showUrlDialog by remember { mutableStateOf(false) }
    var isCheckingAvailability by remember { mutableStateOf(false) }
    var availabilityResults by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var testConnectionResult by remember { mutableStateOf<Boolean?>(null) }
    
    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Cloudflare Bypass",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            // Status Section
            item {
                StatusCard(
                    status = status,
                    providerCount = providers.size
                )
            }
            
            // Info Section
            item {
                InfoCard()
            }
            
            // FlareSolverr Download Card (if download state is provided)
            if (flareSolverrDownloadState != null && onDownloadFlareSolverr != null) {
                item {
                    FlareSolverrStatusCard(
                        state = flareSolverrDownloadState,
                        isServerRunning = isFlareSolverrRunning,
                        onDownloadClick = onDownloadFlareSolverr,
                        onStartServer = onStartFlareSolverr ?: {},
                        onStopServer = onStopFlareSolverr ?: {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            // FlareSolverr Configuration (for external server)
            item {
                FlareSolverrConfigCard(
                    serverUrl = flareSolverrUrl,
                    onConfigureClick = { showUrlDialog = true },
                    onTestClick = {
                        scope.launch {
                            isCheckingAvailability = true
                            testConnectionResult = null
                            val available = bypassManager.hasAvailableProvider()
                            testConnectionResult = available
                            availabilityResults = providers.associate { 
                                it.id to runCatching { 
                                    kotlinx.coroutines.runBlocking { it.isAvailable() }
                                }.getOrDefault(false)
                            }
                            isCheckingAvailability = false
                        }
                    },
                    isChecking = isCheckingAvailability,
                    testResult = testConnectionResult
                )
            }
            
            // Providers Section
            item {
                SectionHeader(title = "Installed Bypass Providers")
            }
            
            if (providers.isEmpty()) {
                item {
                    EmptyProvidersCard()
                }
            } else {
                items(providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        isAvailable = availabilityResults[provider.id]
                    )
                }
            }
            
            // Cache Section
            item {
                CacheCard(
                    onClearCache = { bypassManager.clearCache() }
                )
            }
        }
    }
    
    // URL Configuration Dialog
    if (showUrlDialog) {
        FlareSolverrUrlDialog(
            currentUrl = flareSolverrUrl,
            onDismiss = { showUrlDialog = false },
            onConfirm = { url ->
                onFlareSolverrUrlChange(url)
                showUrlDialog = false
            }
        )
    }
    
    // FlareSolverr Download Dialog
    if (flareSolverrDownloadState != null && onDownloadFlareSolverr != null) {
        FlareSolverrDownloadDialog(
            state = flareSolverrDownloadState,
            onDownload = onDownloadFlareSolverr,
            onDismiss = { flareSolverrDownloadState.hideDialog() }
        )
    }
}

@Composable
private fun StatusCard(
    status: BypassManagerStatus,
    providerCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is BypassManagerStatus.Ready -> MaterialTheme.colorScheme.primaryContainer
                BypassManagerStatus.NoProviders -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (status) {
                    is BypassManagerStatus.Ready -> Icons.Default.Check
                    BypassManagerStatus.NoProviders -> Icons.Default.Close
                },
                contentDescription = null,
                tint = when (status) {
                    is BypassManagerStatus.Ready -> MaterialTheme.colorScheme.onPrimaryContainer
                    BypassManagerStatus.NoProviders -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when (status) {
                        is BypassManagerStatus.Ready -> "Bypass Ready"
                        BypassManagerStatus.NoProviders -> "No Bypass Providers"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (status) {
                        is BypassManagerStatus.Ready -> "$providerCount provider(s) available"
                        BypassManagerStatus.NoProviders -> "Install a bypass provider to access protected sources"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Cloudflare bypass allows accessing sources protected by Cloudflare. " +
                       "The recommended solution is FlareSolverr, which runs a browser to solve challenges automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FlareSolverrConfigCard(
    serverUrl: String,
    onConfigureClick: () -> Unit,
    onTestClick: () -> Unit,
    isChecking: Boolean,
    testResult: Boolean? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FlareSolverr Server",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onConfigureClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure")
                }
                
                Button(
                    onClick = onTestClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Connection")
                }
            }
            
            // Show test result
            if (testResult != null && !isChecking) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (testResult) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (testResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (testResult) "Connection successful!" else "Connection failed - server not responding",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (testResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptyProvidersCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No bypass providers installed",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "The built-in FlareSolverr provider will be used if the service is available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProviderCard(
    provider: CloudflareBypassProvider,
    isAvailable: Boolean?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Priority: ${provider.priority} • ${provider.getStatusDescription()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            when (isAvailable) {
                true -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Available",
                    tint = MaterialTheme.colorScheme.primary
                )
                false -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unavailable",
                    tint = MaterialTheme.colorScheme.error
                )
                null -> { /* Not checked yet */ }
            }
        }
    }
}

@Composable
private fun CacheCard(
    onClearCache: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cookie Cache",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Cached bypass cookies are reused to avoid repeated challenges",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            TextButton(onClick = onClearCache) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun FlareSolverrUrlDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("FlareSolverr Server URL") },
        text = {
            Column {
                Text(
                    text = "Enter the URL of your FlareSolverr server:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("http://localhost:8191/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Default: http://localhost:8191/v1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
