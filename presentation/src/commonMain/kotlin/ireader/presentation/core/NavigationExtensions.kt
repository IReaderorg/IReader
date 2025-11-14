package ireader.presentation.core

import androidx.navigation.NavHostController
import ireader.presentation.core.ui.*
import java.net.URLEncoder

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
    val query = spec.query?.let { "&query=${URLEncoder.encode(it, "UTF-8")}" } ?: ""
    navigate("explore/${spec.sourceId}$query")
}

fun NavHostController.navigate(spec: GlobalSearchScreenSpec) {
    val query = spec.query?.let { "?query=${URLEncoder.encode(it, "UTF-8")}" } ?: ""
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
    val encodedUrl = url?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
    navigate("webView/$sourceId/$bookId/$chapterId?url=$encodedUrl&enableBookFetch=$enableBookFetch&enableChapterFetch=$enableChapterFetch&enableChaptersFetch=$enableChaptersFetch")
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
