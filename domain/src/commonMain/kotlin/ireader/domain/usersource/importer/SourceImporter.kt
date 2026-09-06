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
     * Supports arrays, single sources, wrapped structures (sources/data/list), and flexible rule formats.
     */
    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val trimmed = jsonString.trim()
            val rootElement = json.parseToJsonElement(trimmed)
            
            val sourceObjects: List<kotlinx.serialization.json.JsonObject> = when (rootElement) {
                is kotlinx.serialization.json.JsonArray -> rootElement.filterIsInstance<kotlinx.serialization.json.JsonObject>()
                is kotlinx.serialization.json.JsonObject -> {
                    when {
                        rootElement.containsKey("sources") && rootElement["sources"] is kotlinx.serialization.json.JsonArray -> {
                            (rootElement["sources"] as kotlinx.serialization.json.JsonArray).filterIsInstance<kotlinx.serialization.json.JsonObject>()
                        }
                        rootElement.containsKey("data") && rootElement["data"] is kotlinx.serialization.json.JsonArray -> {
                            (rootElement["data"] as kotlinx.serialization.json.JsonArray).filterIsInstance<kotlinx.serialization.json.JsonObject>()
                        }
                        rootElement.containsKey("list") && rootElement["list"] is kotlinx.serialization.json.JsonArray -> {
                            (rootElement["list"] as kotlinx.serialization.json.JsonArray).filterIsInstance<kotlinx.serialization.json.JsonObject>()
                        }
                        rootElement.containsKey("bookSourceUrl") || rootElement.containsKey("bookSourceName") || rootElement.containsKey("url") -> {
                            listOf(rootElement)
                        }
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }

            if (sourceObjects.isEmpty()) {
                return ImportResult.Error("No valid sources found in the JSON")
            }

            val sources = sourceObjects.mapNotNull { parseSource(it) }
            if (sources.isEmpty()) {
                ImportResult.Error("No valid sources found in the JSON")
            } else {
                ImportResult.Success(sources)
            }
        } catch (e: Exception) {
            ImportResult.Error("Failed to parse JSON", e.message)
        }
    }
    
    /**
     * Import from URL (returns URL string to be parsed or fetched).
     */
    fun parseImportUrl(url: String): String? {
        val trimmed = url.trim()
        return when {
            trimmed.contains("raw.githubusercontent.com") -> trimmed
            trimmed.contains("github.com") && trimmed.contains("/blob/") -> {
                trimmed.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }
            trimmed.contains("gist.github.com") -> "$trimmed/raw"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> null
        }
    }
    
    private fun parseSource(obj: kotlinx.serialization.json.JsonObject): UserSource? {
        val sourceUrl = obj.getString("bookSourceUrl").ifBlank { obj.getString("url") }
        val sourceName = obj.getString("bookSourceName").ifBlank { obj.getString("name") }
        if (sourceUrl.isBlank() || sourceName.isBlank()) {
            return null
        }
        
        val sourceGroup = obj.getString("bookSourceGroup").ifBlank { obj.getString("group") }
        val sourceType = obj.getInt("bookSourceType", obj.getInt("type", UserSource.TYPE_NOVEL))
        val enabled = obj.getBoolean("enabled", true)
        val comment = obj.getString("bookSourceComment").ifBlank { obj.getString("comment") }.ifBlank { obj.getString("note") }
        val lastUpdateTime = obj.getLong("lastUpdateTime", 0L)
        val header = obj.getStringOrObject("header")
        val searchUrl = obj.getString("searchUrl")
        val exploreUrl = obj.getString("exploreUrl")
        
        return UserSource(
            sourceUrl = sourceUrl,
            sourceName = sourceName,
            sourceGroup = sourceGroup,
            sourceType = sourceType,
            enabled = enabled,
            lang = detectLanguage(sourceGroup, sourceName, sourceUrl),
            comment = comment,
            lastUpdateTime = lastUpdateTime,
            header = header,
            searchUrl = searchUrl,
            exploreUrl = exploreUrl,
            ruleSearch = parseSearchRule(obj["ruleSearch"]),
            ruleBookInfo = parseBookInfoRule(obj["ruleBookInfo"]),
            ruleToc = parseTocRule(obj["ruleToc"]),
            ruleContent = parseContentRule(obj["ruleContent"]),
            ruleExplore = parseExploreRule(obj["ruleExplore"])
        )
    }

    private fun detectLanguage(group: String, name: String, url: String): String {
        val g = group.lowercase()
        val n = name.lowercase()
        val u = url.lowercase()
        
        return when {
            g.contains("中文") || g.contains("chinese") || n.contains("中文") -> "zh"
            g.contains("한국") || g.contains("korean") -> "ko"
            g.contains("日本") || g.contains("japanese") -> "ja"
            g.contains("русский") || g.contains("russian") -> "ru"
            u.contains(".cn") || u.contains(".tw") -> "zh"
            u.contains(".kr") -> "ko"
            u.contains(".jp") -> "ja"
            u.contains(".ru") -> "ru"
            else -> "en"
        }
    }

    private fun resolveRuleObject(element: kotlinx.serialization.json.JsonElement?): kotlinx.serialization.json.JsonObject? {
        if (element == null) return null
        return when (element) {
            is kotlinx.serialization.json.JsonObject -> element
            is kotlinx.serialization.json.JsonPrimitive -> {
                val content = element.content.trim()
                if (content.startsWith("{")) {
                    try {
                        json.parseToJsonElement(content) as? kotlinx.serialization.json.JsonObject
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
            else -> null
        }
    }

    private fun parseSearchRule(element: kotlinx.serialization.json.JsonElement?): SearchRule {
        val obj = resolveRuleObject(element) ?: return SearchRule()
        return SearchRule(
            bookList = obj.getString("bookList"),
            name = obj.getString("name"),
            author = obj.getString("author"),
            intro = obj.getString("intro"),
            kind = obj.getString("kind"),
            lastChapter = obj.getString("lastChapter"),
            updateTime = obj.getString("updateTime"),
            bookUrl = obj.getString("bookUrl"),
            coverUrl = obj.getString("coverUrl"),
            wordCount = obj.getString("wordCount")
        )
    }

    private fun parseBookInfoRule(element: kotlinx.serialization.json.JsonElement?): BookInfoRule {
        val obj = resolveRuleObject(element) ?: return BookInfoRule()
        return BookInfoRule(
            init = obj.getString("init"),
            name = obj.getString("name"),
            author = obj.getString("author"),
            intro = obj.getString("intro"),
            kind = obj.getString("kind"),
            lastChapter = obj.getString("lastChapter"),
            updateTime = obj.getString("updateTime"),
            coverUrl = obj.getString("coverUrl"),
            tocUrl = obj.getString("tocUrl"),
            wordCount = obj.getString("wordCount")
        )
    }

    private fun parseTocRule(element: kotlinx.serialization.json.JsonElement?): TocRule {
        val obj = resolveRuleObject(element) ?: return TocRule()
        val chapterList = obj.getString("chapterList")
        val isReverse = obj.getBoolean("isReverse", false) || chapterList.startsWith("-")
        return TocRule(
            chapterList = chapterList,
            chapterName = obj.getString("chapterName"),
            chapterUrl = obj.getString("chapterUrl"),
            updateTime = obj.getString("updateTime"),
            nextTocUrl = obj.getString("nextTocUrl"),
            isReverse = isReverse
        )
    }

    private fun parseContentRule(element: kotlinx.serialization.json.JsonElement?): ContentRule {
        val obj = resolveRuleObject(element) ?: return ContentRule()
        return ContentRule(
            content = obj.getString("content"),
            title = obj.getString("title"),
            nextContentUrl = obj.getString("nextContentUrl"),
            replaceRegex = obj.getString("replaceRegex"),
            imageStyle = obj.getString("imageStyle"),
            purify = obj.getString("purify")
        )
    }

    private fun parseExploreRule(element: kotlinx.serialization.json.JsonElement?): ExploreRule {
        val obj = resolveRuleObject(element) ?: return ExploreRule()
        return ExploreRule(
            bookList = obj.getString("bookList"),
            name = obj.getString("name"),
            author = obj.getString("author"),
            intro = obj.getString("intro"),
            kind = obj.getString("kind"),
            lastChapter = obj.getString("lastChapter"),
            bookUrl = obj.getString("bookUrl"),
            coverUrl = obj.getString("coverUrl")
        )
    }

    private fun kotlinx.serialization.json.JsonObject.getString(key: String, default: String = ""): String {
        val el = this[key] ?: return default
        return when (el) {
            is kotlinx.serialization.json.JsonPrimitive -> el.content
            else -> el.toString()
        }
    }

    private fun kotlinx.serialization.json.JsonObject.getInt(key: String, default: Int = 0): Int {
        val el = this[key] ?: return default
        return when (el) {
            is kotlinx.serialization.json.JsonPrimitive -> {
                el.content.toIntOrNull() ?: default
            }
            else -> default
        }
    }

    private fun kotlinx.serialization.json.JsonObject.getLong(key: String, default: Long = 0L): Long {
        val el = this[key] ?: return default
        return when (el) {
            is kotlinx.serialization.json.JsonPrimitive -> {
                el.content.toLongOrNull() ?: default
            }
            else -> default
        }
    }

    private fun kotlinx.serialization.json.JsonObject.getBoolean(key: String, default: Boolean = true): Boolean {
        val el = this[key] ?: return default
        return when (el) {
            is kotlinx.serialization.json.JsonPrimitive -> {
                when (el.content.lowercase()) {
                    "1", "true" -> true
                    "0", "false" -> false
                    else -> default
                }
            }
            else -> default
        }
    }

    private fun kotlinx.serialization.json.JsonObject.getStringOrObject(key: String): String {
        val el = this[key] ?: return ""
        return when (el) {
            is kotlinx.serialization.json.JsonPrimitive -> el.content
            else -> el.toString()
        }
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
