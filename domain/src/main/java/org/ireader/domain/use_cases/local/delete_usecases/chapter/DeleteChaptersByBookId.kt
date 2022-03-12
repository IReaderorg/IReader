package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return localChapterRepository.deleteChaptersByBookId(bookId)
    }
}






