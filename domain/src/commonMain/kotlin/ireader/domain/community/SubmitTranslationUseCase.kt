package ireader.domain.community

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for submitting translated content to the community.
 * 
 * This is called after a user completes translating a chapter and wants
 * to share it with the community.
 */
class SubmitTranslationUseCase(
    private val communityRepository: CommunityRepository,
    private val communityPreferences: CommunityPreferences
) {
    
    /**
     * Submit a translated chapter to the community.
     * 
     * @param book The book the chapter belongs to
     * @param chapter The chapter being translated
     * @param translatedContent The translated content
     * @param targetLanguage The language the content was translated to
     * @param sourceLanguage The original language of the content
     * @return Result with the submitted chapter ID or error
     */
    suspend fun submitChapter(
        book: Book,
        chapter: Chapter,
        translatedContent: String,
        targetLanguage: String,
        sourceLanguage: String
    ): Result<String> {
        // Check if auto-share is enabled
        if (!communityPreferences.autoShareTranslations().get()) {
            return Result.failure(Exception("Auto-share is disabled"))
        }
        
        val contributorName = communityPreferences.contributorName().get()
        if (contributorName.isBlank()) {
            return Result.failure(Exception("Please set your contributor name in settings"))
        }
        
        // First, ensure the book exists in the community
        val bookResult = ensureBookExists(book, sourceLanguage, contributorName)
        if (bookResult.isFailure) {
            return Result.failure(bookResult.exceptionOrNull() ?: Exception("Failed to create book"))
        }
        
        val bookId = bookResult.getOrNull() ?: return Result.failure(Exception("Book ID not found"))
        
        // Submit the chapter
        val communityChapter = CommunityChapter(
            bookId = bookId,
            name = chapter.name,
            number = chapter.number,
            content = translatedContent,
            language = targetLanguage,
            translatorName = contributorName,
            originalChapterKey = chapter.key,
            createdAt = currentTimeToLong(),
            updatedAt = currentTimeToLong()
        )
        
        return communityRepository.submitChapter(communityChapter)
    }
    
    /**
     * Submit a new book to the community.
     */
    suspend fun submitBook(
        book: Book,
        originalLanguage: String
    ): Result<String> {
        val contributorName = communityPreferences.contributorName().get()
        if (contributorName.isBlank()) {
            return Result.failure(Exception("Please set your contributor name in settings"))
        }
        
        val communityBook = CommunityBook(
            title = book.title,
            author = book.author,
            description = book.description,
            cover = book.cover,
            genres = book.genres,
            status = when (book.status) {
                1L -> "Ongoing"
                2L -> "Completed"
                6L -> "Hiatus"
                5L -> "Dropped"
                else -> "Ongoing"
            },
            originalLanguage = originalLanguage,
            contributorName = contributorName,
            createdAt = currentTimeToLong()
        )
        
        return communityRepository.submitBook(communityBook)
    }
    
    /**
     * Ensure a book exists in the community database.
     * If it doesn't exist, create it.
     */
    private suspend fun ensureBookExists(
        book: Book,
        originalLanguage: String,
        contributorName: String
    ): Result<String> {
        // Try to find existing book by title
        val searchResult = communityRepository.searchBooks(
            query = book.title,
            language = null,
            genre = null,
            status = null,
            page = 1
        )
        
        // Check if book already exists
        val existingBook = searchResult.mangas.find { 
            it.title.equals(book.title, ignoreCase = true) 
        }
        
        if (existingBook != null) {
            return Result.success(existingBook.key)
        }
        
        // Create new book
        val communityBook = CommunityBook(
            title = book.title,
            author = book.author,
            description = book.description,
            cover = book.cover,
            genres = book.genres,
            status = when (book.status) {
                1L -> "Ongoing"
                2L -> "Completed"
                6L -> "Hiatus"
                5L -> "Dropped"
                else -> "Ongoing"
            },
            originalLanguage = originalLanguage,
            contributorName = contributorName,
            createdAt = currentTimeToLong()
        )
        
        return communityRepository.submitBook(communityBook)
    }
    
    /**
     * Rate a translation.
     */
    suspend fun rateTranslation(chapterKey: String, rating: Int): Result<Unit> {
        if (rating < 1 || rating > 5) {
            return Result.failure(Exception("Rating must be between 1 and 5"))
        }
        return communityRepository.rateTranslation(chapterKey, rating)
    }
    
    /**
     * Report a problematic chapter.
     */
    suspend fun reportChapter(chapterKey: String, reason: String): Result<Unit> {
        if (reason.isBlank()) {
            return Result.failure(Exception("Please provide a reason for the report"))
        }
        return communityRepository.reportChapter(chapterKey, reason)
    }
}
