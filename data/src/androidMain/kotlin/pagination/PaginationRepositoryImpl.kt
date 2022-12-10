package ireader.data.pagination

import androidx.paging.PagingSource
import ireader.data.core.AndroidDatabaseHandler
import ireader.data.core.DatabaseHandler
import ireader.data.history.historyWithRelationsMapper
import ireader.domain.data.repository.PaginationRepository
import ireader.domain.models.entities.HistoryWithRelations

class PaginationRepositoryImpl(
    private val handler: AndroidDatabaseHandler
) : PaginationRepository {
    override fun findHistoriesPaging(query: String): PagingSource<Long, HistoryWithRelations> {
        return handler
            .subscribeToPagingSource(
                countQuery = { historyViewQueries.countHistory(query) },
                queryProvider = { limit, offset ->
                    historyViewQueries.history(query, limit, offset, historyWithRelationsMapper)
                },
            )
    }
}