package org.ireader.data.repository.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.data.local.AppDatabase
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteRepository
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing

class GetRemoteBooksByRemoteMediator(
    private val remoteRepository: RemoteRepository,
    private val database: AppDatabase,
) {
    @ExperimentalPagingApi
    operator fun invoke(
        source: CatalogSource,
        listing: Listing?,
        filters: List<Filter<*>>?,
        query: String?,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE),
            pagingSourceFactory = {
                remoteRepository.getAllExploreBookByPaging(source, listing, filters, query)
            },
            remoteMediator = ExploreRemoteMediator(
                source = source,
                database = database,
                listing = listing,
                query = query,
                filters = filters
            ),
        ).flow
    }
}
