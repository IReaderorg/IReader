package ireader.presentation.ui.web

import android.webkit.WebView
import ireader.core.source.CatalogSource
import ireader.core.source.findInstance
import ireader.core.source.model.Command
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Auto-fetch detector for WebView novel content.
 * 
 * This implementation is based on the Source's Command capabilities rather than
 * generic URL patterns. It checks what commands the source supports (Detail.Fetch,
 * Chapter.Fetch, Content.Fetch) and only attempts detection and fetching for
 * content types that the source can handle.
 * 
 * Detection strategy:
 * 1. Check source capabilities via getCommands()
 * 2. Match URL patterns against supported capabilities
 * 3. Verify DOM structure for supported content types
 * 4. Only trigger auto-fetch if source supports the detected content type
 */

/**
 * Result of novel content detection
 */
sealed class NovelDetectionResult {
    /**
     * Novel content detected
     */
    data class Detected(val type: DetectionType, val confidence: Float) : NovelDetectionResult()
    
    /**
     * No novel content detected
     */
    object NotDetected : NovelDetectionResult()
    
    /**
     * Type of novel content detected
     */
    enum class DetectionType {
        BOOK_DETAIL,
        CHAPTER_LIST,
        CHAPTER_CONTENT
    }
}

/**
 * Result of auto-fetch operation
 */
sealed class FetchResult {
    data class Success(val message: String) : FetchResult()
    data class Error(val message: String) : FetchResult()
    object Skipped : FetchResult()
}

/**
 * Interface for detecting and auto-fetching novel content from web pages
 */
interface AutoFetchDetector {
    /**
     * Detect if the current URL contains novel content
     */
    suspend fun detectNovelContent(url: String, webView: WebView, source: CatalogSource?): NovelDetectionResult
    
    /**
     * Automatically fetch novel content based on detection
     */
    suspend fun autoFetch(
        url: String,
        webView: WebView,
        source: CatalogSource?,
        viewModel: WebViewPageModel
    ): FetchResult
}

/**
 * Default implementation of AutoFetchDetector
 * Detection is based on Source capabilities (Commands) rather than URL patterns
 */
class DefaultAutoFetchDetector : AutoFetchDetector {
    
    companion object {
        // Common URL patterns for novel sites (fallback detection)
        private val BOOK_DETAIL_PATTERNS = listOf(
            Regex("/(novel|book|series|story)/[^/]+/?$", RegexOption.IGNORE_CASE),
            Regex("/detail/[^/]+/?$", RegexOption.IGNORE_CASE),
            Regex("/info/[^/]+/?$", RegexOption.IGNORE_CASE),
            Regex("/manga/[^/]+/?$", RegexOption.IGNORE_CASE)
        )
        
        private val CHAPTER_LIST_PATTERNS = listOf(
            Regex("/(novel|book)/[^/]+/(chapters?|toc|index)", RegexOption.IGNORE_CASE),
            Regex("/chapters?/[^/]+/?$", RegexOption.IGNORE_CASE)
        )
        
        private val CHAPTER_CONTENT_PATTERNS = listOf(
            Regex("/(chapter|read)/[^/]+/[^/]+", RegexOption.IGNORE_CASE),
            Regex("/read/[^/]+/?$", RegexOption.IGNORE_CASE),
            Regex("/[^/]+/chapter-\\d+", RegexOption.IGNORE_CASE)
        )
        
        // DOM selectors that indicate novel content (fallback detection)
        private val BOOK_DETAIL_SELECTORS = listOf(
            ".novel-info",
            ".book-info",
            ".series-info",
            "#novel-info",
            ".manga-info",
            "[class*='book-detail']",
            "[class*='novel-detail']"
        )
        
        private val CHAPTER_LIST_SELECTORS = listOf(
            ".chapter-list",
            ".chapters",
            "#chapter-list",
            "[class*='chapter-list']",
            "ul.chapters",
            ".toc"
        )
        
        private val CHAPTER_CONTENT_SELECTORS = listOf(
            ".chapter-content",
            ".chapter-body",
            "#chapter-content",
            "[class*='chapter-content']",
            ".reading-content",
            "[id*='chapter-content']"
        )
    }
    
    override suspend fun detectNovelContent(
        url: String,
        webView: WebView,
        source: CatalogSource?
    ): NovelDetectionResult = withContext(Dispatchers.Main) {
        if (source == null) {
            return@withContext NovelDetectionResult.NotDetected
        }
        
        // Get available commands from the source
        val commands = source.getCommands()
        val hasDetailFetch = commands.findInstance<Command.Detail.Fetch>() != null
        val hasChapterFetch = commands.findInstance<Command.Chapter.Fetch>() != null
        val hasContentFetch = commands.findInstance<Command.Content.Fetch>() != null
        
        // Priority 1: Check URL patterns combined with source capabilities
        val urlDetection = detectFromUrlWithSourceCapabilities(url, hasDetailFetch, hasChapterFetch, hasContentFetch)
        if (urlDetection is NovelDetectionResult.Detected) {
            return@withContext urlDetection
        }
        
        // Priority 2: Check DOM structure combined with source capabilities
        val domDetection = detectFromDomWithSourceCapabilities(webView, hasDetailFetch, hasChapterFetch, hasContentFetch)
        return@withContext domDetection
    }
    
