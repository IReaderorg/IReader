package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository

/**
 * Use case to archive/unarchive books
 */
class ArchiveBookUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Toggle archive status for a book
     * @param bookId The ID of the book to archive/unarchive
     * @param isArchived Whether the book should be archived
     * @return Result indicating success or failure
     */
    suspend fun toggleArchive(bookId: Long, isArchived: Boolean): Result<Unit> {
        return try {
            bookRepository.updateArchiveStatus(bookId, isArchived)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Archive multiple books
     * @param bookIds List of book IDs to archive
     * @return Result indicating success or failure
     */
    suspend fun archiveBooks(bookIds: List<Long>): Result<Unit> {
        return try {
            bookIds.forEach { bookId ->
                bookRepository.updateArchiveStatus(bookId, true)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Unarchive multiple books
     * @param bookIds List of book IDs to unarchive
     * @return Result indicating success or failure
     */
    suspend fun unarchiveBooks(bookIds: List<Long>): Result<Unit> {
        return try {
            bookIds.forEach { bookId ->
                bookRepository.updateArchiveStatus(bookId, false)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
