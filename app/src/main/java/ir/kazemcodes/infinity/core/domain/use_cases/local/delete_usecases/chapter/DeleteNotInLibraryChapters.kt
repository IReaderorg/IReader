package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter

import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters that inLibrary is false from database
 */
class DeleteNotInLibraryChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteNotInLibraryChapters()
    }
}

