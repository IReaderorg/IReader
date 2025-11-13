package ireader.domain.usecases.sync

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.usecases.local.DeleteUseCase
import ireader.core.log.Log
import java.util.Calendar

/**
 * Use case for toggling a book in/out of library with automatic sync
 * Follows clean architecture by encapsulating business logic
 */
class ToggleBookInLibraryUseCase(
    private val bookRepository: BookRepository,
    private val deleteUseCase: DeleteUseCase,
    private val syncBookToRemoteUseCase: SyncBookToRemoteUseCase
) {
    
    suspend operator fun invoke(book: Book): Result<Book> {
        return try {
            val updatedBook = if (!book.favorite) {
                // Add to library
                val newBook = book.copy(
                    favorite = true,
                    dateAdded = Calendar.getInstance().timeInMillis,
                )
                bookRepository.updateBook(newBook)
                
                // Sync to remote
                syncBookToRemoteUseCase(newBook)
                
                newBook
            } else {
                // Remove from library
                deleteUseCase.unFavoriteBook(listOf(book.id))
                
                // Sync removal to remote (handled in UnFavoriteBook use case)
                
                book.copy(favorite = false)
            }
            
            Result.success(updatedBook)
        } catch (e: Exception) {
            Log.error(e, "Failed to toggle book in library: ${book.title}")
            Result.failure(e)
        }
    }
}
