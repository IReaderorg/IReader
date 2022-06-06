package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

class InsertChapters @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>): List<Long> {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext chapterRepository.insertChapters(chapters)
        }
    }
}
