package ireader.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.data.repository.BookCategoryRepository
import ireader.common.models.entities.BookCategory
import ireader.data.local.dao.BookCategoryDao

class BookCategoryRepositoryImpl(
    private val dao: BookCategoryDao
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
