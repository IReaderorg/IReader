package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.data.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Chapter


/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId(private val chapterRepository: ChapterRepository) {
    operator fun invoke(
        bookId: Long,
        sort: String = "default",
    ): Flow<List<Chapter>> {
        return chapterRepository.subscribeChaptersByBookId(bookId = bookId, sort)
    }
}

class FindChaptersByBookId(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
    ): List<Chapter> {
        return chapterRepository.findChaptersByBookId(bookId = bookId)
    }
}
