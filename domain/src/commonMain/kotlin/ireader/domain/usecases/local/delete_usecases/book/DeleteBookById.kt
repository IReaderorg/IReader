package ireader.domain.usecases.local.delete_usecases.book

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.core.db.Transactions


class DeleteBookById(private val bookRepository: BookRepository) {
    suspend operator fun invoke(id: Long) {
        return bookRepository.deleteBookById(id)
    }
}

class UnFavoriteBook(
    private val bookRepository: BookRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val transactions: Transactions,
    private val syncManager: ireader.domain.services.SyncManager,
    private val remoteRepository: ireader.domain.data.repository.RemoteRepository
) {
    suspend operator fun invoke(bookIds: List<Long>) {
        transactions.run {
            bookIds.forEach { bookId ->
                val book = bookRepository.findBookById(bookId) ?: throw IllegalArgumentException()
                bookRepository.updateBook(book.copy(favorite = false))
                bookCategoryRepository.delete(book.id)
                
                // Sync removal to remote if enabled
                try {
                    val user = remoteRepository.getCurrentUser().getOrNull()
                    if (user != null) {
                        syncManager.syncBook(user.id, book.copy(favorite = false))
                    }
                } catch (e: Exception) {
                    // Silently fail - local operation succeeded
                }
            }
        }
    }
}

class DeleteNotInLibraryBooks(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke() {
        bookRepository.deleteNotInLibraryBooks()
    }
}
