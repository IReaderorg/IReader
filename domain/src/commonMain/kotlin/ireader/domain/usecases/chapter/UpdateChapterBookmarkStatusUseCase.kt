package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter

/**
 * Use case for updating chapter bookmark status
 */
class UpdateChapterBookmarkStatusUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Bookmark or unbookmark a chapter
     */
    suspend operator fun invoke(chapterId: Long, isBookmarked: Boolean) {
        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        
        val updatedChapter = chapter.copy(
            bookmark = isBookmarked
        )
        
        chapterRepository.insertChapter(updatedChapter)
    }
    
    /**
     * Toggle bookmark status
     */
    suspend fun toggle(chapterId: Long) {
        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        invoke(chapterId, !chapter.bookmark)
    }
    
    /**
     * Get all bookmarked chapters for a book
     */
    suspend fun getBookmarkedChapters(bookId: Long): List<Chapter> {
        val chapters = chapterRepository.findChaptersByBookId(bookId)
        return chapters.filter { it.bookmark }
    }
}
