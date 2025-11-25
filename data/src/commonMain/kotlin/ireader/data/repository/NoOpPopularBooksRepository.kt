package ireader.data.repository

import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook

/**
 * No-op implementation of PopularBooksRepository
 * Used when Supabase is not configured
 */
class NoOpPopularBooksRepository : PopularBooksRepository {
    override suspend fun getPopularBooks(limit: Int): Result<List<PopularBook>> {
        return Result.success(emptyList())
    }
}
