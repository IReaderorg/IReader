package ireader.presentation.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.BuildKonfig
import ireader.i18n.Images.discord
import ireader.i18n.Images.github
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.LinkIcon
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    getFormattedBuildTime: () -> String,
    onPopBackStack: () -> Unit,
    onNavigateToChangelog: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    // Build version info string for copying
    val versionInfo = remember(getFormattedBuildTime()) {
        when {
            BuildKonfig.DEBUG -> {
                "Debug ${BuildKonfig.COMMIT_SHA} (${getFormattedBuildTime()})"
            }
            BuildKonfig.PREVIEW -> {
                "Preview r${BuildKonfig.COMMIT_COUNT} (${BuildKonfig.COMMIT_SHA}, ${getFormattedBuildTime()})"
            }
            else -> {
                "Stable ${BuildKonfig.VERSION_NAME} (${getFormattedBuildTime()})"
            }
        }
    }
    
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
        ) {
        item {
            LogoHeader()
        }
        
        // Enhanced version information card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Version header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localize(Res.string.version),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Copy to clipboard button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(versionInfo))
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Version info copied to clipboard",
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = localizeHelper.localize(Res.string.copy_version_info),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Version details with improved typography
                    when {
                        BuildKonfig.DEBUG -> {
                            VersionInfoRow(
                                label = "Build Type",
                                value = "Debug"
                            )
                            VersionInfoRow(
                                label = "Commit",
                                value = BuildKonfig.COMMIT_SHA
                            )
                            VersionInfoRow(
                                label = "Build Time",
                                value = getFormattedBuildTime()
                            )
                        }
                        BuildKonfig.PREVIEW -> {
                            VersionInfoRow(
                                label = "Build Type",
                                value = "Preview"
                            )
                            VersionInfoRow(
                                label = "Release",
                                value = "r${BuildKonfig.COMMIT_COUNT}"
                            )
                            VersionInfoRow(
                                label = "Commit",
                                value = BuildKonfig.COMMIT_SHA
                            )
                            VersionInfoRow(
                                label = "Build Time",
                                value = getFormattedBuildTime()
                            )
                        }
                        else -> {
                            VersionInfoRow(
                                label = "Build Type",
                                value = "Stable"
                            )
                            VersionInfoRow(
                                label = "Version",
                                value = BuildKonfig.VERSION_NAME
                            )
                            VersionInfoRow(
                                label = "Build Time",
                                value = getFormattedBuildTime()
                            )
                        }
                    }
                }
            }
        }
        
        item {
            PreferenceRow(
                title = localize(Res.string.check_the_update),
                subtitle = if (isCheckingUpdate) "Checking for updates..." else null,
                onClick = {
                    if (!isCheckingUpdate) {
                        isCheckingUpdate = true
                        scope.launch {
                            try {
                                // Show checking message
                                snackbarHostState.showSnackbar(
                                    message = "Checking for updates...",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                                
                                // Simulate checking (in real implementation, this would check GitHub API)
                                kotlinx.coroutines.delay(1500)
                                
                                // For now, we'll just open the releases page
                                // In a real implementation, you would:
                                // 1. Fetch latest release from GitHub API
                                // 2. Compare with current version
                                // 3. Show appropriate message
                                
                                try {
                                    uriHandler.openUri("https://github.com/kazemcodes/Infinity/releases")
                                    
                                    // Show success message
                                    snackbarHostState.showSnackbar(
                                        message = "Opening releases page. Check if a newer version is available.",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                } catch (uriException: Exception) {
                                    // Failed to open browser
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to open browser. Please visit GitHub manually.",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                }
                                
                                isCheckingUpdate = false
                            } catch (e: Exception) {
                                isCheckingUpdate = false
                                // Network or other error
                                snackbarHostState.showSnackbar(
                                    message = "Failed to check for updates. Please check your internet connection and try again.",
                                    duration = androidx.compose.material3.SnackbarDuration.Long
                                )
                            }
                        }
                    }
                },
                action = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
        item {
            PreferenceRow(
                title = localize(Res.string.whats_new),
                subtitle = "View version history and new features",
                icon = Icons.Outlined.History,
                onClick = onNavigateToChangelog
            )
        }
        // Enhanced social links section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connect with us",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinkIcon(
                        label = localize(Res.string.website),
                        painter = rememberVectorPainter(Icons.Outlined.Public),
                        url = "https://github.com/kazemcodes/IReader",
                    )
                    LinkIcon(
                        label = "Discord",
                        icon = discord(),
                        url = "https://discord.gg/HBU6zD8c5v",
                    )
                    LinkIcon(
                        label = "GitHub",
                        icon = github(),
                        url = "https://github.com/kazemcodes/IReader",
                    )
                }
            }
        }
        
        // Support Development section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Support Development",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PreferenceRow(
                    title = "Donate via Card",
                    subtitle = "Support the project with a donation",
                    onClick = {
                        uriHandler.openUri("https://reymit.ir/kazemcodes")
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun VersionInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
