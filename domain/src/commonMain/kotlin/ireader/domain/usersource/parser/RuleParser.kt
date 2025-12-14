package ireader.domain.usersource.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements

/**
 * Parser for Legado-style rules supporting CSS selectors and attribute extraction.
 * 
 * Supported formats:
 * - CSS selector: "div.class", "tag#id"
 * - Attribute: "selector@attr" (e.g., "a@href", "img@src")
 * - Fallback: "rule1||rule2"
 * - Regex: "selector##pattern##replacement"
 * - Index: "selector.0" (first), "selector.-1" (last)
 */
object RuleParser {
    
    private const val ATTR_SEPARATOR = "@"
    private const val FALLBACK_SEPARATOR = "||"
    private const val REGEX_SEPARATOR = "##"
    
    /**
     * Parse rule and extract text from document.
     */
    fun getString(doc: Document, rule: String?): String {
        if (rule.isNullOrBlank()) return ""
        return parseRule(doc, rule).trim()
    }
    
    /**
     * Parse rule and extract text from element.
     */
    fun getString(element: Element, rule: String?): String {
        if (rule.isNullOrBlank()) return ""
        return parseRuleFromElement(element, rule).trim()
    }
    
    /**
     * Get elements matching a rule.
     */
    fun getElements(doc: Document, rule: String?): Elements {
        if (rule.isNullOrBlank()) return Elements()
        return selectElements(doc, rule)
    }
    
    /**
     * Get elements from parent element.
     */
    fun getElements(element: Element, rule: String?): Elements {
        if (rule.isNullOrBlank()) return Elements()
        return selectElementsFromElement(element, rule)
    }
    
    private fun parseRule(doc: Document, rule: String): String {
        // Handle fallback rules (||)
        if (rule.contains(FALLBACK_SEPARATOR)) {
            for (r in rule.split(FALLBACK_SEPARATOR)) {
                val result = parseRule(doc, r.trim())
                if (result.isNotBlank()) return result
            }
            return ""
        }
        
        val (selector, regexPattern, replacement) = parseRegexRule(rule)
        val rawResult = extractValue(doc, selector)
        return applyRegex(rawResult, regexPattern, replacement)
    }
    
    private fun parseRuleFromElement(element: Element, rule: String): String {
        if (rule.contains(FALLBACK_SEPARATOR)) {
            for (r in rule.split(FALLBACK_SEPARATOR)) {
                val result = parseRuleFromElement(element, r.trim())
                if (result.isNotBlank()) return result
            }
            return ""
        }
        
        val (selector, regexPattern, replacement) = parseRegexRule(rule)
        val rawResult = extractValueFromElement(element, selector)
        return applyRegex(rawResult, regexPattern, replacement)
    }
    
    private fun extractValue(doc: Document, selector: String): String {
        if (selector.contains(ATTR_SEPARATOR) && !selector.startsWith("[")) {
            val parts = selector.split(ATTR_SEPARATOR)
            val cssSelector = parts[0].trim()
            val attr = parts.getOrNull(1)?.trim() ?: return ""
            
            val element = if (cssSelector.isBlank()) doc else doc.selectFirst(cssSelector) ?: return ""
            
            return when (attr.lowercase()) {
                "text" -> element.text()
                "html", "innerhtml" -> element.html()
                "outerhtml" -> element.outerHtml()
                else -> element.attr(attr)
            }
        }
        
        val (baseSelector, index) = parseIndexSelector(selector)
        val elements = doc.select(baseSelector)
        if (elements.isEmpty()) return ""
        
        val element = when {
            index != null && index >= 0 && index < elements.size -> elements[index]
            index != null && index < 0 && elements.size + index >= 0 -> elements[elements.size + index]
            else -> elements.first()
        }
        
        return element?.text() ?: ""
    }
    
