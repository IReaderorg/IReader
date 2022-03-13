package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

class InsertChapter @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        localChapterRepository.insertChapter(chapter)
    }
}

