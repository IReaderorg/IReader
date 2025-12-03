package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.*
import io.ktor.http.encodeURLParameter

/**
 * Extension functions to convert ScreenSpec objects to string-based routes
 * for navigation. This solves the serialization issue by using string routes
 * instead of requiring @Serializable annotations.
 */

// Screen specs with parameters
fun NavHostController.navigate(spec: BookDetailScreenSpec) {
    navigate(NavigationRoutes.bookDetail(spec.bookId))
}

fun NavHostController.navigate(spec: ReaderScreenSpec) {
    navigate("reader/${spec.bookId}/${spec.chapterId}")
}

fun NavHostController.navigate(spec: ExploreScreenSpec) {
    val query = spec.query?.let { "&query=${it.encodeURLParameter()}" } ?: ""
    navigate("explore/${spec.sourceId}$query")
}

fun NavHostController.navigate(spec: GlobalSearchScreenSpec) {
    val query = spec.query?.let { "?query=${it.encodeURLParameter()}" } ?: ""
    navigate("globalSearch$query")
}

// Helper functions for expect classes (WebViewScreenSpec, TTSScreenSpec)
// These need to be called directly since we can't access expect class properties

fun NavHostController.navigateToWebView(
    url: String?,
    sourceId: Long?,
    bookId: Long?,
    chapterId: Long?,
    enableBookFetch: Boolean = false,
    enableChapterFetch: Boolean = false,
    enableChaptersFetch: Boolean = false
) {
    val encodedUrl = url?.encodeURLParameter() ?: ""
    navigate("webView/$sourceId/$bookId/$chapterId?url=$encodedUrl&enableBookFetch=$enableBookFetch&enableChapterFetch=$enableChapterFetch&enableChaptersFetch=$enableChaptersFetch")
}

/**
 * Helper function to ensure URL is absolute before passing to WebView.
 * Call this from view models where you have access to the source.
 * 
 * @param url The URL to convert (may be relative or absolute)
 * @param source The source object that has the baseUrl
 * @return Absolute URL suitable for WebView
 */
fun ensureAbsoluteUrlForWebView(url: String, source: ireader.core.source.Source?): String {
    // If already absolute, return as-is
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }
    
    // Try to get baseUrl from source
    if (source is ireader.core.source.HttpSource) {
        val baseUrl = source.baseUrl
        if (baseUrl.isNotBlank() && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
            // Construct absolute URL
            return if (url.startsWith("/")) {
                "$baseUrl$url"
            } else {
                "$baseUrl/$url"
            }
        }
    }
    
    // If we can't make it absolute, return as-is and log warning
    ireader.core.log.Log.warn("NavigationExtensions: Cannot convert relative URL to absolute: $url (no valid baseUrl)")
    return url
}

fun NavHostController.navigateToTTS(
    bookId: Long,
    chapterId: Long,
    sourceId: Long,
    readingParagraph: Int = 0
) {
    navigate("tts/$bookId/$chapterId/$sourceId/$readingParagraph")
}

// Screen specs without parameters
fun NavHostController.navigate(spec: ChatGptLoginScreenSpec) {
    navigate("chatGptLogin")
}

fun NavHostController.navigate(spec: DeepSeekLoginScreenSpec) {
    navigate("deepSeekLogin")
}

// SourceDetailScreen - requires Catalog object
// For now, we'll navigate to a route with the catalog's sourceId
fun NavHostController.navigate(spec: ireader.presentation.ui.home.sources.extension.SourceDetailScreen) {
    navigate("sourceDetail/${spec.catalog.sourceId}")
}
