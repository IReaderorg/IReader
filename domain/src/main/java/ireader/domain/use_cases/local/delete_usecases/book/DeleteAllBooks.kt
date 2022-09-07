package ireader.domain.use_cases.local.delete_usecases.book

import ireader.common.data.repository.BookRepository

/**
 * Delete All Book from database
 */
class DeleteAllBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke() {
        return bookRepository.deleteAllBooks()
    }
}
