package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

class InsertChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return org.ireader.common_extensions.withIOContext {
            chapterRepository.insertChapter(chapter)
        }
    }
}
