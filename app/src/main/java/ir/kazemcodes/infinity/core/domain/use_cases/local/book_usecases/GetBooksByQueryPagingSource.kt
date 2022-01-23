package ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository

class GetBooksByQueryPagingSource(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        query: String,
    ): PagingSource<Int, Book> {
        return localBookRepository.getBooksByQueryPagingSource(
            query = query
        )
    }
}