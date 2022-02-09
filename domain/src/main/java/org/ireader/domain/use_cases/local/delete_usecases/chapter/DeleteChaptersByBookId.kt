package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(bookId: Int) {
        return localChapterRepository.deleteChaptersByBookId(bookId)
    }
}







