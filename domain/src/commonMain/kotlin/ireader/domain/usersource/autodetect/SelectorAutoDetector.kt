package ireader.domain.usersource.autodetect

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element

/**
 * Auto-detects CSS selectors from HTML content.
 * Analyzes page structure and suggests the most likely selectors for each rule type.
 */
class SelectorAutoDetector {
    
    /**
     * Result of auto-detection with confidence scores.
     */
    data class DetectionResult(
        val selector: String,
        val confidence: Float, // 0.0 to 1.0
        val sampleText: String,
        val matchCount: Int
    )
    
    /**
     * All detected rules for a page type.
     */
    data class PageDetectionResult(
        val bookList: List<DetectionResult> = emptyList(),
        val title: List<DetectionResult> = emptyList(),
        val author: List<DetectionResult> = emptyList(),
        val cover: List<DetectionResult> = emptyList(),
        val description: List<DetectionResult> = emptyList(),
        val link: List<DetectionResult> = emptyList(),
        val chapterList: List<DetectionResult> = emptyList(),
        val content: List<DetectionResult> = emptyList()
    )
    
    /**
     * Analyze search results page and detect selectors.
     */
    fun detectSearchPage(html: String, baseUrl: String): PageDetectionResult {
        val doc = Ksoup.parse(html, baseUrl)
        
        return PageDetectionResult(
            bookList = detectBookListContainers(doc),
            title = detectTitleSelectors(doc),
            author = detectAuthorSelectors(doc),
            cover = detectCoverSelectors(doc),
            description = detectDescriptionSelectors(doc),
            link = detectLinkSelectors(doc)
        )
    }
    
    /**
     * Analyze book detail page and detect selectors.
     */
    fun detectBookInfoPage(html: String, baseUrl: String): PageDetectionResult {
        val doc = Ksoup.parse(html, baseUrl)
        
        return PageDetectionResult(
            title = detectMainTitleSelectors(doc),
            author = detectAuthorSelectors(doc),
            cover = detectMainCoverSelectors(doc),
            description = detectDescriptionSelectors(doc),
            chapterList = detectChapterListContainers(doc)
        )
    }
    
    /**
     * Analyze chapter list page and detect selectors.
     */
    fun detectChapterListPage(html: String, baseUrl: String): PageDetectionResult {
        val doc = Ksoup.parse(html, baseUrl)
        
        return PageDetectionResult(
            chapterList = detectChapterListContainers(doc),
            link = detectChapterLinkSelectors(doc)
        )
    }
    
    /**
     * Analyze content page and detect selectors.
     */
    fun detectContentPage(html: String, baseUrl: String): PageDetectionResult {
        val doc = Ksoup.parse(html, baseUrl)
        
        return PageDetectionResult(
            content = detectContentSelectors(doc),
            title = detectChapterTitleSelectors(doc)
        )
    }
    
    // ==================== Book List Detection ====================
    
    private fun detectBookListContainers(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        // Common book list patterns
        val patterns = listOf(
            "div.search-result", "div.result-item", "div.book-item",
            "div.novel-item", "div.story-item", "div.manga-item",
            "ul.book-list li", "ul.novel-list li", "ul.search-list li",
            "div.c-tabs-item__content", "div.row.c-tabs-item__content",
            "article.post", "div.post", "div.entry",
            "div[class*=book]", "div[class*=novel]", "div[class*=item]",
            "li[class*=book]", "li[class*=novel]"
        )
        
        for (pattern in patterns) {
            try {
                val elements = doc.select(pattern)
                if (elements.size >= 2) { // Need multiple items
                    val sample = elements.first()?.text()?.take(50) ?: ""
                    val confidence = calculateListConfidence(elements.size, pattern)
                    candidates.add(DetectionResult(pattern, confidence, sample, elements.size))
                }
            } catch (e: Exception) { }
        }
        
        // Try to find repeating structures
        findRepeatingStructures(doc)?.let { candidates.add(it) }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    private fun findRepeatingStructures(doc: Document): DetectionResult? {
        // Find parent elements with multiple similar children
        val potentialLists = doc.select("ul, ol, div, section")
        
        for (parent in potentialLists) {
            val children = parent.children()
            if (children.size < 3) continue
            
            // Check if children have similar structure
            val firstChildTag = children.first()?.tagName() ?: continue
            val similarChildren = children.filter { it.tagName() == firstChildTag }
            
            if (similarChildren.size >= 3 && similarChildren.size.toFloat() / children.size > 0.7f) {
                val selector = buildSelector(parent) + " > " + firstChildTag
                val sample = similarChildren.first()?.text()?.take(50) ?: ""
                return DetectionResult(selector, 0.7f, sample, similarChildren.size)
            }
        }
        return null
    }

    
    // ==================== Title Detection ====================
    
    private fun detectTitleSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "h3.title a", "h3 a", "h4.title a", "h4 a",
            "div.title a", "div.post-title a", "div.book-title a",
            "a.title", "a.book-name", "a.novel-name",
            "span.title", "div.name a", "h2 a"
        )
        
