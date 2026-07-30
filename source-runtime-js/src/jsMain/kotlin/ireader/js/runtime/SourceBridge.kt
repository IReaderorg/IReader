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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.Promise

/**
 * Bridge object that exposes source functionality to iOS JavaScriptCore.
 *
 * This is the main entry point for iOS to interact with Kotlin sources.
 * All methods return JSON strings for easy interop with Swift/Objective-C.
 *
 * Each source request cancels any previous in-flight request for the same source,
 * ensuring that stale requests do not consume resources or return stale data.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
object SourceBridge {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val loadedSources = mutableMapOf<String, Source>()
    private val sourceScopes = mutableMapOf<String, CoroutineScope>()

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
     * Unregister a source and cancel any in-flight requests for it.
     */
    fun unregisterSource(id: String) {
        loadedSources.remove(id)
        sourceScopes.remove(id)?.cancel()
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

    private fun sourceScope(sourceId: String): CoroutineScope {
        sourceScopes.remove(sourceId)?.cancel()
        val newScope = CoroutineScope(SupervisorJob() + scope.coroutineContext)
        sourceScopes[sourceId] = newScope
        return newScope
    }

    private fun <T> runSourceRequest(
        sourceId: String,
        block: suspend CoroutineScope.() -> T
    ): Promise<String> {
        val sc = sourceScope(sourceId)
        return Promise { resolve, reject ->
            sc.launch {
                try {
                    val result = block()
                    resolve(json.encodeToString(result))
                } catch (e: Exception) {
                    console.error("SourceBridge: Request error - ${e.message}")
                    reject(e)
                }
            }
        }
    }

    /**
     * Search for manga/novels.
     * Returns a Promise that resolves to JSON string of MangasPageInfo.
     */
    fun search(sourceId: String, query: String, page: Int): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest MangasPageInfo.empty()

            try {
                val filters = source.getFilters().toMutableList()
                filters.filterIsInstance<Filter.Title>().firstOrNull()?.let {
                    it.value = query
                }
                source.getMangaList(filters = filters, page = page)
            } catch (e: Exception) {
                console.error("SourceBridge: Search error - ${e.message}")
                MangasPageInfo.empty()
            }
        }
    }

    /**
     * Get popular manga/novels listing.
     * Returns a Promise that resolves to JSON string of MangasPageInfo.
     */
    fun getPopular(sourceId: String, page: Int): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest MangasPageInfo.empty()

            try {
                val listings = source.getListings()
                source.getMangaList(listings.firstOrNull(), page)
            } catch (e: Exception) {
                console.error("SourceBridge: GetPopular error - ${e.message}")
                MangasPageInfo.empty()
            }
        }
    }

    /**
     * Get manga/novel details.
     * Returns a Promise that resolves to JSON string of MangaInfo.
     */
    fun getBookDetails(sourceId: String, bookJson: String): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest "{}"

            try {
                val book = json.decodeFromString<MangaInfo>(bookJson)
                source.getMangaDetails(book, emptyList())
            } catch (e: Exception) {
                console.error("SourceBridge: GetBookDetails error - ${e.message}")
                "{}"
            }
        }
    }

    /**
     * Get chapter list for a manga/novel.
     * Returns a Promise that resolves to JSON string of List<ChapterInfo>.
     */
    fun getChapters(sourceId: String, bookJson: String): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest "[]"

            try {
                val book = json.decodeFromString<MangaInfo>(bookJson)
                source.getChapterList(book, emptyList())
            } catch (e: Exception) {
                console.error("SourceBridge: GetChapters error - ${e.message}")
                "[]"
            }
        }
    }

    /**
     * Get chapter content.
     * Returns a Promise that resolves to JSON string of List<Page>.
     */
    fun getContent(sourceId: String, chapterJson: String): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest "[]"

            try {
                val chapter = json.decodeFromString<ChapterInfo>(chapterJson)
                source.getPageList(chapter, emptyList())
            } catch (e: Exception) {
                console.error("SourceBridge: GetContent error - ${e.message}")
                "[]"
            }
        }
    }

    /**
     * Get chapter content as plain text (for novels).
     * Returns a Promise that resolves to JSON array of strings.
     */
    fun getContentText(sourceId: String, chapterJson: String): Promise<String> {
        return runSourceRequest(sourceId) {
            val source = loadedSources[sourceId] as? CatalogSource
                ?: return@runSourceRequest "[]"

            try {
                val chapter = json.decodeFromString<ChapterInfo>(chapterJson)
                source.getPageList(chapter, emptyList())
                    .mapNotNull { page ->
                        when (page) {
                            is ireader.core.source.model.Text -> page.text
                            else -> null
                        }
                    }
            } catch (e: Exception) {
                console.error("SourceBridge: GetContentText error - ${e.message}")
                emptyList<String>()
            }
        }
    }
}