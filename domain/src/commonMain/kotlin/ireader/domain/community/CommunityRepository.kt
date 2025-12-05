package ireader.domain.community

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo

/**
 * Repository interface for Community Source data operations.
 * 
 * This interface defines the contract for fetching community-contributed
 * books and chapters from the Supabase backend.
 */
interface CommunityRepository {
    
    /**
     * Get the latest books added to the community.
     */
    suspend fun getLatestBooks(page: Int): MangasPageInfo
    
    /**
     * Get the most popular books in the community.
     */
    suspend fun getPopularBooks(page: Int): MangasPageInfo
    
    /**
     * Get books that were recently translated.
     */
    suspend fun getRecentlyTranslatedBooks(page: Int): MangasPageInfo
    
    /**
     * Search books with filters.
     */
    suspend fun searchBooks(
        query: String,
        language: String?,
        genre: String?,
        status: String?,
        page: Int
    ): MangasPageInfo
    
    /**
     * Get detailed information about a specific book.
     */
    suspend fun getBookDetails(bookKey: String): MangaInfo
    
    /**
     * Get chapters for a book, optionally filtered by language.
     */
    suspend fun getChapters(bookKey: String, language: String?): List<ChapterInfo>
    
    /**
     * Get the content of a specific chapter.
     */
    suspend fun getChapterContent(chapterKey: String): String
    
    /**
     * Submit a new book to the community.
     */
    suspend fun submitBook(book: CommunityBook): Result<String>
    
    /**
     * Submit a translated chapter to the community.
     */
    suspend fun submitChapter(chapter: CommunityChapter): Result<String>
    
    /**
     * Get available languages for a book.
     */
    suspend fun getAvailableLanguages(bookKey: String): List<String>
    
    /**
     * Report a chapter for issues.
     */
    suspend fun reportChapter(chapterKey: String, reason: String): Result<Unit>
    
    /**
     * Get user's contributed books.
     */
    suspend fun getUserContributions(userId: String): List<CommunityBook>
    
    /**
     * Rate a translation.
     */
    suspend fun rateTranslation(chapterKey: String, rating: Int): Result<Unit>
}

/**
 * Data class representing a community-contributed book.
 */
data class CommunityBook(
    val id: String = "",
    val title: String,
    val author: String = "",
    val description: String = "",
    val cover: String = "",
    val genres: List<String> = emptyList(),
    val status: String = "Ongoing",
    val originalLanguage: String = "en",
    val availableLanguages: List<String> = emptyList(),
    val contributorId: String = "",
    val contributorName: String = "",
    val viewCount: Long = 0,
    val chapterCount: Int = 0,
    val lastUpdated: Long = 0,
    val createdAt: Long = 0
)

/**
 * Data class representing a community-contributed chapter.
 */
data class CommunityChapter(
    val id: String = "",
    val bookId: String,
    val name: String,
    val number: Float = -1f,
    val content: String,
    val language: String,
    val translatorId: String = "",
    val translatorName: String = "",
    val originalChapterKey: String = "",
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val viewCount: Long = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

/**
 * Data class for chapter translation submission.
 */
data class TranslationSubmission(
    val bookId: String,
    val chapterName: String,
    val chapterNumber: Float,
    val content: String,
    val targetLanguage: String,
    val sourceLanguage: String,
    val originalChapterKey: String? = null,
    val notes: String = ""
)
