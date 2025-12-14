package ireader.domain.usersource.catalog

import io.ktor.client.HttpClient
import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import ireader.domain.usersource.engine.UserSourceEngine
import ireader.domain.usersource.model.UserSource

/**
 * CatalogSource implementation that wraps a UserSource.
 * This allows user-defined sources to be used alongside regular catalog sources.
 */
class UserSourceCatalogSource(
    private val userSource: UserSource,
    httpClient: HttpClient
) : CatalogSource {
    
    private val engine = UserSourceEngine(userSource, httpClient)
    
    override val id: Long = userSource.generateId()
    override val name: String = userSource.sourceName
    override val lang: String = userSource.lang
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return engine.getMangaList(sort, page)
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return engine.getMangaList(filters, page)
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return engine.getMangaDetails(manga, commands)
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return engine.getChapterList(manga, commands)
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return engine.getPageList(chapter, commands)
    }
    
    override fun getListings(): List<Listing> {
        return engine.getListings()
    }
    
    override fun getFilters(): FilterList {
        return engine.getFilters()
    }
    
    override fun getCommands(): CommandList {
        return engine.getCommands()
    }
    
    override fun toString(): String {
        return "UserSourceCatalogSource(name=$name, id=$id)"
    }
}
