package ireader.domain.js.loader

import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.domain.js.models.PluginMetadata
import ireader.domain.models.entities.JSPluginCatalog

/**
 * Pending source for JS plugins when JS engine is not available.
 * Shows the source in the list but indicates that JS engine is required.
 * When user clicks on this source, the app should show RequiredPluginHandler.
 *
 * If [retryBlock] is provided it will be called on first real-use;
 * on success the replacement catalog is sent through [onReloaded].
 */
class JSPluginPendingSource(
    private val metadata: PluginMetadata,
    dependencies: Dependencies,
    private val retryBlock: (suspend () -> JSPluginCatalog?)? = null,
    private val onReloaded: (suspend (JSPluginCatalog) -> Unit)? = null
) : HttpSource(dependencies) {

    override val name: String = metadata.name
    override val lang: String = metadata.lang
    override val baseUrl: String = metadata.site

    @kotlin.concurrent.Volatile
    private var attemptedSelfHeal = false

    /**
     * Indicates this is a pending source that requires JS engine.
     */
    val isPending: Boolean = true

    private suspend fun trySelfHeal() {
        if (attemptedSelfHeal) return
        attemptedSelfHeal = true
        val block = retryBlock ?: return
        try {
            val catalog = block()
            if (catalog != null) {
                onReloaded?.invoke(catalog)
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        trySelfHeal()
        return MangasPageInfo(
            mangas = emptyList(),
            hasNextPage = false
        )
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        trySelfHeal()
        return MangasPageInfo(
            mangas = emptyList(),
            hasNextPage = false
        )
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        trySelfHeal()
        return manga
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        trySelfHeal()
        return emptyList()
    }

    override suspend fun getPageList(
        chapter: ChapterInfo,
        commands: List<Command<*>>
    ): List<Page> {
        trySelfHeal()
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
