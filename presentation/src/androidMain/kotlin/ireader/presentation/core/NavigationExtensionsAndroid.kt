package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.TTSScreenSpec
import ireader.presentation.core.ui.WebViewScreenSpec

/**
 * Android-specific navigation extensions for expect classes
 */

actual fun NavHostController.navigateTo(spec: WebViewScreenSpec) {
    // For Android, navigate to webView with parameters
    navigate("webView")
}

actual fun NavHostController.navigateTo(spec: TTSScreenSpec) {
    navigate("tts/${spec.bookId}/${spec.chapterId}/${spec.sourceId}/${spec.readingParagraph}")
}
