package ireader.domain.data.repository

import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.coroutines.flow.Flow

interface UpdatesRepository {

    fun subscribeAllUpdates(after: Long): Flow<List<UpdatesWithRelations>>

}
