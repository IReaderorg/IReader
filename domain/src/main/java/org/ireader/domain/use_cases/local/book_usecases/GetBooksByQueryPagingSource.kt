package org.ireader.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.PagingSource
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

class GetBooksByQueryPagingSource(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        query: String,
    ): PagingSource<Int, Book> {
        return localBookRepository.getBooksByQueryPagingSource(
            query = query
        )
    }
}