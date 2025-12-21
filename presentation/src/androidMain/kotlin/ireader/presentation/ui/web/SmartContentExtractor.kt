package ireader.presentation.ui.web

import android.webkit.WebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode

/**
 * Smart content extractor that analyzes HTML to find chapter content
 * when the source's parser fails or returns empty content
 */
class SmartContentExtractor {
    
    /**
     * Content candidate with scoring information
     */
    data class ContentCandidate(
        val element: Element,
        val score: Double,
        val textLength: Int,
        val reason: String
    )
    
    /**
     * Result of content extraction
     */
    data class ExtractionResult(
        val content: String,
        val confidence: Double,
        val method: String,
        val candidates: List<ContentCandidate> = emptyList()
    )
    
    /**
     * Extract chapter content from HTML using multiple strategies
     */
    fun extractContent(html: String): ExtractionResult {
        val doc = Ksoup.parse(html)
        
        // Try strategies in order of reliability
        tryCommonSelectors(doc)?.let { return it }
        trySemanticAnalysis(doc)?.let { return it }
        tryContentDensity(doc)?.let { return it }
        tryParagraphClustering(doc)?.let { return it }
        tryLargestTextBlock(doc)?.let { return it }
        
        // Fallback
        return ExtractionResult(
            content = doc.body().text(),
            confidence = 0.1,
            method = "fallback"
        )
    }
    
    /**
     * Try common CSS selectors for chapter content
     */
    private fun tryCommonSelectors(doc: Document): ExtractionResult? {
        val selectors = listOf(
            "div.chapter-content", "div.content", "div.chapter-body",
            "div.chapter-text", "div.text-content", "article.chapter",
            "article.content", "div#chapter-content", "div#content",
            "div.entry-content", "div.post-content", "main article",
            "div.reader-content", "div.reading-content", "div.novel-content",
            "div.story-content", "div.chapter_content", "div.chapter_body",
            "div.txt", "div#contentTxt", "div.contentTxt", "div.bookContent",
            "div.book-content", "div.read-content", "div#readContent",
            "div.article-content", "div.main-content", "section.chapter"
        )
        
        for (selector in selectors) {
            doc.selectFirst(selector)?.let { element ->
                if (element.text().length > 100) {
                    return ExtractionResult(
                        content = cleanContent(element),
                        confidence = 0.9,
                        method = "common-selector: $selector"
                    )
                }
            }
        }
        return null
    }
    
    /**
     * Analyze semantic HTML attributes
     */
    private fun trySemanticAnalysis(doc: Document): ExtractionResult? {
        val selectors = listOf(
            "[role=main]", "[role=article]",
            "[itemprop=articleBody]", "[itemprop=text]",
            "main", "article"
        )
        
        for (selector in selectors) {
            doc.selectFirst(selector)?.let { element ->
                if (element.text().length > 100) {
                    return ExtractionResult(
                        content = cleanContent(element),
                        confidence = 0.85,
                        method = "semantic: $selector"
                    )
                }
            }
        }
        return null
    }
    
    /**
     * Find content by text density analysis
     */
    private fun tryContentDensity(doc: Document): ExtractionResult? {
        val candidates = doc.select("div, article, section, main")
        var bestElement: Element? = null
        var bestScore = 0.0
        
        for (element in candidates) {
            val textLength = element.ownText().length
            val htmlLength = element.html().length
            if (htmlLength == 0 || textLength < 100) continue
            
            val density = textLength.toDouble() / htmlLength
            val paragraphCount = element.select("p").size
            val brCount = element.select("br").size
            val score = density * (1 + paragraphCount * 0.1 + brCount * 0.05)
            
            if (score > bestScore) {
                bestScore = score
                bestElement = element
            }
        }
        
        return bestElement?.let {
            ExtractionResult(
                content = cleanContent(it),
                confidence = 0.75,
                method = "density-analysis"
            )
        }
    }
    
