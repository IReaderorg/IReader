package ireader.domain.services.book

import ireader.domain.models.entities.Book

/**
 * Sealed class representing one-time events emitted by the Book Controller.
 * These events are used for UI feedback and should be consumed once.
 * 
 * Requirements: 5.1, 5.4
 */
sealed class BookEvent {
    /**
     * An error occurred during a book operation.
     */
    data class Error(val error: BookError) : BookEvent()
    
    /**
     * A book was successfully loaded.
     */
    data class BookLoaded(val book: Book) : BookEvent()
    
    /**
     * Book metadata was successfully updated.
     */
    object MetadataUpdated : BookEvent()
    
    /**
     * Reading progress was successfully saved to the database.
     */
    object ProgressSaved : BookEvent()
    
    /**
     * Book favorite status was toggled.
     */
    data class FavoriteToggled(val isFavorite: Boolean) : BookEvent()
    
    /**
     * Book category was updated.
     */
    data class CategoryUpdated(val categoryId: Long) : BookEvent()
    
    /**
     * Book was refreshed from source.
     */
    object RefreshCompleted : BookEvent()
}
