package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.remote.SyncedBook

/**
 * Use case for syncing books to remote backend
 */
class SyncBooksUseCase(
    private val remoteRepository: RemoteRepository
) {
    
    suspend operator fun invoke(userId: String, books: List<Book>): Result<Unit> {
        return try {
            // Filter: Only favorite books AND not local books
            val favoriteBooks = books.filter { book ->
                book.favorite && !isLocalBook(book)
            }
            
            val syncedBooks = favoriteBooks.map { book ->
                SyncedBook(
                    userId = userId,
                    bookId = "${book.sourceId}-${book.id}",
                    lastRead = System.currentTimeMillis(),
                    title = book.title,
                    bookUrl = book.key,
                    sourceId = book.sourceId,
                )
            }
            
            // Sync each book
            syncedBooks.forEach { syncedBook ->
                remoteRepository.syncBook(syncedBook).getOrThrow()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a book from remote sync
     */
    suspend fun unsyncBook(userId: String, book: Book): Result<Unit> {
        return try {
            val bookId = "${book.sourceId}-${book.id}"
            remoteRepository.deleteSyncedBook(userId, bookId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a book is a local book (should not be synced)
     */
    private fun isLocalBook(book: Book): Boolean {
        // Local books typically have sourceId of 0 or negative values
        // Or check if the source is the local catalog
        return book.sourceId <= 0
    }
    
    suspend fun syncSingleBook(userId: String, book: Book): Result<Unit> {
        return try {
            // Don't sync local books
            if (isLocalBook(book)) {
                return Result.success(Unit)
            }
            
            // If book is favorite, sync it
            if (book.favorite) {
                val syncedBook = SyncedBook(
                    userId = userId,
                    bookId = "${book.sourceId}-${book.id}",
                    sourceId = book.sourceId,
                    title = book.title,
                    bookUrl = book.key,
                    lastRead = System.currentTimeMillis()
                )
                
                remoteRepository.syncBook(syncedBook)
            } else {
                // If book is not favorite anymore, remove from sync
                unsyncBook(userId, book)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
