package ireader.data.category

import ireader.common.models.entities.BookCategory
import ireader.data.local.DatabaseHandler
import ireader.domain.data.repository.BookCategoryRepository
import kotlinx.coroutines.flow.Flow


class BookCategoryRepositoryImpl(
    private val handler: DatabaseHandler,
) : BookCategoryRepository {
    override fun subscribeAll(): Flow<List<BookCategory>> {
        return handler.subscribeToList {
            this.bookcategoryQueries.findAll(bookCategoryMapper)
        }
    }

    override suspend fun findAll(): List<BookCategory> {
        return handler.awaitList {
            bookcategoryQueries.findAll(bookCategoryMapper)
        }
    }

    override suspend fun insert(category: BookCategory) {
        return handler.await {
            bookcategoryQueries.insert(category.bookId, category.categoryId)
        }
    }

    override suspend fun insertAll(categories: List<BookCategory>) {
        return handler.await(true) {
            categories.forEach { category ->
                bookcategoryQueries.insert(category.bookId, category.categoryId)
            }
        }
    }

    override suspend fun delete(category: List<BookCategory>) {
        return handler.await(true) {
            category.forEach { item ->
                bookcategoryQueries.deleteByBookId(item.bookId)
            }
        }
    }

    override suspend fun delete(bookId: Long) {
        return handler.await {
            bookcategoryQueries.deleteByBookId(bookId)
        }
    }

    override suspend fun deleteAll(category: List<BookCategory>) {
        return handler.await {
            bookcategoryQueries.deleteAll()
        }
    }

}
