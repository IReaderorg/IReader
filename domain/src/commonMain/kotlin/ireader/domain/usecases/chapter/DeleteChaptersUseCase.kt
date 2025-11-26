package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter

/**
 * Use case for deleting chapters
 */
class DeleteChaptersUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Delete a single chapter
     */
    suspend operator fun invoke(chapter: Chapter) {
        chapterRepository.deleteChapter(chapter)
    }
    
    /**
     * Delete multiple chapters
     */
    suspend operator fun invoke(chapters: List<Chapter>) {
        chapterRepository.deleteChapters(chapters)
    }
    
    /**
     * Delete all chapters for a book
     */
    suspend fun deleteByBookId(bookId: Long) {
        chapterRepository.deleteChaptersByBookId(bookId)
    }
    
    /**
     * Delete chapter by ID
     */
    suspend fun deleteById(chapterId: Long) {
        val chapter = chapterRepository.findChapterById(chapterId)
        if (chapter != null) {
            invoke(chapter)
        }
    }
}
