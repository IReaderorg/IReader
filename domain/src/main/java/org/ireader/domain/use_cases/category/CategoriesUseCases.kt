package org.ireader.domain.use_cases.category

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_models.entities.BookCategory
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithCount
import javax.inject.Inject

class CategoriesUseCases @Inject internal constructor(
    private val repo: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository,
) {

    suspend fun await(): List<CategoryWithCount> {
        return repo.findAll()
    }
    val systemCategories = listOf<Category>(
        Category(id = Category.ALL_ID, "", Category.ALL_ID.toInt(), 0, 0),
        Category(id = Category.UNCATEGORIZED_ID, "", Category.UNCATEGORIZED_ID.toInt(), 0, 0),
    )

    fun subscribe(withAllCategory: Boolean): Flow<List<CategoryWithCount>> {
        return repo.subscribeAll().map { categories ->
            categories.mapNotNull { categoryAndCount ->
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

                    // User created category, always show
                    else -> categoryAndCount
                }
            }
        }
            .distinctUntilChanged()
    }

    suspend fun deleteCategory(category: Category) {
        repo.delete(category)
    }
    suspend fun insertBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.insertAll(categories)
    }

    suspend fun deleteBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.deleteAll(categories)
    }
    suspend fun deleteBookCategory(categories: BookCategory) {
        bookCategoryRepository.delete(categories)
    }

    fun subscribeBookCategories(): Flow<List<BookCategory>> {
        return bookCategoryRepository.subscribeAll()
    }
}
