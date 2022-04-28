package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_data.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteAllChapters()
    }
}
