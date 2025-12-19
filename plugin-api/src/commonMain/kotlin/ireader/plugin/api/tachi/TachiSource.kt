package ireader.plugin.api.tachi

import ireader.plugin.api.source.*

/**
 * Tachiyomi-compatible source interface.
 * Mirrors eu.kanade.tachiyomi.source.Source for compatibility.
 * 
 * This is the base interface for all Tachi sources. Most sources
 * will implement [TachiCatalogueSource] which extends this.
 */
interface TachiSource {
    /**
     * Unique source ID. Generated from name, lang, and versionId.
     */
    val id: Long
    
    /**
     * Source display name.
     */
    val name: String
    
    /**
     * ISO 639-1 language code (e.g., "en", "ja").
     */
    val lang: String
        get() = ""
    
    /**
     * Get updated manga details.
     */
    suspend fun getMangaDetails(manga: TachiManga): TachiManga
    
    /**
     * Get all chapters for a manga.
     */
    suspend fun getChapterList(manga: TachiManga): List<TachiChapter>
    
    /**
     * Get pages for a chapter.
     */
    suspend fun getPageList(chapter: TachiChapter): List<TachiPage>
}

/**
 * Tachiyomi-compatible catalogue source interface.
 * Mirrors eu.kanade.tachiyomi.source.CatalogueSource.
 */
interface TachiCatalogueSource : TachiSource {
    /**
     * Whether this source supports latest updates.
     */
    val supportsLatest: Boolean
    
    /**
     * Get popular manga.
     */
    suspend fun getPopularManga(page: Int): TachiMangasPage
    
    /**
     * Search for manga.
     */
    suspend fun getSearchManga(page: Int, query: String, filters: TachiFilterList): TachiMangasPage
    
    /**
     * Get latest manga updates.
     */
    suspend fun getLatestUpdates(page: Int): TachiMangasPage
    
    /**
     * Get available filters for search.
     */
    fun getFilterList(): TachiFilterList
}

/**
 * HTTP-based source with base URL.
 */
interface TachiHttpSource : TachiCatalogueSource {
    val baseUrl: String
    val versionId: Int get() = 1
    
    fun getMangaUrl(manga: TachiManga): String
    fun getChapterUrl(chapter: TachiChapter): String
    suspend fun getImageUrl(page: TachiPage): String
}

/**
 * Configurable source with preferences.
 */
interface TachiConfigurableSource : TachiSource {
    fun getPreferences(): Map<String, Any>
    fun setPreference(key: String, value: Any)
    fun getPreferenceDefinitions(): List<TachiPreference>
}

/**
 * Preference definition for configurable sources.
 */
@kotlinx.serialization.Serializable
sealed class TachiPreference {
    abstract val key: String
    abstract val title: String
    abstract val summary: String?
    
    @kotlinx.serialization.Serializable
    data class EditText(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val defaultValue: String = ""
    ) : TachiPreference()
    
    @kotlinx.serialization.Serializable
    data class Switch(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val defaultValue: Boolean = false
    ) : TachiPreference()
    
    @kotlinx.serialization.Serializable
    data class ListPreference(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val entries: List<String>,
        val entryValues: List<String>,
        val defaultValue: String = ""
    ) : TachiPreference()
    
    @kotlinx.serialization.Serializable
    data class MultiSelectList(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val entries: List<String>,
        val entryValues: List<String>,
        val defaultValues: Set<String> = emptySet()
    ) : TachiPreference()
}

/**
 * Factory for creating multiple sources from a single extension.
 */
interface TachiSourceFactory {
    fun createSources(): List<TachiSource>
}

// ==================== Adapter to UnifiedSource ====================

/**
 * Adapter that wraps a TachiCatalogueSource as a UnifiedSource.
 * Enables seamless integration with IReader's source UI.
 */
