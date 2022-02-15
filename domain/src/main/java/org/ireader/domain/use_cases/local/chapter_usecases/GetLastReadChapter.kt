package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository

/**
 * Get latest read chapter
 *  * note: if nothing is found it return a resource of error
 */
class GetLastReadChapter(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.findLastReadChapter(bookId)

    }
}

class FindFirstChapter(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.findFirstChapter(bookId)

    }
}

class SetLastReadToFalse(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
    ) {
        return localChapterRepository.setLastReadToFalse(bookId)

    }
}



