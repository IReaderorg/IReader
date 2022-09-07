package ireader.domain.use_cases.local.book_usecases

import ireader.common.data.repository.BookRepository
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
