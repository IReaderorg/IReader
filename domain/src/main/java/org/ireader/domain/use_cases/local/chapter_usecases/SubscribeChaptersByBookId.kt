package org.ireader.domain.use_cases.local.chapter_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
    ): Flow<List<Chapter>> = flow {
        try {
            localChapterRepository.subscribeChaptersByBookId(bookId = bookId, isAsc)
                .first { chapters ->
                    emit(chapters)
                    true
                }
        } catch (e: Exception) {
        }
    }
}

class FindChaptersByBookId @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
        isAsc: Boolean = true,
    ): List<Chapter> {
        return localChapterRepository.findChaptersByBookId(bookId = bookId, isAsc)
    }
}


class GetLocalChaptersByPaging @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Long,
        isAsc: Boolean,
        query: String = "",
    ): Flow<PagingData<Chapter>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_BIG_PAGE_SIZE,
                maxSize = Constants.MAX_BIG_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localChapterRepository.findLocalChaptersByPaging(bookId = bookId,
                    isAsc = isAsc,
                    query)
            }
        ).flow
    }
}