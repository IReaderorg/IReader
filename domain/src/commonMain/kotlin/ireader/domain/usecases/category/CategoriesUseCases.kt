package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.CategoryRepository
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
) {

    suspend fun await(): List<CategoryWithCount> {
        return repo.findAll()
    }
    
//    /**
//     * Get smart categories with their book counts
//     */
//    suspend fun getSmartCategories(): List<CategoryWithCount> {
//        // Only include the main smart categories (not Archived)
//        val smartCategories = listOf(
//            SmartCategory.RecentlyAdded,
//            SmartCategory.CurrentlyReading,
//            SmartCategory.Completed,
//            SmartCategory.Unread
//        )
//
//        return smartCategories.map { smartCategory ->
//            val count = getSmartCategoryBooksUseCase.getCount(smartCategory)
//            smartCategory.toCategoryWithCount(count)
//        }
//    }
    val systemCategories = listOf<Category>(
        Category(id = Category.ALL_ID, "", Category.ALL_ID, 0),
        Category(id = Category.UNCATEGORIZED_ID, "", Category.UNCATEGORIZED_ID, 0),
    )

    fun subscribe(withAllCategory: Boolean, showEmptyCategories: Boolean = false, scope: CoroutineScope): Flow<List<CategoryWithCount>> {

        return combine(
            repo.subscribe(),
            libraryPreferences.showSmartCategories().stateIn(scope)
        ) { categories, showSmartCategories ->
            // Get smart categories with counts only if enabled
            val smartCategories = if (showSmartCategories) {
                try {
                    val smartCats = listOf(
                        SmartCategory.RecentlyAdded,
                        SmartCategory.CurrentlyReading,
                        SmartCategory.Completed,
                        SmartCategory.Unread
                    )

                    smartCats.mapNotNull { smartCategory ->
                        try {
                            val count = getSmartCategoryBooksUseCase.getCount(smartCategory)
                            if (count > 0 || showEmptyCategories) {
                                smartCategory.toCategoryWithCount(count)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                } catch (e: Exception) {
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

                    // Uncategorized category only shown if there are entries and user categories exist
                    Category.UNCATEGORIZED_ID -> {
                        if (count > 0 &&
                            (!withAllCategory || categories.any { !it.category.isSystemCategory })
                        ) {
                            categoryAndCount
                        } else {
                            null
                        }
                    }

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
