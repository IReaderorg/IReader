package ireader.data.repository

import ireader.domain.data.repository.UpdatesRepository
import ireader.common.models.entities.UpdatesWithRelations
import ireader.data.local.DatabaseHandler
import ireader.data.updates.updatesMapper
import kotlinx.coroutines.flow.Flow


class UpdatesRepositoryImpl(private val handler: DatabaseHandler,) :
    UpdatesRepository {
    override fun subscribeAllUpdates(after: Long): Flow<List<UpdatesWithRelations>> {
        return handler.subscribeToList {
            updateViewQueries.updates(after = after, updatesMapper)
        }
    }

}
