package ireader.domain.usecases.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ireader.domain.data.repository.PaginationRepository
import ireader.domain.models.entities.HistoryWithRelations
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class HistoryPagingUseCase(
    val paginationRepository: PaginationRepository
) {

    fun findHistoriesPaging(query: String): Flow<PagingData<HistoryWithRelations>> {
        return Pager(
            PagingConfig(pageSize = 25),
        ) {
            paginationRepository.findHistoriesPaging(query)
        }.flow
    }
}