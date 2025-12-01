package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.WebViewScreenSpec

/**
 * Android-specific navigation extensions for expect classes
 */

actual fun NavHostController.navigateTo(spec: WebViewScreenSpec) {
    // For Android, navigate to webView with all parameters
    // Using launchSingleTop and popUpTo to prevent duplicate destinations and manage back stack
    navigate(
        NavigationRoutes.webView(
            url = spec.url ?: "",
            sourceId = spec.sourceId,
            bookId = spec.bookId,
            chapterId = spec.chapterId,
            enableBookFetch = spec.enableBookFetch,
            enableChapterFetch = spec.enableChapterFetch,
            enableChaptersFetch = spec.enableChaptersFetch
        )
    ) {
        launchSingleTop = true
        // Clear other WebView instances to prevent memory buildup
        popUpTo("webView") {
            inclusive = true
        }
    }
}

// TTSScreenSpec navigation is now defined in common Navigator.kt
