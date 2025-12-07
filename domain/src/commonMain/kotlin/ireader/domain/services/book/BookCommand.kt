package ireader.domain.services.book

/**
 * Sealed class representing all commands that can be dispatched to the Book Controller.
 * All book operations are expressed as commands for predictable state management.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
sealed class BookCommand {
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and subscribe to reactive updates from the database.
     * Requirements: 2.1
     */
    data class LoadBook(val bookId: Long) : BookCommand()
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     * Requirements: 2.5
     */
    object Cleanup : BookCommand()
    
    // ========== Progress Commands ==========
    
    /**
     * Update reading progress for the current book.
     * Requirements: 2.3
     */
    data class UpdateReadingProgress(
        val chapterId: Long,
        val progress: Float = 0f
    ) : BookCommand()
    
    // ========== Book Operations ==========
    
    /**
     * Toggle the favorite status of the current book.
     * Requirements: 2.4
     */
    object ToggleFavorite : BookCommand()
    
    /**
     * Set the category for the current book.
     */
    data class SetCategory(val categoryId: Long) : BookCommand()
    
    /**
     * Update book metadata (title, author, description).
     */
    data class UpdateMetadata(
        val title: String? = null,
        val author: String? = null,
        val description: String? = null
    ) : BookCommand()
    
    /**
     * Refresh book data from the source.
     */
    object RefreshFromSource : BookCommand()
}
