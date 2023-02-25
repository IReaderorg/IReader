package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.library.LibrarySort



class FindAllInLibraryBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke(): List<Book> {
        return bookRepository.findAllInLibraryBooks(
            sortType = LibrarySort.default,
            isAsc = false,
            unreadFilter = false
        )
    }
}
