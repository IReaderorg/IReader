package org.ireader.data.repository.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.data.local.AppDatabase
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteRepository
import tachiyomi.source.CatalogSource

class GetRemoteBooksByRemoteMediator(
    private val remoteRepository: RemoteRepository,
    private val database: AppDatabase,
) {
    @ExperimentalPagingApi
    operator fun invoke(
        source: CatalogSource,
        exploreType: ExploreType,
        query: String?,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE),
            pagingSourceFactory = {
                remoteRepository.getAllExploreBookByPaging(source, exploreType, query)
            },
            remoteMediator = ExploreRemoteMediator(
                source = source,
                database = database,
                exploreType = exploreType,
                query = query
            ),
        ).flow
    }
}
