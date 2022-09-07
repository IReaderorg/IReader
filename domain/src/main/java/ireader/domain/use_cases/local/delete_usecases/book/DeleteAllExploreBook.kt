package ireader.domain.use_cases.local.delete_usecases.book

import ireader.common.data.repository.BookRepository
import ireader.common.models.entities.Book


/**
 * Delete All Books That are paged in Explore Screen
 */
class DeleteAllExploreBook(private val bookRepository: BookRepository) {
    suspend operator fun invoke() {
        return bookRepository.deleteAllExploreBook()
    }
}

class DeleteBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke(books: List<Book>) {
        bookRepository.deleteBooks(books)
    }
}
