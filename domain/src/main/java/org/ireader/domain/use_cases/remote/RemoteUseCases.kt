package org.ireader.domain.use_cases.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteRepository
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing

data class RemoteUseCases(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteBookByPaginationUseCase: GetRemoteBookByPaginationUseCase,
    val getRemoteChapters: GetRemoteChapters,
)

class GetRemoteBookByPaginationUseCase(private val remoteRepository: RemoteRepository) {
    @OptIn(ExperimentalPagingApi::class)
    operator fun invoke(
        source: CatalogSource,
        listing: Listing?,
        filters: List<Filter<*>>?,
        query: String?,
    ): Flow<PagingData<Book>> {
        return remoteRepository.getRemoteBooksByRemoteMediator(source, listing, filters, query)
    }
}








