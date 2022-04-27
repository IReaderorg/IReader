package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertChapters @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>): List<Long> {
        return withIOContext {
            return@withIOContext localChapterRepository.insertChapters(chapters)
        }
    }
}