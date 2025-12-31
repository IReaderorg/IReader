package ireader.core.util

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import ireader.core.source.model.Page
import ireader.core.source.model.Text

/**
 * HTML parser specifically designed for LNReader plugin sources.
 * Converts HTML chapter content to clean text paragraphs for IReader's List<Page> format.
 */
object LNReaderHtmlParser {
    
    /**
     * Parse HTML content from LNReader sources and convert to List<Page>.
     * 
     * @param html Raw HTML content from LNReader plugin
     * @return List of Text pages, each representing a paragraph
     */
    fun parseToPages(html: String): List<Page> {
        if (html.isBlank()) return emptyList()
        
        val paragraphs = parseHtmlToParagraphs(html)
        return paragraphs.map { Text(it) }
    }
    
    /**
     * Parse HTML content and extract clean text paragraphs.
     * 
     * @param html Raw HTML content
     * @return List of clean text paragraphs
     */
    fun parseHtmlToParagraphs(html: String): List<String> {
        if (html.isBlank()) return emptyList()
        
        return try {
            val doc = Ksoup.parse(html)
            
            // Remove unwanted elements
            removeUnwantedElements(doc)
            
            // Extract paragraphs
            val paragraphs = extractParagraphs(doc)
            
            // If no paragraphs found, try fallback extraction
            if (paragraphs.isEmpty()) {
                extractParagraphsFallback(html)
            } else {
                paragraphs
            }
        } catch (e: Exception) {
            // Ultimate fallback: strip HTML and split by newlines
            extractParagraphsFallback(html)
        }
    }
    
    /**
     * Remove script, style, and other unwanted elements from the document.
     */
    private fun removeUnwantedElements(doc: Document) {
        val selectorsToRemove = listOf(
            "script", "style", "noscript", "iframe", "object", "embed",
            "nav", "header", "footer", "aside",
            ".advertisement", ".ads", ".ad", ".adsbygoogle",
            ".social-share", ".share-buttons", ".social",
            ".comments", "#comments", ".comment-section",
            ".related-posts", ".related", ".recommended",
            ".popup", ".modal", ".overlay",
            ".navigation", ".nav", ".menu",
            ".sidebar", ".widget",
            "[id*='google']", "[class*='google']",
            "[id*='disqus']", "[class*='disqus']",
            "[id*='facebook']", "[class*='facebook']",
            "[id*='twitter']", "[class*='twitter']"
        )
        
        selectorsToRemove.forEach { selector ->
            try {
                doc.select(selector).remove()
            } catch (_: Exception) {
                // Ignore invalid selectors
            }
        }
    }
    
