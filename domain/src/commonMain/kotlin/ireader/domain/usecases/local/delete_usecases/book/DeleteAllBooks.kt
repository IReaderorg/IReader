package ireader.domain.usecases.local.delete_usecases.book

import ireader.domain.data.repository.BookRepository

/**
 * Delete All Book from database
 */
class DeleteAllBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke() {
        return bookRepository.deleteAllBooks()
    }
}
