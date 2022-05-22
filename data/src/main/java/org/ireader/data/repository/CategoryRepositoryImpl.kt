package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category
import org.ireader.data.local.dao.CategoryDao

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : org.ireader.common_data.repository.CategoryRepository {
    override fun subscribeAll(): Flow<List<Category>> {
        return dao.subscribeAll()
    }

    override suspend fun findAll(): List<Category> {
        return dao.findAll()
    }

    override suspend fun insert(category: Category): Long {
        return dao.insert(category)
    }

    override suspend fun insertAll(category: List<Category>): List<Long> {
        return dao.insert(category)
    }

    override suspend fun delete(category: Category) {
        return dao.delete(category)
    }

    override suspend fun deleteAll(category: List<Category>) {
        return dao.delete(category)
    }
}