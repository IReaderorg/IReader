package ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType

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

