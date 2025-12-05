package ireader.domain.community

import ireader.core.source.CatalogSource
import ireader.core.source.HttpSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.Text

/**
 * Community Source - A source for user-contributed translated content.
 * 
 * This source connects to a Supabase backend where users can share their
 * translated books and chapters with the community.
 * 
 * Features:
 * - Browse community-translated books
 * - Search by title, author, language
 * - Filter by language, genre, status
 * - Read chapters translated by community members
 * - Support for multiple languages
 */
class CommunitySource(
    private val repository: CommunityRepository
) : CatalogSource {
    
    companion object {
        const val SOURCE_ID = -300L
        const val SOURCE_NAME = "Community Source"
        const val SOURCE_LANG = "multi"
    }
    
    override val id: Long = SOURCE_ID
    override val name: String = SOURCE_NAME
    override val lang: String = SOURCE_LANG
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return repository.getBookDetails(manga.key)
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        val language = commands.filterIsInstance<Command.Chapter.Select>()
            .firstOrNull()?.value?.toString()
        return repository.getChapters(manga.key, language)
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        val content = repository.getChapterContent(chapter.key)
        return listOf(Text(content))
    }
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return when (sort) {
            is LatestListing -> repository.getLatestBooks(page)
            is PopularListing -> repository.getPopularBooks(page)
            is RecentlyTranslatedListing -> repository.getRecentlyTranslatedBooks(page)
            else -> repository.getLatestBooks(page)
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
        val language = filters.filterIsInstance<LanguageFilter>().firstOrNull()?.selected
        val genre = filters.filterIsInstance<GenreFilter>().firstOrNull()?.selected
        val status = filters.filterIsInstance<StatusFilter>().firstOrNull()?.selected
        
        return repository.searchBooks(
            query = query,
            language = language,
            genre = genre,
            status = status,
            page = page
        )
    }
    
    override fun getListings(): List<Listing> {
        return listOf(
            LatestListing(),
            PopularListing(),
            RecentlyTranslatedListing()
        )
    }
    
    override fun getFilters(): FilterList {
        return listOf(
            Filter.Title(),
            LanguageFilter(SUPPORTED_LANGUAGES),
            GenreFilter(GENRES),
            StatusFilter(STATUSES)
        )
    }
    
    override fun getCommands(): CommandList {
        return listOf(
            Command.Chapter.Select(
                name = "Language",
                options = SUPPORTED_LANGUAGES.map { it.second }.toTypedArray()
            )
        )
    }
    
    // Custom Listings
    class LatestListing : Listing("Latest")
    class PopularListing : Listing("Popular")
    class RecentlyTranslatedListing : Listing("Recently Translated")
    
    // Custom Filters
    class LanguageFilter(languages: List<Pair<String, String>>) : Filter.Select(
        name = "Language",
        options = languages.map { it.second }.toTypedArray(),
        value = 0
    ) {
        val selected: String?
            get() = if (value > 0) SUPPORTED_LANGUAGES[value].first else null
    }
    
    class GenreFilter(genres: List<String>) : Filter.Select(
        name = "Genre",
        options = (listOf("All") + genres).toTypedArray(),
        value = 0
    ) {
        val selected: String?
            get() = if (value > 0) GENRES[value - 1] else null
    }
    
    class StatusFilter(statuses: List<String>) : Filter.Select(
        name = "Status",
        options = (listOf("All") + statuses).toTypedArray(),
        value = 0
    ) {
        val selected: String?
            get() = if (value > 0) STATUSES[value - 1] else null
    }
}

// Supported languages for community translations
val SUPPORTED_LANGUAGES = listOf(
    "all" to "All Languages",
    "en" to "English",
    "es" to "Spanish",
    "pt" to "Portuguese",
    "fr" to "French",
    "de" to "German",
    "it" to "Italian",
    "ru" to "Russian",
    "ja" to "Japanese",
    "ko" to "Korean",
    "zh" to "Chinese",
    "ar" to "Arabic",
    "hi" to "Hindi",
    "id" to "Indonesian",
    "th" to "Thai",
    "vi" to "Vietnamese",
    "tr" to "Turkish",
    "pl" to "Polish",
    "nl" to "Dutch",
    "fil" to "Filipino",
    "fa" to "Persian",
)

val GENRES = listOf(
    "Action",
    "Adventure",
    "Comedy",
    "Drama",
    "Fantasy",
    "Horror",
    "Mystery",
    "Romance",
    "Sci-Fi",
    "Slice of Life",
    "Supernatural",
    "Thriller",
    "Historical",
    "Martial Arts",
    "Mecha",
    "Psychological",
    "Sports",
    "Tragedy",
    "Wuxia",
    "Xianxia"
)

val STATUSES = listOf(
    "Ongoing",
    "Completed",
    "Hiatus",
    "Dropped"
)
