package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book

/**
 * Use case for searching books
 */
class SearchBooksUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Search books by key
     */
    suspend fun searchByKey(key: String): List<Book> {
        return bookRepository.findBooksByKey(key)
    }
    
    /**
     * Search books by title (fuzzy search)
     */
    suspend fun searchByTitle(title: String): List<Book> {
        val allBooks = bookRepository.findAllBooks()
        return allBooks.filter { book ->
            book.title.contains(title, ignoreCase = true)
        }
    }
    
    /**
     * Search books in library by query
     */
    suspend fun searchInLibrary(query: String): List<Book> {
        val libraryBooks = bookRepository.findAllInLibraryBooks(
            sortType = ireader.domain.models.library.LibrarySort.default,
            isAsc = true,
            unreadFilter = false
        )
        
        return libraryBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
            book.author.contains(query, ignoreCase = true) ||
            book.description.contains(query, ignoreCase = true)
        }
    }
}
