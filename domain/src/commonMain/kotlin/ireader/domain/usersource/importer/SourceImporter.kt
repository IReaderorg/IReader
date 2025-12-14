package ireader.domain.usersource.importer

import ireader.domain.usersource.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Imports sources from various formats including Legado JSON.
 */
class SourceImporter {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Import result with success/failure info.
     */
    sealed class ImportResult {
        data class Success(val sources: List<UserSource>) : ImportResult()
        data class Error(val message: String, val details: String? = null) : ImportResult()
    }
    
    /**
     * Import sources from JSON string.
     * Supports both single source and array of sources.
     */
    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val trimmed = jsonString.trim()
            
            when {
                trimmed.startsWith("[") -> {
                    // Array of sources
                    val legadoSources = json.decodeFromString<List<LegadoBookSource>>(trimmed)
                    val sources = legadoSources.mapNotNull { convertLegadoSource(it) }
                    if (sources.isEmpty()) {
                        ImportResult.Error("No valid sources found in the JSON array")
                    } else {
                        ImportResult.Success(sources)
                    }
                }
                trimmed.startsWith("{") -> {
                    // Single source
                    val legadoSource = json.decodeFromString<LegadoBookSource>(trimmed)
                    val source = convertLegadoSource(legadoSource)
                    if (source != null) {
                        ImportResult.Success(listOf(source))
                    } else {
                        ImportResult.Error("Failed to parse source")
                    }
                }
                else -> {
                    ImportResult.Error("Invalid JSON format", "JSON must start with [ or {")
                }
            }
        } catch (e: Exception) {
            ImportResult.Error("Failed to parse JSON", e.message)
        }
    }
    
    /**
     * Import from URL (returns JSON string to be parsed).
     */
    fun parseImportUrl(url: String): String? {
        // Handle common source sharing URLs
        return when {
            url.contains("raw.githubusercontent.com") -> url
            url.contains("github.com") && url.contains("/blob/") -> {
                url.replace("github.com", "raw.githubusercontent.com")
                   .replace("/blob/", "/")
            }
            url.contains("gist.github.com") -> {
                "$url/raw"
            }
            url.endsWith(".json") -> url
            else -> null
        }
    }
    
    /**
     * Convert Legado BookSource to our UserSource format.
     */
    private fun convertLegadoSource(legado: LegadoBookSource): UserSource? {
        if (legado.bookSourceUrl.isBlank() || legado.bookSourceName.isBlank()) {
            return null
        }
        
        return UserSource(
            sourceUrl = legado.bookSourceUrl,
            sourceName = legado.bookSourceName,
            sourceGroup = legado.bookSourceGroup,
            sourceType = legado.bookSourceType,
            enabled = legado.enabled,
            lang = detectLanguage(legado),
            comment = legado.bookSourceComment,
            lastUpdateTime = legado.lastUpdateTime,
            header = legado.header,
            searchUrl = legado.searchUrl,
            exploreUrl = legado.exploreUrl,
            ruleSearch = convertSearchRule(legado.ruleSearch),
            ruleBookInfo = convertBookInfoRule(legado.ruleBookInfo),
            ruleToc = convertTocRule(legado.ruleToc),
            ruleContent = convertContentRule(legado.ruleContent),
            ruleExplore = convertExploreRule(legado.ruleExplore)
        )
    }
    
    private fun detectLanguage(legado: LegadoBookSource): String {
        val group = legado.bookSourceGroup.lowercase()
        val name = legado.bookSourceName.lowercase()
        val url = legado.bookSourceUrl.lowercase()
        
        return when {
            group.contains("中文") || group.contains("chinese") -> "zh"
            group.contains("한국") || group.contains("korean") -> "ko"
            group.contains("日本") || group.contains("japanese") -> "ja"
            group.contains("русский") || group.contains("russian") -> "ru"
            url.contains(".cn") || url.contains(".tw") -> "zh"
            url.contains(".kr") -> "ko"
            url.contains(".jp") -> "ja"
            url.contains(".ru") -> "ru"
            else -> "en"
        }
    }
    
    private fun convertSearchRule(rule: LegadoSearchRule?): SearchRule {
        if (rule == null) return SearchRule()
        return SearchRule(
            bookList = rule.bookList,
            name = rule.name,
            author = rule.author,
            intro = rule.intro,
            kind = rule.kind,
            lastChapter = rule.lastChapter,
            updateTime = rule.updateTime,
            bookUrl = rule.bookUrl,
            coverUrl = rule.coverUrl,
            wordCount = rule.wordCount
        )
    }
    
    private fun convertBookInfoRule(rule: LegadoBookInfoRule?): BookInfoRule {
        if (rule == null) return BookInfoRule()
        return BookInfoRule(
            init = rule.init,
            name = rule.name,
            author = rule.author,
            intro = rule.intro,
            kind = rule.kind,
            lastChapter = rule.lastChapter,
            updateTime = rule.updateTime,
            coverUrl = rule.coverUrl,
            tocUrl = rule.tocUrl,
            wordCount = rule.wordCount
        )
    }
    
    private fun convertTocRule(rule: LegadoTocRule?): TocRule {
        if (rule == null) return TocRule()
        return TocRule(
            chapterList = rule.chapterList,
            chapterName = rule.chapterName,
            chapterUrl = rule.chapterUrl,
            updateTime = rule.updateTime,
            nextTocUrl = rule.nextTocUrl,
            isReverse = rule.isReverse ?: false
        )
    }
    
    private fun convertContentRule(rule: LegadoContentRule?): ContentRule {
        if (rule == null) return ContentRule()
        return ContentRule(
            content = rule.content,
            title = rule.title,
            nextContentUrl = rule.nextContentUrl,
            replaceRegex = rule.replaceRegex,
            imageStyle = rule.imageStyle,
            purify = rule.purify
        )
    }
    
    private fun convertExploreRule(rule: LegadoExploreRule?): ExploreRule {
        if (rule == null) return ExploreRule()
        return ExploreRule(
            bookList = rule.bookList,
            name = rule.name,
            author = rule.author,
            intro = rule.intro,
            kind = rule.kind,
            lastChapter = rule.lastChapter,
            bookUrl = rule.bookUrl,
            coverUrl = rule.coverUrl
        )
    }
}


