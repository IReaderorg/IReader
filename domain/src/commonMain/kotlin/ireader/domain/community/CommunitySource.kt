package ireader.domain.community

import ireader.core.log.Log
import ireader.core.source.CatalogSource
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
import ireader.domain.community.cloudflare.CommunityTranslationRepository
import ireader.domain.community.cloudflare.TranslationMetadata

/**
 * Community Source - A source for browsing community-shared AI translations.
 * 
 * This source connects to Cloudflare D1/R2 where users share their
 * AI translations with the community.
 * 
 * Features:
 * - Browse books with community translations
 * - Search by title
 * - Filter by target language
 * - Read chapters translated by community members
 * - View translation ratings and download counts
 */
class CommunitySource(
    private val translationRepository: CommunityTranslationRepository?,
    private val communityPreferences: CommunityPreferences?
) : CatalogSource {
    
    override val id: Long = SOURCE_ID
    override val name: String = SOURCE_NAME
    override val lang: String = SOURCE_LANG
    
    private fun isConfigured(): Boolean {
        return communityPreferences?.isCloudflareConfigured() == true && translationRepository != null
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        // Return the manga info as-is since metadata is stored with translations
        return manga
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        if (!isConfigured() || translationRepository == null) {
            return emptyList()
        }
        
        return try {
            val languageIndex = commands.filterIsInstance<Command.Chapter.Select>()
                .firstOrNull()?.value ?: 0
            val language = if (languageIndex > 0) SUPPORTED_LANGUAGES[languageIndex].first else null
            
            // Parse book info from manga key (format: "bookTitle:::bookAuthor")
            val parts = manga.key.split(":::")
            val bookTitle = parts.getOrNull(0) ?: manga.title
            val bookAuthor = parts.getOrNull(1) ?: manga.author
            
            val translations = translationRepository.getBookTranslations(
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                targetLanguage = language
            )
            
            translations.map { metadata ->
                ChapterInfo(
                    key = metadata.id,
                    name = buildChapterName(metadata),
                    number = parseChapterNumber(metadata.chapterName, metadata.chapterNumber),
                    dateUpload = metadata.createdAt,
                    scanlator = metadata.contributorName.ifBlank { "Anonymous" }
                )
            }.sortedBy { it.number }
        } catch (e: Exception) {
            Log.error("CommunitySource: Failed to get chapters", e)
            emptyList()
        }
    }
    
    /**
     * Parse chapter number from chapter name.
     * Extracts numeric value from strings like "Chapter 12", "Ch. 5", "12 - Title", etc.
     * Falls back to metadata chapter number if parsing fails.
     */
    private fun parseChapterNumber(chapterName: String, fallback: Float): Float {
        // Try to extract number from chapter name
        // Patterns: "Chapter 12", "Ch. 5", "12 - Title", "Episode 3", etc.
        val patterns = listOf(
            Regex("""(?:chapter|ch\.?|episode|ep\.?)\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""^(\d+(?:\.\d+)?)\s*[-:.]"""),
            Regex("""(\d+(?:\.\d+)?)""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(chapterName)
            if (match != null) {
                val numberStr = match.groupValues[1]
                numberStr.toFloatOrNull()?.let { return it }
            }
        }
        
        return fallback
    }
    
    private fun buildChapterName(metadata: TranslationMetadata): String {
        // Don't add language suffix to chapter name - language is now in book title
        val engineSuffix = " (${metadata.engineId})"
        return "${metadata.chapterName}$engineSuffix"
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        if (!isConfigured() || translationRepository == null) {
            return listOf(Text(NOT_CONFIGURED_MESSAGE.trimIndent()))
        }
        
        return try {
            val translationId = chapter.key
            
            // Search all translations to find this one
            val allTranslations = translationRepository.searchTranslations("", null, 1000)
            val metadata = allTranslations.find { it.id == translationId }
            
            if (metadata != null) {
                val result = translationRepository.getTranslationContent(metadata)
                if (result.isSuccess) {
                    val content = result.getOrNull() ?: ""
                    val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
                    paragraphs.map { Text(it) }
                } else {
                    listOf(Text("Failed to load translation content: ${result.exceptionOrNull()?.message}"))
                }
            } else {
                listOf(Text("Translation not found. It may have been removed."))
            }
        } catch (e: Exception) {
            Log.error("CommunitySource: Failed to get page list", e)
            listOf(Text("Error loading chapter: ${e.message}"))
        }
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        if (!isConfigured() || translationRepository == null) {
            return MangasPageInfo(
                mangas = listOf(createWelcomePlaceholder()),
                hasNextPage = false
            )
        }
        
        return try {
            val languageIndex = (sort as? LanguageListing)?.languageIndex ?: 0
            val language = if (languageIndex > 0) SUPPORTED_LANGUAGES[languageIndex].first else null
            
            val translations = if (language != null) {
                translationRepository.getPopularTranslations(language, 100)
            } else {
                translationRepository.searchTranslations("", null, 100)
            }
            
            // Group by book to show unique books
            val books = translations.groupBy { "${it.bookTitle}:::${it.bookAuthor}" }
            
            val mangas = books.map { (key, bookTranslations) ->
                val first = bookTranslations.first()
                val chapterCount = bookTranslations.size
                val languages = bookTranslations.map { it.targetLanguage }.distinct()
                val languageDisplay = languages.joinToString(", ") { it.uppercase() }
                
                MangaInfo(
                    key = key,
                    title = "${first.bookTitle} [$languageDisplay]",
                    author = first.bookAuthor,
                    description = "Community translations: $chapterCount chapters\nContributors: ${bookTranslations.map { it.contributorName }.distinct().take(3).joinToString(", ")}",
                    genres = listOf("Community", "AI Translation"),
                    status = MangaInfo.UNKNOWN,
                    cover = first.bookCover.ifBlank { ICON_URL }
                )
            }
            
            MangasPageInfo(
                mangas = mangas.ifEmpty { listOf(createWelcomePlaceholder()) },
                hasNextPage = false
            )
        } catch (e: Exception) {
            Log.error("CommunitySource: Failed to get manga list", e)
            MangasPageInfo(
                mangas = listOf(createErrorPlaceholder(e.message ?: "Unknown error")),
                hasNextPage = false
            )
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        if (!isConfigured() || translationRepository == null) {
            return MangasPageInfo(
                mangas = listOf(createWelcomePlaceholder()),
                hasNextPage = false
            )
        }
        
        return try {
            var query = ""
            var language: String? = null
            
            filters.forEach { filter ->
                when (filter) {
                    is Filter.Title -> query = filter.value
                    is LanguageFilter -> {
                        if (filter.value > 0) {
                            language = SUPPORTED_LANGUAGES[filter.value].first
                        }
                    }
                    else -> {}
                }
            }
            
            val translations = translationRepository.searchTranslations(query, language, 100)
            
            // Group by book
            val books = translations.groupBy { "${it.bookTitle}:::${it.bookAuthor}" }
            
            val mangas = books.map { (key, bookTranslations) ->
                val first = bookTranslations.first()
                val chapterCount = bookTranslations.size
                val languages = bookTranslations.map { it.targetLanguage }.distinct()
                val languageDisplay = languages.joinToString(", ") { it.uppercase() }
                
                MangaInfo(
                    key = key,
                    title = "${first.bookTitle} [$languageDisplay]",
                    author = first.bookAuthor,
                    description = "Community translations: $chapterCount chapters\nContributors: ${bookTranslations.map { it.contributorName }.distinct().take(3).joinToString(", ")}",
                    genres = listOf("Community", "AI Translation"),
                    status = MangaInfo.UNKNOWN,
                    cover = first.bookCover.ifBlank { ICON_URL }
                )
            }
            
            MangasPageInfo(
                mangas = mangas.ifEmpty { listOf(createNoResultsPlaceholder(query)) },
                hasNextPage = false
            )
        } catch (e: Exception) {
            Log.error("CommunitySource: Failed to search manga", e)
            MangasPageInfo(
                mangas = listOf(createErrorPlaceholder(e.message ?: "Unknown error")),
                hasNextPage = false
            )
        }
    }

    private fun createWelcomePlaceholder(): MangaInfo {
        return MangaInfo(
            key = "welcome",
            title = "Welcome to Community Translations",
            author = "IReader Community",
            description = NOT_CONFIGURED_MESSAGE.trimIndent(),
            genres = listOf("Info"),
            status = MangaInfo.UNKNOWN,
            cover = ICON_URL
        )
    }
    
    private fun createNoResultsPlaceholder(query: String): MangaInfo {
        return MangaInfo(
            key = "no-results",
            title = "No Results Found",
            author = "",
            description = "No community translations found for '$query'.\n\nBe the first to contribute! Enable 'Share AI Translations' in settings and translate some chapters.",
            genres = listOf("Info"),
            status = MangaInfo.UNKNOWN,
            cover = ICON_URL
        )
    }
    
    private fun createErrorPlaceholder(error: String): MangaInfo {
        return MangaInfo(
            key = "error",
            title = "Error Loading Translations",
            author = "",
            description = "Failed to load community translations:\n$error\n\nPlease check your internet connection and try again.",
            genres = listOf("Error"),
            status = MangaInfo.UNKNOWN,
            cover = ICON_URL
        )
    }
    
    override fun getListings(): List<Listing> {
        return listOf(
            LanguageListing("All Languages", 0),
            LanguageListing("English", 1),
            LanguageListing("Spanish", 2),
            LanguageListing("French", 3),
            LanguageListing("German", 4),
            LanguageListing("Portuguese", 5),
            LanguageListing("Russian", 6),
            LanguageListing("Japanese", 7),
            LanguageListing("Korean", 8),
            LanguageListing("Chinese", 9)
        )
    }
    
    override fun getFilters(): FilterList {
        return listOf(
            Filter.Title(),
            LanguageFilter()
        )
    }
    
    override fun getCommands(): CommandList {
        return listOf(
            Command.Chapter.Select(
                name = "Target Language",
                options = SUPPORTED_LANGUAGES.map { it.second }.toTypedArray(),
                value = 0
            )
        )
    }
    
    // Custom Listing for language selection
    class LanguageListing(
        name: String,
        val languageIndex: Int
    ) : Listing(name)
    
    // Custom Filter for language
    class LanguageFilter : Filter.Select(
        name = "Target Language",
        options = SUPPORTED_LANGUAGES.map { it.second }.toTypedArray(),
        value = 0
    )
    
    companion object {
        const val SOURCE_ID = -300L
        const val SOURCE_NAME = "Community Translations"
        const val SOURCE_LANG = "multi"
        const val ICON_URL = "https://raw.githubusercontent.com/IReaderorg/badge-repo/main/app-icon.png"
        
        private const val NOT_CONFIGURED_MESSAGE = """
Welcome to Community Translations!

Browse and read AI translations shared by the community.
Translations are stored in Cloudflare for fast, free access.

To configure:
1. Go to Settings → Community Hub → Community Source
2. Cloudflare D1/R2 should be pre-configured
3. Or set up your own Cloudflare backend

To contribute:
1. Enable "Share AI Translations" in settings
2. Set your contributor name
3. Translate chapters using AI (OpenAI, Gemini, DeepSeek)
4. Translations are automatically shared!
        """
        
        val SUPPORTED_LANGUAGES = listOf(
            "" to "All Languages",
            "en" to "English",
            "es" to "Spanish",
            "fr" to "French",
            "de" to "German",
            "pt" to "Portuguese",
            "ru" to "Russian",
            "ja" to "Japanese",
            "ko" to "Korean",
            "zh" to "Chinese",
            "ar" to "Arabic",
            "hi" to "Hindi",
            "it" to "Italian",
            "nl" to "Dutch",
            "pl" to "Polish",
            "tr" to "Turkish",
            "vi" to "Vietnamese",
            "th" to "Thai",
            "id" to "Indonesian",
            "fil" to "Filipino"
        )
    }
}
