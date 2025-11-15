package ireader.presentation.ui.plugins.management.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Confirmation dialog for uninstalling a plugin
 * Requirements: 14.3
 */
@Composable
fun UninstallConfirmationDialog(
    pluginName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uninstall Plugin") },
        text = {
            Text("Are you sure you want to uninstall \"$pluginName\"? All plugin data will be removed.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Uninstall")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}
