package ireader.data.repository

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository as OldBookRepository
import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import ireader.domain.models.updates.BookUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Adapter implementation that wraps the old BookRepository to implement
 * the new consolidated BookRepository interface.
 */
class ConsolidatedBookRepositoryImpl(
    private val oldRepository: OldBookRepository,
    private val bookCategoryRepository: BookCategoryRepository
) : BookRepository {
    
    override suspend fun getBookById(id: Long): Book? {
        return oldRepository.findBookById(id)
    }
    
    override fun getBookByIdAsFlow(id: Long): Flow<Book?> {
        return oldRepository.subscribeBookById(id)
    }
    
    override suspend fun getBookByUrlAndSourceId(url: String, sourceId: Long): Book? {
        return oldRepository.find(url, sourceId)
    }
    
    override fun getBookByUrlAndSourceIdAsFlow(url: String, sourceId: Long): Flow<Book?> {
        return flow {
            val books = oldRepository.subscribeBooksByKey(url, "")
            books.collect { list ->
                emit(list.firstOrNull())
            }
        }
    }
    
    override suspend fun getFavorites(): List<Book> {
        return oldRepository.findAllInLibraryBooks(
            sortType = LibrarySort(LibrarySort.Type.Title, true),
            isAsc = true,
            unreadFilter = false
        )
    }
    
    override fun getFavoritesAsFlow(): Flow<List<Book>> {
        return flow {
            val books = oldRepository.subscribeBooksByKey("", "")
            books.collect { list ->
                emit(list)
            }
        }
    }
    
    override suspend fun getLibraryBooks(): List<LibraryBook> {
        // Return empty list as LibraryBook requires specific database queries
        // This should be implemented properly with actual library book queries
        return emptyList()
    }
    
    override fun getLibraryBooksAsFlow(): Flow<List<LibraryBook>> {
        return flow {
            emit(emptyList())
        }
    }
    
    override suspend fun getDuplicateLibraryBooks(id: Long, title: String): List<Book> {
        // Find books with similar titles
        return oldRepository.findBooksByKey(title)
            .filter { it.id != id && it.favorite }
    }
    
    override suspend fun setBookCategories(bookId: Long, categoryIds: List<Long>) {
        bookCategoryRepository.delete(bookId)
        categoryIds.forEach { categoryId ->
            bookCategoryRepository.insert(
                BookCategory(
                    bookId = bookId,
                    categoryId = categoryId
                )
            )
        }
    }
    
    override suspend fun update(update: BookUpdate): Boolean {
        return try {
            val book = oldRepository.findBookById(update.id) ?: return false
            val updatedBook = applyUpdate(book, update)
            oldRepository.updateBook(updatedBook)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateAll(updates: List<BookUpdate>): Boolean {
        return try {
            val books = updates.mapNotNull { update ->
                val book = oldRepository.findBookById(update.id) ?: return@mapNotNull null
                applyUpdate(book, update)
            }
            oldRepository.updateBook(books)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun insertNetworkBooks(books: List<Book>): List<Book> {
        return try {
            val ids = oldRepository.insertBooks(books)
            books.mapIndexed { index, book ->
                book.copy(id = ids.getOrNull(index) ?: book.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun deleteBooks(bookIds: List<Long>): Boolean {
        return try {
            bookIds.forEach { oldRepository.deleteBookById(it) }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun deleteNotInLibraryBooks(): Boolean {
        return try {
            oldRepository.deleteNotInLibraryBooks()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun applyUpdate(book: Book, update: BookUpdate): Book {
        return book.copy(
            title = update.title ?: book.title,
            author = update.author ?: book.author,
            description = update.description ?: book.description,
            genres = update.genres ?: book.genres,
            status = update.status ?: book.status,
            cover = update.cover ?: book.cover,
            favorite = update.favorite ?: book.favorite,
            lastUpdate = update.lastUpdate ?: book.lastUpdate,
            initialized = update.initialized ?: book.initialized,
            viewer = update.viewer ?: book.viewer,
            flags = update.flags ?: book.flags
        )
    }
}
