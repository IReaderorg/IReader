package ireader.domain.use_cases.local.chapter_usecases

import ireader.common.data.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import ireader.common.models.entities.Chapter
import ireader.common.resources.LAST_CHAPTER


/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChapterById(private val chapterRepository: ChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return chapterRepository.subscribeChapterById(chapterId = chapterId)
            .distinctUntilChanged()
    }
}

class FindChapterById(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long? = null,
    ): Chapter? {
        return if (chapterId != LAST_CHAPTER) {
            chapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.findLastReadChapter(bookId)
        } else {
            chapterRepository.findChapterById(chapterId)
        }
    }
}

class FindAllInLibraryChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return chapterRepository.findAllInLibraryChapter()
    }
}
