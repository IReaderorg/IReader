package ireader.common.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import ireader.common.models.entities.Update
import ireader.common.models.entities.UpdateWithInfo

interface UpdatesRepository {

    fun subscribeAllUpdates(): Flow<Map<LocalDate, List<UpdateWithInfo>>>

    suspend fun insertUpdate(update: Update): Long
    suspend fun insertUpdates(update: List<Update>): List<Long>
    suspend fun deleteUpdate(update: Update)
    suspend fun deleteUpdates(update: List<Update>)
    suspend fun deleteAllUpdates()
}
