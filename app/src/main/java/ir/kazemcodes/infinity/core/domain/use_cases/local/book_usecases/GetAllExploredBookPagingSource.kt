package ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Constants
import kotlinx.coroutines.flow.Flow

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
            config = PagingConfig(pageSize = Constants.DEFAULT_BIG_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getAllExploreBookPagingSource()
            }
        ).flow
    }
}
