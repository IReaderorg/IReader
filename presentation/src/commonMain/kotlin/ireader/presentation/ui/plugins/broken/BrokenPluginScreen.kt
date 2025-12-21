package ireader.presentation.ui.plugins.broken

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Information about a broken plugin.
 */
data class BrokenPluginInfo(
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val errorMessage: String,
    val possibleCauses: List<String>,
    val suggestions: List<String>,
    val canUpdate: Boolean = false,
    val canUninstall: Boolean = true
)

/**
 * Full screen displayed when a plugin is not working.
 * Similar to RequiredPluginHandler but for broken/malfunctioning plugins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokenPluginScreen(
    info: BrokenPluginInfo,
    onBack: () -> Unit,
    onUpdatePlugin: (() -> Unit)?,
    onUpdateApp: () -> Unit,
    onUninstall: (() -> Unit)?,
    onSkipForNow: () -> Unit,
    onReportIssue: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scrollState = rememberScrollState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.plugin_not_working)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Plugin Name
            Text(
                text = info.pluginName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Version
            Text(
                text = "Version ${info.version}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Message Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.notification_error),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = info.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Possible Causes
            if (info.possibleCauses.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.possible_causes),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        info.possibleCauses.forEach { cause ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cause,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Suggestions
            if (info.suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.what_you_can_try),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        info.suggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Update Plugin button
                if (info.canUpdate && onUpdatePlugin != null) {
                    Button(
                        onClick = onUpdatePlugin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.update_plugin))
                    }
                }
                
                // Update App button
                OutlinedButton(
                    onClick = onUpdateApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.check_for_app_updates))
                }
                
                // Uninstall button
                if (info.canUninstall && onUninstall != null) {
                    OutlinedButton(
                        onClick = onUninstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.uninstall_plugin))
                    }
                }
                
                // Report Issue button
                if (onReportIssue != null) {
                    TextButton(
                        onClick = onReportIssue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.report_issue))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skip for now button
            TextButton(
                onClick = onSkipForNow
            ) {
                Text(localizeHelper.localize(Res.string.skip_for_now_dont_show_again))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Create BrokenPluginInfo from an exception.
 */
fun createBrokenPluginInfo(
    pluginId: String,
    pluginName: String,
    version: String,
    error: Throwable
): BrokenPluginInfo {
    val errorMessage = error.message ?: "Unknown error"
    val errorLower = errorMessage.lowercase()
    
    val (causes, suggestions) = when {
        errorLower.contains("class not found") || errorLower.contains("no class def") -> {
            listOf(
                "Plugin is incompatible with current app version",
                "Plugin was built for a different version of the app",
                "Required dependency is missing"
            ) to listOf(
                "Update the plugin to the latest version",
                "Update the app to the latest version",
                "Uninstall and reinstall the plugin"
            )
        }
        errorLower.contains("native library") || errorLower.contains("unsatisfied link") -> {
            listOf(
                "Plugin requires native libraries not available on this device",
                "Plugin is not compatible with your device architecture"
            ) to listOf(
                "Check if there's a version compatible with your device",
                "Contact the plugin developer for support"
            )
        }
        errorLower.contains("permission") -> {
            listOf(
                "Plugin requires permissions that were not granted",
                "App permissions have changed"
            ) to listOf(
                "Check app permissions in system settings",
                "Reinstall the plugin"
            )
        }
        errorLower.contains("timeout") || errorLower.contains("network") -> {
            listOf(
                "Network connection issue",
                "Plugin server is not responding"
            ) to listOf(
                "Check your internet connection",
                "Try again later"
            )
        }
        else -> {
            listOf(
                "Plugin encountered an unexpected error",
                "Plugin may be corrupted or outdated"
            ) to listOf(
                "Update the plugin to the latest version",
                "Update the app to the latest version",
                "Uninstall and reinstall the plugin",
                "Report the issue to the plugin developer"
            )
        }
    }
    
    return BrokenPluginInfo(
        pluginId = pluginId,
        pluginName = pluginName,
        version = version,
        errorMessage = errorMessage,
        possibleCauses = causes,
        suggestions = suggestions,
        canUpdate = true,
        canUninstall = true
    )
}
