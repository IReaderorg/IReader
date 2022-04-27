package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return withIOContext {
            localChapterRepository.insertChapter(chapter)
        }
    }
}

