package ireader.domain.usecases.local.chapter_usecases

import ireader.common.models.entities.Chapter
import ireader.domain.data.repository.ChapterRepository
import ireader.i18n.LAST_CHAPTER



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

class FindAllInLibraryChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return chapterRepository.findAllInLibraryChapter()
    }
}
