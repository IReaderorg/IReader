package ireader.data.repository

import ireader.domain.data.repository.UpdatesRepository
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.data.updates.updatesMapper
import kotlinx.coroutines.flow.Flow
import ireader.data.core.DatabaseHandler

class UpdatesRepositoryImpl(private val handler: DatabaseHandler,) :
    UpdatesRepository {
    override fun subscribeAllUpdates(after: Long): Flow<List<UpdatesWithRelations>> {
        return handler.subscribeToList {
            updateViewQueries.updates(after = after, updatesMapper)
        }
    }

}
