package ireader.domain.use_cases.local.chapter_usecases

import ireader.common.data.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Chapter
import org.koin.core.annotation.Factory

/**
 * Get latest read chapter
 *  * note: if nothing is found it return a resource of error
 */
@Factory
class SubscribeLastReadChapter(private val chapterRepository: ChapterRepository) {
    operator fun invoke(
        bookId: Long?,
    ): Flow<Chapter?>? {
        return bookId?.let { chapterRepository.subscribeLastReadChapter(it) }
    }
}
@Factory
class FindLastReadChapter(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
    ): Chapter? {
        return chapterRepository.findLastReadChapter(bookId)
    }
}
@Factory
class SubscribeFirstChapter(private val chapterRepository: ChapterRepository) {
    operator fun invoke(
        bookId: Long,
    ): Flow<Chapter?> {
        return chapterRepository.subscribeFirstChapter(bookId)
    }
}
@Factory
class FindFirstChapter  constructor(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
    ): Chapter? {
        return chapterRepository.findFirstChapter(bookId)
    }
}
