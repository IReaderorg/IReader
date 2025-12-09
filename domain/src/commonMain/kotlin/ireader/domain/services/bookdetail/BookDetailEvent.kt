package ireader.domain.services.bookdetail

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Sealed class representing one-time events emitted by the BookDetail Controller.
 * These events are used for UI feedback and should be consumed once.
 * 
 * Requirements: 3.4, 3.5, 4.2, 4.3
 */
sealed class BookDetailEvent {
    /**
     * An error occurred during a book detail operation.
     * Requirements: 4.2, 4.3
     */
    data class Error(val error: BookDetailError) : BookDetailEvent()
    
    /**
     * Book was successfully loaded.
     */
    data class BookLoaded(val book: Book) : BookDetailEvent()
    
    /**
     * Chapters were successfully loaded.
     */
    data class ChaptersLoaded(val count: Int) : BookDetailEvent()
    
    /**
     * Navigate to reader with specific book and chapter.
     */
    data class NavigateToReader(val bookId: Long, val chapterId: Long) : BookDetailEvent()
    
    /**
     * Navigate to web view for a URL.
     */
    data class NavigateToWebView(val url: String, val sourceId: Long, val bookId: Long) : BookDetailEvent()
    
    /**
     * Navigate back from book detail screen.
     */
    object NavigateBack : BookDetailEvent()
    
    /**
     * Book was refreshed from source.
     */
    object BookRefreshed : BookDetailEvent()
    
    /**
     * Chapters were refreshed from source.
     */
    data class ChaptersRefreshed(val newCount: Int, val totalCount: Int) : BookDetailEvent()
    
    /**
     * Selection state changed.
     */
    data class SelectionChanged(val selectedCount: Int) : BookDetailEvent()
    
    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : BookDetailEvent()
}
