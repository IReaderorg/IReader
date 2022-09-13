package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository
import ireader.common.models.entities.Book
import ireader.common.models.library.LibrarySort
import org.koin.core.annotation.Factory

@Factory
class FindAllInLibraryBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke(): List<Book> {
        return bookRepository.findAllInLibraryBooks(
            sortType = LibrarySort.default,
            isAsc = false,
            unreadFilter = false
        )
    }
}