    private fun extractValueFromElement(element: Element, selector: String): String {
        if (selector.contains(ATTR_SEPARATOR) && !selector.startsWith("[")) {
            val parts = selector.split(ATTR_SEPARATOR)
            val cssSelector = parts[0].trim()
            val attr = parts.getOrNull(1)?.trim() ?: return ""
            
            val targetElement = if (cssSelector.isBlank()) element else element.selectFirst(cssSelector) ?: return ""
            
            return when (attr.lowercase()) {
                "text" -> targetElement.text()
                "html", "innerhtml" -> targetElement.html()
                else -> targetElement.attr(attr)
            }
        }
        
        val (baseSelector, index) = parseIndexSelector(selector)
        
        if (baseSelector.isBlank()) return element.text()
        
        val elements = element.select(baseSelector)
        if (elements.isEmpty()) return ""
        
        val targetElement = when {
            index != null && index >= 0 && index < elements.size -> elements[index]
            index != null && index < 0 && elements.size + index >= 0 -> elements[elements.size + index]
            else -> elements.first()
        }
        
        return targetElement?.text() ?: ""
    }
    
    private fun selectElements(doc: Document, selector: String): Elements {
        val (baseSelector, _) = parseIndexSelector(selector.substringBefore(ATTR_SEPARATOR))
        val isReverse = baseSelector.startsWith("-")
        val cleanSelector = if (isReverse) baseSelector.substring(1) else baseSelector
        
        val elements = doc.select(cleanSelector)
        return if (isReverse) Elements(elements.reversed()) else elements
    }
    
    private fun selectElementsFromElement(element: Element, selector: String): Elements {
        val (baseSelector, _) = parseIndexSelector(selector.substringBefore(ATTR_SEPARATOR))
        val isReverse = baseSelector.startsWith("-")
        val cleanSelector = if (isReverse) baseSelector.substring(1) else baseSelector
        
        val elements = element.select(cleanSelector)
        return if (isReverse) Elements(elements.reversed()) else elements
    }
    
    private fun parseRegexRule(rule: String): Triple<String, String?, String?> {
        if (!rule.contains(REGEX_SEPARATOR)) {
            return Triple(rule, null, null)
        }
        val parts = rule.split(REGEX_SEPARATOR)
        return Triple(parts[0], parts.getOrNull(1), parts.getOrNull(2) ?: "")
    }
    
    private fun parseIndexSelector(selector: String): Pair<String, Int?> {
        val regex = Regex("""^(.+)\.(-?\d+)$""")
        val match = regex.find(selector)
        return if (match != null) {
            Pair(match.groupValues[1], match.groupValues[2].toIntOrNull())
        } else {
            Pair(selector, null)
        }
    }
    
    private fun applyRegex(value: String, pattern: String?, replacement: String?): String {
        if (pattern.isNullOrBlank()) return value
        return try {
            val regex = Regex(pattern)
            if (replacement != null) value.replace(regex, replacement) else regex.find(value)?.value ?: value
        } catch (e: Exception) {
            value
        }
    }
}

/**
 * URL template parser.
 */
object UrlParser {
    
    private val PLACEHOLDER_REGEX = Regex("""\{\{(\w+)\}\}""")
    
    fun parse(template: String, baseUrl: String, key: String? = null, page: Int = 1): String {
        val params = mutableMapOf(
            "baseUrl" to baseUrl.trimEnd('/'),
            "page" to page.toString(),
            "pageIndex" to (page - 1).toString()
        )
        
        key?.let {
            params["key"] = encodeUrl(it)
            params["keyword"] = encodeUrl(it)
            params["searchKey"] = encodeUrl(it)
        }
        
        var result = PLACEHOLDER_REGEX.replace(template) { match ->
            params[match.groupValues[1]] ?: match.value
        }
        
        return toAbsoluteUrl(result, baseUrl)
    }
    
    fun toAbsoluteUrl(url: String, baseUrl: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "${baseUrl.trimEnd('/')}$url"
            url.startsWith("?") -> "${baseUrl.trimEnd('/')}$url"
            else -> "${baseUrl.trimEnd('/')}/$url"
        }
    }
    
    private fun encodeUrl(value: String): String {
        return buildString {
            for (char in value) {
                when {
                    char.isLetterOrDigit() || char in "-_.~" -> append(char)
                    char == ' ' -> append("+")
                    else -> {
                        val bytes = char.toString().encodeToByteArray()
                        for (byte in bytes) {
                            append('%')
                            append(byte.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0'))
                        }
                    }
                }
            }
        }
    }
}
