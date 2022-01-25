package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository

class DeleteChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        return localChapterRepository.deleteChapters(chapters)
    }
}