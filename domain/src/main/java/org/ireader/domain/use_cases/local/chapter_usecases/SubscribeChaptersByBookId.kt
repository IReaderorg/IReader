package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
        query: String = "",
    ): Flow<List<Chapter>> = flow {
        localChapterRepository.subscribeChaptersByBookId(bookId = bookId, isAsc).collect { books ->
            emit(books.filter { it.title.contains(query, ignoreCase = true) })
        }

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
