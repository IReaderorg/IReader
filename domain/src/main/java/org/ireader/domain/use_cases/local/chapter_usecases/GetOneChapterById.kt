package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class GetOneChapterById(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.findOneChapterById(chapterId = chapterId)
    }
}