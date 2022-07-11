package org.ireader.domain.use_cases.local.delete_usecases.chapter

import javax.inject.Inject

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext chapterRepository.deleteChaptersByBookId(bookId)
        }
    }
}
