package ireader.domain.data.repository

import androidx.paging.PagingSource
import ireader.domain.models.entities.HistoryWithRelations

interface PaginationRepository {
    fun findHistoriesPaging(query: String):
            PagingSource<Long, HistoryWithRelations>
}