package ireader.domain.js.converter

/**
 * AST-based JavaScript plugin analyzer.
 * More robust than regex-based parsing.
 */
class JSPluginAnalyzerAST {
    
    private val astParser = JSASTParser()
    
    data class PluginMetadata(
        val name: String,
        val version: String,
        val lang: String,
        val baseUrl: String,
        val icon: String = "",
        val id: String = ""
    )
    
    data class SearchPattern(
        val urlTemplate: String,
        val selector: String,
        val titleSelector: String,
        val linkSelector: String,
        val coverSelector: String? = null
    )
    
    data class DetailsPattern(
        val authorSelector: String? = null,
        val descriptionSelector: String? = null,
        val genresSelector: String? = null,
        val statusSelector: String? = null,
        val coverSelector: String? = null,
        val chaptersSelector: String? = null,
        val chapterNameSelector: String? = null,
        val chapterUrlSelector: String? = null
    )
    
    data class ContentPattern(
        val selector: String,
        val textJoin: String = "\n\n"
    )
    
    data class AnalyzedPlugin(
        val metadata: PluginMetadata,
        val searchPattern: SearchPattern?,
        val detailsPattern: DetailsPattern?,
        val contentPattern: ContentPattern?
    )
    
    /**
     * Analyze JavaScript plugin code using AST parsing.
     */
    fun analyze(jsCode: String): AnalyzedPlugin {
        println("JSPluginAnalyzerAST: Parsing plugin (${jsCode.length} chars)")
        
        val parsed = astParser.parse(jsCode)
        
        val metadata = extractMetadata(parsed, jsCode)
        val searchPattern = extractSearchPattern(parsed)
        val detailsPattern = extractDetailsPattern(parsed)
        val contentPattern = extractContentPattern(parsed)
        
        println("JSPluginAnalyzerAST: Analysis complete:")
        println("  Name: ${metadata.name}")
        println("  ID: ${metadata.id}")
        println("  Version: ${metadata.version}")
        println("  Lang: ${metadata.lang}")
        println("  BaseURL: ${metadata.baseUrl}")
        println("  Icon: ${metadata.icon}")
        println("  Has search: ${searchPattern != null}")
        println("  Has details: ${detailsPattern != null}")
        println("  Has content: ${contentPattern != null}")
        
        return AnalyzedPlugin(metadata, searchPattern, detailsPattern, contentPattern)
    }
    
    /**
     * Extract metadata from parsed AST.
     */
    private fun extractMetadata(parsed: JSASTParser.ParsedPlugin, originalCode: String): PluginMetadata {
        // Try to get metadata from object literal first
        val metadataObj = parsed.metadata?.properties ?: emptyMap()
        
        // Merge with class properties
        val allProperties = metadataObj + parsed.classProperties
        
        println("JSPluginAnalyzerAST: Extracted metadata object:")
        println("  Properties found: ${allProperties.keys}")
        println("  Raw properties: $allProperties")
        
        // Extract fields with fallbacks
        val id = getStringProperty(allProperties, "id", "")
        val name = getStringProperty(allProperties, "name", "sourceName", "pluginName")
            .ifBlank {
                // Generate from ID if available
                if (id.isNotBlank()) {
                    id.split(".", "-", "_")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                } else {
                    "Unknown Plugin"
                }
            }
        
        val version = getStringProperty(allProperties, "version", "pluginVersion")
            .ifBlank { "1.0.0" }
        
        val lang = getStringProperty(allProperties, "lang", "language", "locale")
            .ifBlank { "en" }
        
        val baseUrl = getStringProperty(allProperties, "baseUrl", "site", "sourceSite", "url")
            .ifBlank {
                // Try to find any URL in the code
                """https?://[^\s'"]+""".toRegex()
                    .find(originalCode)
                    ?.value
                    ?: ""
            }
        
        val icon = getStringProperty(allProperties, "icon", "iconUrl", "image")
        
        println("JSPluginAnalyzerAST: Final metadata:")
        println("  ID: '$id'")
        println("  Name: '$name'")
        println("  BaseURL: '$baseUrl'")
        
        return PluginMetadata(
            name = name,
            version = version,
            lang = lang,
            baseUrl = baseUrl,
            icon = icon,
            id = id
        )
    }
    
    /**
     * Extract search pattern from parsed functions.
     */
    private fun extractSearchPattern(parsed: JSASTParser.ParsedPlugin): SearchPattern? {
        // Look for search-related functions
        val searchFunctions = listOf(
            "searchNovels", "search", "popularNovels", "latestNovels",
            "getMangaList", "getPopular", "getLatest", "browse"
        )
        
        val searchFunc = searchFunctions.firstNotNullOfOrNull { funcName ->
            parsed.functions[funcName]
        }
        
        if (searchFunc == null) {
            println("JSPluginAnalyzerAST: No search function found")
            return null
        }
        
        println("JSPluginAnalyzerAST: Found search function: ${searchFunc.name}")
        
        val body = searchFunc.body
        
        // Extract URL template
        val urlTemplate = extractUrlFromFunction(body)
        
        // Extract selectors
        val mainSelector = extractSelector(body, listOf(
            "novel", "book", "manga", "series", "story", "item", "entry", "post", "article"
        )) ?: ".novel-item, .book-item, .item"
        
        val titleSelector = extractSelector(body, listOf("title", "name", "heading"))
            ?: ".title, h3, h2, .name"
        
        val linkSelector = "a, .link"
        
        val coverSelector = extractSelector(body, listOf("cover", "image", "img", "thumbnail"))
        
        return SearchPattern(
            urlTemplate = urlTemplate,
            selector = mainSelector,
            titleSelector = titleSelector,
            linkSelector = linkSelector,
            coverSelector = coverSelector
        )
    }
    
