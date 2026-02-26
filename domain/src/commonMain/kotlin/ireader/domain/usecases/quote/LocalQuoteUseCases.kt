package ireader.domain.usecases.quote

import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.LocalQuoteRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteContext
import ireader.domain.models.quote.ShareValidation
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow

/**
 * Use cases for managing local quotes
 */
class LocalQuoteUseCases(
    private val localQuoteRepository: LocalQuoteRepository,
    private val chapterRepository: ChapterRepository
) {
    
    /**
     * Convert chapter content (List<Page>) to a single string
     */
    private fun Chapter.contentAsString(): String {
        return content.mapNotNull { page ->
            when (page) {
                is Text -> page.text.takeIf { it.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
    }
    
    /**
     * Check if chapter has non-empty content
     */
    private fun Chapter.hasContent(): Boolean {
        return content.any { page ->
            page is Text && page.text.isNotBlank()
        }
    }
    
    /**
     * Save a new quote with optional context backup
     */
    suspend fun saveQuote(
        text: String,
        bookId: Long,
        bookTitle: String,
        chapterTitle: String,
        chapterNumber: Int?,
        author: String?,
        includeContext: Boolean = false,
        currentChapterId: Long? = null,
        prevChapterId: Long? = null,
        nextChapterId: Long? = null
    ): Result<Long> = runCatching {
        val quote = LocalQuote(
            text = text,
            bookId = bookId,
            bookTitle = bookTitle,
            chapterTitle = chapterTitle,
            chapterNumber = chapterNumber,
            author = author,
            createdAt = currentTimeToLong(),
            hasContextBackup = includeContext
        )
        
        val quoteId = localQuoteRepository.insert(quote)
        
        // Save context if requested
        if (includeContext && currentChapterId != null) {
            val contexts = mutableListOf<QuoteContext>()
            
            // Get previous chapter content
            prevChapterId?.let { id ->
                chapterRepository.findChapterById(id)?.let { chapter ->
                    if (chapter.hasContent()) {
                        contexts.add(QuoteContext(
                            quoteId = quoteId,
                            chapterId = id,
                            chapterTitle = chapter.name,
                            content = chapter.contentAsString()
                        ))
                    }
                }
            }
            
            // Get current chapter content
            chapterRepository.findChapterById(currentChapterId)?.let { chapter ->
                if (chapter.hasContent()) {
                    contexts.add(QuoteContext(
                        quoteId = quoteId,
                        chapterId = currentChapterId,
                        chapterTitle = chapter.name,
                        content = chapter.contentAsString()
                    ))
                }
            }
            
            // Get next chapter content
            nextChapterId?.let { id ->
                chapterRepository.findChapterById(id)?.let { chapter ->
                    if (chapter.hasContent()) {
                        contexts.add(QuoteContext(
                            quoteId = quoteId,
                            chapterId = id,
                            chapterTitle = chapter.name,
                            content = chapter.contentAsString()
                        ))
                    }
                }
            }
            
            if (contexts.isNotEmpty()) {
                localQuoteRepository.saveContext(quoteId, contexts)
            }
        }
        
        quoteId
    }
    
    /**
     * Get all quotes
     */
    suspend fun getQuotes(): List<LocalQuote> {
        return localQuoteRepository.getAll()
    }
    
    /**
     * Get quotes for a specific book
     */
    suspend fun getQuotesByBook(bookId: Long): List<LocalQuote> {
        return localQuoteRepository.getByBookId(bookId)
    }
    
    /**
     * Search quotes by text
     */
    suspend fun searchQuotes(query: String): List<LocalQuote> {
        return localQuoteRepository.search(query)
    }
    
    /**
     * Delete a quote
     */
    suspend fun deleteQuote(id: Long) {
        localQuoteRepository.delete(id)
    }
    
    /**
     * Get quote with its context backup
     */
    suspend fun getQuoteWithContext(id: Long): Pair<LocalQuote, List<QuoteContext>>? {
        val quote = localQuoteRepository.getById(id) ?: return null
        val context = if (quote.hasContextBackup) {
            localQuoteRepository.getContext(id)
        } else {
            emptyList()
        }
        return quote to context
    }
    
    /**
     * Observe all quotes as Flow
     */
    fun observeQuotes(): Flow<List<LocalQuote>> {
        return localQuoteRepository.observeAll()
    }
    
    /**
     * Observe quotes for a specific book
     */
    fun observeQuotesByBook(bookId: Long): Flow<List<LocalQuote>> {
        return localQuoteRepository.observeByBookId(bookId)
    }
    
    /**
     * Validate if quote can be shared to Discord
     * Discord has no practical length limit, but we validate for UX
     */
    fun validateForCommunityShare(quote: LocalQuote): ShareValidation {
        val length = quote.text.length
        return when {
            length < 10 -> ShareValidation(
                canShare = false,
                currentLength = length,
                reason = "Quote is too short. Minimum 10 characters required."
            )
            else -> ShareValidation(
                canShare = true,
                currentLength = length
            )
        }
    }
    
    /**
     * Get total quote count
     */
    suspend fun getQuoteCount(): Long {
        return localQuoteRepository.getCount()
    }
    
    /**
     * Update an existing quote
     */
    suspend fun updateQuote(quote: LocalQuote) {
        localQuoteRepository.update(quote)
    }
}
