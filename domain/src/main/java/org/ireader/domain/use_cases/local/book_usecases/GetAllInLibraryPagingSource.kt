package org.ireader.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository


class GetAllInLibraryPagingSource(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        return localBookRepository.getAllInLibraryPagingSource(
            sortType = sortType,
            isAsc = isAsc,
            unreadFilter = unreadFilter,
        )
    }
}

class GetAllInDownloadsPagingData(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getAllInDownloadPagingSource(
                    sortType = sortType,
                    isAsc = isAsc,
                    unreadFilter = unreadFilter,
                )
            }
        ).flow
    }
}

