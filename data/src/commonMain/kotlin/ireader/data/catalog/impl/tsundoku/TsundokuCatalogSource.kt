package ireader.data.catalog.impl.tsundoku

import ireader.core.source.CatalogSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page

/**
 * CatalogSource wrapper that delegates to a Tsundoku source via reflection.
 *
 * Tsundoku source-api types (SManga, SChapter, etc.) are accessed via reflection
 * since they live in the source-api module, not directly importable from data.
 */
class TsundokuCatalogSource(
    private val tsundokuSource: Any
) : CatalogSource {

    override val id: Long = invokeGetter("id") as? Long ?: 0L
    override val name: String = invokeGetter("name") as? String ?: "Unknown"
    override val lang: String = invokeGetter("lang") as? String ?: ""

    val isNovelSource: Boolean = try {
        invokeGetter("isNovelSource") as? Boolean ?: false
    } catch (e: Exception) { false }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            val result = when {
                sort == null || sort is PopularListing -> invokeSuspend("getPopularManga", page)
                sort is LatestListing -> invokeSuspend("getLatestUpdates", page)
                else -> invokeSuspend("getPopularManga", page)
            }
            result?.toMangasPageInfo() ?: MangasPageInfo.empty()
        } catch (e: Exception) { MangasPageInfo.empty() }
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            val query = filters.filterIsInstance<ireader.core.source.model.Filter.Text>()
                .firstOrNull { it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true) }
                ?.value ?: ""
            val result = invokeSuspend("getSearchManga", page, query, createEmptyTsundokuFilterList())
            result?.toMangasPageInfo() ?: MangasPageInfo.empty()
        } catch (e: Exception) { MangasPageInfo.empty() }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            val smanga = manga.toTsundokuSManga()
            val result = invokeSuspend("getMangaDetails", smanga)
            result?.toMangaInfo() ?: manga
        } catch (e: Exception) { manga }
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            val smanga = manga.toTsundokuSManga()
            @Suppress("UNCHECKED_CAST")
            val result = invokeSuspend("getChapterList", smanga) as? List<Any> ?: emptyList()
            result.map { it.toChapterInfo() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            val schapter = chapter.toTsundokuSChapter()
            @Suppress("UNCHECKED_CAST")
            val result = invokeSuspend("getPageList", schapter) as? List<Any> ?: emptyList()
            result.flatMap { it.toIReaderPages() }
        } catch (e: Exception) { emptyList() }
    }

    override fun getFilters(): FilterList = emptyList()
    override fun getListings(): List<Listing> {
        val listings = mutableListOf<Listing>(PopularListing())
        val supportsLatest = invokeGetter("supportsLatest") as? Boolean ?: false
        if (supportsLatest) listings.add(LatestListing())
        return listings
    }
    override fun getCommands(): CommandList = emptyList()

    override fun toString(): String = "TsundokuCatalogSource(name=$name, lang=$lang, id=$id)"

    // ==================== Model Conversions ====================

    private fun Any.toMangaInfo(): MangaInfo {
        return MangaInfo(
            key = getField("url") as? String ?: "",
            title = getField("title") as? String ?: "",
            artist = getField("artist") as? String ?: "",
            author = getField("author") as? String ?: "",
            description = getField("description") as? String ?: "",
            genres = (getField("genre") as? String)
                ?.split(Regex("[,;|]+"))?.map { it.trim() }?.filter { it.isNotBlank() }?.distinct()
                ?: emptyList(),
            status = (getField("status") as? Int ?: 0).toLong(),
            cover = getField("thumbnail_url") as? String ?: ""
        )
    }

    private fun MangaInfo.toTsundokuSManga(): Any {
        val smanga = Class.forName("eu.kanade.tachiyomi.source.model.SManga").getMethod("create").invoke(null)
        smanga.setField("url", this.key)
        smanga.setField("title", this.title)
        smanga.setField("thumbnail_url", this.cover.ifBlank { null })
        smanga.setField("artist", this.artist)
        smanga.setField("author", this.author)
        smanga.setField("description", this.description)
        smanga.setField("genre", this.genres.joinToString(", "))
        smanga.setField("status", this.status.toInt())
        smanga.setField("initialized", true)
        return smanga
    }

    private fun Any.toChapterInfo(): ChapterInfo {
        return ChapterInfo(
            key = getField("url") as? String ?: "",
            name = getField("name") as? String ?: "",
            dateUpload = getField("date_upload") as? Long ?: 0L,
            number = getField("chapter_number") as? Float ?: -1f,
            scanlator = getField("scanlator") as? String ?: ""
        )
    }

    private fun ChapterInfo.toTsundokuSChapter(): Any {
        val schapter = Class.forName("eu.kanade.tachiyomi.source.model.SChapter").getMethod("create").invoke(null)
        schapter.setField("url", this.key)
        schapter.setField("name", this.name)
        schapter.setField("chapter_number", this.number)
        schapter.setField("date_upload", this.dateUpload)
        schapter.setField("scanlator", this.scanlator.ifBlank { null })
        return schapter
    }

    private fun Any.toIReaderPages(): List<Page> {
        val imageUrl = getField("imageUrl") as? String
        val url = getField("url") as? String ?: ""
        val text = getField("text") as? String
        val pages = mutableListOf<Page>()
        if (!text.isNullOrBlank()) pages.add(ireader.core.source.model.Text(text))
        if (!imageUrl.isNullOrBlank()) pages.add(ireader.core.source.model.ImageUrl(imageUrl))
        else if (url.isNotBlank()) pages.add(ireader.core.source.model.PageUrl(url))
        return pages
    }

    private fun Any.toMangasPageInfo(): MangasPageInfo {
        @Suppress("UNCHECKED_CAST")
        val mangas = getField("mangas") as? List<Any> ?: emptyList()
        val hasNextPage = getField("hasNextPage") as? Boolean ?: false
        return MangasPageInfo(mangas = mangas.map { it.toMangaInfo() }, hasNextPage = hasNextPage)
    }

    private fun createEmptyTsundokuFilterList(): Any {
        val filterListClass = Class.forName("eu.kanade.tachiyomi.source.model.FilterList")
        return filterListClass.getDeclaredConstructor(List::class.java).newInstance(emptyList<Any>())
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
                } catch (e: NoSuchFieldException) { clazz = clazz.superclass }
            }
            null
        } catch (e: Exception) { null }
    }

    private fun Any.setField(name: String, value: Any?) {
        try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    field.set(this, value)
                    return
                } catch (e: NoSuchFieldException) { clazz = clazz.superclass }
            }
        } catch (e: Exception) { }
    }

    private fun invokeGetter(name: String): Any? {
        return try {
            val method = tsundokuSource.javaClass.methods.firstOrNull {
                it.name == "get${name.replaceFirstChar { it.uppercase() }}" && it.parameterCount == 0
            }
            method?.invoke(tsundokuSource)
        } catch (e: Exception) { null }
    }

    private suspend fun invokeSuspend(methodName: String, vararg args: Any?): Any? {
        val method = tsundokuSource.javaClass.methods.firstOrNull {
            it.name == methodName && it.parameterCount == args.size
        } ?: throw NoSuchMethodException("$methodName not found")
        return method.invoke(tsundokuSource, *args)
    }

    class PopularListing : Listing("Popular")
    class LatestListing : Listing("Latest")
}
