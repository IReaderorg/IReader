package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.library.LibrarySort
import javax.inject.Inject

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    operator fun invoke(
        bookId: Long,
        sort: String = "default",
    ): Flow<List<Chapter>>{
        return chapterRepository.subscribeChaptersByBookId(bookId = bookId, sort)
    }
}

class FindChaptersByBookId @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
    ): List<Chapter> {
        return chapterRepository.findChaptersByBookId(bookId = bookId, isAsc)
    }
}
