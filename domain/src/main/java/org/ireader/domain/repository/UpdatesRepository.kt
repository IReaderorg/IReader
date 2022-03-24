package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Update

interface UpdatesRepository {


    fun subscribeAllUpdates(): Flow<List<Update>>

    suspend fun insertUpdate(update: Update)
    suspend fun insertUpdates(update: List<Update>)
}