package ireader.presentation.core.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android 17+ (API 37+) requires ACCESS_LOCAL_NETWORK (NEARBY_DEVICES group) for
 * direct LAN TCP connections, e.g. to a locally hosted OpenAI-compatible endpoint
 * at a private IP. See issue #295.
 *
 * Only Android 17+ enforces this; older versions grant local access implicitly
 * via INTERNET, so we never request on < 17 (the permission constant doesn't
 * exist there and would be ignored anyway).
 */
private const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

private fun hasLocalNetworkPermission(context: android.content.Context): Boolean {
    if (Build.VERSION.SDK_INT < 37) return true // permission not enforced before Android 17
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_LOCAL_NETWORK
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
actual fun rememberLocalNetworkPermissionRequest(url: String): (onResult: (Boolean) -> Unit) -> Unit {
    val context = LocalContext.current

    // Keep the latest callback in a holder so the launcher result can invoke it.
    val pending = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<(Boolean) -> Unit>({}) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> pending.value(granted) }

    return { onResult ->
        pending.value = onResult
        when {
            Build.VERSION.SDK_INT < 37 -> onResult(true) // not enforced
            !isLocalNetworkUrl(url) -> onResult(true)    // public endpoint, no LAN needed
            hasLocalNetworkPermission(context) -> onResult(true)
            else -> launcher.launch(LOCAL_NETWORK_PERMISSION)
        }
    }
}