    /**
     * Find content by clustering paragraphs
     */
    private fun tryParagraphClustering(doc: Document): ExtractionResult? {
        val paragraphs = doc.select("p")
        if (paragraphs.isEmpty()) return null
        
        val parentCounts = mutableMapOf<Element, Int>()
        var maxClusterParent: Element? = null
        var maxClusterSize = 0
        
        for (p in paragraphs) {
            val parent = p.parent()
            if (parent != null && p.text().length > 20) {
                val count = parentCounts.getOrDefault(parent, 0) + 1
                parentCounts[parent] = count
                
                if (count > maxClusterSize) {
                    maxClusterSize = count
                    maxClusterParent = parent
                }
            }
        }
        
        return if (maxClusterParent != null && maxClusterSize >= 3) {
            ExtractionResult(
                content = cleanContent(maxClusterParent),
                confidence = 0.7,
                method = "paragraph-clustering"
            )
        } else null
    }
    
    /**
     * Find largest text block as fallback
     */
    private fun tryLargestTextBlock(doc: Document): ExtractionResult? {
        val candidates = doc.select("div, article, section, main")
        var largestElement: Element? = null
        var maxLength = 0
        
        for (element in candidates) {
            val textLength = element.text().length
            val childDivs = element.select("div").size
            val adjustedLength = if (childDivs > 10) textLength / 2 else textLength
            
            if (adjustedLength > maxLength && textLength > 100) {
                maxLength = adjustedLength
                largestElement = element
            }
        }
        
        return largestElement?.let {
            ExtractionResult(
                content = cleanContent(it),
                confidence = 0.6,
                method = "largest-block"
            )
        }
    }
    
    /**
     * Clean extracted content
     */
    private fun cleanContent(element: Element): String {
        val clone = element.clone()
        
        // Remove unwanted elements
        clone.select(
            "script, style, nav, header, footer, aside, " +
            ".ads, .advertisement, .social-share, .comments, " +
            ".navigation, .nav, .menu, .sidebar, " +
            "[class*=ad], [id*=ad], [class*=banner], " +
            "[class*=share], [class*=social]"
        ).remove()
        
        // Remove empty paragraphs
        clone.select("p").forEach { p ->
            if (p.text().trim().isEmpty()) p.remove()
        }
        
        return clone.html()
    }
    
    /**
     * Get all possible content candidates for manual selection
     */
    fun findCandidates(html: String): List<ContentCandidate> {
        val doc = Ksoup.parse(html)
        val candidates = mutableListOf<ContentCandidate>()
        
        doc.select("div, article, section, main").forEach { element ->
            val textLength = element.text().length
            if (textLength > 100) {
                val score = calculateScore(element)
                candidates.add(
                    ContentCandidate(
                        element = element,
                        score = score,
                        textLength = textLength,
                        reason = buildSelector(element)
                    )
                )
            }
        }
        
        return candidates.sortedByDescending { it.score }.take(10)
    }
    
    private fun calculateScore(element: Element): Double {
        val textLength = element.text().length
        val htmlLength = element.html().length
        if (htmlLength == 0) return 0.0
        
        val density = textLength.toDouble() / htmlLength
        val paragraphCount = element.select("p").size
        return density * (1 + paragraphCount * 0.1)
    }
    
    private fun buildSelector(element: Element): String {
        val id = element.id()
        if (id.isNotEmpty()) return "#$id"
        
        val classes = element.classNames()
        if (classes.isNotEmpty()) {
            return "${element.tagName()}.${classes.first()}"
        }
        
        return element.tagName()
    }
    
    /**
     * Extract content from WebView using JavaScript
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun extractFromWebView(webView: WebView): String = suspendCancellableCoroutine { continuation ->
        webView.evaluateJavascript(
            """
            (function() {
                // Try to find content using multiple strategies
                function findContent() {
                    // Strategy 1: Common selectors
                    const selectors = [
                        '.chapter-content', '.content', '.chapter-body',
                        '#chapter-content', '#content', '.reader-content',
                        '.novel-content', 'article', 'main'
                    ];
                    
                    for (let selector of selectors) {
                        const el = document.querySelector(selector);
                        if (el && el.innerText.length > 100) {
                            return el.innerHTML;
                        }
                    }
                    
                    // Strategy 2: Find element with most text
                    let maxLength = 0;
                    let bestElement = null;
                    
                    document.querySelectorAll('div, article, section').forEach(el => {
                        const textLength = el.innerText.length;
                        if (textLength > maxLength && textLength > 100) {
                            maxLength = textLength;
                            bestElement = el;
                        }
                    });
                    
                    return bestElement ? bestElement.innerHTML : document.body.innerHTML;
                }
                
                return findContent();
            })();
            """.trimIndent()
        ) { result ->
            continuation.resume(result.orEmpty()) {}
        }
    }
}
