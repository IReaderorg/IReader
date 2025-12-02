package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.WebViewScreenSpec

/**
 * Desktop-specific navigation extensions for expect classes
 */

actual fun NavHostController.navigateTo(spec: WebViewScreenSpec) {
    // For desktop, open URL in external browser directly
    spec.url?.let { urlString ->
        try {
            val desktop = java.awt.Desktop.getDesktop()
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                desktop.browse(java.net.URI(urlString))
            }
        } catch (_: Exception) {
            // Silently ignore browser open errors
        }
    }
}

// TTSScreenSpec navigation is now defined in common Navigator.kt