    /**
     * Extract details pattern from parsed functions.
     */
    private fun extractDetailsPattern(parsed: JSASTParser.ParsedPlugin): DetailsPattern? {
        val detailsFunctions = listOf(
            "getDetails", "fetchDetails", "getMangaDetails", "getNovelDetails",
            "parseNovelDetails", "novelDetails"
        )
        
        val detailsFunc = detailsFunctions.firstNotNullOfOrNull { funcName ->
            parsed.functions[funcName]
        }
        
        if (detailsFunc == null) {
            println("JSPluginAnalyzerAST: No details function found, using defaults")
            return DetailsPattern(
                authorSelector = ".author, .writer",
                descriptionSelector = ".description, .summary, .synopsis",
                genresSelector = ".genre, .tag, .category",
                statusSelector = ".status",
                coverSelector = ".cover, img.cover",
                chaptersSelector = ".chapter, .chapter-item, li.chapter"
            )
        }
        
        println("JSPluginAnalyzerAST: Found details function: ${detailsFunc.name}")
        
        val body = detailsFunc.body
        
        return DetailsPattern(
            authorSelector = extractSelector(body, listOf("author", "writer")),
            descriptionSelector = extractSelector(body, listOf("description", "summary", "synopsis", "about")),
            genresSelector = extractSelector(body, listOf("genre", "tag", "category")),
            statusSelector = extractSelector(body, listOf("status")),
            coverSelector = extractSelector(body, listOf("cover", "image", "img")),
            chaptersSelector = extractSelector(body, listOf("chapter", "episode", "chapters"))
        )
    }
    
    /**
     * Extract content pattern from parsed functions.
     */
    private fun extractContentPattern(parsed: JSASTParser.ParsedPlugin): ContentPattern? {
        val contentFunctions = listOf(
            "getContent", "fetchContent", "getChapter", "getChapterContent",
            "parseChapter", "chapterContent", "getPageList"
        )
        
        val contentFunc = contentFunctions.firstNotNullOfOrNull { funcName ->
            parsed.functions[funcName]
        }
        
        if (contentFunc == null) {
            println("JSPluginAnalyzerAST: No content function found, using defaults")
            return ContentPattern(
                selector = ".content, .chapter-content, #content, .text, p",
                textJoin = "\n\n"
            )
        }
        
        println("JSPluginAnalyzerAST: Found content function: ${contentFunc.name}")
        
        val body = contentFunc.body
        
        val selector = extractSelector(body, listOf("content", "text", "chapter", "body"))
            ?: ".content, .chapter-content, #content, p"
        
        val textJoin = when {
            body.contains("""join(['"]\\n\\n['"]\)""") -> "\n\n"
            body.contains("""join(['"]\\n['"]\)""") -> "\n"
            body.contains("""join(['"]\s*['"]\)""") -> " "
            else -> "\n\n"
        }
        
        return ContentPattern(
            selector = selector,
            textJoin = textJoin
        )
    }
    
    /**
     * Extract URL template from function body.
     */
    private fun extractUrlFromFunction(body: String): String {
        // Look for URL patterns
        val patterns = listOf(
            // Template literals: `${this.site}/search?q=${query}`
            """`([^`]*\$\{[^`]+)`""".toRegex(),
            // String concatenation: this.site + "/search"
            """this\.(?:site|baseUrl)\s*\+\s*['"]([^'"]+)['"]""".toRegex(),
            // Direct URL: "https://example.com/search"
            """['"]([^'"]*(?:search|page|novel|browse)[^'"]*\?[^'"]+)['"]""".toRegex(),
            // fetchApi calls
            """fetchApi\s*\(\s*['"]([^'"]+)['"]""".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val url = match.groupValues[1]
                if (url.length > 5) {
                    println("JSPluginAnalyzerAST: Found URL pattern: $url")
                    return url
                }
            }
        }
        
        return "/search?q=\${query}&page=\${page}"
    }
    
    /**
     * Extract CSS selector from function body based on keywords.
     */
    private fun extractSelector(body: String, keywords: List<String>): String? {
        // Look for selector patterns
        val patterns = listOf(
            // Cheerio/jQuery: $('selector')
            """\$\(['"]([^'"]+)['"]\)""".toRegex(),
            // querySelector: .querySelector('selector')
            """\.querySelector(?:All)?\s*\(\s*['"]([^'"]+)['"]\)""".toRegex(),
            // select: .select('selector')
            """\.select\s*\(\s*['"]([^'"]+)['"]\)""".toRegex(),
            // find: .find('selector')
            """\.find\s*\(\s*['"]([^'"]+)['"]\)""".toRegex()
        )
        
        for (pattern in patterns) {
            pattern.findAll(body).forEach { match ->
                val selector = match.groupValues[1]
                // Check if selector contains any of the keywords
                if (keywords.any { keyword -> selector.contains(keyword, ignoreCase = true) }) {
                    println("JSPluginAnalyzerAST: Found selector for ${keywords.first()}: $selector")
                    return selector
                }
            }
        }
        
        return null
    }
    
    /**
     * Helper to get string property from map with multiple possible keys.
     */
    private fun getStringProperty(properties: Map<String, Any>, vararg keys: String): String {
        for (key in keys) {
            val value = properties[key]
            if (value != null) {
                val stringValue = astParser.getStringValue(value)
                if (stringValue.isNotBlank()) {
                    return stringValue
                }
            }
        }
        return ""
    }
}
