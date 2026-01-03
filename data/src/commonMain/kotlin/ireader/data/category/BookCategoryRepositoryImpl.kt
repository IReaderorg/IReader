package ireader.data.category

import ireader.core.log.IReaderLog
import ireader.domain.models.entities.BookCategory
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.services.library.LibraryChangeNotifier
import kotlinx.coroutines.flow.Flow


class BookCategoryRepositoryImpl(
    private val handler: DatabaseHandler,
    private val changeNotifier: LibraryChangeNotifier? = null,
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
        handler.await {
            bookcategoryQueries.insert(category.bookId, category.categoryId)
        }
        // Notify library that category changed
        changeNotifier?.notifyChange(
            LibraryChangeNotifier.ChangeType.CategoryChanged(category.bookId, category.categoryId)
        )
    }

    override suspend fun insertAll(categories: List<BookCategory>) {
        IReaderLog.info("BookCategoryRepositoryImpl.insertAll() called with ${categories.size} categories", "BookCategoryRepo")
        handler.await(true) {
            categories.forEach { category ->
                bookcategoryQueries.insert(category.bookId, category.categoryId)
            }
        }
        // Notify library that categories changed so UI refreshes
        if (categories.isNotEmpty()) {
            val bookIds = categories.map { it.bookId }.distinct()
            IReaderLog.info("BookCategoryRepositoryImpl: Notifying CategoriesChanged for bookIds=$bookIds, changeNotifier=${if (changeNotifier != null) "present" else "NULL"}", "BookCategoryRepo")
            changeNotifier?.notifyChange(LibraryChangeNotifier.ChangeType.CategoriesChanged(bookIds))
            IReaderLog.info("BookCategoryRepositoryImpl: Notification sent", "BookCategoryRepo")
        }
    }

    override suspend fun delete(category: List<BookCategory>) {
        handler.await(true) {
            category.forEach { item ->
                bookcategoryQueries.deleteByBookIdAndCategoryId(item.bookId, item.categoryId)
            }
        }
        // Notify library that categories changed
        if (category.isNotEmpty()) {
            val bookIds = category.map { it.bookId }.distinct()
            changeNotifier?.notifyChange(LibraryChangeNotifier.ChangeType.CategoriesChanged(bookIds))
        }
    }

    override suspend fun delete(bookId: Long) {
        handler.await {
            bookcategoryQueries.deleteByBookId(bookId)
        }
        // Notify library that book's categories changed
        changeNotifier?.notifyChange(LibraryChangeNotifier.ChangeType.CategoryChanged(bookId, null))
    }

    override suspend fun deleteAll(category: List<BookCategory>) {
        return handler.await {
            bookcategoryQueries.deleteAll()
        }
    }

    override suspend fun replaceAll(bookId: Long, categoryIds: List<Long>) {
        IReaderLog.info("BookCategoryRepositoryImpl.replaceAll() called for bookId=$bookId with ${categoryIds.size} categories", "BookCategoryRepo")
        handler.await(inTransaction = true) {
            // Delete all existing category assignments for this book
            bookcategoryQueries.deleteByBookId(bookId)
            
            // Insert new category assignments
            categoryIds.forEach { categoryId ->
                bookcategoryQueries.insert(bookId, categoryId)
            }
        }
        
        // Notify library that book's categories changed
        IReaderLog.info("BookCategoryRepositoryImpl.replaceAll(): Notifying CategoryChanged for bookId=$bookId", "BookCategoryRepo")
        changeNotifier?.notifyChange(LibraryChangeNotifier.ChangeType.CategoriesChanged(listOf(bookId)))
    }
}
