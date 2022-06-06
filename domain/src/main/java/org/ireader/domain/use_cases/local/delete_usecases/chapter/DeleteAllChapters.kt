package org.ireader.domain.use_cases.local.delete_usecases.chapter

import javax.inject.Inject

/**
 * Delete All Chapters from database
 */
class DeleteAllChapters @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke() {
        return chapterRepository.deleteAllChapters()
    }
}
