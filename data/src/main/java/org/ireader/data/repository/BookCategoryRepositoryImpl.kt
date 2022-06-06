package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_models.entities.BookCategory
import org.ireader.data.local.dao.BookCategoryDao

class BookCategoryRepositoryImpl(
    private val dao:BookCategoryDao
) : BookCategoryRepository {
    override fun subscribeAll(): Flow<List<BookCategory>> {
        return dao.subscribeAll()
    }

    override suspend fun findAll(): List<BookCategory> {
        return dao.findAll()
    }

    override suspend fun insert(category: BookCategory) {
        return dao.insertOrUpdate(category)
    }

    override suspend fun insertAll(category: List<BookCategory>) {
        return dao.insertOrUpdate(category)
    }

    override suspend fun delete(category: BookCategory) {
        return dao.delete(category)
    }

    override suspend fun delete(bookId: Long) {
        return dao.delete(bookId)
    }

    override suspend fun deleteAll(category: List<BookCategory>) {
        return dao.delete(category)
    }
}