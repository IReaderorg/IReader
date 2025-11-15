package ireader.domain.js.bridge

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.PluginMetadata

/**
 * Implementation of IReader's CatalogSource interface using a JavaScript plugin.
 * Delegates all operations to the JSPluginBridge.
 */
class JSPluginSource(
    private val bridge: JSPluginBridge,
    private val metadata: PluginMetadata,
    override val id: Long,
    override val name: String,
    override val lang: String
) : CatalogSource {
    
    /**
     * Gets detailed information about a manga/novel.
     */
    override suspend fun getMangaDetails(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): MangaInfo {
        return try {
            Log.debug { "[JSPluginSource] Getting manga details for: ${manga.title}" }
            val startTime = System.currentTimeMillis()
            
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            val mangaInfo = sourceNovel.toMangaInfo()
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got manga details in ${duration}ms" }
            
            mangaInfo
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get manga details: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting manga details")
            throw JSPluginError.ExecutionError(metadata.id, "getMangaDetails", e)
        }
    }
    
    /**
     * Gets the list of chapters for a manga/novel.
     */
    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        return try {
            Log.debug { "[JSPluginSource] Getting chapter list for: ${manga.title}" }
            val startTime = System.currentTimeMillis()
            
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            val chapters = sourceNovel.chapters.map { it.toChapterInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got ${chapters.size} chapters in ${duration}ms" }
            
            chapters
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get chapter list: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting chapter list")
            throw JSPluginError.ExecutionError(metadata.id, "getChapterList", e)
        }
    }
    
    /**
     * Gets the content pages for a chapter.
     * For novel sources, returns a single Text page with HTML content.
     */
    override suspend fun getPageList(
        chapter: ChapterInfo,
        commands: List<Command<*>>
    ): List<Page> {
        return try {
            Log.debug { "[JSPluginSource] Getting page list for chapter: ${chapter.name}" }
            val startTime = System.currentTimeMillis()
            
            val chapterPath = chapter.key
            val htmlContent = bridge.parseChapter(chapterPath)
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got chapter content (${htmlContent.length} chars) in ${duration}ms" }
            
            // Wrap HTML content in a Text page
            listOf(Text(htmlContent))
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get page list: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting page list")
            throw JSPluginError.ExecutionError(metadata.id, "getPageList", e)
        }
    }
    
    /**
     * Gets popular novels from the source.
     * Custom method for browsing popular content.
     */
    suspend fun getPopularNovels(page: Int, filters: Map<String, Any> = emptyMap()): List<MangaInfo> {
        return try {
            Log.debug { "[JSPluginSource] Getting popular novels, page: $page" }
            val startTime = System.currentTimeMillis()
            
            val novels = bridge.popularNovels(page, filters)
            val mangaList = novels.map { it.toMangaInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got ${mangaList.size} popular novels in ${duration}ms" }
            
            mangaList
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get popular novels: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting popular novels")
            throw JSPluginError.ExecutionError(metadata.id, "getPopularNovels", e)
        }
    }
    
    /**
     * Searches for novels in the source.
     * Custom method for searching content.
     */
    suspend fun searchNovels(query: String, page: Int): List<MangaInfo> {
        return try {
            Log.debug { "[JSPluginSource] Searching novels, query: $query, page: $page" }
            val startTime = System.currentTimeMillis()
            
            val novels = bridge.searchNovels(query, page)
            val mangaList = novels.map { it.toMangaInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Found ${mangaList.size} novels in ${duration}ms" }
            
            mangaList
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to search novels: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error searching novels")
            throw JSPluginError.ExecutionError(metadata.id, "searchNovels", e)
        }
    }
    
    /**
     * Gets the filter definitions for this source (internal method).
     */
    suspend fun getPluginFilters(): Map<String, ireader.domain.js.models.FilterDefinition> {
        return try {
            bridge.getFilters()
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get filters")
            emptyMap()
        }
    }
    
    // CatalogSource interface implementation
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            Log.debug { "[JSPluginSource] Getting manga list with listing: ${sort?.name}, page: $page" }
            val novels = bridge.popularNovels(page, emptyMap())
            val mangaList = novels.map { it.toMangaInfo() }
            MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get manga list")
            MangasPageInfo(emptyList(), hasNextPage = false)
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            Log.debug { "[JSPluginSource] Getting manga list with filters, page: $page" }
            val novels = bridge.popularNovels(page, emptyMap())
            val mangaList = novels.map { it.toMangaInfo() }
            MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get manga list with filters")
            MangasPageInfo(emptyList(), hasNextPage = false)
        }
    }
    
    override fun getListings(): List<Listing> {
        // Return a default "Popular" listing
        return listOf(
            object : Listing("Popular") {},
            object : Listing("Latest") {}
        )
    }
    
    override fun getFilters(): FilterList {
        // Return empty filter list for now
        // TODO: Convert JS plugin filters to IReader FilterList
        return emptyList()
    }
    
    override fun getCommands(): CommandList {
        // Return empty command list
        return emptyList()
    }
}
