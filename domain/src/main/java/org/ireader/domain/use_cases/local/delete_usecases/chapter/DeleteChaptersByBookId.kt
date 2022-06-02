package org.ireader.domain.use_cases.local.delete_usecases.chapter

import javax.inject.Inject

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localChapterRepository.deleteChaptersByBookId(bookId)
        }
    }
}

