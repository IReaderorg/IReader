package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Dialog for configuring custom Google Drive OAuth credentials & viewing instructions
 */
@Composable
fun GoogleDriveCredentialsDialog(
    initialClientId: String,
    initialClientSecret: String,
    onDismiss: () -> Unit,
    onSave: (clientId: String, clientSecret: String) -> Unit,
    onConnect: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var clientId by androidx.compose.runtime.remember(initialClientId) { 
        androidx.compose.runtime.mutableStateOf(initialClientId) 
    }
    var clientSecret by androidx.compose.runtime.remember(initialClientSecret) { 
        androidx.compose.runtime.mutableStateOf(initialClientSecret) 
    }
    var showHelp by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Google Drive Setup & Credentials")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configure your Google Cloud OAuth 2.0 credentials to enable cloud backup & delta sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = { Text("OAuth Client ID") },
                    placeholder = { Text("e.g. 123456...apps.googleusercontent.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clientSecret,
                    onValueChange = { clientSecret = it },
                    label = { Text("Client Secret (optional on Android)") },
                    placeholder = { Text("e.g. GOCSPX-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = { showHelp = !showHelp },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (showHelp) "Hide Setup Guide" else "How to get credentials?")
                }

                if (showHelp) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Visit console.cloud.google.com", style = MaterialTheme.typography.labelMedium)
                            Text("2. Create a project and enable 'Google Drive API'", style = MaterialTheme.typography.labelMedium)
                            Text("3. In 'OAuth consent screen', set up User Type and add '.../auth/drive.file' scope", style = MaterialTheme.typography.labelMedium)
                            Text("4. Create OAuth 2.0 Client ID (Android / Desktop Application)", style = MaterialTheme.typography.labelMedium)
                            Text("5. Paste the generated Client ID & Secret above", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(clientId, clientSecret)
                    onConnect()
                }
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for Google Drive OAuth2 authentication
 * 
 * Legacy informational dialog.
 */
@Composable
fun GoogleDriveAuthDialog(
    onDismiss: () -> Unit,
    onAuthComplete: (String) -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null
            )
        },
        title = {
            Text(localizeHelper.localize(Res.string.google_drive_authentication))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.to_enable_google_drive_backup_you_need_to),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.configure_oauth2_credentials_in) +
                            "2. Add Google Drive API dependencies\n" +
                            "3. Implement platform-specific authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = localizeHelper.localize(Res.string.this_feature_is_currently_in),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.ok))
            }
        }
    )
}

/**
 * Loading dialog shown during authentication
 */
@Composable
fun AuthenticationLoadingDialog(
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(localizeHelper.localize(Res.string.authenticating))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = localizeHelper.localize(Res.string.please_wait_while_we_connect_to_google_drive),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
