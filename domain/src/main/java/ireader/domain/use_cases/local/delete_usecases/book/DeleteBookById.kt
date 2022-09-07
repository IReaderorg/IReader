package ireader.domain.use_cases.local.delete_usecases.book

import ireader.common.data.repository.BookCategoryRepository
import ireader.common.data.repository.BookRepository
import ireader.core.api.db.Transactions
import org.koin.core.annotation.Factory
@Factory
class DeleteBookById(private val bookRepository: BookRepository) {
    suspend operator fun invoke(id: Long) {
        return bookRepository.deleteBookById(id)
    }
}
@Factory
class UnFavoriteBook(
    private val bookRepository: BookRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val transactions: Transactions
) {
    suspend operator fun invoke(bookIds: List<Long>) {
        transactions.run {
            bookIds.forEach { bookId ->
                val book = bookRepository.findBookById(bookId) ?: throw IllegalArgumentException()
                bookRepository.insertBook(book.copy(favorite = false))
                bookCategoryRepository.delete(book.id)
            }
        }
    }
}
@Factory
class DeleteNotInLibraryBooks(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke() {
        bookRepository.deleteNotInLibraryBooks()
    }
}
