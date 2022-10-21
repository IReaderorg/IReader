package ireader.domain.usecases.local.delete_usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.common.models.entities.Chapter


class DeleteChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        return chapterRepository.deleteChapters(chapters)
    }
}
