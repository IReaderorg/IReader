package ireader.domain.js.bridge

import kotlinx.serialization.Serializable

/**
 * Interface for LNReader JavaScript plugins.
 * This is the bridge between Kotlin and JavaScript code.
 */
interface LNReaderPlugin {
    /**
     * Get plugin metadata
     */
    suspend fun getId(): String
    suspend fun getName(): String
    suspend fun getSite(): String
    suspend fun getVersion(): String
    suspend fun getLang(): String
    suspend fun getIcon(): String
    
    /**
     * Search for novels
     */
    suspend fun searchNovels(query: String, page: Int): List<PluginNovel>
    
    /**
     * Get popular novels
     * @param page Page number
     * @param filters Optional map of filter values in LNReader format
     */
    suspend fun popularNovels(page: Int, filters: Map<String, Any> = emptyMap()): List<PluginNovel>
    
    /**
     * Get latest novels
     */
    suspend fun latestNovels(page: Int): List<PluginNovel>
    
    /**
     * Get novel details
     */
    suspend fun getNovelDetails(url: String): PluginNovelDetails
    
    /**
     * Get chapter list
     */
    suspend fun getChapters(url: String): List<PluginChapter>
    
    /**
     * Get chapter content
     */
    suspend fun getChapterContent(url: String): String
    
    /**
     * Get filter definitions from plugin (optional)
     * Returns a map of filter definitions in LNReader format
     * Note: This is not a suspend function as filter definitions are typically static
     */
    fun getFilters(): Map<String, Any> {
        return emptyMap()
    }
    

}

/**
 * Novel data from plugin
 */
@Serializable
data class PluginNovel(
    val name: String,
    val url: String,
    val cover: String = ""
)

/**
 * Detailed novel information from plugin
 */
@Serializable
data class PluginNovelDetails(
    val name: String,
    val url: String,
    val cover: String = "",
    val author: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val status: String? = null
)

/**
 * Chapter data from plugin
 */
@Serializable
data class PluginChapter(
    val name: String,
    val url: String,
    val releaseTime: String? = null
)
