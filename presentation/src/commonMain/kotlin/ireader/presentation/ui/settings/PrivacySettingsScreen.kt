package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Privacy settings screen for managing data collection and user consent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    analyticsEnabled: Boolean,
    crashReportingEnabled: Boolean,
    performanceMonitoringEnabled: Boolean,
    usageStatisticsEnabled: Boolean,
    diagnosticDataEnabled: Boolean,
    autoErrorReportingEnabled: Boolean,
    anonymousTrackingEnabled: Boolean,
    onAnalyticsChanged: (Boolean) -> Unit,
    onCrashReportingChanged: (Boolean) -> Unit,
    onPerformanceMonitoringChanged: (Boolean) -> Unit,
    onUsageStatisticsChanged: (Boolean) -> Unit,
    onDiagnosticDataChanged: (Boolean) -> Unit,
    onAutoErrorReportingChanged: (Boolean) -> Unit,
    onAnonymousTrackingChanged: (Boolean) -> Unit,
    onEnablePrivacyMode: () -> Unit,
    onDisablePrivacyMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Settings") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Privacy mode section
            PrivacyModeSection(
                onEnablePrivacyMode = onEnablePrivacyMode,
                onDisablePrivacyMode = onDisablePrivacyMode
            )
            
            HorizontalDivider()
            
            // Data collection settings
            PrivacySettingItem(
                title = "Analytics",
                description = "Help improve the app by sharing anonymous usage data",
                icon = Icons.Default.Analytics,
                checked = analyticsEnabled,
                onCheckedChange = onAnalyticsChanged
            )
            
            PrivacySettingItem(
                title = "Crash Reporting",
                description = "Automatically send crash reports to help fix bugs",
                icon = Icons.Default.BugReport,
                checked = crashReportingEnabled,
                onCheckedChange = onCrashReportingChanged
            )
            
            PrivacySettingItem(
                title = "Performance Monitoring",
                description = "Track app performance to identify slow operations",
                icon = Icons.Default.Speed,
                checked = performanceMonitoringEnabled,
                onCheckedChange = onPerformanceMonitoringChanged
            )
            
            PrivacySettingItem(
                title = "Usage Statistics",
                description = "Collect statistics about feature usage",
                icon = Icons.Default.BarChart,
                checked = usageStatisticsEnabled,
                onCheckedChange = onUsageStatisticsChanged
            )
            
            PrivacySettingItem(
                title = "Diagnostic Data",
                description = "Collect diagnostic data for troubleshooting",
                icon = Icons.Default.Healing,
                checked = diagnosticDataEnabled,
                onCheckedChange = onDiagnosticDataChanged
            )
            
            PrivacySettingItem(
                title = "Automatic Error Reporting",
                description = "Automatically report errors when they occur",
                icon = Icons.Default.ErrorOutline,
                checked = autoErrorReportingEnabled,
                onCheckedChange = onAutoErrorReportingChanged
            )
            
            PrivacySettingItem(
                title = "Anonymous Tracking",
                description = "Track app usage without identifying information",
                icon = Icons.Default.VisibilityOff,
                checked = anonymousTrackingEnabled,
                onCheckedChange = onAnonymousTrackingChanged
            )
            
            // Privacy information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your Privacy Matters",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All data collection is optional and can be disabled at any time. " +
                                        "We never collect personally identifiable information without your explicit consent.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyModeSection(
    onEnablePrivacyMode: () -> Unit,
    onDisablePrivacyMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Privacy Mode",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Quickly disable all data collection features",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Privacy Mode")
            }
            
            Button(
                onClick = onDisablePrivacyMode,
                modifier = Modifier.weight(1f)
            ) {
                Text("Disable Privacy Mode")
            }
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null) },
            title = { Text("Enable Privacy Mode?") },
            text = {
                Text("This will disable all data collection features including analytics, crash reporting, and diagnostics.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEnablePrivacyMode()
                        showDialog = false
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PrivacySettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
