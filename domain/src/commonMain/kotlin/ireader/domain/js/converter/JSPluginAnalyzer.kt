package ireader.domain.js.converter

/**
 * Analyzes JavaScript plugin code to extract metadata and logic patterns
 */
class JSPluginAnalyzer {
    
    data class PluginMetadata(
        val name: String,
        val version: String,
        val lang: String,
        val baseUrl: String,
        val icon: String = ""
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
    
    fun analyze(jsCode: String): AnalyzedPlugin {
        // Log first part of code for debugging
        println("JSPluginAnalyzer: Analyzing plugin (${jsCode.length} chars)")
        println("JSPluginAnalyzer: First 500 chars:")
        println(jsCode.take(500))
        println("---")
        
        val metadata = extractMetadata(jsCode)
        val searchPattern = extractSearchPattern(jsCode)
        val detailsPattern = extractDetailsPattern(jsCode)
        val contentPattern = extractContentPattern(jsCode)
        
        // Log what was extracted for debugging
        println("JSPluginAnalyzer: Analysis complete:")
        println("  Name: ${metadata.name}")
        println("  Version: ${metadata.version}")
        println("  Lang: ${metadata.lang}")
        println("  BaseURL: ${metadata.baseUrl}")
        println("  Icon: ${metadata.icon}")
        println("  Has search pattern: ${searchPattern != null}")
        println("  Has details pattern: ${detailsPattern != null}")
        println("  Has content pattern: ${contentPattern != null}")
        
        if (searchPattern == null) {
            println("JSPluginAnalyzer: WARNING - No search pattern detected.")
            println("  This plugin may not support browsing/searching.")
            println("  You can still read novels if you have direct URLs.")
        }
        
        return AnalyzedPlugin(metadata, searchPattern, detailsPattern, contentPattern)
    }
    
    private fun extractMetadata(jsCode: String): PluginMetadata {
        // Try multiple patterns for each field to handle different plugin formats
        
        // First, try to find minified metadata - look for the pattern with id, name, version together
        // Pattern: {id:"x",name:"y",version:"z",site:"w",...} or similar variations
        val minifiedPatterns = listOf(
            // Pattern 1: id,name,version,site in sequence
            """\{[^}]{0,200}id\s*:\s*['"]([^'"]+)['"][^}]{0,100}name\s*:\s*['"]([^'"]+)['"][^}]{0,100}version\s*:\s*['"]([^'"]+)['"][^}]{0,100}(?:site|baseUrl)\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            // Pattern 2: id,sourceName,version,sourceSite (different naming)
            """\{[^}]{0,200}id\s*:\s*['"]([^'"]+)['"][^}]{0,100}sourceName\s*:\s*['"]([^'"]+)['"][^}]{0,100}version\s*:\s*['"]([^'"]+)['"][^}]{0,100}sourceSite\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            // Pattern 3: Just id and name close together
            """\{[^}]{0,100}id\s*:\s*['"]([^'"]+)['"][^}]{0,50}name\s*:\s*['"]([^'"]+)['"]""".toRegex()
        )
        
        for (pattern in minifiedPatterns) {
            val match = pattern.find(jsCode)
            if (match != null) {
                val id = match.groupValues.getOrNull(1) ?: ""
                val name = match.groupValues.getOrNull(2) ?: ""
                val version = match.groupValues.getOrNull(3) ?: "1.0.0"
                val site = match.groupValues.getOrNull(4) ?: ""
                
                if (name.isNotBlank() && name.length > 2) {
                    println("JSPluginAnalyzer: Found minified metadata: id='$id', name='$name'")
                    
                    // Try to find icon and lang in nearby code
                    val contextStart = maxOf(0, (match.range.first - 500))
                    val contextEnd = minOf(jsCode.length, (match.range.last + 500))
                    val context = jsCode.substring(contextStart, contextEnd)
                    
                    val iconMatch = """icon\s*:\s*['"]([^'"]+)['"]""".toRegex().find(context)
                    val langMatch = """lang\s*:\s*['"]([a-z]{2,5})['"]""".toRegex().find(context)
                    
                    return PluginMetadata(
                        name = name,
                        version = version,
                        lang = langMatch?.groupValues?.get(1) ?: "en",
                        baseUrl = site,
                        icon = iconMatch?.groupValues?.get(1) ?: ""
                    )
                }
            }
        }
        
        // Name patterns - try many variations
        val namePatterns = listOf(
            // Object property: name: "value"
            """name\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            // String key: "name": "value"
            """['"]name['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            // Assignment: this.name = "value"
            """this\.name\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            // Class property: get name() { return "value" }
            """get\s+name\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex(),
            // Direct property in class
            """class\s+\w+.*?name\s*=\s*['"]([^'"]+)['"]""".toRegex(RegexOption.DOT_MATCHES_ALL),
            // In metadata object
            """metadata\s*:\s*\{[^}]*name\s*:\s*['"]([^'"]+)['"]""".toRegex(RegexOption.DOT_MATCHES_ALL),
            // Minified with id before name: id:"x",name:"y"
            """id\s*:\s*['"][^'"]+['"],\s*name\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            // Any occurrence of name:"value" (last resort)
            """name\s*:\s*['"]([^'"]{3,})['"]""".toRegex()
        )
        val name = namePatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(jsCode)?.groupValues?.get(1)?.takeIf { it.isNotBlank() && it.length > 2 }
        } ?: run {
            println("JSPluginAnalyzer: Could not extract name from plugin")
            "Unknown Plugin"
        }
        
        // Version patterns - look for version near name/id
        val versionPatterns = listOf(
            """version\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]version['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.version\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            """get\s+version\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex(),
            // In minified: name:"x",version:"y"
            """name\s*:\s*['"][^'"]+['"],\s*version\s*:\s*['"]([^'"]+)['"]""".toRegex()
        )
        val version = versionPatterns.firstNotNullOfOrNull { 
            it.find(jsCode)?.groupValues?.get(1)?.takeIf { v -> v.matches(Regex("""[\d.]+""")) }
        } ?: "1.0.0"
        
        // Lang patterns
        val langPatterns = listOf(
            """lang\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]lang['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """language\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.lang\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            """get\s+lang\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex()
        )
        val lang = langPatterns.firstNotNullOfOrNull { 
            it.find(jsCode)?.groupValues?.get(1)?.takeIf { l -> l.length == 2 || l.length == 5 }
        } ?: "en"
        
        // BaseUrl/Site patterns - look for URLs
        val baseUrlPatterns = listOf(
            """baseUrl\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """site\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]baseUrl['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]site['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.site\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.baseUrl\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            """get\s+site\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex(),
            """get\s+baseUrl\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex(),
            // Look for https:// URLs
            """['"]?(https?://[^'"]+)['"]?""".toRegex()
        )
        val baseUrl = baseUrlPatterns.firstNotNullOfOrNull { 
            it.find(jsCode)?.groupValues?.get(1)?.takeIf { url -> url.startsWith("http") }
        } ?: ""
        
        // Icon patterns
        val iconPatterns = listOf(
            """icon\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]icon['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.icon\s*=\s*['"]([^'"]+)['"]""".toRegex(),
            """get\s+icon\s*\(\s*\)\s*\{\s*return\s*['"]([^'"]+)['"]""".toRegex()
        )
        val icon = iconPatterns.firstNotNullOfOrNull { it.find(jsCode)?.groupValues?.get(1) } ?: ""
        
        // ID patterns (some plugins use 'id' instead of deriving from name)
        val idPatterns = listOf(
            """id\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """['"]id['"]\s*:\s*['"]([^'"]+)['"]""".toRegex(),
            """this\.id\s*=\s*['"]([^'"]+)['"]""".toRegex()
        )
        val id = idPatterns.firstNotNullOfOrNull { it.find(jsCode)?.groupValues?.get(1) }
        
        // If name is "Unknown Plugin" but we have an ID, use the ID to generate a name
        val finalName = if (name == "Unknown Plugin" && id != null && id.isNotBlank()) {
            // Convert ID to readable name: "wuxiaworld.site" -> "Wuxiaworld Site"
            id.split(".", "-", "_")
                .joinToString(" ") { word -> 
                    word.replaceFirstChar { it.uppercase() }
                }
                .also { generatedName ->
                    println("JSPluginAnalyzer: Generated name from ID: '$generatedName'")
                }
        } else {
            name
        }
        
        // Log what was found for debugging
        println("JSPluginAnalyzer: Metadata extraction results:")
        println("  Name: '$finalName'")
        println("  ID: '${id ?: "not found"}'")
        println("  Version: '$version'")
        println("  Lang: '$lang'")
        println("  BaseURL: '$baseUrl'")
        println("  Icon: '$icon'")
        
        return PluginMetadata(
            name = finalName,
            version = version,
            lang = lang,
            baseUrl = baseUrl,
            icon = icon
        )
    }
    
    private fun extractSearchPattern(jsCode: String): SearchPattern? {
        // For minified plugins, look for searchNovels or popularNovels functions
        // These are the actual functions that fetch novel lists
        val searchFunctionPatterns = listOf(
            // searchNovels function (most common)
            """searchNovels[^{]*\{""".toRegex(),
            // popularNovels function (also common)
            """popularNovels[^{]*\{""".toRegex(),
            // getMangaList (alternative naming)
            """getMangaList[^{]*\{""".toRegex(),
            // search function (simple naming)
            """(?:async\s+)?search\s*\([^)]*\)\s*\{""".toRegex()
        )
        
        // Check if any search/popular function exists
        val hasSearchFunction = searchFunctionPatterns.any { pattern ->
            pattern.find(jsCode) != null
        }
        
        if (!hasSearchFunction) {
            println("JSPluginAnalyzer: No search/popular function found")
            return null
        }
        
        println("JSPluginAnalyzer: Found search/popular function")
        
        // Try to extract URL patterns from the code
        val searchBody = jsCode
        
        // Extract URL patterns - look for common URL construction patterns
        val urlPatterns = listOf(
            // Template literals
            """['"]([^'"]*(?:search|page|novel|series|browse)[^'"]*\?[^'"]+)['"]""".toRegex(),
            // fetchApi calls
            """fetchApi\s*\(\s*['"]([^'"]+)['"]""".toRegex(),
            // this.site concatenation
            """this\.site\s*\+\s*['"]([^'"]+)['"]""".toRegex(),
            // Direct URLs
            """(?:https?://[^'"]+)""".toRegex()
        )
        
        var urlTemplate = urlPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(searchBody)?.groupValues?.get(1)?.takeIf { it.length > 5 }
        }
        
        // If no template found, construct a generic one
        if (urlTemplate == null) {
            println("JSPluginAnalyzer: No URL template found, using generic")
            urlTemplate = "/search?q=\${query}&page=\${page}"
        } else {
            println("JSPluginAnalyzer: Found URL pattern: $urlTemplate")
        }
        
        // Extract selectors - look for common HTML parsing patterns
        val selectorPatterns = listOf(
            // Cheerio/jQuery patterns
            """\$\(['"]([^'"]+)['"]\)\.(?:map|each|find)""".toRegex(),
            """\.select\(['"]([^'"]+)['"]\)""".toRegex(),
            // Common novel list selectors in the wild
            """['"]([^'"]*(?:novel|book|manga|series|story|item|entry|post|article)[^'"]*)['"]\)""".toRegex()
        )
        
        val mainSelector = selectorPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(searchBody)?.groupValues?.get(1)?.takeIf { 
                it.contains("-") || it.contains(".") || it.contains("#")
            }
        } ?: run {
            println("JSPluginAnalyzer: No selector found, using defaults")
            ".novel-item, .book-item, .manga-item, .series-item, .item, .post, article"
        }
        
        // Try to find title selector
        val titlePatterns = listOf(
            """\.find\(['"]([^'"]*title[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*title[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.querySelector\(['"]([^'"]*title[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val titleSelector = titlePatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(searchBody)?.groupValues?.get(1)
        } ?: ".title, h3, h2, h1, .name, .book-title"
        
        // Try to find link selector
        val linkSelector = "a, .link"
        
        // Try to find cover selector
        val coverPatterns = listOf(
            """\.find\(['"]([^'"]*(?:img|cover|image)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:img|cover|image)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val coverSelector = coverPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(searchBody)?.groupValues?.get(1)
        }
        
        return SearchPattern(
            urlTemplate = urlTemplate,
            selector = mainSelector,
            titleSelector = titleSelector,
            linkSelector = linkSelector,
            coverSelector = coverSelector
        )
    }
    
    private fun extractDetailsPattern(jsCode: String): DetailsPattern? {
        // Look for details/manga function patterns
        val detailsFunctionPatterns = listOf(
            """(?:async\s+)?(?:getDetails|fetchDetails|getMangaDetails|getNovelDetails)\s*\([^)]*\)\s*\{([^}]+(?:\{[^}]*\}[^}]*)*)\}""".toRegex(),
            """(?:getDetails|fetchDetails):\s*(?:async\s+)?function\s*\([^)]*\)\s*\{([^}]+(?:\{[^}]*\}[^}]*)*)\}""".toRegex()
        )
        
        val detailsBody = detailsFunctionPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(jsCode)?.groupValues?.get(1)
        } ?: return DetailsPattern() // Return empty pattern instead of null
        
        // Extract author selector
        val authorPatterns = listOf(
            """\$\(['"]([^'"]*author[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*author[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.querySelector\(['"]([^'"]*author[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val authorSelector = authorPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        } ?: ".author, .writer, .author-name"
        
        // Extract description selector
        val descPatterns = listOf(
            """\$\(['"]([^'"]*(?:description|summary|synopsis)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:description|summary|synopsis)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val descriptionSelector = descPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        } ?: ".description, .summary, .synopsis, .about"
        
        // Extract genres selector
        val genresPatterns = listOf(
            """\$\(['"]([^'"]*(?:genre|tag|category)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:genre|tag|category)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val genresSelector = genresPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        } ?: ".genre, .tag, .category, .genres"
        
        // Extract status selector
        val statusPatterns = listOf(
            """\$\(['"]([^'"]*status[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*status[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val statusSelector = statusPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        } ?: ".status, .book-status"
        
        // Extract cover selector
        val coverPatterns = listOf(
            """\$\(['"]([^'"]*(?:cover|image|img)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:cover|image|img)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val coverSelector = coverPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        }
        
        // Extract chapters selector
        val chaptersPatterns = listOf(
            """\$\(['"]([^'"]*(?:chapter|episode)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:chapter|episode)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        val chaptersSelector = chaptersPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(detailsBody)?.groupValues?.get(1)
        } ?: ".chapter, .chapter-item, .episode, li.chapter"
        
        return DetailsPattern(
            authorSelector = authorSelector,
            descriptionSelector = descriptionSelector,
            genresSelector = genresSelector,
            statusSelector = statusSelector,
            coverSelector = coverSelector,
            chaptersSelector = chaptersSelector
        )
    }
    
    private fun extractContentPattern(jsCode: String): ContentPattern? {
        // Look for content/chapter function patterns
        val contentFunctionPatterns = listOf(
            """(?:async\s+)?(?:getContent|fetchContent|getChapter|getPageList)\s*\([^)]*\)\s*\{([^}]+(?:\{[^}]*\}[^}]*)*)\}""".toRegex(),
            """(?:getContent|fetchContent):\s*(?:async\s+)?function\s*\([^)]*\)\s*\{([^}]+(?:\{[^}]*\}[^}]*)*)\}""".toRegex()
        )
        
        val contentBody = contentFunctionPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(jsCode)?.groupValues?.get(1)
        } ?: return ContentPattern(".content, .chapter-content, #content, .text") // Default pattern
        
        // Extract content selector
        val selectorPatterns = listOf(
            """\$\(['"]([^'"]*(?:content|text|chapter)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.select\(['"]([^'"]*(?:content|text|chapter)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE),
            """\.querySelectorAll\(['"]([^'"]*(?:content|text|chapter)[^'"]*)['"]\)""".toRegex(RegexOption.IGNORE_CASE)
        )
        
        val selector = selectorPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(contentBody)?.groupValues?.get(1)
        } ?: ".content, .chapter-content, #content, .text, p"
        
        // Detect join pattern
        val textJoin = if (contentBody.contains("join")) {
            when {
                contentBody.contains("""join\(['"]\\n\\n['"]\)""") -> "\n\n"
                contentBody.contains("""join\(['"]\\n['"]\)""") -> "\n"
                contentBody.contains("""join\(['"]\s*['"]\)""") -> " "
                else -> "\n\n"
            }
        } else {
            "\n\n"
        }
        
        return ContentPattern(
            selector = selector,
            textJoin = textJoin
        )
    }
}
