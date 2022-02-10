package org.ireader.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

class GetAllExploredBookPagingSource(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
    ): PagingSource<Int, Book> {
        return localBookRepository.getAllExploreBookPagingSource()
    }
}

class GetAllExploredBookPagingData(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getAllExploreBookPagingSource()
            }
        ).flow
    }
}
