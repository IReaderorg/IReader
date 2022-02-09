package org.ireader.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.Book
import org.ireader.infinity.core.domain.repository.LocalBookRepository

/**
 * get a paging data that is used in library screen
 */
class GetBooksByQueryByPagination(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(query: String): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getBooksByQueryPagingSource(query)
            }
        ).flow
    }
}