package ireader.data.catalog.impl

import ireader.core.source.CatalogSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wrapper that bridges a JavaScript source to the Kotlin CatalogSource interface.
 * 
 * This class delegates all source operations to the JavaScript runtime via IosCatalogLoader.
 */
class JsSourceWrapper(
    private val sourceId: String,
    private val info: JsSourceInfo,
    private val loader: IosCatalogLoader
) : CatalogSource {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override val id: Long = info.id
    override val name: String = info.name
    override val lang: String = info.lang
    
    val baseUrl: String = info.baseUrl
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return withContext(Dispatchers.Default) {
            try {
                val resultJson = loader.jsSearch(sourceId, "", page)
                json.decodeFromString<MangasPageInfo>(resultJson)
            } catch (e: Exception) {
                println("[JsSourceWrapper] getMangaList error: ${e.message}")
                MangasPageInfo.empty()
            }
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return withContext(Dispatchers.Default) {
            try {
                // Extract query from filters
                val query = filters.filterIsInstance<Filter.Title>()
                    .firstOrNull()?.value ?: ""
                
                val resultJson = loader.jsSearch(sourceId, query, page)
                json.decodeFromString<MangasPageInfo>(resultJson)
            } catch (e: Exception) {
                println("[JsSourceWrapper] getMangaList(filters) error: ${e.message}")
                MangasPageInfo.empty()
            }
        }
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return withContext(Dispatchers.Default) {
            try {
                val bookJson = json.encodeToString(MangaInfo.serializer(), manga)
                val resultJson = loader.jsGetDetails(sourceId, bookJson)
                json.decodeFromString<MangaInfo>(resultJson)
            } catch (e: Exception) {
                println("[JsSourceWrapper] getMangaDetails error: ${e.message}")
                manga
            }
        }
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return withContext(Dispatchers.Default) {
            try {
                val bookJson = json.encodeToString(MangaInfo.serializer(), manga)
                val resultJson = loader.jsGetChapters(sourceId, bookJson)
                json.decodeFromString<List<ChapterInfo>>(resultJson)
            } catch (e: Exception) {
                println("[JsSourceWrapper] getChapterList error: ${e.message}")
                emptyList()
            }
        }
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return withContext(Dispatchers.Default) {
            try {
                val chapterJson = json.encodeToString(ChapterInfo.serializer(), chapter)
                val resultJson = loader.jsGetContent(sourceId, chapterJson)
                json.decodeFromString<List<Page>>(resultJson)
            } catch (e: Exception) {
                println("[JsSourceWrapper] getPageList error: ${e.message}")
                emptyList()
            }
        }
    }
    
    override fun getListings(): List<Listing> {
        // JS sources typically support Popular and Latest
        return listOf(
            object : Listing("Popular") {},
            object : Listing("Latest") {}
        )
    }
    
    override fun getFilters(): FilterList {
        // Basic filter support - title search
        return listOf(Filter.Title())
    }
    
    override fun getCommands(): CommandList = emptyList()
}

/**
 * Source info parsed from JavaScript runtime.
 */
@Serializable
data class JsSourceInfo(
    val id: Long,
    val name: String,
    val lang: String,
    val baseUrl: String = ""
)
