package ireader.domain.data.repository

import ireader.domain.models.remote.PopularBook

/**
 * Repository for fetching popular books based on reader count
 */
interface PopularBooksRepository {
    /**
     * Get most popular books based on how many users are reading them
     * @param limit Maximum number of books to return
     */
    suspend fun getPopularBooks(limit: Int = 50): Result<List<PopularBook>>
}
