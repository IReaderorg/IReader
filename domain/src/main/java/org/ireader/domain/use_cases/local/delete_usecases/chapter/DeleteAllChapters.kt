package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteAllChapters()
    }
}