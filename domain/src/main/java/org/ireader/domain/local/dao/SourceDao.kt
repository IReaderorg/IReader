package org.ireader.domain.local.dao

import androidx.room.*
import org.ireader.domain.models.source.SourceEntity

@Dao
interface SourceTowerDao {

    @Query("SELECT * FROM source_table")
    suspend fun getSources(): List<SourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllSources(sources: List<SourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSource(sources: SourceEntity)

    @Query("DELETE FROM source_table")
    suspend fun deleteAllSources()

    @Delete
    suspend fun deleteSource(sourceEntity: SourceEntity)
}
