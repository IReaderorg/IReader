package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Update
import org.ireader.domain.models.entities.UpdateWithInfo

interface UpdatesRepository {


    fun subscribeAllUpdates(): Flow<Map<String, List<UpdateWithInfo>>>

    suspend fun insertUpdate(update: Update)
    suspend fun insertUpdates(update: List<Update>)
    suspend fun deleteUpdate(update: Update)
    suspend fun deleteUpdates(update: List<Update>)
    suspend fun deleteAllUpdates()
}