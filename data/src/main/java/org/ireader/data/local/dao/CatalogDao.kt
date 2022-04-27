package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface CatalogDao : BaseDao<org.ireader.common_models.entities.CatalogRemote> {

    @Query("SELECT * FROM catalog ORDER BY lang, name")
    suspend fun findAll(): List<org.ireader.common_models.entities.CatalogRemote>

    @Query("SELECT * FROM catalog ORDER BY lang, name")
    fun subscribeAll(): Flow<List<org.ireader.common_models.entities.CatalogRemote>>

    @Query("DELETE FROM catalog")
    fun deleteAll()


}

