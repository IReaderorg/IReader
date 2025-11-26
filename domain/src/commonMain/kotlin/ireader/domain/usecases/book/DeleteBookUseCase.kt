package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Book

/**
 * Use case for deleting books with proper cleanup
 */
class DeleteBookUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository
) {
    /**
     * Delete a single book with all related data
     */
    suspend operator fun invoke(bookId: Long) {
        // Delete chapters first
        chapterRepository.deleteChaptersByBookId(bookId)
        
        // Delete history entries
        historyRepository.deleteHistoryByBookId(bookId)
        
        // Delete the book
        bookRepository.deleteBookById(bookId)
    }
    
    /**
     * Delete multiple books with all related data
     */
    suspend operator fun invoke(books: List<Book>) {
        books.forEach { book ->
            invoke(book.id)
        }
    }
    
    /**
     * Delete book by key
     */
    suspend fun deleteByKey(key: String) {
        // Delete chapters first
        val book = bookRepository.findBookByKey(key)
        if (book != null) {
            invoke(book.id)
        }
    }
}