class TachiUnifiedSourceAdapter(
    private val tachiSource: TachiCatalogueSource,
    private val extensionIconUrl: String? = null,
    private val extensionIsNsfw: Boolean = false
) : UnifiedSource {
    
    override val id: Long = tachiSource.id
    override val name: String = tachiSource.name
    override val lang: String = tachiSource.lang
    override val isNsfw: Boolean = extensionIsNsfw
    override val iconUrl: String? = extensionIconUrl
    override val baseUrl: String? = (tachiSource as? TachiHttpSource)?.baseUrl
    override val loaderType: SourceLoaderType = SourceLoaderType.TACHIYOMI
    override val contentType: SourceContentType = SourceContentType.MANGA
    override val supportsLatest: Boolean = tachiSource.supportsLatest
    
    override val capabilities: Set<SourceCapability> = buildSet {
        add(SourceCapability.BROWSE)
        add(SourceCapability.SEARCH)
        if (tachiSource.supportsLatest) add(SourceCapability.LATEST)
        if (tachiSource.getFilterList().filters.isNotEmpty()) add(SourceCapability.FILTERS)
        if (tachiSource is TachiConfigurableSource) add(SourceCapability.PREFERENCES)
    }
    
    override suspend fun getPopular(page: Int): SourceItemsPage {
        val result = tachiSource.getPopularManga(page)
        return SourceItemsPage(
            items = result.mangas.map { it.toSourceItem(id) },
            hasNextPage = result.hasNextPage
        )
    }
    
    override suspend fun getLatest(page: Int): SourceItemsPage {
        val result = tachiSource.getLatestUpdates(page)
        return SourceItemsPage(
            items = result.mangas.map { it.toSourceItem(id) },
            hasNextPage = result.hasNextPage
        )
    }
    
    override suspend fun search(query: String, page: Int, filters: SourceFilterList): SourceItemsPage {
        val tachiFilters = filters.toTachiFilterList()
        val result = tachiSource.getSearchManga(page, query, tachiFilters)
        return SourceItemsPage(
            items = result.mangas.map { it.toSourceItem(id) },
            hasNextPage = result.hasNextPage
        )
    }
    
    override fun getFilterList(): SourceFilterList {
        return tachiSource.getFilterList().toSourceFilterList()
    }
    
    override suspend fun getDetails(item: SourceItem): SourceItemDetails {
        val manga = item.toTachiManga()
        val details = tachiSource.getMangaDetails(manga)
        return details.toSourceItemDetails(id)
    }
    
    override suspend fun getChapters(item: SourceItem): List<SourceChapter> {
        val manga = item.toTachiManga()
        val chapters = tachiSource.getChapterList(manga)
        return chapters.map { it.toSourceChapter(id, item.url) }
    }
    
    override suspend fun getChapterContent(chapter: SourceChapter): SourceChapterContent {
        val tachiChapter = chapter.toTachiChapter()
        val pages = tachiSource.getPageList(tachiChapter)
        return SourceChapterContent(
            chapterUrl = chapter.url,
            type = ContentDeliveryType.IMAGE,
            pages = pages.map { it.toSourcePage() }
        )
    }
    
    override suspend fun getImageUrl(page: SourcePage): String {
        if (page.imageUrl != null) return page.imageUrl
        
        val httpSource = tachiSource as? TachiHttpSource
            ?: throw IllegalStateException("Source doesn't support image URL resolution")
        
        val tachiPage = TachiPage(index = page.index, url = page.url)
        return httpSource.getImageUrl(tachiPage)
    }
    
    override fun getPreferences(): List<SourcePreference> {
        val configurable = tachiSource as? TachiConfigurableSource ?: return emptyList()
        return configurable.getPreferenceDefinitions().map { it.toSourcePreference() }
    }
    
    override fun setPreference(key: String, value: Any) {
        val configurable = tachiSource as? TachiConfigurableSource ?: return
        configurable.setPreference(key, value)
    }
}

// ==================== Conversion Extensions ====================

private fun TachiManga.toSourceItem(sourceId: Long): SourceItem = SourceItem(
    url = url,
    title = title,
    coverUrl = thumbnailUrl,
    description = description,
    author = author,
    artist = artist,
    genres = genre,
    status = when (status) {
        TachiManga.STATUS_ONGOING -> SourceItemStatus.ONGOING
        TachiManga.STATUS_COMPLETED -> SourceItemStatus.COMPLETED
        TachiManga.STATUS_LICENSED -> SourceItemStatus.LICENSED
        TachiManga.STATUS_PUBLISHING_FINISHED -> SourceItemStatus.PUBLISHING_FINISHED
        TachiManga.STATUS_CANCELLED -> SourceItemStatus.CANCELLED
        TachiManga.STATUS_ON_HIATUS -> SourceItemStatus.ON_HIATUS
        else -> SourceItemStatus.UNKNOWN
    },
    initialized = initialized,
    sourceId = sourceId
)