    /**
     * Extract paragraphs from the document using multiple strategies.
     */
    private fun extractParagraphs(doc: Document): List<String> {
        val paragraphs = mutableListOf<String>()
        
        // Strategy 1: Try common chapter content selectors
        val contentSelectors = listOf(
            "div.chapter-content",
            "div.text-content", 
            "div.content",
            "div.chapter-text",
            "div.novel-content",
            "div.entry-content",
            "article.content",
            "div#chapter-content",
            "div#content",
            ".chapter-c",
            ".text-left"
        )
        
        for (selector in contentSelectors) {
            try {
                val contentElement = doc.selectFirst(selector)
                if (contentElement != null) {
                    val extracted = extractTextFromElement(contentElement)
                    if (extracted.isNotEmpty()) {
                        return extracted
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        
        // Strategy 2: Extract from <p> tags directly
        val pElements = doc.select("p")
        if (pElements.isNotEmpty()) {
            pElements.forEach { p ->
                val text = cleanText(p.text())
                if (isValidParagraph(text)) {
                    paragraphs.add(text)
                }
            }
            if (paragraphs.isNotEmpty()) {
                return paragraphs
            }
        }
        
        // Strategy 3: Extract from body
        val bodyText = extractTextFromElement(doc.body())
        if (bodyText.isNotEmpty()) {
            return bodyText
        }
        
        return paragraphs
    }
    
    /**
     * Extract text paragraphs from an element, preserving paragraph structure.
     */
    private fun extractTextFromElement(element: Element): List<String> {
        val paragraphs = mutableListOf<String>()
        
        // First try to get <p> tags within the element
        val pElements = element.select("p")
        if (pElements.isNotEmpty()) {
            pElements.forEach { p ->
                val text = cleanText(p.text())
                if (isValidParagraph(text)) {
                    paragraphs.add(text)
                }
            }
            if (paragraphs.isNotEmpty()) {
                return paragraphs
            }
        }
        
        // Try <br> separated content
        val html = element.html()
        if (html.contains("<br", ignoreCase = true)) {
            val brSplit = html
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .let { Ksoup.parse(it).text() }
                .split("\n")
                .map { cleanText(it) }
                .filter { isValidParagraph(it) }
            
            if (brSplit.isNotEmpty()) {
                return brSplit
            }
        }
        
        // Fallback: get all text and split by double newlines or periods
        val fullText = cleanText(element.text())
        if (fullText.isNotBlank()) {
            // Try splitting by double newlines first
            val byNewlines = fullText.split(Regex("\n{2,}"))
                .map { cleanText(it) }
                .filter { isValidParagraph(it) }
            
            if (byNewlines.size > 1) {
                return byNewlines
            }
            
            // If single block, try to split by sentence patterns that indicate paragraph breaks
            val bySentences = splitIntoParagraphs(fullText)
            if (bySentences.isNotEmpty()) {
                return bySentences
            }
            
            // Last resort: return as single paragraph
            if (isValidParagraph(fullText)) {
                return listOf(fullText)
            }
        }
        
        return paragraphs
    }
    
    /**
     * Fallback extraction when Ksoup parsing fails.
     */
    private fun extractParagraphsFallback(html: String): List<String> {
        // Strip all HTML tags
        val text = stripHtmlTags(html)
        
        // Clean the text
        val cleaned = cleanText(text)
        
        if (cleaned.isBlank()) return emptyList()
        
        // Split into paragraphs
        return splitIntoParagraphs(cleaned)
    }
    
    /**
     * Strip all HTML tags from text.
     */
    private fun stripHtmlTags(html: String): String {
        return html
            // Replace <br> tags with newlines
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            // Replace </p> and </div> with double newlines (paragraph breaks)
            .replace(Regex("</p>|</div>", RegexOption.IGNORE_CASE), "\n\n")
            // Remove all remaining HTML tags
            .replace(Regex("<[^>]+>"), "")
            // Decode common HTML entities
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            // Fix escaped quotes from JSON-encoded content (common in LNReader plugins)
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            // Fix escaped backslashes
            .replace("\\\\", "\\")
            .replace(Regex("&#(\\d+);")) { match ->
                val code = match.groupValues[1].toIntOrNull()
                if (code != null && code in 32..126) code.toChar().toString() else ""
            }
    }
    
    /**
     * Clean text by removing unwanted characters and normalizing whitespace.
     */
    private fun cleanText(text: String): String {
        return text
            // Remove carriage returns
            .replace("\r", "")
            // Replace tabs with spaces
            .replace("\t", " ")
            // Fix escaped quotes from JSON-encoded content (common in LNReader plugins)
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            // Fix escaped backslashes
            .replace("\\\\", "\\")
            // Normalize multiple spaces to single space
            .replace(Regex(" {2,}"), " ")
            // Normalize multiple newlines to double newline (paragraph break)
            .replace(Regex("\n{3,}"), "\n\n")
            // Trim each line
            .split("\n")
            .joinToString("\n") { it.trim() }
            // Final trim
            .trim()
    }
    
    /**
     * Split text into paragraphs using various heuristics.
     */
    private fun splitIntoParagraphs(text: String): List<String> {
        // First try splitting by double newlines
        val byDoubleNewline = text.split(Regex("\n{2,}"))
            .map { it.trim() }
            .filter { isValidParagraph(it) }
        
        if (byDoubleNewline.size > 1) {
            return byDoubleNewline
        }
        
        // Try splitting by single newlines if they seem to be paragraph breaks
        val byNewline = text.split("\n")
            .map { it.trim() }
            .filter { isValidParagraph(it) }
        
        if (byNewline.size > 1) {
            return byNewline
        }
        
        // If still single block, check if it's very long and try to split by dialog patterns
        if (text.length > 500) {
            // Split by dialog indicators (quotes followed by action)
            val byDialog = text.split(Regex("(?<=[.!?]\"\\s)(?=[A-Z])"))
                .map { it.trim() }
                .filter { isValidParagraph(it) }
            
            if (byDialog.size > 1) {
                return byDialog
            }
        }
        
        // Return as single paragraph if valid
        return if (isValidParagraph(text)) listOf(text) else emptyList()
    }
    
    /**
     * Check if text is a valid paragraph (not too short, not just whitespace).
     */
    private fun isValidParagraph(text: String): Boolean {
        val trimmed = text.trim()
        // Must have at least 2 characters and not be just punctuation/whitespace
        return trimmed.length >= 2 && trimmed.any { it.isLetterOrDigit() }
    }
}
