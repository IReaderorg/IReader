package ireader.domain.usecases.local

import ireader.core.source.LocalCatalogSource
import ireader.core.source.LocalSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

/**
 * iOS local library folder. Scanning files comes later; this exists so
 * CatalogStore can start and the simulator can open the app.
 */
class LocalSourceImpl : LocalCatalogSource {
    override val id: Long = LocalSource.SOURCE_ID
    override val name: String = "Local Source"
    override val lang: String = "en"

    override fun getLocalFolderPath(): String {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask,
        )
        val documents = (paths.firstOrNull() as? NSURL)?.path.orEmpty()
        return "$documents/${LocalSource.LOCAL_FOLDER_NAME}"
    }

    override suspend fun scanLocalNovels(): List<MangaInfo> = emptyList()

    override suspend fun scanNovelChapters(novelKey: String): List<ChapterInfo> = emptyList()

    override suspend fun readChapterFile(chapterKey: String): List<Page> = emptyList()

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo = manga

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> =
        emptyList()

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> = emptyList()

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo = MangasPageInfo.empty()

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo = MangasPageInfo.empty()

    override fun getListings(): List<Listing> = emptyList()

    override fun getFilters(): FilterList = emptyList()

    override fun getCommands(): CommandList = emptyList()
}
