package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.data.local.dao.CategoryDao

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : org.ireader.common_data.repository.CategoryRepository {
    override fun subscribeAll(): Flow<List<CategoryWithCount>> {
        return dao.subscribeAll().map { list ->
            list.map {
            it.toCategoryWithCount()
        } }
    }

    override suspend fun findAll(): List<CategoryWithCount> {
        return dao.findAll().map { it.toCategoryWithCount() }
    }

    override suspend fun find(categoryId: Long): Category {
        return dao.find(categoryId)
    }

    override suspend fun findCategoriesOfBook(bookId: Long): List<Category> {
        return  dao.findCategoriesOfBook(bookId)
    }

    override suspend fun updateAllFlags(flags: Long) {
        dao.updateAllFlags(flags)
    }

    override suspend fun insertOrUpdate(category: Category): Long {
        return dao.insertOrUpdate(category)
    }

    override suspend fun insertOrUpdate(category: List<Category>): List<Long> {
        return dao.insertOrUpdate(category)
    }

    override suspend fun delete(category: Category) {
        return dao.delete(category)
    }

    override suspend fun delete(category: List<Category>) {
        return dao.delete(category)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
