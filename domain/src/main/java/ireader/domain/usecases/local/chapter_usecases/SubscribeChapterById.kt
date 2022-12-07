package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.models.entities.Chapter
import ireader.domain.data.repository.ChapterRepository
import ireader.i18n.LAST_CHAPTER
import kotlinx.coroutines.flow.emptyFlow


class FindChapterById(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long?,
        bookId: Long? = null,
    ): Chapter? {
        if (chapterId == null) return null
        return if (chapterId != LAST_CHAPTER) {
            chapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.findLastReadChapter(bookId)
        } else {
            chapterRepository.findChapterById(chapterId)
        }
    }
}

class SubscribeChapterById(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
            chapterId: Long?,
            bookId: Long? = null,
    ): kotlinx.coroutines.flow.Flow<Chapter?> {
        if (chapterId == null) return emptyFlow()
        return if (chapterId != LAST_CHAPTER) {
            chapterRepository.subscribeChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.subscribeLastReadChapter(bookId)
        } else {
            chapterRepository.subscribeChapterById(chapterId)
        }
    }
}

class FindAllInLibraryChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return chapterRepository.findAllInLibraryChapter()
    }
}
