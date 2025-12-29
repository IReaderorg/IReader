package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.CategoryAutoRuleRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.CategoryAutoRule

/**
 * Use case for automatically categorizing books based on auto-categorization rules.
 * 
 * This use case checks all enabled rules and assigns books to categories
 * when they match genre or source criteria.
 */
class AutoCategorizeBookUseCase(
    private val categoryAutoRuleRepository: CategoryAutoRuleRepository,
    private val bookCategoryRepository: BookCategoryRepository,
) {
    /**
     * Auto-categorize a single book based on enabled rules.
     * 
     * @param book The book to categorize
     * @param sourceName Optional source name for source-based rules
     * @return List of category IDs the book was assigned to
     */
    suspend operator fun invoke(book: Book, sourceName: String? = null): List<Long> {
        val enabledRules = categoryAutoRuleRepository.findEnabledRules()
        val matchingCategoryIds = mutableSetOf<Long>()
        
        for (rule in enabledRules) {
            if (rule.matches(book, sourceName)) {
                matchingCategoryIds.add(rule.categoryId)
            }
        }
        
        // Insert book-category relationships for matching categories
        val bookCategories = matchingCategoryIds.map { categoryId ->
            BookCategory(bookId = book.id, categoryId = categoryId)
        }
        
        if (bookCategories.isNotEmpty()) {
            bookCategoryRepository.insertAll(bookCategories)
        }
        
        return matchingCategoryIds.toList()
    }
    
    /**
     * Auto-categorize multiple books based on enabled rules.
     * 
     * @param books The books to categorize
     * @param getSourceName Function to get source name for a book's sourceId
     * @return Map of book ID to list of assigned category IDs
     */
    suspend fun categorizeMultiple(
        books: List<Book>,
        getSourceName: suspend (Long) -> String? = { null }
    ): Map<Long, List<Long>> {
        val enabledRules = categoryAutoRuleRepository.findEnabledRules()
        if (enabledRules.isEmpty()) return emptyMap()
        
        val result = mutableMapOf<Long, MutableList<Long>>()
        val allBookCategories = mutableListOf<BookCategory>()
        
        for (book in books) {
            val sourceName = getSourceName(book.sourceId)
            val matchingCategoryIds = mutableListOf<Long>()
            
            for (rule in enabledRules) {
                if (rule.matches(book, sourceName)) {
                    matchingCategoryIds.add(rule.categoryId)
                    allBookCategories.add(BookCategory(bookId = book.id, categoryId = rule.categoryId))
                }
            }
            
            if (matchingCategoryIds.isNotEmpty()) {
                result[book.id] = matchingCategoryIds
            }
        }
        
        if (allBookCategories.isNotEmpty()) {
            bookCategoryRepository.insertAll(allBookCategories)
        }
        
        return result
    }
    
    /**
     * Check which categories a book would be assigned to without actually assigning.
     * Useful for preview/dry-run functionality.
     */
    suspend fun previewCategorization(book: Book, sourceName: String? = null): List<Long> {
        val enabledRules = categoryAutoRuleRepository.findEnabledRules()
        return enabledRules
            .filter { it.matches(book, sourceName) }
            .map { it.categoryId }
            .distinct()
    }
}
