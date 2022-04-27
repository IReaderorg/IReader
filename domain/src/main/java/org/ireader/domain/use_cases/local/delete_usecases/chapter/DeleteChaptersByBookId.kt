package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

/**
 * Delete All Chapters that is have a bookId
 */
class DeleteChaptersByBookId @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return withIOContext {
            return@withIOContext localChapterRepository.deleteChaptersByBookId(bookId)
        }
    }
}

class UpdateChaptersUseCase @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(bookId: Long, chapters: List<Chapter>) {
        return withIOContext {
            return@withIOContext localChapterRepository.updateChapters(bookId,
                chapters.map { it.copy(bookId = bookId) })
        }
    }
}






