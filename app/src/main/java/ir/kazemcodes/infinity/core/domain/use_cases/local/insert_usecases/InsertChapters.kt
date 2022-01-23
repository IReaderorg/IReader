package ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository

class InsertChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke( chapters: List<Chapter>) {
        localChapterRepository.insertChapters(chapters)
    }
}