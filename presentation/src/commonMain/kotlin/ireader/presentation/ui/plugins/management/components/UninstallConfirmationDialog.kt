package ireader.presentation.ui.plugins.management.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.uninstall_plugin)) },
        text = {
            Text("Are you sure you want to uninstall \"$pluginName\"? All plugin data will be removed.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localizeHelper.localize(Res.string.uninstall))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        },
        modifier = modifier
    )
}
