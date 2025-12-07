package ireader.domain.services.library

/**
 * Sealed class representing one-time events emitted by the Library Controller.
 * These events are used for UI feedback and should be consumed once.
 * 
 * Requirements: 5.1, 5.4
 */
sealed class LibraryEvent {
    /**
     * An error occurred during a library operation.
     */
    data class Error(val error: LibraryError) : LibraryEvent()
    
    /**
     * Library was successfully loaded.
     */
    object LibraryLoaded : LibraryEvent()
    
    /**
     * Library refresh completed successfully.
     */
    object RefreshCompleted : LibraryEvent()
    
    /**
     * Selection was changed.
     */
    data class SelectionChanged(val count: Int) : LibraryEvent()
    
    /**
     * Filter was changed.
     */
    data class FilterChanged(val filter: LibraryFilter) : LibraryEvent()
    
    /**
     * Sort was changed.
     */
    data class SortChanged(val sort: LibrarySort) : LibraryEvent()
}
