package ireader.presentation.ui.plugins.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginMonetization
import ireader.domain.plugins.PluginStatus
import ireader.presentation.ui.plugins.details.InstallationState
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Install button handling different states and plugin types
 * Requirements: 2.4, 2.5, 8.1, 8.2, 8.3
 */
@Composable
fun InstallButton(
    plugin: PluginInfo,
    installationState: InstallationState,
    installProgress: Float,
    onInstall: () -> Unit,
    onPurchase: () -> Unit,
    onOpen: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        when (installationState) {
            is InstallationState.NotInstalled -> {
                NotInstalledButton(
                    plugin = plugin,
                    onInstall = onInstall,
                    onPurchase = onPurchase
                )
            }
            is InstallationState.Downloading -> {
                DownloadingButton(progress = installProgress)
            }
            is InstallationState.Installing -> {
                InstallingButton()
            }
            is InstallationState.Installed -> {
                InstalledButton(
                    plugin = plugin,
                    onOpen = onOpen
                )
            }
            is InstallationState.Error -> {
                ErrorButton(
                    message = installationState.message,
                    onRetry = onRetry
                )
            }
        }
    }
}

/**
 * Button for not installed plugins
 */
@Composable
private fun NotInstalledButton(
    plugin: PluginInfo,
    onInstall: () -> Unit,
    onPurchase: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val monetization = plugin.manifest.monetization
    
    when (monetization) {
        is PluginMonetization.Premium -> {
            if (plugin.isPurchased) {
                Button(
                    onClick = onInstall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localizeHelper.localize(Res.string.install))
                }
            } else {
                Button(
                    onClick = onPurchase,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buy for ${monetization.currency} ${String.format("%.2f", monetization.price)}")
                }
            }
        }
        is PluginMonetization.Freemium -> {
            Button(
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.install_free_with_in_app_purchases))
            }
        }
        is PluginMonetization.Free, null -> {
            Button(
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.install))
            }
        }
    }
}

/**
 * Button showing download progress
 */
@Composable
private fun DownloadingButton(progress: Float) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.downloading_1),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Button showing installation in progress
 */
@Composable
private fun InstallingButton() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = localizeHelper.localize(Res.string.installing_1),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Button for installed plugins
 */
@Composable
private fun InstalledButton(
    plugin: PluginInfo,
    onOpen: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    when (plugin.status) {
        PluginStatus.ENABLED -> {
            OutlinedButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.enabled))
            }
        }
        PluginStatus.DISABLED -> {
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.enable))
            }
        }
        PluginStatus.ERROR -> {
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(localizeHelper.localize(Res.string.error_tap_to_retry))
            }
        }
        PluginStatus.UPDATING -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.updating))
            }
        }
    }
}

/**
 * Button for error state
 */
@Composable
private fun ErrorButton(
    message: String,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(localizeHelper.localize(Res.string.retry))
        }
    }
}
