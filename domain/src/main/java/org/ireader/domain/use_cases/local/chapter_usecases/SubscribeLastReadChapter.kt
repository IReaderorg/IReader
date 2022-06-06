package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

/**
 * Get latest read chapter
 *  * note: if nothing is found it return a resource of error
 */
class SubscribeLastReadChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    operator fun invoke(
        bookId: Long?,
    ): Flow<Chapter?>? {
        return bookId?.let { chapterRepository.subscribeLastReadChapter(it) }
    }
}

    class FindLastReadChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
        suspend operator fun invoke(
            bookId: Long,
        ): Chapter? {
            return chapterRepository.findLastReadChapter(bookId)
        }
    }

    class SubscribeFirstChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
        operator fun invoke(
            bookId: Long,
        ): Flow<Chapter?> {
            return chapterRepository.subscribeFirstChapter(bookId)
        }
    }

    class FindFirstChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
        suspend operator fun invoke(
            bookId: Long,
        ): Chapter? {
            return chapterRepository.findFirstChapter(bookId)
        }
    }
