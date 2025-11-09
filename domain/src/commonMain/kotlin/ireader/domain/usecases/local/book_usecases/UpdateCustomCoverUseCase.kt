package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository

/**
 * Use case to update custom cover for a book
 */
class UpdateCustomCoverUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Update custom cover URL for a book
     * @param bookId The ID of the book
     * @param customCoverUrl The URL or file path of the custom cover
     * @return Result indicating success or failure
     */
    suspend fun updateCustomCover(bookId: Long, customCoverUrl: String): Result<Unit> {
        return try {
            val book = bookRepository.findBookById(bookId)
            if (book != null) {
                val updatedBook = book.copy(customCover = customCoverUrl)
                bookRepository.updateBook(updatedBook)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Book not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset custom cover to original cover
     * @param bookId The ID of the book
     * @return Result indicating success or failure
     */
    suspend fun resetCustomCover(bookId: Long): Result<Unit> {
        return try {
            val book = bookRepository.findBookById(bookId)
            if (book != null) {
                val updatedBook = book.copy(customCover = book.cover)
                bookRepository.updateBook(updatedBook)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Book not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
