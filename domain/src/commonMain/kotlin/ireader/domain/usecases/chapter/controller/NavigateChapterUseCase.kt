package ireader.domain.usecases.chapter.controller

import ireader.domain.data.repository.ChapterRepository

/**
 * Use case interface for chapter navigation.
 * Provides methods to find next and previous chapters in sequence.
 */
interface NavigateChapterUseCase {
    /**
     * Get the ID of the next chapter in sequence.
     *
     * @param bookId The unique identifier of the book
     * @param currentChapterId The ID of the current chapter
     * @return The ID of the next chapter, or null if at the last chapter
     */
    suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long?

    /**
     * Get the ID of the previous chapter in sequence.
     *
     * @param bookId The unique identifier of the book
     * @param currentChapterId The ID of the current chapter
     * @return The ID of the previous chapter, or null if at the first chapter
     */
    suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long?
}

/**
 * Default implementation of [NavigateChapterUseCase].
 * Uses chapter number ordering to determine sequence.
 */
class NavigateChapterUseCaseImpl(
    private val chapterRepository: ChapterRepository
) : NavigateChapterUseCase {

    override suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long? {
        val chapters = chapterRepository.findChaptersByBookId(bookId)
            .sortedBy { it.number }
        
        if (chapters.isEmpty()) return null
        
        val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
        if (currentIndex == -1) return null
        
        // Return next chapter if not at the end
        return if (currentIndex < chapters.lastIndex) {
            chapters[currentIndex + 1].id
        } else {
            null
        }
    }

    override suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long? {
        val chapters = chapterRepository.findChaptersByBookId(bookId)
            .sortedBy { it.number }
        
        if (chapters.isEmpty()) return null
        
        val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
        if (currentIndex == -1) return null
        
        // Return previous chapter if not at the beginning
        return if (currentIndex > 0) {
            chapters[currentIndex - 1].id
        } else {
            null
        }
    }
}
