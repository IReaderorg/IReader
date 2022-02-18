package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return localChapterRepository.deleteChaptersByBookId(bookId)
    }
}






