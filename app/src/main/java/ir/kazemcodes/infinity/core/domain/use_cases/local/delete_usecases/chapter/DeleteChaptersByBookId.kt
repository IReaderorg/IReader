package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter

import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(bookId: Int) {
        return localChapterRepository.deleteChaptersByBookId(bookId)
    }
}