        for (pattern in patterns) {
            try {
                val elements = doc.select(pattern)
                if (elements.isNotEmpty()) {
                    val sample = elements.first()?.text()?.take(50) ?: ""
                    if (sample.length > 2) {
                        val confidence = calculateTitleConfidence(sample, elements.size)
                        candidates.add(DetectionResult(pattern, confidence, sample, elements.size))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    private fun detectMainTitleSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "h1", "h1.title", "h1.book-title", "h1.novel-title",
            "div.title h1", "div.book-info h1", "div.post-title h1",
            "h2.title", "div.name", "span.title"
        )
        
        for (pattern in patterns) {
            try {
                val element = doc.selectFirst(pattern)
                if (element != null) {
                    val text = element.text().take(100)
                    if (text.length > 2) {
                        val confidence = if (pattern.startsWith("h1")) 0.9f else 0.7f
                        candidates.add(DetectionResult(pattern, confidence, text, 1))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Author Detection ====================
    
    private fun detectAuthorSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "span.author", "a.author", "div.author", "span.author-content a",
            "div.author-content a", "span[class*=author]", "a[class*=author]",
            "div.mg_author a", "span:contains(Author)", "span:contains(作者)",
            "div.info span:nth-child(1)", "p.author"
        )
        
        for (pattern in patterns) {
            try {
                val elements = doc.select(pattern)
                if (elements.isNotEmpty()) {
                    val sample = elements.first()?.text()?.take(50) ?: ""
                    if (sample.isNotBlank() && !sample.contains("http")) {
                        val confidence = calculateAuthorConfidence(sample, pattern)
                        candidates.add(DetectionResult(pattern, confidence, sample, elements.size))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Cover Detection ====================
    
    private fun detectCoverSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "img@src", "img@data-src", "img@data-lazy-src",
            "img.cover@src", "img.book-cover@src", "img.thumb@src",
            "div.cover img@src", "div.thumb img@src", "div.image img@src",
            "img.img-responsive@data-src", "img.img-responsive@src",
            "div.summary_image img@src", "div.book-img img@src"
        )
        
        for (pattern in patterns) {
            try {
                val (selector, attr) = if (pattern.contains("@")) {
                    pattern.substringBeforeLast("@") to pattern.substringAfterLast("@")
                } else {
                    pattern to "src"
                }
                
                val elements = doc.select(selector)
                for (element in elements) {
                    val url = element.attr(attr)
                    if (url.isNotBlank() && isImageUrl(url)) {
                        val confidence = calculateCoverConfidence(element, url)
                        candidates.add(DetectionResult(pattern, confidence, url.take(60), 1))
                        break
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    private fun detectMainCoverSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "div.summary_image img@src", "div.book-cover img@src",
            "div.cover img@src", "img.book-cover@src",
            "div.thumb img@src", "img[class*=cover]@src",
            "div[class*=cover] img@src", "div.book-img img@src"
        )
        
        for (pattern in patterns) {
            try {
                val (selector, attr) = pattern.substringBeforeLast("@") to pattern.substringAfterLast("@")
                val element = doc.selectFirst(selector)
                if (element != null) {
                    val url = element.attr(attr).ifBlank { element.attr("data-src") }
                    if (url.isNotBlank() && isImageUrl(url)) {
                        candidates.add(DetectionResult(pattern, 0.8f, url.take(60), 1))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Description Detection ====================
    
    private fun detectDescriptionSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "div.description", "div.synopsis", "div.summary", "div.intro",
            "div.summary__content p", "div.description-summary p",
            "div[class*=desc]", "div[class*=intro]", "div[class*=synopsis]",
            "p.description", "div.book-intro", "div.content-intro"
        )
        
        for (pattern in patterns) {
            try {
                val element = doc.selectFirst(pattern)
                if (element != null) {
                    val text = element.text()
                    if (text.length > 50) { // Description should be substantial
                        val confidence = calculateDescriptionConfidence(text)
                        candidates.add(DetectionResult(pattern, confidence, text.take(100), 1))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }

    
    // ==================== Link Detection ====================
    
    private fun detectLinkSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "a@href", "h3 a@href", "h4 a@href", "div.title a@href",
            "a.title@href", "a.book-name@href", "div.post-title a@href"
        )
        
        for (pattern in patterns) {
            try {
                val (selector, attr) = pattern.substringBeforeLast("@") to pattern.substringAfterLast("@")
                val elements = doc.select(selector)
                if (elements.isNotEmpty()) {
                    val url = elements.first()?.attr(attr) ?: ""
                    if (url.isNotBlank() && !url.startsWith("#") && !url.startsWith("javascript")) {
                        candidates.add(DetectionResult(pattern, 0.7f, url.take(60), elements.size))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Chapter List Detection ====================
    
    private fun detectChapterListContainers(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "ul.chapter-list li", "ul.chapters li", "div.chapter-list a",
            "li.wp-manga-chapter", "div.chapters a", "ul.list-chapter li",
            "div.chapter-item", "dd a", "div[class*=chapter] a",
            "ul[class*=chapter] li", "ol.chapter-list li"
        )
        
        for (pattern in patterns) {
            try {
                val elements = doc.select(pattern)
                if (elements.size >= 3) { // Need multiple chapters
                    val sample = elements.first()?.text()?.take(50) ?: ""
                    val confidence = calculateChapterListConfidence(elements.size, sample)
                    candidates.add(DetectionResult(pattern, confidence, sample, elements.size))
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    private fun detectChapterLinkSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "a@href", "li a@href", "div.chapter a@href"
        )
        
        for (pattern in patterns) {
            try {
                val (selector, attr) = pattern.substringBeforeLast("@") to pattern.substringAfterLast("@")
                val elements = doc.select(selector)
                val validLinks = elements.filter { 
                    val href = it.attr(attr)
                    href.isNotBlank() && !href.startsWith("#") && !href.startsWith("javascript")
                }
                if (validLinks.size >= 3) {
                    val sample = validLinks.first()?.attr(attr)?.take(60) ?: ""
                    candidates.add(DetectionResult(pattern, 0.7f, sample, validLinks.size))
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Content Detection ====================
    
    private fun detectContentSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "div.content", "div.chapter-content", "div#content",
            "div.text-left", "div.reading-content", "article",
            "div.entry-content", "div.post-content", "div.novel-content",
            "div[class*=content]", "div[class*=chapter]", "div[class*=text]"
        )
        
        for (pattern in patterns) {
            try {
                val element = doc.selectFirst(pattern)
                if (element != null) {
                    val text = element.text()
                    if (text.length > 200) { // Content should be substantial
                        val confidence = calculateContentConfidence(text, element)
                        candidates.add(DetectionResult(pattern, confidence, text.take(100), 1))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    private fun detectChapterTitleSelectors(doc: Document): List<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        
        val patterns = listOf(
            "h1", "h2", "h1.chapter-title", "div.chapter-title",
            "h3.title", "div.title"
        )
        
        for (pattern in patterns) {
            try {
                val element = doc.selectFirst(pattern)
                if (element != null) {
                    val text = element.text()
                    if (text.length in 3..200) {
                        candidates.add(DetectionResult(pattern, 0.7f, text.take(50), 1))
                    }
                }
            } catch (e: Exception) { }
        }
        
        return candidates.sortedByDescending { it.confidence }.take(5)
    }
    
    // ==================== Confidence Calculations ====================
    
    private fun calculateListConfidence(count: Int, pattern: String): Float {
        var confidence = 0.5f
        if (count >= 5) confidence += 0.2f
        if (count >= 10) confidence += 0.1f
        if (pattern.contains("book") || pattern.contains("novel")) confidence += 0.1f
        if (pattern.contains("search") || pattern.contains("result")) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateTitleConfidence(text: String, count: Int): Float {
        var confidence = 0.5f
        if (text.length in 3..100) confidence += 0.2f
        if (count >= 3) confidence += 0.1f
        if (!text.contains("http")) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateAuthorConfidence(text: String, pattern: String): Float {
        var confidence = 0.5f
        if (text.length in 2..50) confidence += 0.2f
        if (pattern.contains("author")) confidence += 0.2f
        if (!text.contains("http") && !text.contains("<")) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateCoverConfidence(element: Element, url: String): Float {
        var confidence = 0.5f
        val className = element.className().lowercase()
        if (className.contains("cover") || className.contains("thumb")) confidence += 0.2f
        if (url.contains("cover") || url.contains("thumb")) confidence += 0.1f
        val width = element.attr("width").toIntOrNull() ?: 0
        val height = element.attr("height").toIntOrNull() ?: 0
        if (width > 100 || height > 100) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateDescriptionConfidence(text: String): Float {
        var confidence = 0.5f
        if (text.length > 100) confidence += 0.2f
        if (text.length > 300) confidence += 0.1f
        if (text.split(" ").size > 20) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateChapterListConfidence(count: Int, sample: String): Float {
        var confidence = 0.5f
        if (count >= 10) confidence += 0.2f
        if (count >= 50) confidence += 0.1f
        if (sample.contains("chapter", ignoreCase = true) || 
            sample.contains("第") || sample.contains("章")) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    private fun calculateContentConfidence(text: String, element: Element): Float {
        var confidence = 0.5f
        if (text.length > 500) confidence += 0.2f
        if (text.length > 2000) confidence += 0.1f
        val className = element.className().lowercase()
        if (className.contains("content") || className.contains("chapter")) confidence += 0.1f
        // Check for paragraph structure
        if (element.select("p").size > 3) confidence += 0.1f
        return confidence.coerceAtMost(1.0f)
    }
    
    // ==================== Utility ====================
    
    private fun buildSelector(element: Element): String {
        val id = element.id()
        if (id.isNotBlank()) return "#$id"
        
        val className = element.className().split(" ").firstOrNull { it.isNotBlank() }
        if (className != null) return "${element.tagName()}.$className"
        
        return element.tagName()
    }
    
    private fun isImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".jpg") || lower.contains(".jpeg") || 
               lower.contains(".png") || lower.contains(".gif") ||
               lower.contains(".webp") || lower.contains("image") ||
               lower.contains("cover") || lower.contains("thumb")
    }
}
