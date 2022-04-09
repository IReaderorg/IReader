package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.CatalogRemote


@Dao
interface CatalogDao : BaseDao<CatalogRemote> {

    @Query("SELECT * FROM catalog_remote ORDER BY lang, name")
    suspend fun findAll(): List<CatalogRemote>

    @Query("SELECT * FROM catalog_remote ORDER BY lang, name")
    fun subscribeAll(): Flow<List<CatalogRemote>>

    @Query("DELETE FROM catalog_remote")
    fun deleteAll()


}

