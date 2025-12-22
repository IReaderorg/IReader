package ireader.domain.data.repository

import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.coroutines.flow.Flow

interface UpdatesRepository {

    fun subscribeAllUpdates(after: Long): Flow<List<UpdatesWithRelations>>
    
    /**
     * Get paginated updates from database.
     * 
     * @param after Timestamp to filter updates after
     * @param limit Maximum number of items to return
     * @param offset Number of items to skip
     * @return List of updates for the requested page
     */
    suspend fun findUpdatesPaginated(
        after: Long,
        limit: Int,
        offset: Int
    ): List<UpdatesWithRelations>
    
    /**
     * Get total count of updates after the given timestamp.
     */
    suspend fun getUpdatesCount(after: Long): Int

}
