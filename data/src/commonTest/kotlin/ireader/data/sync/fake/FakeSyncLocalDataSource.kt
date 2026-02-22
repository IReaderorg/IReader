package ireader.data.sync.fake

import ireader.domain.models.sync.SyncableBook

/**
 * Fake implementation of local data source for testing.
 * Simulates local database operations.
 */
class FakeSyncLocalDataSource {
    
    private val books = mutableListOf<SyncableBook>()
    
    fun getBooks(): List<SyncableBook> = books.toList()
    
    fun setBooks(newBooks: List<SyncableBook>) {
        books.clear()
        books.addAll(newBooks)
    }
    
    fun addBook(book: SyncableBook) {
        books.add(book)
    }
    
    fun updateBook(book: SyncableBook) {
        val index = books.indexOfFirst { it.id == book.id }
        if (index >= 0) {
            books[index] = book
        }
    }
    
    fun deleteBook(bookId: Long) {
        books.removeAll { it.id == bookId }
    }
    
    fun clear() {
        books.clear()
    }
}
