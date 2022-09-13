package ireader.domain.data.repository

import ireader.common.models.entities.UpdatesWithRelations
import kotlinx.coroutines.flow.Flow

interface UpdatesRepository {

    fun subscribeAllUpdates(after: Long): Flow<List<UpdatesWithRelations>>

}
