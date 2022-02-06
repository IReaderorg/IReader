package ir.kazemcodes.infinity.core.domain.use_cases.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreRemoteMediator
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow

class GetRemoteBooksByRemoteMediator(private val remoteRepository: RemoteRepository, private val database: BookDatabase) {
    @ExperimentalPagingApi
    operator fun invoke(
        source: Source,
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
