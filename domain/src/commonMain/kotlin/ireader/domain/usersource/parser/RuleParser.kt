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
    
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
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
    
    // ==================== JSONPath support ====================
    
    fun getString(element: kotlinx.serialization.json.JsonElement, rule: String?): String {
        if (rule.isNullOrBlank()) return ""
        val (cleanRule, regexPattern, replacement) = parseRegexRule(rule)
        val elements = getJsonElements(element, cleanRule)
        if (elements.isEmpty()) return ""
        val rawResult = elements.joinToString("\n") { el ->
            when (el) {
                is kotlinx.serialization.json.JsonPrimitive -> el.content
                else -> el.toString()
            }
        }
        return applyRegex(rawResult, regexPattern, replacement).trim()
    }
    
    fun getStringFromJson(jsonString: String, rule: String?): String {
        return try {
            val root = json.parseToJsonElement(jsonString.trim())
            getString(root, rule)
        } catch (e: Exception) {
            ""
        }
    }
    
    fun getJsonElements(
        element: kotlinx.serialization.json.JsonElement,
        rule: String?
    ): List<kotlinx.serialization.json.JsonElement> {
        if (rule.isNullOrBlank()) return emptyList()
        val path = rule.trim()
            .removePrefix("@json:")
            .removePrefix("@Json:")
            .removePrefix("$.")
            .removePrefix("$")
        if (path.isBlank()) return listOf(element)
        return navigateJson(element, path)
    }
    
    private fun navigateJson(
        root: kotlinx.serialization.json.JsonElement,
        path: String
    ): List<kotlinx.serialization.json.JsonElement> {
        val tokens = tokenizeJsonPath(path)
        var current = listOf(root)
        
        for (token in tokens) {
            when {
                token == "*" -> {
                    current = current.flatMap { el ->
                        if (el is kotlinx.serialization.json.JsonArray) el else listOf(el)
                    }
                }
                token.toIntOrNull() != null -> {
                    val idx = token.toInt()
                    current = current.mapNotNull { el ->
                        if (el is kotlinx.serialization.json.JsonArray) {
                            val resolvedIdx = if (idx < 0) el.size + idx else idx
                            el.getOrNull(resolvedIdx)
                        } else null
                    }
                }
                else -> {
                    current = current.flatMap { el ->
                        when (el) {
                            is kotlinx.serialization.json.JsonObject -> listOfNotNull(el[token])
                            is kotlinx.serialization.json.JsonArray -> el.mapNotNull {
                                (it as? kotlinx.serialization.json.JsonObject)?.get(token)
                            }
                            else -> emptyList()
                        }
                    }
                }
            }
            if (current.isEmpty()) break
        }
        return current
    }
    
    private fun tokenizeJsonPath(path: String): List<String> {
        val tokenRegex = Regex("""([^\.\[\]]+)|\[(\*|-?\d+)\]""")
        val tokens = mutableListOf<String>()
        tokenRegex.findAll(path).forEach { match ->
            val field = match.groups[1]?.value
            val arrayIdx = match.groups[2]?.value
            if (field != null) tokens.add(field)
            if (arrayIdx != null) tokens.add(arrayIdx)
        }
        return tokens
    }
    
    // ==================== HTML / CSS parsing ====================
    
    private fun parseRule(doc: Document, rule: String): String {
        if (rule.contains(FALLBACK_SEPARATOR)) {
            for (r in rule.split(FALLBACK_SEPARATOR)) {
                val result = parseRule(doc, r.trim())
                if (result.isNotBlank()) return result
            }
            return ""
        }
        
        val (selector, regexPattern, replacement) = parseRegexRule(rule)
        val rawResult = extractChainedValue(listOf(doc), selector)
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
        val rawResult = extractChainedValue(listOf(element), selector)
        return applyRegex(rawResult, regexPattern, replacement)
    }
    
    private fun extractChainedValue(initialElements: List<Element>, selector: String): String {
        if (selector.isBlank()) return initialElements.firstOrNull()?.text().orEmpty()
        
        val tokens = selector.split(ATTR_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return initialElements.firstOrNull()?.text().orEmpty()
        
        val lastToken = tokens.last()
        val isExtractor = isExtractorToken(lastToken)
        
        val selectorTokens = if (isExtractor && tokens.size > 1) {
            tokens.subList(0, tokens.size - 1)
        } else if (isExtractor && tokens.size == 1) {
            emptyList()
        } else {
            tokens
        }
        
        val extractor = if (isExtractor) lastToken else "text"
        
        val targetElements = if (selectorTokens.isNotEmpty()) {
            navigateElements(initialElements, selectorTokens)
        } else {
            initialElements
        }
        
        if (targetElements.isEmpty()) return ""
        
        return when (extractor.lowercase()) {
            "text" -> targetElements.firstOrNull()?.text() ?: ""
            "textnodes" -> {
                targetElements.flatMap { el ->
                    el.textNodes().map { it.text().trim() }.filter { it.isNotEmpty() }
                }.joinToString("\n")
            }
            "owntext" -> targetElements.firstOrNull()?.ownText() ?: ""
            "html", "innerhtml" -> targetElements.firstOrNull()?.html() ?: ""
            "outerhtml", "all" -> targetElements.joinToString("\n") { it.outerHtml() }
            else -> targetElements.firstOrNull()?.attr(extractor) ?: ""
        }
    }
    
    private fun isExtractorToken(token: String): Boolean {
        val lower = token.lowercase()
        if (lower in setOf("text", "textnodes", "owntext", "html", "innerhtml", "outerhtml", "all")) {
            return true
        }
        if (lower in setOf("href", "src", "data-src", "content", "title", "alt")) {
            return true
        }
        // If it starts with a known selector prefix, it's not an extractor
        if (lower.startsWith("tag.") || lower.startsWith("class.") || lower.startsWith("id.") || lower.startsWith("text.")) {
            return false
        }
        // If it contains CSS selector characters, it's not a pure attribute extractor
        if (lower.contains(".") || lower.contains("#") || lower.contains("[") || lower.contains(":") || lower.contains(">") || lower.contains(" ")) {
            return false
        }
        return false
    }
    
    private fun navigateElements(initialElements: List<Element>, tokens: List<String>): List<Element> {
        var current = initialElements
        
        for (token in tokens) {
            val isReverse = token.startsWith("-")
            val cleanToken = if (isReverse) token.substring(1).trim() else token.trim()
            val (baseSelector, index) = parseIndexSelector(cleanToken)
            val css = translateToCss(baseSelector)
            
            val nextList = mutableListOf<Element>()
            for (parent in current) {
                val matched = if (css.isBlank()) parent.children() else parent.select(css)
                val resolvedMatched = if (isReverse) matched.reversed() else matched
                
                if (index != null) {
                    val target = when {
                        index >= 0 && index < resolvedMatched.size -> resolvedMatched[index]
                        index < 0 && resolvedMatched.size + index >= 0 -> resolvedMatched[resolvedMatched.size + index]
                        else -> null
                    }
                    if (target != null) nextList.add(target)
                } else {
                    nextList.addAll(resolvedMatched)
                }
            }
            current = nextList
            if (current.isEmpty()) break
        }
        return current
    }
    
    private fun translateToCss(token: String): String {
        val trimmed = token.trim()
        return when {
            trimmed.startsWith("class.") -> "." + trimmed.substring(6).trim()
            trimmed.startsWith("id.") -> "#" + trimmed.substring(3).trim()
            trimmed.startsWith("tag.") -> trimmed.substring(4).trim()
            trimmed.startsWith("text.") -> ":containsOwn(${trimmed.substring(5).trim()})"
            trimmed == "children" -> "*"
            else -> trimmed
        }
    }
    
    private fun selectElements(doc: Document, selector: String): Elements {
        val isReverse = selector.startsWith("-")
        val cleanSelector = if (isReverse) selector.substring(1).trim() else selector.trim()
        val tokens = cleanSelector.split(ATTR_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        val elements = navigateElements(listOf(doc), tokens)
        return if (isReverse) Elements(elements.reversed()) else Elements(elements)
    }
    
    private fun selectElementsFromElement(element: Element, selector: String): Elements {
        val isReverse = selector.startsWith("-")
        val cleanSelector = if (isReverse) selector.substring(1).trim() else selector.trim()
        val tokens = cleanSelector.split(ATTR_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        val elements = navigateElements(listOf(element), tokens)
        return if (isReverse) Elements(elements.reversed()) else Elements(elements)
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
 * Represents a parsed HTTP request with method, headers, and body.
 */
data class ParsedRequest(
    val url: String,
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val charset: String? = null
)

/**
 * URL template parser supporting Legado-style URL templates and options.
 */
object UrlParser {
    
    private val PLACEHOLDER_REGEX = Regex("""\{\{([^{}]+)\}\}""")
    private val PAGE_PATTERN = Regex("""<([^<>]+)>""")
    private val OPTION_SEPARATOR_REGEX = Regex("""\s*,\s*(?=\{)""")
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    fun parseRequest(
        template: String,
        baseUrl: String,
        key: String? = null,
        page: Int = 1
    ): ParsedRequest {
        var processed = template.trim()
        
        // 1. Replace angle-bracket page patterns: <1,2,3> or <,index_2.html,index_3.html>
        processed = PAGE_PATTERN.replace(processed) { match ->
            val pages = match.groupValues[1].split(",")
            if (page <= pages.size) {
                pages[page - 1].trim()
            } else {
                pages.last().trim()
            }
        }
        
        // 2. Replace placeholders: {{key}}, {{page}}, {{page-1}}, {{searchPage}}, etc.
        val encodedKey = key?.let { encodeUrl(it) } ?: ""
        processed = PLACEHOLDER_REGEX.replace(processed) { match ->
            val expr = match.groupValues[1].trim()
            when (expr) {
                "key", "keyword", "searchKey" -> encodedKey
                "page", "searchPage" -> page.toString()
                "pageIndex", "page-1", "page - 1" -> maxOf(0, page - 1).toString()
                "baseUrl" -> baseUrl.trimEnd('/')
                else -> match.value
            }
        }
        
        // 3. Separate URL from options if separated by comma followed by JSON: ,\s*\{
        var rawUrl = processed
        var method = "GET"
        val headers = mutableMapOf<String, String>()
        var body: String? = null
        var charset: String? = null
        
        val optionMatch = OPTION_SEPARATOR_REGEX.find(processed)
        if (optionMatch != null) {
            rawUrl = processed.substring(0, optionMatch.range.first).trim()
            val optionJsonStr = processed.substring(optionMatch.range.first + optionMatch.value.length).trim()
            try {
                val element = json.parseToJsonElement(optionJsonStr)
                if (element is kotlinx.serialization.json.JsonObject) {
                    element["method"]?.let {
                        if (it is kotlinx.serialization.json.JsonPrimitive) {
                            method = it.content.uppercase()
                        }
                    }
                    element["headers"]?.let { h ->
                        if (h is kotlinx.serialization.json.JsonObject) {
                            h.forEach { (k, v) ->
                                if (v is kotlinx.serialization.json.JsonPrimitive) {
                                    headers[k] = v.content
                                }
                            }
                        }
                    }
                    element["body"]?.let { b ->
                        body = when (b) {
                            is kotlinx.serialization.json.JsonPrimitive -> b.content
                            else -> b.toString()
                        }
                    }
                    element["charset"]?.let { c ->
                        if (c is kotlinx.serialization.json.JsonPrimitive) {
                            charset = c.content
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore option parse failure, treat whole string as URL
            }
        }
        
        // If body has literal "searchKey" placeholder, replace it
        if (body != null && key != null) {
            body = body!!.replace("searchKey", encodeUrl(key))
        }
        
        // 4. Extract pipe charset if present: url|char=gbk or url|charset=gbk
        if (rawUrl.contains("|")) {
            val pipeParts = rawUrl.split("|")
            rawUrl = pipeParts[0].trim()
            for (i in 1 until pipeParts.size) {
                val part = pipeParts[i].trim()
                if (part.startsWith("char=") || part.startsWith("charset=")) {
                    charset = part.substringAfter("=")
                }
            }
        }
        
        val absUrl = toAbsoluteUrl(rawUrl, baseUrl)
        return ParsedRequest(
            url = absUrl,
            method = method,
            headers = headers,
            body = body,
            charset = charset
        )
    }
    
    fun parse(template: String, baseUrl: String, key: String? = null, page: Int = 1): String {
        return parseRequest(template, baseUrl, key, page).url
    }
    
    fun toAbsoluteUrl(url: String, baseUrl: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "${baseUrl.trimEnd('/')}$trimmed"
            trimmed.startsWith("?") -> "${baseUrl.trimEnd('/')}$trimmed"
            else -> "${baseUrl.trimEnd('/')}/$trimmed"
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
