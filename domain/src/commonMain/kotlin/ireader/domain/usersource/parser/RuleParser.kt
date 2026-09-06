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
        if (isJsRule(rule)) {
            return evalJsRule(doc, rule).trim()
        }
        return parseRule(doc, rule).trim()
    }
    
    /**
     * Parse rule and extract text from element.
     */
    fun getString(element: Element, rule: String?): String {
        if (rule.isNullOrBlank()) return ""
        if (isJsRule(rule)) {
            return evalJsRule(element, rule).trim()
        }
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
        
        if (extractor.startsWith("js:", ignoreCase = true) || extractor.startsWith("@js:", ignoreCase = true)) {
            val jsCode = if (extractor.startsWith("@js:", ignoreCase = true)) extractor.substring(4) else extractor.substring(3)
            return evalJsRule(targetElements.first(), jsCode)
        }
        
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
            "href" -> targetElements.firstOrNull()?.let {
                val abs = it.absUrl("href")
                if (abs.isNotBlank()) abs else it.attr("href")
            } ?: ""
            "src" -> targetElements.firstOrNull()?.let {
                val abs = it.absUrl("src")
                if (abs.isNotBlank()) abs else it.attr("src")
            } ?: ""
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
        if (lower.startsWith("js:") || lower.startsWith("@js:")) {
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
    
    private sealed class ElementFilter {
        object None : ElementFilter()
        data class Single(val index: Int) : ElementFilter()
        data class Exclude(val index: Int) : ElementFilter()
        data class Slice(val start: Int?, val end: Int?) : ElementFilter()
        data class Multiple(val indices: List<Int>) : ElementFilter()
    }

    private data class ParsedSelectorToken(
        val baseSelector: String,
        val filter: ElementFilter
    )

    private fun parseFilterSelector(token: String): ParsedSelectorToken {
        val trimmed = token.trim()
        
        // 1. Bracket notation: tag.tr[1:], tag.tr[0], tag.tr[-1], tag.tr[:2], tag.tr[!0], tag.tr[0, 2]
        if (trimmed.endsWith("]") && trimmed.contains("[")) {
            val openBracket = trimmed.lastIndexOf('[')
            val base = trimmed.substring(0, openBracket).trim()
            val expr = trimmed.substring(openBracket + 1, trimmed.length - 1).trim()
            
            val filter = when {
                expr.contains(":") -> {
                    val parts = expr.split(":")
                    val start = parts.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                    val end = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                    ElementFilter.Slice(start, end)
                }
                expr.startsWith("!") -> {
                    val idx = expr.substring(1).trim().toIntOrNull()
                    if (idx != null) ElementFilter.Exclude(idx) else ElementFilter.None
                }
                expr.contains(",") -> {
                    val indices = expr.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (indices.isNotEmpty()) ElementFilter.Multiple(indices) else ElementFilter.None
                }
                else -> {
                    val idx = expr.toIntOrNull()
                    if (idx != null) ElementFilter.Single(idx) else ElementFilter.None
                }
            }
            return ParsedSelectorToken(base, filter)
        }
        
        // 2. Dot index notation: selector.0, selector.-1
        val dotRegex = Regex("""^(.+)\.(-?\d+)$""")
        val match = dotRegex.find(trimmed)
        if (match != null) {
            val base = match.groupValues[1].trim()
            val idx = match.groupValues[2].toIntOrNull()
            if (idx != null) {
                return ParsedSelectorToken(base, ElementFilter.Single(idx))
            }
        }
        
        return ParsedSelectorToken(trimmed, ElementFilter.None)
    }

    private fun applyFilter(elements: List<Element>, filter: ElementFilter): List<Element> {
        if (elements.isEmpty()) return emptyList()
        return when (filter) {
            is ElementFilter.None -> elements
            is ElementFilter.Single -> {
                val idx = if (filter.index < 0) elements.size + filter.index else filter.index
                if (idx in elements.indices) listOf(elements[idx]) else emptyList()
            }
            is ElementFilter.Exclude -> {
                val idx = if (filter.index < 0) elements.size + filter.index else filter.index
                elements.filterIndexed { i, _ -> i != idx }
            }
            is ElementFilter.Slice -> {
                val s = when {
                    filter.start == null -> 0
                    filter.start < 0 -> maxOf(0, elements.size + filter.start)
                    else -> minOf(filter.start, elements.size)
                }
                val e = when {
                    filter.end == null -> elements.size
                    filter.end < 0 -> maxOf(0, elements.size + filter.end)
                    else -> minOf(filter.end, elements.size)
                }
                if (s < e) elements.subList(s, e) else emptyList()
            }
            is ElementFilter.Multiple -> {
                filter.indices.mapNotNull { idx ->
                    val resolved = if (idx < 0) elements.size + idx else idx
                    elements.getOrNull(resolved)
                }
            }
        }
    }

    private fun navigateElements(initialElements: List<Element>, tokens: List<String>): List<Element> {
        var current = initialElements
        
        for (token in tokens) {
            val isReverse = token.startsWith("-")
            val cleanToken = if (isReverse) token.substring(1).trim() else token.trim()
            val parsed = parseFilterSelector(cleanToken)
            val css = translateToCss(parsed.baseSelector)
            
            val nextList = mutableListOf<Element>()
            for (parent in current) {
                val matched = if (css.isBlank()) parent.children() else parent.select(css)
                val resolvedMatched = if (isReverse) matched.reversed() else matched
                val filtered = applyFilter(resolvedMatched, parsed.filter)
                nextList.addAll(filtered)
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
    
    private fun applyRegex(value: String, pattern: String?, replacement: String?): String {
        if (pattern.isNullOrBlank()) return value
        return try {
            val regex = Regex(pattern)
            if (replacement != null) value.replace(regex, replacement) else regex.find(value)?.value ?: value
        } catch (e: Exception) {
            value
        }
    }

    // ==================== @js: Rule Evaluation ====================

    fun isJsRule(rule: String?): Boolean {
        if (rule.isNullOrBlank()) return false
        val trimmed = rule.trim()
        return trimmed.startsWith("@js:", ignoreCase = true) ||
               (trimmed.startsWith("<js>", ignoreCase = true) && trimmed.endsWith("</js>", ignoreCase = true))
    }

    private fun extractJsCode(rule: String): String {
        val trimmed = rule.trim()
        return when {
            trimmed.startsWith("@js:", ignoreCase = true) -> trimmed.substring(4).trim()
            trimmed.startsWith("<js>", ignoreCase = true) && trimmed.endsWith("</js>", ignoreCase = true) ->
                trimmed.substring(4, trimmed.length - 5).trim()
            else -> trimmed
        }
    }

    fun evalJsRule(element: Element, rule: String): String {
        val jsCode = extractJsCode(rule)
        
        // 1. Content paragraph extraction with ad filtering
        if (jsCode.contains(".select(") && (jsCode.contains("indexOf(") || jsCode.contains("includes(") || jsCode.contains("out.push"))) {
            val contentResult = evalContentRule(element, jsCode)
            if (contentResult.isNotBlank()) return contentResult
        }
        
        // 2. Parent / sibling traversal (e.g. coverUrl from sibling)
        if (jsCode.contains("parent()") && (jsCode.contains("img") || jsCode.contains("children()"))) {
            val siblingResult = evalSiblingTraversal(element, jsCode)
            if (siblingResult.isNotBlank()) return siblingResult
        }
        
        // 3. Regex matching: String(result).match(/pattern/)
        if (jsCode.contains(".match(/")) {
            val regexResult = evalRegexMatch(element, jsCode)
            if (regexResult.isNotBlank()) return regexResult
        }
        
        // 4. Jsoup method call chain on result / doc
        val chainResult = evalJsoupChain(element, jsCode)
        if (chainResult.isNotBlank()) return chainResult
        
        return ""
    }

    private fun evalJsoupChain(element: Element, jsCode: String): String {
        val chainPattern = Regex("""(?:result|doc)((?:\.(?:select|get|first|last|children|parent|text|ownText|html|outerHtml|attr)\s*\([^)]*\))+)""")
        val match = chainPattern.find(jsCode) ?: return ""
        val chainStr = match.groupValues[1]
        
        val stepRegex = Regex("""\.(select|get|first|last|children|parent|text|ownText|html|outerHtml|attr)\s*\(([^)]*)\)""")
        val steps = stepRegex.findAll(chainStr).toList()
        if (steps.isEmpty()) return ""
        
        var current = listOf(element)
        for (step in steps) {
            val method = step.groupValues[1]
            val arg = step.groupValues[2].trim().trim('\'', '"')
            
            when (method) {
                "select" -> {
                    current = current.flatMap { it.select(arg) }
                }
                "get" -> {
                    val idx = arg.toIntOrNull() ?: 0
                    val item = if (idx < 0) current.getOrNull(current.size + idx) else current.getOrNull(idx)
                    current = listOfNotNull(item)
                }
                "first" -> {
                    current = listOfNotNull(current.firstOrNull())
                }
                "last" -> {
                    current = listOfNotNull(current.lastOrNull())
                }
                "parent" -> {
                    current = current.mapNotNull { it.parent() }
                }
                "children" -> {
                    current = current.flatMap { it.children() }
                }
                "text" -> {
                    return current.firstOrNull()?.text().orEmpty().trim()
                }
                "ownText" -> {
                    return current.firstOrNull()?.ownText().orEmpty().trim()
                }
                "html" -> {
                    return current.firstOrNull()?.html().orEmpty().trim()
                }
                "outerHtml" -> {
                    return current.firstOrNull()?.outerHtml().orEmpty().trim()
                }
                "attr" -> {
                    val target = current.firstOrNull() ?: return ""
                    val value = if (arg.equals("href", ignoreCase = true) || arg.equals("src", ignoreCase = true)) {
                        val abs = target.absUrl(arg)
                        if (abs.isNotBlank()) abs else target.attr(arg)
                    } else {
                        target.attr(arg)
                    }
                    return value.trim()
                }
            }
            if (current.isEmpty()) return ""
        }
        return current.firstOrNull()?.text().orEmpty().trim()
    }

    private fun evalSiblingTraversal(element: Element, jsCode: String): String {
        val parent = element.parent() ?: return ""
        val siblings = parent.children()
        val currentIndex = siblings.indexOf(element)
        if (currentIndex < 0) return ""
        
        val offsetRegex = Regex("""i\s*([+-])\s*(\d+)""")
        val offsetMatch = offsetRegex.find(jsCode)
        val offset = if (offsetMatch != null) {
            val sign = if (offsetMatch.groupValues[1] == "-") -1 else 1
            val delta = offsetMatch.groupValues[2].toIntOrNull() ?: 1
            sign * delta
        } else {
            -1
        }
        
        val targetSibling = siblings.getOrNull(currentIndex + offset) ?: return ""
        val imgEl = targetSibling.select("img").firstOrNull() ?: return ""
        val src = imgEl.absUrl("src").ifBlank { imgEl.attr("src") }
        return src.trim()
    }

    private fun evalRegexMatch(element: Element, jsCode: String): String {
        val matchRegex = Regex("""\.match\(/([^/]+)/([a-z]*)\)""")
        val match = matchRegex.find(jsCode) ?: return ""
        val pattern = match.groupValues[1]
        val flags = match.groupValues[2]
        val options = mutableSetOf<RegexOption>()
        if ('i' in flags) options.add(RegexOption.IGNORE_CASE)
        if ('m' in flags) options.add(RegexOption.MULTILINE)
        
        val regex = try {
            Regex(pattern, options)
        } catch (e: Exception) {
            return ""
        }
        
        val targetText = if (element is Document) element.html() else element.outerHtml()
        val textMatch = regex.find(targetText) ?: regex.find(element.text()) ?: return ""
        
        val groupRegex = Regex("""m\[(\d+)\]""")
        val groupMatch = groupRegex.find(jsCode)
        val groupIndex = groupMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        
        return textMatch.groupValues.getOrNull(groupIndex) ?: textMatch.groupValues.getOrNull(0) ?: textMatch.value
    }

    private fun evalContentRule(element: Element, jsCode: String): String {
        val rootDoc = if (element is Document) element else (element.ownerDocument() ?: element)
        
        val selectorMatches = Regex("""\.select\(['"]([^'"]+)['"]\)""").findAll(jsCode)
            .map { it.groupValues[1] }
            .toList()
        
        var paragraphs: List<Element> = emptyList()
        for (sel in selectorMatches) {
            val matched = rootDoc.select(sel)
            if (matched.isNotEmpty()) {
                paragraphs = matched
                break
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs = rootDoc.select("#htmlContent p, .panel-readcontent p, p")
        }
        if (paragraphs.isEmpty()) return ""
        
        val continueConditions = extractContinueConditions(jsCode)
        
        val out = mutableListOf<String>()
        for (p in paragraphs) {
            val t = p.text().trim()
            if (t.isBlank()) continue
            if (shouldSkipParagraph(t, continueConditions)) continue
            out.add(t)
        }
        return out.joinToString("\n")
    }

    private fun extractContinueConditions(jsCode: String): List<String> {
        val conditions = mutableListOf<String>()
        var idx = 0
        while (idx < jsCode.length) {
            val ifIdx = jsCode.indexOf("if", idx)
            if (ifIdx < 0) break
            
            var openParen = ifIdx + 2
            while (openParen < jsCode.length && jsCode[openParen].isWhitespace()) openParen++
            if (openParen < jsCode.length && jsCode[openParen] == '(') {
                var depth = 1
                var curr = openParen + 1
                while (curr < jsCode.length && depth > 0) {
                    when (jsCode[curr]) {
                        '(' -> depth++
                        ')' -> depth--
                    }
                    curr++
                }
                if (depth == 0) {
                    val cond = jsCode.substring(openParen + 1, curr - 1).trim()
                    val rest = jsCode.substring(curr).trimStart()
                    if (rest.startsWith("continue")) {
                        conditions.add(cond)
                    }
                    idx = curr
                    continue
                }
            }
            idx = ifIdx + 2
        }
        return conditions
    }

    private fun shouldSkipParagraph(t: String, continueConditions: List<String>): Boolean {
        for (cond in continueConditions) {
            val replaced = replaceIndexOfCalls(cond, t)
            if (evalBooleanExpr(replaced)) {
                return true
            }
        }
        return false
    }

    private fun replaceIndexOfCalls(condition: String, text: String): String {
        var s = condition
        
        val positiveIndexRegex = Regex("""(?:t|text|str)\.indexOf\(['"]([^'"]+)['"]\)\s*(?:>\s*-?1|>=\s*0|!=\s*-1)""")
        s = positiveIndexRegex.replace(s) { m ->
            val kw = m.groupValues[1]
            if (text.contains(kw)) " true " else " false "
        }
        
        val negativeIndexRegex = Regex("""(?:t|text|str)\.indexOf\(['"]([^'"]+)['"]\)\s*(?:==\s*-1|<=\s*-?1|<\s*0)""")
        s = negativeIndexRegex.replace(s) { m ->
            val kw = m.groupValues[1]
            if (text.contains(kw)) " false " else " true "
        }
        
        val includesRegex = Regex("""(?:t|text|str)\.includes\(['"]([^'"]+)['"]\)""")
        s = includesRegex.replace(s) { m ->
            val kw = m.groupValues[1]
            if (text.contains(kw)) " true " else " false "
        }
        
        s = s.replace(Regex("""!(?:t|text|str)\b"""), " false ")
        
        return s
    }

    private class BooleanExprParser(private val s: String) {
        var pos = 0

        fun parseOr(): Boolean {
            var left = parseAnd()
            while (pos + 1 < s.length && s[pos] == '|' && s[pos + 1] == '|') {
                pos += 2
                val right = parseAnd()
                left = left || right
            }
            return left
        }

        private fun parseAnd(): Boolean {
            var left = parsePrimary()
            while (pos + 1 < s.length && s[pos] == '&' && s[pos + 1] == '&') {
                pos += 2
                val right = parsePrimary()
                left = left && right
            }
            return left
        }

        private fun parsePrimary(): Boolean {
            if (pos >= s.length) return false
            if (s.startsWith("true", pos)) {
                pos += 4
                return true
            }
            if (s.startsWith("false", pos)) {
                pos += 5
                return false
            }
            if (s[pos] == '!') {
                pos++
                return !parsePrimary()
            }
            if (s[pos] == '(') {
                pos++
                val res = parseOr()
                if (pos < s.length && s[pos] == ')') pos++
                return res
            }
            return false
        }
    }

    private fun evalBooleanExpr(expr: String): Boolean {
        val s = expr.replace(" ", "")
        return try {
            BooleanExprParser(s).parseOr()
        } catch (e: Exception) {
            false
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

    fun decodeUrl(value: String): String {
        return try {
            val bytes = mutableListOf<Byte>()
            var i = 0
            while (i < value.length) {
                when (val c = value[i]) {
                    '%' -> {
                        if (i + 2 < value.length) {
                            val hex = value.substring(i + 1, i + 3)
                            val byteVal = hex.toIntOrNull(16)
                            if (byteVal != null) {
                                bytes.add(byteVal.toByte())
                                i += 3
                            } else {
                                bytes.addAll(c.toString().encodeToByteArray().toList())
                                i++
                            }
                        } else {
                            bytes.addAll(c.toString().encodeToByteArray().toList())
                            i++
                        }
                    }
                    '+' -> {
                        bytes.add(' '.code.toByte())
                        i++
                    }
                    else -> {
                        bytes.addAll(c.toString().encodeToByteArray().toList())
                        i++
                    }
                }
            }
            bytes.toByteArray().decodeToString()
        } catch (e: Exception) {
            value
        }
    }
}
