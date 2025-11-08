package ireader.presentation.ui.settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.LinkIcon
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    getFormattedBuildTime: () -> String,
    onPopBackStack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
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
    
    LazyColumn(
        modifier = modifier,
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
                            text = localize(MR.strings.version),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Copy to clipboard button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(versionInfo))
                                scope.launch {
                                    // Could show a snackbar here if needed
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy version info",
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
                title = localize(MR.strings.check_the_update),
                onClick = {
                    uriHandler.openUri("https://github.com/kazemcodes/Infinity/releases")
                },
            )
        }
        item {
            PreferenceRow(
                title = localize(MR.strings.whats_new),
                onClick = { uriHandler.openUri("https://github.com/kazemcodes/IReader/releases/latest") },
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
                        label = localize(MR.strings.website),
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