private fun TachiManga.toSourceItemDetails(sourceId: Long): SourceItemDetails = SourceItemDetails(
    item = toSourceItem(sourceId).copy(initialized = true),
    fullDescription = description,
    alternativeTitles = emptyList()
)

private fun TachiChapter.toSourceChapter(sourceId: Long, itemUrl: String): SourceChapter = SourceChapter(
    url = url,
    name = name,
    number = chapterNumber,
    dateUpload = dateUpload,
    scanlator = scanlator,
    sourceId = sourceId,
    itemUrl = itemUrl
)

private fun TachiPage.toSourcePage(): SourcePage = SourcePage(
    index = index,
    url = url,
    imageUrl = imageUrl
)

private fun SourceItem.toTachiManga(): TachiManga = TachiManga(
    url = url,
    title = title,
    thumbnailUrl = coverUrl,
    description = description,
    author = author,
    artist = artist,
    genre = genres,
    status = when (status) {
        SourceItemStatus.ONGOING -> TachiManga.STATUS_ONGOING
        SourceItemStatus.COMPLETED -> TachiManga.STATUS_COMPLETED
        SourceItemStatus.LICENSED -> TachiManga.STATUS_LICENSED
        SourceItemStatus.PUBLISHING_FINISHED -> TachiManga.STATUS_PUBLISHING_FINISHED
        SourceItemStatus.CANCELLED -> TachiManga.STATUS_CANCELLED
        SourceItemStatus.ON_HIATUS -> TachiManga.STATUS_ON_HIATUS
        else -> TachiManga.STATUS_UNKNOWN
    },
    initialized = initialized
)

private fun SourceChapter.toTachiChapter(): TachiChapter = TachiChapter(
    url = url,
    name = name,
    chapterNumber = number,
    dateUpload = dateUpload,
    scanlator = scanlator
)

private fun TachiFilterList.toSourceFilterList(): SourceFilterList = SourceFilterList(
    filters = filters.map { it.toSourceFilter() }
)

private fun TachiFilter.toSourceFilter(): SourceFilter = when (this) {
    is TachiFilter.Header -> SourceFilter.Header(name)
    is TachiFilter.Separator -> SourceFilter.Separator(name)
    is TachiFilter.Text -> SourceFilter.Text(name, state)
    is TachiFilter.CheckBox -> SourceFilter.CheckBox(name, state)
    is TachiFilter.TriState -> SourceFilter.TriState(name, state)
    is TachiFilter.Select -> SourceFilter.Select(name, values, state)
    is TachiFilter.Sort -> SourceFilter.Sort(name, values, selection?.let { SourceFilter.Sort.SortState(it.index, it.ascending) })
    is TachiFilter.Group -> SourceFilter.Group(name, filters.map { it.toSourceFilter() })
}

private fun SourceFilterList.toTachiFilterList(): TachiFilterList = TachiFilterList(
    filters = filters.map { it.toTachiFilter() }
)

private fun SourceFilter.toTachiFilter(): TachiFilter = when (this) {
    is SourceFilter.Header -> TachiFilter.Header(name)
    is SourceFilter.Separator -> TachiFilter.Separator(name)
    is SourceFilter.Text -> TachiFilter.Text(name, state)
    is SourceFilter.CheckBox -> TachiFilter.CheckBox(name, state)
    is SourceFilter.TriState -> TachiFilter.TriState(name, state)
    is SourceFilter.Select -> TachiFilter.Select(name, values, state)
    is SourceFilter.Sort -> TachiFilter.Sort(name, values, state?.let { TachiFilter.Sort.SortSelection(it.index, it.ascending) })
    is SourceFilter.Group -> TachiFilter.Group(name, filters.map { it.toTachiFilter() })
}

private fun TachiPreference.toSourcePreference(): SourcePreference = when (this) {
    is TachiPreference.EditText -> SourcePreference.EditText(key, title, summary, defaultValue)
    is TachiPreference.Switch -> SourcePreference.Switch(key, title, summary, defaultValue)
    is TachiPreference.ListPreference -> SourcePreference.ListSelection(key, title, summary, entries, entryValues, defaultValue)
    is TachiPreference.MultiSelectList -> SourcePreference.MultiSelect(key, title, summary, entries, entryValues, defaultValues)
}
