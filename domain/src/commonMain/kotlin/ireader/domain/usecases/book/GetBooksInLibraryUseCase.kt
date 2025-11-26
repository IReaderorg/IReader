package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.library.LibrarySort

/**
 * Use case for retrieving books in the library with sorting and filtering
 */
class GetBooksInLibraryUseCase(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(
        sortType: LibrarySort = LibrarySort.default,
        isAscending: Boolean = true,
        unreadFilter: Boolean = false
    ): List<Book> {
        return bookRepository.findAllInLibraryBooks(
            sortType = sortType,
            isAsc = isAscending,
            unreadFilter = unreadFilter
        )
    }
}
