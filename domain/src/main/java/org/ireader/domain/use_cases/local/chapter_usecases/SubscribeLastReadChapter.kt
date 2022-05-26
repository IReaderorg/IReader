package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

/**
 * Get latest read chapter
 *  * note: if nothing is found it return a resource of error
 */
class SubscribeLastReadChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    operator fun invoke(
        bookId: Long?,
    ): Flow<Chapter?>? {
        return bookId?.let { localChapterRepository.subscribeLastReadChapter(it) }
    }
}

    class FindLastReadChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
        suspend operator fun invoke(
            bookId: Long,
        ): Chapter? {
            return localChapterRepository.findLastReadChapter(bookId)
        }
    }

    class SubscribeFirstChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
        operator fun invoke(
            bookId: Long,
        ): Flow<Chapter?> {
            return localChapterRepository.subscribeFirstChapter(bookId)
        }
    }

    class FindFirstChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
        suspend operator fun invoke(
            bookId: Long,
        ): Chapter? {
            return localChapterRepository.findFirstChapter(bookId)
        }
    }
