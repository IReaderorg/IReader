package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.WebViewScreenSpec
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS-specific navigation extensions
 */
actual fun NavHostController.navigateTo(spec: WebViewScreenSpec) {
    // For iOS, open URL in Safari
    spec.url?.let { urlString ->
        val url = NSURL.URLWithString(urlString)
        if (url != null) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
