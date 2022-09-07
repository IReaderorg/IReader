package ireader.domain.use_cases.local.delete_usecases.chapter

import ireader.common.data.repository.ChapterRepository

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke() {
        return chapterRepository.deleteAllChapters()
    }
}
