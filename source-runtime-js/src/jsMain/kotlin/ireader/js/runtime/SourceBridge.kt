package ireader.js.runtime

import ireader.core.source.CatalogSource
import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Filter
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.Promise

/**
 * Bridge object that exposes source functionality to iOS JavaScriptCore.
 * 
 * This is the main entry point for iOS to interact with Kotlin sources.
 * All methods return JSON strings for easy interop with Swift/Objective-C.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
object SourceBridge {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val loadedSources = mutableMapOf<String, Source>()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    /**
     * Register a source with the bridge.
     */
    fun registerSource(id: String, source: Source) {
        loadedSources[id] = source
        console.log("SourceBridge: Registered source '$id' (${source.name})")
    }
    
    /**
     * Unregister a source.
     */
    fun unregisterSource(id: String) {
        loadedSources.remove(id)
        console.log("SourceBridge: Unregistered source '$id'")
    }
    
    /**
     * Get list of all registered source IDs.
     */
    fun getRegisteredSourceIds(): Array<String> {
        return loadedSources.keys.toTypedArray()
    }
    
    /**
     * Get source info as JSON.
     */
    fun getSourceInfo(sourceId: String): String {
        val source = loadedSources[sourceId] ?: return "{}"
        
        val info = SourceInfo(
            id = source.id,
            name = source.name,
            lang = source.lang,
            baseUrl = (source as? HttpSource)?.baseUrl ?: ""
        )
        
        return json.encodeToString(info)
    }
    
    /**
     * Get all sources info as JSON array.
     */
    fun getAllSourcesInfo(): String {
        val infos = loadedSources.map { (id, source) ->
            SourceInfo(
                id = source.id,
                name = source.name,
                lang = source.lang,
                baseUrl = (source as? HttpSource)?.baseUrl ?: ""
            )
        }
        return json.encodeToString(infos)
    }

    
    /**
     * Search for manga/novels.
     * Returns a Promise that resolves to JSON string of MangasPageInfo.
     */
    fun search(sourceId: String, query: String, page: Int): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise json.encodeToString(MangasPageInfo.empty())
        
        try {
            // Create filters with the search query
            val filters = source.getFilters().toMutableList()
            filters.filterIsInstance<Filter.Title>().firstOrNull()?.let { 
                it.value = query 
            }
            
            val result = source.getMangaList(filters = filters, page = page)
            json.encodeToString(result)
        } catch (e: Exception) {
            console.error("SourceBridge: Search error - ${e.message}")
            json.encodeToString(MangasPageInfo.empty())
        }
    }
    
    /**
     * Get popular manga/novels listing.
     * Returns a Promise that resolves to JSON string of MangasPageInfo.
     */
    fun getPopular(sourceId: String, page: Int): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise json.encodeToString(MangasPageInfo.empty())
        
        try {
            val listings = source.getListings()
            val result = source.getMangaList(listings.firstOrNull(), page)
            json.encodeToString(result)
        } catch (e: Exception) {
            console.error("SourceBridge: GetPopular error - ${e.message}")
            json.encodeToString(MangasPageInfo.empty())
        }
    }
    
    /**
     * Get manga/novel details.
     * Returns a Promise that resolves to JSON string of MangaInfo.
     */
    fun getBookDetails(sourceId: String, bookJson: String): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise "{}"
        
        try {
            val book = json.decodeFromString<MangaInfo>(bookJson)
            val result = source.getMangaDetails(book, emptyList())
            json.encodeToString(result)
        } catch (e: Exception) {
            console.error("SourceBridge: GetBookDetails error - ${e.message}")
            "{}"
        }
    }
    
    /**
     * Get chapter list for a manga/novel.
     * Returns a Promise that resolves to JSON string of List<ChapterInfo>.
     */
    fun getChapters(sourceId: String, bookJson: String): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise "[]"
        
        try {
            val book = json.decodeFromString<MangaInfo>(bookJson)
            val result = source.getChapterList(book, emptyList())
            json.encodeToString(result)
        } catch (e: Exception) {
            console.error("SourceBridge: GetChapters error - ${e.message}")
            "[]"
        }
    }
    
    /**
     * Get chapter content.
     * Returns a Promise that resolves to JSON string of List<Page>.
     */
    fun getContent(sourceId: String, chapterJson: String): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise "[]"
        
        try {
            val chapter = json.decodeFromString<ChapterInfo>(chapterJson)
            val result = source.getPageList(chapter, emptyList())
            json.encodeToString(result)
        } catch (e: Exception) {
            console.error("SourceBridge: GetContent error - ${e.message}")
            "[]"
        }
    }
    
    /**
     * Get chapter content as plain text (for novels).
     * Returns a Promise that resolves to JSON array of strings.
     */
    fun getContentText(sourceId: String, chapterJson: String): Promise<String> = scope.promise {
        val source = loadedSources[sourceId] as? CatalogSource
            ?: return@promise "[]"
        
        try {
            val chapter = json.decodeFromString<ChapterInfo>(chapterJson)
            val pages = source.getPageList(chapter, emptyList())
            
            // Extract text from Text pages
            val textContent = pages.mapNotNull { page ->
                when (page) {
                    is ireader.core.source.model.Text -> page.text
                    else -> null
                }
            }
            
            json.encodeToString(textContent)
        } catch (e: Exception) {
            console.error("SourceBridge: GetContentText error - ${e.message}")
            "[]"
        }
    }
}

/**
 * Source metadata for registration and display.
 */
@Serializable
data class SourceInfo(
    val id: Long,
    val name: String,
    val lang: String,
    val baseUrl: String
)
