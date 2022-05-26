package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
    ): Flow<List<Chapter>>{
        return localChapterRepository.subscribeChaptersByBookId(bookId = bookId, isAsc)
    }
}

class FindChaptersByBookId @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
    ): List<Chapter> {
        return localChapterRepository.findChaptersByBookId(bookId = bookId, isAsc)
    }
}