// ==================== Legado Format Models ====================

/**
 * Legado BookSource JSON format.
 */
@Serializable
data class LegadoBookSource(
    val bookSourceUrl: String = "",
    val bookSourceName: String = "",
    val bookSourceGroup: String = "",
    val bookSourceType: Int = 0,
    val enabled: Boolean = true,
    val bookSourceComment: String = "",
    val lastUpdateTime: Long = 0,
    val header: String = "",
    val searchUrl: String = "",
    val exploreUrl: String = "",
    val ruleSearch: LegadoSearchRule? = null,
    val ruleBookInfo: LegadoBookInfoRule? = null,
    val ruleToc: LegadoTocRule? = null,
    val ruleContent: LegadoContentRule? = null,
    val ruleExplore: LegadoExploreRule? = null
)

@Serializable
data class LegadoSearchRule(
    val bookList: String = "",
    val name: String = "",
    val author: String = "",
    val intro: String = "",
    val kind: String = "",
    val lastChapter: String = "",
    val updateTime: String = "",
    val bookUrl: String = "",
    val coverUrl: String = "",
    val wordCount: String = ""
)

@Serializable
data class LegadoBookInfoRule(
    val init: String = "",
    val name: String = "",
    val author: String = "",
    val intro: String = "",
    val kind: String = "",
    val lastChapter: String = "",
    val updateTime: String = "",
    val coverUrl: String = "",
    val tocUrl: String = "",
    val wordCount: String = ""
)

@Serializable
data class LegadoTocRule(
    val chapterList: String = "",
    val chapterName: String = "",
    val chapterUrl: String = "",
    val updateTime: String = "",
    val nextTocUrl: String = "",
    val isReverse: Boolean? = null
)

@Serializable
data class LegadoContentRule(
    val content: String = "",
    val title: String = "",
    val nextContentUrl: String = "",
    val replaceRegex: String = "",
    val imageStyle: String = "",
    val purify: String = ""
)

@Serializable
data class LegadoExploreRule(
    val bookList: String = "",
    val name: String = "",
    val author: String = "",
    val intro: String = "",
    val kind: String = "",
    val lastChapter: String = "",
    val bookUrl: String = "",
    val coverUrl: String = ""
)