    private fun detectFromUrlWithSourceCapabilities(
        url: String,
        hasDetailFetch: Boolean,
        hasChapterFetch: Boolean,
        hasContentFetch: Boolean
    ): NovelDetectionResult {
        // Check for chapter content patterns first (most specific)
        if (hasContentFetch && CHAPTER_CONTENT_PATTERNS.any { it.containsMatchIn(url) }) {
            return NovelDetectionResult.Detected(
                NovelDetectionResult.DetectionType.CHAPTER_CONTENT,
                0.9f // High confidence due to source capability + URL pattern
            )
        }
        
        // Check for chapter list patterns
        if (hasChapterFetch && CHAPTER_LIST_PATTERNS.any { it.containsMatchIn(url) }) {
            return NovelDetectionResult.Detected(
                NovelDetectionResult.DetectionType.CHAPTER_LIST,
                0.9f
            )
        }
        
        // Check for book detail patterns
        if (hasDetailFetch && BOOK_DETAIL_PATTERNS.any { it.containsMatchIn(url) }) {
            return NovelDetectionResult.Detected(
                NovelDetectionResult.DetectionType.BOOK_DETAIL,
                0.9f
            )
        }
        
        return NovelDetectionResult.NotDetected
    }
    
    private suspend fun detectFromDomWithSourceCapabilities(
        webView: WebView,
        hasDetailFetch: Boolean,
        hasChapterFetch: Boolean,
        hasContentFetch: Boolean
    ): NovelDetectionResult = withContext(Dispatchers.Main) {
        try {
            // Check for chapter content elements first (most specific)
            if (hasContentFetch) {
                for (selector in CHAPTER_CONTENT_SELECTORS) {
                    val hasElement = checkElementExists(webView, selector)
                    if (hasElement) {
                        return@withContext NovelDetectionResult.Detected(
                            NovelDetectionResult.DetectionType.CHAPTER_CONTENT,
                            0.8f // Good confidence due to source capability + DOM element
                        )
                    }
                }
            }
            
            // Check for chapter list elements
            if (hasChapterFetch) {
                for (selector in CHAPTER_LIST_SELECTORS) {
                    val hasElement = checkElementExists(webView, selector)
                    if (hasElement) {
                        return@withContext NovelDetectionResult.Detected(
                            NovelDetectionResult.DetectionType.CHAPTER_LIST,
                            0.8f
                        )
                    }
                }
            }
            
            // Check for book detail elements
            if (hasDetailFetch) {
                for (selector in BOOK_DETAIL_SELECTORS) {
                    val hasElement = checkElementExists(webView, selector)
                    if (hasElement) {
                        return@withContext NovelDetectionResult.Detected(
                            NovelDetectionResult.DetectionType.BOOK_DETAIL,
                            0.8f
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // If DOM analysis fails, return not detected
            return@withContext NovelDetectionResult.NotDetected
        }
        
        return@withContext NovelDetectionResult.NotDetected
    }

    
    private suspend fun checkElementExists(webView: WebView, selector: String): Boolean = withContext(Dispatchers.Main) {
        try {
            // Use JavaScript to check if element exists
            val script = """
                (function() {
                    var element = document.querySelector('$selector');
                    return element !== null;
                })();
            """.trimIndent()
            
            var result = false
            webView.evaluateJavascript(script) { value ->
                result = value == "true"
            }
            
            // Give a small delay for the JavaScript to execute
            kotlinx.coroutines.delay(100)
            return@withContext result
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    override suspend fun autoFetch(
        url: String,
        webView: WebView,
        source: CatalogSource?,
        viewModel: WebViewPageModel
    ): FetchResult {
        if (source == null || !viewModel.autoFetchEnabled) {
            return FetchResult.Skipped
        }
        
        // Get source capabilities
        val commands = source.getCommands()
        val hasDetailFetch = commands.findInstance<Command.Detail.Fetch>() != null
        val hasChapterFetch = commands.findInstance<Command.Chapter.Fetch>() != null
        val hasContentFetch = commands.findInstance<Command.Content.Fetch>() != null
        
        // Detect content type based on source capabilities
        val detection = detectNovelContent(url, webView, source)
        
        return when (detection) {
            is NovelDetectionResult.Detected -> {
                when (detection.type) {
                    NovelDetectionResult.DetectionType.BOOK_DETAIL -> {
                        // Auto-fetch book details only if source supports it
                        if (hasDetailFetch && viewModel.enableBookFetch) {
                            try {
                                viewModel.getDetails(webView)
                                FetchResult.Success("Book details fetched automatically")
                            } catch (e: Exception) {
                                FetchResult.Error("Failed to fetch book details: ${e.message}")
                            }
                        } else {
                            FetchResult.Skipped
                        }
                    }
                    NovelDetectionResult.DetectionType.CHAPTER_LIST -> {
                        // Auto-fetch chapters only if source supports it
                        val book = viewModel.stateBook
                        if (hasChapterFetch && book != null && viewModel.enableChaptersFetch) {
                            try {
                                viewModel.getChapters(book, webView)
                                FetchResult.Success("Chapters fetched automatically")
                            } catch (e: Exception) {
                                FetchResult.Error("Failed to fetch chapters: ${e.message}")
                            }
                        } else {
                            FetchResult.Skipped
                        }
                    }
                    NovelDetectionResult.DetectionType.CHAPTER_CONTENT -> {
                        // Auto-fetch chapter content only if source supports it
                        val chapter = viewModel.stateChapter
                        if (hasContentFetch && chapter != null && viewModel.enableChapterFetch) {
                            try {
                                viewModel.getContentFromWebView(chapter, webView)
                                FetchResult.Success("Chapter content fetched automatically")
                            } catch (e: Exception) {
                                FetchResult.Error("Failed to fetch chapter content: ${e.message}")
                            }
                        } else {
                            FetchResult.Skipped
                        }
                    }
                }
            }
            NovelDetectionResult.NotDetected -> FetchResult.Skipped
        }
    }
}
