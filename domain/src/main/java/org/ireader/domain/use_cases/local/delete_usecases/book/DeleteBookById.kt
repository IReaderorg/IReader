package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.core_api.db.Transactions
import javax.inject.Inject

class DeleteBookById @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(id: Long) {
        return bookRepository.deleteBookById(id)
    }
}

class UnFavoriteBook @Inject constructor(
    private val bookRepository: org.ireader.common_data.repository.BookRepository,
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

class DeleteNotInLibraryBooks @Inject constructor(
    private val bookRepository: org.ireader.common_data.repository.BookRepository,
) {
    suspend operator fun invoke() {
        bookRepository.deleteNotInLibraryBooks()
    }
}



