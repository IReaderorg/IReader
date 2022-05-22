package org.ireader.domain.use_cases.category

import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_models.entities.BookCategory
import org.ireader.common_models.entities.Category
import javax.inject.Inject

class GetCategories @Inject internal constructor(
    private val repo : CategoryRepository,
    private val bookCategoryRepository : BookCategoryRepository,
) {

    suspend fun await(): List<Category> {
        return repo.findAll()
    }

    fun subscribe(
    ): Flow<List<Category>> {
        return repo.subscribeAll()
    }

    suspend fun insertCategory(category: Category) {
        repo.insert(category)
    }
    suspend fun insertBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.insertAll(categories)
    }
    suspend fun deleteBookCategory(categories: List<BookCategory>) {
        bookCategoryRepository.deleteAll(categories)
    }

    fun subscribeBookCategories(): Flow<List<BookCategory>> {
      return  bookCategoryRepository.subscribeAll()
    }

}
