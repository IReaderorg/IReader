package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChapterById(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.subscribeChapterById(chapterId = chapterId)
    }
}

class FindChapterById(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long,
    ): Chapter? {
        return localChapterRepository.findChapterById(chapterId = chapterId)
    }
}