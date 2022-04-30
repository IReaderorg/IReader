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
class SubscribeChapterById @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.subscribeChapterById(chapterId = chapterId)
            .distinctUntilChanged()
    }
}

class FindChapterById @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long? = null,
    ): Chapter? {
        return if (chapterId != LAST_CHAPTER) {
            localChapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            localChapterRepository.findLastReadChapter(bookId)
        } else null
    }
}

class FindChapterByIdByBatch @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(
        chapterIds: List<Long>,
    ): List<Chapter> {
        return localChapterRepository.findChapterByIdByBatch(chapterId = chapterIds)
    }
}

class FindAllInLibraryChapters @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return localChapterRepository.findAllInLibraryChapter()
    }
}
