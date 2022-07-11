package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.LAST_CHAPTER
import javax.inject.Inject

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChapterById @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return chapterRepository.subscribeChapterById(chapterId = chapterId)
            .distinctUntilChanged()
    }
}

class FindChapterById @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long? = null,
    ): Chapter? {
        return if (chapterId != LAST_CHAPTER) {
            chapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.findLastReadChapter(bookId)
        } else null
    }
}

class FindAllInLibraryChapters @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return chapterRepository.findAllInLibraryChapter()
    }
}
