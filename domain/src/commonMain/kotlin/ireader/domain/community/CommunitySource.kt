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
        const val ICON_URL = "https://raw.githubusercontent.com/IReaderorg/badge-repo/main/app-icon.png"
        
        /** Message shown when source is not configured */
        private const val NOT_CONFIGURED_MESSAGE = """
Welcome to Community Source!

This source allows you to browse and read novels translated by the community, 
and share your own AI translations with others.

To get started:
1. Go to Settings → Community Source
2. Configure your Supabase or Cloudflare backend
3. Set your contributor name to share translations

Once configured, you'll see community-translated novels here.
        """
    }
    
    override val id: Long = SOURCE_ID
    override val name: String = SOURCE_NAME
    override val lang: String = SOURCE_LANG
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            repository.getBookDetails(manga.key)
        } catch (e: Exception) {
            manga // Return original manga info if fetch fails
        }
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            val language = commands.filterIsInstance<Command.Chapter.Select>()
                .firstOrNull()?.value?.toString()
            repository.getChapters(manga.key, language)
        } catch (e: Exception) {
            emptyList() // Return empty list if not configured
        }
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            val content = repository.getChapterContent(chapter.key)
            listOf(Text(content))
        } catch (e: Exception) {
            listOf(Text("Unable to load chapter content. Please check your Community Source configuration in Settings."))
        }
    }
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            when (sort) {
                is LatestListing -> repository.getLatestBooks(page)
                is PopularListing -> repository.getPopularBooks(page)
                is RecentlyTranslatedListing -> repository.getRecentlyTranslatedBooks(page)
                else -> repository.getLatestBooks(page)
            }
        } catch (e: Exception) {
            // Return a helpful placeholder when not configured
            if (page == 1) {
                MangasPageInfo(
                    mangas = listOf(createWelcomePlaceholder()),
                    hasNextPage = false
                )
            } else {
                MangasPageInfo.empty()
            }
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
            val language = filters.filterIsInstance<LanguageFilter>().firstOrNull()?.selected
            val genre = filters.filterIsInstance<GenreFilter>().firstOrNull()?.selected
            val status = filters.filterIsInstance<StatusFilter>().firstOrNull()?.selected
            
            repository.searchBooks(
                query = query,
                language = language,
                genre = genre,
                status = status,
                page = page
            )
        } catch (e: Exception) {
            // Return a helpful placeholder when not configured
            if (page == 1) {
                MangasPageInfo(
                    mangas = listOf(createWelcomePlaceholder()),
                    hasNextPage = false
                )
            } else {
                MangasPageInfo.empty()
            }
        }
    }
    
    /**
     * Create a welcome placeholder manga that explains how to configure the source.
     */
    private fun createWelcomePlaceholder(): MangaInfo {
        return MangaInfo(
            key = "welcome",
            title = "Welcome to Community Source",
            author = "IReader Team",
            description = NOT_CONFIGURED_MESSAGE.trimIndent(),
            cover = ICON_URL,
            genres = listOf("Guide", "Setup"),
            status = MangaInfo.UNKNOWN
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
