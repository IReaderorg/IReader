package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book

/**
 * Use case for updating book information
 */
class UpdateBookUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Update a single book
     */
    suspend operator fun invoke(book: Book) {
        bookRepository.updateBook(book)
    }
    
    /**
     * Update multiple books
     */
    suspend operator fun invoke(books: List<Book>) {
        bookRepository.updateBook(books)
    }
    
    /**
     * Update only changed fields (partial update)
     */
    suspend fun updatePartial(book: Book): Long {
        return bookRepository.updatePartial(book)
    }
}
