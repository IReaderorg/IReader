package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository

/**
 * Delete All Chapters that inLibrary is false from database
 */
class DeleteNotInLibraryChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteNotInLibraryChapters()
    }
}

