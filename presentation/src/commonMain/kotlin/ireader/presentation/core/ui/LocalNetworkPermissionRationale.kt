package ireader.presentation.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Shows a rationale dialog before requesting ACCESS_LOCAL_NETWORK, then either
 * fires [onRequestPermission] (which shows the system Nearby devices prompt)
 * or [onProceedWithoutPermission] (e.g. the caller still attempts the action
 * and surfaces its own error).
 *
 * On platforms / configurations where the permission is not applicable (non-Android,
 * Android < 17, public URL, already granted), [onRequestPermission] is invoked
 * directly without showing the rationale.
 *
 * Usage: keep the returned lambda in a `remember`ed var; call it when the user
 * performs the action that needs LAN access (e.g. tapping Translate).
 */
@Composable
fun rememberLocalNetworkPermissionWithRationale(
    url: String,
    requestPermission: (onResult: (Boolean) -> Unit) -> Unit,
    onProceed: (granted: Boolean) -> Unit
): () -> Unit {
    var showRationale by remember { mutableStateOf(false) }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Local network access") },
            text = { Text(LOCAL_NETWORK_PERMISSION_RATIONALE) },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    requestPermission { granted -> onProceed(granted) }
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onProceed(false)
                }) { Text("Not now") }
            }
        )
    }

    return {
        if (isLocalNetworkUrl(url)) showRationale = true
        else onProceed(true)
    }
}
