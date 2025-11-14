package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.TTSScreenSpec
import ireader.presentation.core.ui.WebViewScreenSpec

/**
 * Android-specific navigation extensions for expect classes
 */

actual fun NavHostController.navigateTo(spec: WebViewScreenSpec) {
    // For Android, navigate to webView with all parameters
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
    )
}

actual fun NavHostController.navigateTo(spec: TTSScreenSpec) {
    navigate("tts/${spec.bookId}/${spec.chapterId}/${spec.sourceId}/${spec.readingParagraph}")
}
