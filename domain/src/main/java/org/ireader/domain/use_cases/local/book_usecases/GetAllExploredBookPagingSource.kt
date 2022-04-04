package org.ireader.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class GetAllExploredBookPagingSource @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
    ): PagingSource<Int, Book> {
        return localBookRepository.getAllExploreBookPagingSource()
    }
}

class GetAllExploredBookPagingData @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE,
        maxSize: Int = Constants.MAX_PAGE_SIZE,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize,
                maxSize = maxSize, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getAllExploreBookPagingSource()
            }
        ).flow
    }
}
