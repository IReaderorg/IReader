package ireader.presentation.ui.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.settings.components.SettingsDivider
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader
import ireader.presentation.ui.settings.components.SettingsSwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    viewModel: NetworkSettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Network") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // User Agent Section
            SettingsSectionHeader(
                title = "User Agent",
                icon = Icons.Default.Language
            )

            SettingsSwitchItem(
                title = "Use Default User Agent",
                description = "Use the app's default user agent string",
                checked = state.useDefaultUserAgent,
                onCheckedChange = viewModel::setUseDefaultUserAgent
            )

            if (!state.useDefaultUserAgent) {
                var customUA by remember { mutableStateOf(state.customUserAgent) }
                OutlinedTextField(
                    value = customUA,
                    onValueChange = {
                        customUA = it
                        viewModel.setCustomUserAgent(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Custom User Agent") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            SettingsDivider()

            // Cookies Section
            SettingsSectionHeader(
                title = "Cookies",
                icon = Icons.Default.Delete
            )

            SettingsItem(
                title = "Clear Cookies",
                description = "Remove all stored cookies",
                onClick = viewModel::showClearCookiesDialog,
                showNavigationIcon = false
            )

            SettingsDivider()

            // Proxy Section
            SettingsSectionHeader(
                title = "Proxy",
                icon = Icons.Default.Wifi
            )

            SettingsSwitchItem(
                title = "Enable Proxy",
                description = "Route requests through a proxy server",
                checked = state.proxyEnabled,
                onCheckedChange = viewModel::setProxyEnabled
            )

            if (state.proxyEnabled) {
                var proxyHost by remember { mutableStateOf(state.proxyHost) }
                var proxyPort by remember { mutableStateOf(state.proxyPort) }

                OutlinedTextField(
                    value = proxyHost,
                    onValueChange = {
                        proxyHost = it
                        viewModel.setProxyHost(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Proxy Host") },
                    placeholder = { Text("e.g., proxy.example.com") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = proxyPort,
                    onValueChange = {
                        proxyPort = it
                        viewModel.setProxyPort(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Proxy Port") },
                    placeholder = { Text("e.g., 8080") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            // Spacer at bottom
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Clear Cookies Dialog
    if (state.showClearCookiesDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideClearCookiesDialog,
            title = { Text("Clear Cookies") },
            text = { Text("Are you sure you want to clear all cookies? This may log you out of some sources.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearCookies) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideClearCookiesDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}