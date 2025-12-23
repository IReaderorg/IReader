package ireader.core.source

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.ChaptersPageInfo
import ireader.core.source.model.Command
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Page

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc...
 */
interface Source {

    /**
     * Id for the source. Must be unique.
     */
    val id: Long

    /**
     * Name of the source.
     */
    val name: String

    val lang: String

    /**
     * Returns an observable with the updated details for a manga.
     */
    suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo

    /**
     * Returns an observable with all the available chapters for a manga.
     * For sources that support pagination, this returns the first page by default.
     */
    suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo>
    
    /**
     * Returns a paginated list of chapters for a manga.
     * Override this method to support paginated chapter loading.
     * 
     * @param manga The manga to get chapters for
     * @param page The page number (1-based)
     * @param commands Optional commands for the request
     * @return ChaptersPageInfo containing chapters and pagination info
     */
    suspend fun getChapterListPaged(
        manga: MangaInfo,
        page: Int,
        commands: List<Command<*>>
    ): ChaptersPageInfo {
        // Default implementation: return all chapters as single page
        val chapters = getChapterList(manga, commands)
        return ChaptersPageInfo.singlePage(chapters)
    }
    
    /**
     * Returns the total number of chapter pages for a manga.
     * Override this method if your source supports paginated chapters.
     * 
     * @param manga The manga to get page count for
     * @return Total number of pages, or 1 if not paginated
     */
    suspend fun getChapterPageCount(manga: MangaInfo): Int {
        return 1
    }
    
    /**
     * Returns whether this source supports paginated chapter loading.
     * Sources that override getChapterListPaged should return true.
     */
    fun supportsPaginatedChapters(): Boolean {
        return false
    }

    /**
     * Returns an observable with the list of pages a chapter has.
     */
    suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page>

    /**
     * Returns a regex used to determine chapter information.
     */
    fun getRegex(): Regex {
        return Regex("")
    }
    
    fun getSourceKey(): String {
        return "$name-$lang-$id"
    }
    
    fun matchesId(sourceId: Long): Boolean {
        return this.id == sourceId
    }
}
