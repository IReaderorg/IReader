package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocalNetworkPermissionRequest(url: String): (onResult: (Boolean) -> Unit) -> Unit {
    return { onResult -> onResult(true) }
}
