package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete All Chapters that inLibrary is false from database
 */
class DeleteNotInLibraryChapters @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke() {
        return localChapterRepository.deleteNotInLibraryChapters()
    }
}

