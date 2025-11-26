package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter

/**
 * Use case for updating chapter read status
 */
class UpdateChapterReadStatusUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Mark a chapter as read or unread
     */
    suspend operator fun invoke(chapterId: Long, isRead: Boolean) {
        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        
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
     */
    suspend fun markAllAsRead(bookId: Long) {
        val chapters = chapterRepository.findChaptersByBookId(bookId)
        val updatedChapters = chapters.map { chapter ->
            chapter.copy(
                read = true
            )
        }
        chapterRepository.insertChapters(updatedChapters)
    }
    
    /**
     * Mark all chapters of a book as unread
     */
    suspend fun markAllAsUnread(bookId: Long) {
        val chapters = chapterRepository.findChaptersByBookId(bookId)
        val updatedChapters = chapters.map { chapter ->
            chapter.copy(
                read = false
            )
        }
        chapterRepository.insertChapters(updatedChapters)
    }
}
