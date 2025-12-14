package ireader.domain.usersource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User-defined source configuration in Legado/Yuedu style.
 * Users can create and share these sources via JSON.
 */
@Serializable
data class UserSource(
    /** Unique URL identifier for the source */
    @SerialName("bookSourceUrl")
    val sourceUrl: String = "",
    
    /** Display name of the source */
    @SerialName("bookSourceName")
    val sourceName: String = "",
    
    /** Group/category for organizing sources */
    @SerialName("bookSourceGroup")
    val sourceGroup: String = "",
    
    /** Source type: 0=Novel, 1=Audio, 2=Image/Manga */
    @SerialName("bookSourceType")
    val sourceType: Int = TYPE_NOVEL,
    
    /** Whether the source is enabled */
    val enabled: Boolean = true,
    
    /** Language code (e.g., "en", "zh", "ru") */
    val lang: String = "en",
    
    /** Custom order for sorting */
    val customOrder: Int = 0,
    
    /** Comment/description for the source */
    @SerialName("bookSourceComment")
    val comment: String = "",
    
    /** Last update timestamp */
    val lastUpdateTime: Long = 0,
    
    /** Custom headers as JSON string */
    val header: String = "",
    
    /** Search URL template with {{key}} and {{page}} placeholders */
    val searchUrl: String = "",
    
    /** Explore/browse URL for discovery */
    val exploreUrl: String = "",
    
    /** Search result parsing rules */
    val ruleSearch: SearchRule = SearchRule(),
    
    /** Book detail page parsing rules */
    val ruleBookInfo: BookInfoRule = BookInfoRule(),
    
    /** Table of contents parsing rules */
    val ruleToc: TocRule = TocRule(),
    
    /** Chapter content parsing rules */
    val ruleContent: ContentRule = ContentRule(),
    
    /** Explore/browse parsing rules */
    val ruleExplore: ExploreRule = ExploreRule()
) {
    companion object {
        const val TYPE_NOVEL = 0
        const val TYPE_AUDIO = 1
        const val TYPE_IMAGE = 2
    }
    
    /** Generate a unique ID for this source */
    fun generateId(): Long {
        val key = "${sourceName.lowercase()}/$lang/${sourceUrl.hashCode()}"
        var hash = 0L
        for (char in key) {
            hash = 31 * hash + char.code
        }
        return hash and Long.MAX_VALUE
    }
    
    /** Check if source has minimum required configuration */
    fun isValid(): Boolean {
        return sourceUrl.isNotBlank() && 
               sourceName.isNotBlank() &&
               (searchUrl.isNotBlank() || exploreUrl.isNotBlank())
    }
}

/**
 * Search result parsing rules.
 */
@Serializable
data class SearchRule(
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

/**
 * Book detail page parsing rules.
 */
@Serializable
data class BookInfoRule(
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

/**
 * Table of contents parsing rules.
 */
@Serializable
data class TocRule(
    val chapterList: String = "",
    val chapterName: String = "",
    val chapterUrl: String = "",
    val updateTime: String = "",
    val nextTocUrl: String = "",
    val isReverse: Boolean = false
)

/**
 * Chapter content parsing rules.
 */
@Serializable
data class ContentRule(
    val content: String = "",
    val title: String = "",
    val nextContentUrl: String = "",
    val replaceRegex: String = "",
    val imageStyle: String = "",
    val purify: String = ""  // Selectors to remove
)

/**
 * Explore/browse page parsing rules.
 */
@Serializable
data class ExploreRule(
    val bookList: String = "",
    val name: String = "",
    val author: String = "",
    val intro: String = "",
    val kind: String = "",
    val lastChapter: String = "",
    val bookUrl: String = "",
    val coverUrl: String = ""
)
