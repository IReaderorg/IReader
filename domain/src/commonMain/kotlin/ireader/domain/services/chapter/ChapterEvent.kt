package ireader.domain.services.chapter

import ireader.domain.models.entities.Chapter

/**
 * Sealed class representing one-time events emitted by the Chapter Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class ChapterEvent {
    /**
     * An error occurred during a chapter operation.
     */
    data class Error(val error: ChapterError) : ChapterEvent()
    
    /**
     * A chapter was successfully loaded with content.
     */
    data class ChapterLoaded(val chapter: Chapter) : ChapterEvent()
    
    /**
     * The current chapter has been completed (reached the end).
     */
    object ChapterCompleted : ChapterEvent()
    
    /**
     * Reading progress was successfully saved to the database.
     */
    data class ProgressSaved(val chapterId: Long) : ChapterEvent()
    
    /**
     * Chapter content was successfully fetched from remote source.
     */
    data class ContentFetched(val chapterId: Long) : ChapterEvent()
}
