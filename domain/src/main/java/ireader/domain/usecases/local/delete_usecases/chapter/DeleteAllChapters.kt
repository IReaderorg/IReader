package ireader.domain.usecases.local.delete_usecases.chapter

import ireader.domain.data.repository.ChapterRepository

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke() {
        return chapterRepository.deleteAllChapters()
    }
}
