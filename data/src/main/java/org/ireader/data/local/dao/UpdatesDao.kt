package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Update


@Dao
interface UpdatesDao {


    @Query("""
        SELECT * FROM updates
    """)
    fun subscribeUpdates(): Flow<List<Update>>

    @Insert
    suspend fun insertUpdate(update: Update)

    @Insert
    suspend fun insertUpdates(updates: List<Update>)
}