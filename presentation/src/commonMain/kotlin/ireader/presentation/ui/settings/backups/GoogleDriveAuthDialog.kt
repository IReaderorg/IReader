package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Dialog for Google Drive OAuth2 authentication
 * 
 * This is a placeholder dialog that explains the authentication process.
 * In a full implementation, this would:
 * - On Android: Launch GoogleSignInClient with ActivityResultContracts
 * - On Desktop: Open browser with OAuth URL and listen for callback
 * - On iOS: Use Google Sign-In iOS SDK
 */
@Composable
fun GoogleDriveAuthDialog(
    onDismiss: () -> Unit,
    onAuthComplete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null
            )
        },
        title = {
            Text("Google Drive Authentication")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "To enable Google Drive backup, you need to:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "1. Configure OAuth2 credentials in Google Cloud Console\n" +
                            "2. Add Google Drive API dependencies\n" +
                            "3. Implement platform-specific authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "This feature is currently in development and requires additional setup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Authenticating...")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Please wait while we connect to Google Drive",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
