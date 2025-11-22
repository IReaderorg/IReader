package ireader.domain.js.loader

import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.model.*
import ireader.domain.js.models.PluginMetadata

/**
 * Stub/placeholder source for JS plugins that loads instantly.
 * Displays basic info while the actual plugin loads in the background.
 * All operations return loading states or empty results until replaced by real source.
 */
class JSPluginStubSource(
    private val metadata: PluginMetadata,
    dependencies: Dependencies
) : HttpSource(dependencies) {
    
    // Use the same name as the real source so IDs match
    override val name: String = metadata.name
    override val lang: String = metadata.lang
    override val baseUrl: String = metadata.site
    
    private val loadingMessage = "Plugin is loading in the background. Please wait..."
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
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
        return manga.copy(
            description = loadingMessage
        )
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
        return listOf(
             PopularListing(),
            LatestListing()
        )
    }
    
    /**
     * Check if this is a stub source.
     */
    fun isStub(): Boolean = true
}
