package org.ireader.domain.use_cases.local.book_usecases

import androidx.paging.PagingSource
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

