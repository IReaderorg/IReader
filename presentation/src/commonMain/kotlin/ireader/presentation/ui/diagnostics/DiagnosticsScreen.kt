package ireader.presentation.ui.diagnostics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ireader.core.system.HealthCheckResult
import ireader.core.system.HealthStatus
import ireader.core.system.SystemInfo

/**
 * Diagnostics screen for system information and troubleshooting
 */
@Composable
fun DiagnosticsScreen(
    systemInfo: SystemInfo,
    healthCheckResult: HealthCheckResult,
    diagnosticReport: String,
    onRunHealthCheck: () -> Unit,
    onExportDiagnostics: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Diagnostics") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health status card
            HealthStatusCard(
                healthCheckResult = healthCheckResult,
                onRunHealthCheck = onRunHealthCheck
            )
            
            // System information card
            SystemInfoCard(systemInfo = systemInfo)
            
            // Memory information card
            MemoryInfoCard(systemInfo = systemInfo)
            
            // Actions card
            DiagnosticActionsCard(
                onExportDiagnostics = onExportDiagnostics,
                onClearCache = onClearCache
            )
            
            // Diagnostic report
            DiagnosticReportCard(diagnosticReport = diagnosticReport)
        }
    }
}

@Composable
private fun HealthStatusCard(
    healthCheckResult: HealthCheckResult,
    onRunHealthCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (healthCheckResult.status) {
                HealthStatus.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
                HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Icon(
                        imageVector = when (healthCheckResult.status) {
                            HealthStatus.HEALTHY -> Icons.Default.CheckCircle
                            HealthStatus.WARNING -> Icons.Default.Warning
                            HealthStatus.CRITICAL -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (healthCheckResult.status) {
                            HealthStatus.HEALTHY -> MaterialTheme.colorScheme.primary
                            HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                            HealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "System Health",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = healthCheckResult.status.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                IconButton(onClick = onRunHealthCheck) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Run Health Check"
                    )
                }
            }
            
            if (healthCheckResult.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Issues:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
                healthCheckResult.issues.forEach { issue ->
                    Text(
                        text = "• $issue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (healthCheckResult.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Warnings:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                healthCheckResult.warnings.forEach { warning ->
                    Text(
                        text = "• $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemInfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("OS", "${systemInfo.osName} ${systemInfo.osVersion}")
            InfoRow("Architecture", systemInfo.osArch)
            InfoRow("Java Version", systemInfo.javaVersion)
            InfoRow("Java Vendor", systemInfo.javaVendor)
            InfoRow("Processors", systemInfo.availableProcessors.toString())
        }
    }
}

@Composable
private fun MemoryInfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier
) {
    val usedMemory = systemInfo.totalMemoryMB - systemInfo.freeMemoryMB
    val memoryPercent = (usedMemory.toFloat() / systemInfo.totalMemoryMB.toFloat())
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Memory Information",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Total Memory", "${systemInfo.totalMemoryMB} MB")
            InfoRow("Used Memory", "$usedMemory MB")
            InfoRow("Free Memory", "${systemInfo.freeMemoryMB} MB")
            InfoRow("Max Memory", "${systemInfo.maxMemoryMB} MB")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = memoryPercent,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${(memoryPercent * 100).toInt()}% used",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiagnosticActionsCard(
    onExportDiagnostics: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium
            )
            
            Button(
                onClick = onExportDiagnostics,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Diagnostics")
            }
            
            OutlinedButton(
                onClick = onClearCache,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Cache")
            }
        }
    }
}

@Composable
private fun DiagnosticReportCard(
    diagnosticReport: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Diagnostic Report",
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = diagnosticReport,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
