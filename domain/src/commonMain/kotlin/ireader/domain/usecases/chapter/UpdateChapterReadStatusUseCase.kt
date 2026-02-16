package ireader.domain.usecases.chapter

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter

/**
 * Use case for updating chapter read status
 * 
 * IMPORTANT: This use case uses findChapterById for single chapter updates
 * which preserves content. However, markAllAsRead/markAllAsUnread use
 * findChaptersByBookId which uses a LIGHT mapper without content!
 * 
 * This could potentially cause content loss if the repository doesn't
 * handle empty content properly.
 */
class UpdateChapterReadStatusUseCase(
    private val chapterRepository: ChapterRepository
) {
    companion object {
        private const val TAG = "UpdateChapterReadStatus"
    }
    
    /**
     * Mark a chapter as read or unread
     */
    suspend operator fun invoke(chapterId: Long, isRead: Boolean) {
        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        
        Log.debug { 
            "$TAG: invoke - chapterId=$chapterId, isRead=$isRead, hasContent=${chapter.content.isNotEmpty()}"
        }
        
        val updatedChapter = chapter.copy(
            read = isRead
        )
        
        chapterRepository.insertChapter(updatedChapter)
    }
    
    /**
     * Mark multiple chapters as read or unread
     */
    suspend fun updateMultiple(chapterIds: List<Long>, isRead: Boolean) {
        chapterIds.forEach { chapterId ->
            invoke(chapterId, isRead)
        }
    }
    
    /**
     * Mark all chapters of a book as read
     * 
     * WARNING: This uses findChaptersByBookId which returns chapters WITHOUT content.
     * The repository's insertChapters should handle this by not overwriting content.
     */
    suspend fun markAllAsRead(bookId: Long) {
        Log.debug { "$TAG: markAllAsRead - bookId=$bookId" }
        
        val chapters = chapterRepository.findChaptersByBookId(bookId)
        
        Log.warn { 
            "$TAG: markAllAsRead - Fetched ${chapters.size} chapters with LIGHT mapper. " +
            "Chapters with empty content: ${chapters.count { it.content.isEmpty() }}"
        }
        
        val updatedChapters = chapters.map { chapter ->
            chapter.copy(
                read = true
            )
        }
        chapterRepository.insertChapters(updatedChapters)
    }
    
    /**
     * Mark all chapters of a book as unread
     * 
     * WARNING: This uses findChaptersByBookId which returns chapters WITHOUT content.
     * The repository's insertChapters should handle this by not overwriting content.
     */
    suspend fun markAllAsUnread(bookId: Long) {
        Log.debug { "$TAG: markAllAsUnread - bookId=$bookId" }
        
        val chapters = chapterRepository.findChaptersByBookId(bookId)
        
        Log.warn { 
            "$TAG: markAllAsUnread - Fetched ${chapters.size} chapters with LIGHT mapper. " +
            "Chapters with empty content: ${chapters.count { it.content.isEmpty() }}"
        }
        
        val updatedChapters = chapters.map { chapter ->
            chapter.copy(
                read = false
            )
        }
        chapterRepository.insertChapters(updatedChapters)
    }
}
