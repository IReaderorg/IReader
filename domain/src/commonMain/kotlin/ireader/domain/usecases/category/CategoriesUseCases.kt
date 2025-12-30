package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.entities.SmartCategory
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.local.book_usecases.GetSmartCategoryBooksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged


class CategoriesUseCases  internal constructor(
    private val repo: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val getSmartCategoryBooksUseCase: GetSmartCategoryBooksUseCase,
    private val libraryPreferences: LibraryPreferences,
    private val libraryRepository: LibraryRepository,
) {

    suspend fun await(): List<CategoryWithCount> {
        return repo.findAll().filter { it.category.id != Category.UNCATEGORIZED_ID }
    }
    
    /**
     * Get smart categories with their book counts using efficient database COUNT queries.
     * This avoids loading all books into memory, preventing OOM on large libraries.
     */
    suspend fun getSmartCategoriesWithCounts(): List<CategoryWithCount> {
        val smartCategories = listOf(
            SmartCategory.RecentlyAdded,
            SmartCategory.CurrentlyReading,
            SmartCategory.Completed,
            SmartCategory.Unread
        )

        return smartCategories.map { smartCategory ->
            val count = getSmartCategoryCountEfficient(smartCategory)
            smartCategory.toCategoryWithCount(count)
        }
    }
    
    /**
     * Get count for a smart category using efficient database COUNT queries.
     * Uses cached chapter counts in the book table for O(1) counting.
     */
    private suspend fun getSmartCategoryCountEfficient(smartCategory: SmartCategory): Int {
        return when (smartCategory) {
            is SmartCategory.CurrentlyReading -> libraryRepository.getCurrentlyReadingCount()
            is SmartCategory.RecentlyAdded -> libraryRepository.getRecentlyAddedCount(daysAgo = 7)
            is SmartCategory.Completed -> libraryRepository.getCompletedCount()
            is SmartCategory.Unread -> libraryRepository.getUnreadCount()
            is SmartCategory.Archived -> libraryRepository.getArchivedCount()
        }
    }
    
    val systemCategories = listOf<Category>(
        Category(id = Category.ALL_ID, "", Category.ALL_ID, 0),
        Category(id = Category.UNCATEGORIZED_ID, "", Category.UNCATEGORIZED_ID, 0),
    )

    fun subscribe(withAllCategory: Boolean, showEmptyCategories: Boolean = false, scope: CoroutineScope): Flow<List<CategoryWithCount>> {

        return combine(
            repo.subscribe(),
            libraryPreferences.showSmartCategories().stateIn(scope)
        ) { categories, showSmartCategories ->
            // Smart categories now use efficient database COUNT queries
            // This prevents OOM with large libraries (10,000+ books)
            val smartCategories = if (showSmartCategories) {
                try {
                    getSmartCategoriesWithCounts()
                } catch (e: Exception) {
                    // Fallback to empty list if count queries fail
                    emptyList()
                }
            } else {
                emptyList()
            }

            // Filter regular categories
            val regularCategories = categories.mapNotNull { categoryAndCount ->
                val (category, count) = categoryAndCount
                when (category.id) {
                    // All category only shown when requested
                    Category.ALL_ID -> if (withAllCategory) categoryAndCount else null

                    // Uncategorized category - always hidden (removed per user request)
                    Category.UNCATEGORIZED_ID -> null

                    // User created category - filter by empty status if preference is set
                    else -> {
                        if (showEmptyCategories || count > 0) {
                            categoryAndCount
                        } else {
                            null
                        }
                    }
                }
            }

            // Prepend smart categories before regular categories only if enabled
            smartCategories + regularCategories
        }
            .distinctUntilChanged()
    }

    suspend fun deleteCategory(category: Category) {
        repo.delete(category.id)
    }
    suspend fun insertBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.insertAll(categories)
    }

    suspend fun deleteBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.delete(categories)
    }
    suspend fun deleteBookCategory(categories: BookCategory) {
        bookCategoryRepository.delete(listOf(categories))
    }

    fun subscribeBookCategories(): Flow<List<BookCategory>> {
        return bookCategoryRepository.subscribeAll()
    }
}
