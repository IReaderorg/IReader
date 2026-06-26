package ireader.data.catalog.impl.tsundoku

import ireader.core.source.CatalogSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.CommandList
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.data.catalog.impl.tsundoku.TsundokuFilterAdapter.applyIReaderValuesToTsundokuFilters
import ireader.data.catalog.impl.tsundoku.TsundokuFilterAdapter.convertTsundokuFiltersToIReader
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toChapterInfo
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toIReaderPages
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toMangaInfo
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toMangasPageInfo
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toTsundokuSChapter
import ireader.data.catalog.impl.tsundoku.TsundokuModelAdapter.toTsundokuSManga
import ireader.core.source.model.Command

/**
 * CatalogSource wrapper that delegates to a Tsundoku CatalogueSource.
 *
 * This adapter allows Tsundoku (Tachiyomi/Mihon) extensions to be used
 * natively within IReader's catalog system. It converts between the two
 * source APIs transparently.
 *
 * The wrapped tsundoku source is accessed via reflection since we can't
 * directly reference tsundoku types at compile time (different module).
 */
class TsundokuCatalogSource(
    private val tsundokuSource: Any
) : CatalogSource {

    // Tsundoku Source interface properties (accessed via reflection)
    override val id: Long = tsundokuSource.getField("id") as? Long ?: 0L
    override val name: String = tsundokuSource.getField("name") as? String ?: "Unknown"
    override val lang: String = tsundokuSource.getField("lang") as? String ?: ""

    /**
     * Whether this is a novel source (text-based content).
     */
    val isNovelSource: Boolean = try {
        tsundokuSource.getField("isNovelSource") as? Boolean ?: false
    } catch (e: Exception) {
        false
    }

    // ==================== CatalogSource Implementation ====================

    /**
     * Get manga list by listing (popular/latest).
     * Maps IReader Listing to Tsundoku getPopularManga/getLatestUpdates.
     */
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            val result = when {
                sort == null || sort is PopularListing -> {
                    invokeSuspend("getPopularManga", page)
                }
                sort is LatestListing -> {
                    invokeSuspend("getLatestUpdates", page)
                }
                else -> {
                    invokeSuspend("getPopularManga", page)
                }
            }
            result?.toMangasPageInfo() ?: MangasPageInfo.empty()
        } catch (e: Exception) {
            MangasPageInfo.empty()
        }
    }

    /**
     * Get manga list by filters.
     * Converts IReader filters to Tsundoku filters, then calls getSearchManga.
     */
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            // Get tsundoku filters and apply IReader filter values
            val tsundokuFilters = getTsundokuFilterList()
            applyIReaderValuesToTsundokuFilters(filters, tsundokuFilters)

            // Build search query from active filters
            val query = extractSearchQuery(filters)

            // Create a Tsundoku FilterList
            val filterListObj = createTsundokuFilterList(tsundokuFilters)

            // Call getSearchManga(page, query, filters)
            val result = invokeSuspendWithArgs(
                "getSearchManga",
                arrayOf(Int::class.java, String::class.java, filterListObj.javaClass),
                arrayOf(page, query, filterListObj)
            )
            result?.toMangasPageInfo() ?: MangasPageInfo.empty()
        } catch (e: Exception) {
            MangasPageInfo.empty()
        }
    }

    /**
     * Get manga details.
     * Converts MangaInfo → SManga, calls tsundoku, converts back.
     */
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            val smanga = manga.toTsundokuSManga()
            val result = invokeSuspend("getMangaDetails", smanga)
            result?.toMangaInfo() ?: manga
        } catch (e: Exception) {
            manga
        }
    }

    /**
     * Get chapter list.
     * Converts MangaInfo → SManga, calls tsundoku, converts back.
     */
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            val smanga = manga.toTsundokuSManga()
            @Suppress("UNCHECKED_CAST")
            val result = invokeSuspend("getChapterList", smanga) as? List<Any> ?: emptyList()
            result.map { it.toChapterInfo() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get page list.
     * Converts ChapterInfo → SChapter, calls tsundoku, converts back.
     */
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            val schapter = chapter.toTsundokuSChapter()
            @Suppress("UNCHECKED_CAST")
            val result = invokeSuspend("getPageList", schapter) as? List<Any> ?: emptyList()
            result.flatMap { it.toIReaderPages() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== Filter & Listing ====================

    /**
     * Get IReader-compatible filters by converting from tsundoku filters.
     */
    override fun getFilters(): FilterList {
        return try {
            val tsundokuFilters = getTsundokuFilterList()
            convertTsundokuFiltersToIReader(tsundokuFilters)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get available listings (popular + latest).
     */
    override fun getListings(): List<Listing> {
        val listings = mutableListOf<Listing>()

        // Tsundoku sources always support popular
        listings.add(PopularListing())

        // Check if source supports latest
        val supportsLatest = try {
            tsundokuSource.getField("supportsLatest") as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
        if (supportsLatest) {
            listings.add(LatestListing())
        }

        return listings
    }

    /**
     * Get commands (IReader-specific, not supported by tsundoku sources).
     */
    override fun getCommands(): CommandList = emptyList()

    override fun toString(): String = "TsundokuCatalogSource(name=$name, lang=$lang, id=$id)"

    // ==================== Internal Helpers ====================

    /**
     * Get the tsundoku filter list via getFilterList() method.
     */
    private fun getTsundokuFilterList(): List<Any> {
        return try {
            val method = tsundokuSource.javaClass.getMethod("getFilterList")
            val filterList = method.invoke(tsundokuSource)
            // FilterList implements List<Filter<*>>
            @Suppress("UNCHECKED_CAST")
            (filterList as? List<Any>) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Create a Tsundoku FilterList object from a list of filters.
     */
    private fun createTsundokuFilterList(filters: List<Any>): Any {
        val filterListClass = Class.forName("eu.kanade.tachiyomi.source.model.FilterList")
        return filterListClass.getDeclaredConstructor(List::class.java).newInstance(filters)
    }

    /**
     * Extract a search query from IReader filters (looks for Title filter).
     */
    private fun extractSearchQuery(filters: FilterList): String {
        val titleFilter = filters.filterIsInstance<ireader.core.source.model.Filter.Text>()
            .firstOrNull { it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true) }
        return titleFilter?.value ?: ""
    }

    /**
     * Invoke a suspend method on the tsundoku source by name.
     * Uses reflection to find and call the method.
     */
    private suspend fun invokeSuspend(methodName: String, vararg args: Any?): Any? {
        val methods = tsundokuSource.javaClass.methods
        val method = methods.firstOrNull {
            it.name == methodName && it.parameterCount == args.size
        } ?: throw NoSuchMethodException("$methodName not found on ${tsundokuSource.javaClass.name}")

        return method.invoke(tsundokuSource, *args)
    }

    /**
     * Invoke a suspend method with specific argument types.
     */
    private suspend fun invokeSuspendWithArgs(
        methodName: String,
        paramTypes: Array<Class<*>>,
        args: Array<Any?>
    ): Any? {
        val method = try {
            tsundokuSource.javaClass.getMethod(methodName, *paramTypes)
        } catch (e: NoSuchMethodException) {
            // Try superclass methods
            tsundokuSource.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterCount == args.size
            } ?: throw e
        }
        return method.invoke(tsundokuSource, *args)
    }

    // ==================== Reflection Helpers ====================

    private fun Any.getField(name: String): Any? {
        return try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    return field.get(this)
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Listing Types ====================

    class PopularListing : Listing("Popular")
    class LatestListing : Listing("Latest")
}
