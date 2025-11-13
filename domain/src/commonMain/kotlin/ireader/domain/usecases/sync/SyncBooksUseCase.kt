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
            // Only sync favorite books
            val favoriteBooks = books.filter { it.favorite }
            
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
    
    suspend fun syncSingleBook(userId: String, book: Book): Result<Unit> {
        return try {
            // Only sync if book is favorite
            if (!book.favorite) {
                return Result.success(Unit)
            }
            
            val syncedBook = SyncedBook(
                userId = userId,
                bookId = "${book.sourceId}-${book.id}",
                sourceId = book.sourceId,
                title = book.title,
                bookUrl = book.key,
                lastRead = System.currentTimeMillis()
            )
            
            remoteRepository.syncBook(syncedBook)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
