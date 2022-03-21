package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChapterById @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Chapter?> {
        return localChapterRepository.subscribeChapterById(chapterId = chapterId)
    }
}

class FindChapterById @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long?,
        lastRead: Boolean = false,
    ): Chapter? {
        return if (!lastRead) {
            localChapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            return localChapterRepository.findLastReadChapter(bookId)
        } else null
    }
}

class FindAllInLibraryChapters @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(
    ): List<Chapter> {
        return localChapterRepository.findAllInLibraryChapter()
    }
}