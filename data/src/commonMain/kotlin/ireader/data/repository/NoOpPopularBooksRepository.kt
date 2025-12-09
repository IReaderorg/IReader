package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook

/**
 * No-op implementation of PopularBooksRepository.
 * Used when Supabase is not configured.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpPopularBooksRepository : NoOpRepositoryBase(), PopularBooksRepository {
    
    override suspend fun getPopularBooks(limit: Int): Result<List<PopularBook>> =
        emptyListResult()
}
