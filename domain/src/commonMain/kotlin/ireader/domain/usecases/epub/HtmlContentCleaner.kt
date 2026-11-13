package ireader.domain.usecases.epub

/**
 * Utility for cleaning HTML content before ePub export.
 * Removes scripts, styles, ads, watermarks, and other unwanted elements.
 * 
 * Requirements: 15.2 - Implement HTML content cleaning to remove scripts, styles, ads, and watermarks
 */
object HtmlContentCleaner {

    private const val AD_NAME = "(?<![A-Za-z0-9])ad(?:s|vert\\w*)?(?![A-Za-z0-9])"

    /**
     * Cleans HTML content by removing unwanted elements and attributes.
     * 
     * @param html The raw HTML content
     * @return Cleaned HTML content suitable for ePub
     */
    fun clean(html: String): String {
        var cleaned = html
        
        // Remove script tags and their content (using (?s) inline flag for DOT_MATCHES_ALL)
        cleaned = cleaned.replace(Regex("(?s)<script[^>]*>.*?</script>"), "")
        
        // Remove style tags and their content
        cleaned = cleaned.replace(Regex("(?s)<style[^>]*>.*?</style>"), "")
        
        // Remove HTML comments (often used for ads)
        cleaned = cleaned.replace(Regex("(?s)<!--.*?-->"), "")
        
        // Remove common ad containers
        // "ad" must be a whole name part (ad, ads, ad-top, advert...), not inside "header", "reader", "loading"
        cleaned = cleaned.replace(Regex("(?s)<div[^>]*class=[\"'][^\"']*$AD_NAME[^\"']*[\"'][^>]*>.*?</div>"), "")
        cleaned = cleaned.replace(Regex("(?s)<div[^>]*id=[\"'][^\"']*$AD_NAME[^\"']*[\"'][^>]*>.*?</div>"), "")
        
        // Remove common watermark patterns
        cleaned = cleaned.replace(Regex("(?s)<div[^>]*class=[\"'][^\"']*watermark[^\"']*[\"'][^>]*>.*?</div>"), "")
        cleaned = cleaned.replace(Regex("(?s)<span[^>]*class=[\"'][^\"']*watermark[^\"']*[\"'][^>]*>.*?</span>"), "")
        
        // Remove iframe tags (often used for ads)
        cleaned = cleaned.replace(Regex("(?s)<iframe[^>]*>.*?</iframe>"), "")
        
        // Remove noscript tags
        cleaned = cleaned.replace(Regex("(?s)<noscript[^>]*>.*?</noscript>"), "")
        
        // Remove inline event handlers (onclick, onload, etc.)
        cleaned = cleaned.replace(Regex("\\s+on\\w+=[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")
        
        // Remove inline styles (we use our own stylesheet)
        cleaned = cleaned.replace(Regex("\\s+style=[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")
        
        // Remove data attributes that might contain tracking
        cleaned = cleaned.replace(Regex("\\s+data-[\\w-]+=[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")
        
        // Remove common tracking/analytics elements
        cleaned = cleaned.replace(Regex("<img[^>]*analytics[^>]*>", RegexOption.IGNORE_CASE), "")
        cleaned = cleaned.replace(Regex("<img[^>]*tracking[^>]*>", RegexOption.IGNORE_CASE), "")
        
        // Remove empty paragraphs and divs
        cleaned = cleaned.replace(Regex("<p>\\s*</p>"), "")
        cleaned = cleaned.replace(Regex("<div>\\s*</div>"), "")
        
        // Collapse runs of spaces/tabs only; newlines separate the paragraphs the EPUB exporter splits on
        cleaned = cleaned.replace(Regex("[^\\S\\n]{2,}"), " ")
        
        // Trim the result
        cleaned = cleaned.trim()
        
        return cleaned
    }
    
    /**
     * Extracts plain text from HTML, removing all tags.
     * Useful for chapter content that's already in HTML format.
     * 
     * @param html The HTML content
     * @return Plain text content
     */
    fun extractPlainText(html: String): String {
        // First clean the HTML
        var text = clean(html)
        
        // Remove all HTML tags
        text = text.replace(Regex("<[^>]+>"), "")
        
        // Decode common HTML entities
        text = text.replace("&nbsp;", " ")
        text = text.replace("&amp;", "&")
        text = text.replace("&lt;", "<")
        text = text.replace("&gt;", ">")
        text = text.replace("&quot;", "\"")
        text = text.replace("&apos;", "'")
        text = text.replace("&#39;", "'")
        text = text.replace("&mdash;", "—")
        text = text.replace("&ndash;", "–")
        text = text.replace("&hellip;", "…")
        
        // Remove excessive whitespace
        text = text.replace(Regex("\\s{2,}"), " ")
        text = text.trim()
        
        return text
    }
    
    /**
     * Checks if content appears to be HTML.
     * 
     * @param content The content to check
     * @return true if content contains HTML tags
     */
    fun isHtml(content: String): Boolean {
        return content.contains(Regex("<[^>]+>"))
    }
}
