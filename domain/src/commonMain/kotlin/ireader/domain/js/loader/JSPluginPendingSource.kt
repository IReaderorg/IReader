package ireader.domain.js.loader

import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.model.*
import ireader.domain.js.models.PluginMetadata

/**
 * Pending source for JS plugins when JS engine is not available.
 * Shows the source in the list but indicates that JS engine is required.
 * When user clicks on this source, the app should show RequiredPluginHandler.
 */
class JSPluginPendingSource(
    private val metadata: PluginMetadata,
    dependencies: Dependencies
) : HttpSource(dependencies) {
    
    override val name: String = metadata.name
    override val lang: String = metadata.lang
    override val baseUrl: String = metadata.site
    
    /**
     * Indicates this is a pending source that requires JS engine.
     */
    val isPending: Boolean = true
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        // Return empty - the UI should intercept clicks and show RequiredPluginHandler
        return MangasPageInfo(
            mangas = emptyList(),
            hasNextPage = false
        )
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return MangasPageInfo(
            mangas = emptyList(),
            hasNextPage = false
        )
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return manga
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return emptyList()
    }

    override suspend fun getPageList(
        chapter: ChapterInfo,
        commands: List<Command<*>>
    ): List<Page> {
        return emptyList()
    }

    override fun getFilters(): FilterList {
        return emptyList()
    }
    
    class LatestListing() : Listing(name = "Latest")
    class PopularListing() : Listing(name = "Popular")
    
    override fun getListings(): List<Listing> {
        return listOf(PopularListing(), LatestListing())
    }
}
