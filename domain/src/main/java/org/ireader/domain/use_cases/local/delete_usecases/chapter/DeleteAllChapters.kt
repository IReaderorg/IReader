package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteAllChapters()
    }
}