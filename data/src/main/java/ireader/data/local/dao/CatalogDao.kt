package ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao : BaseDao<ireader.common.models.entities.CatalogRemote> {

    @Query("SELECT * FROM catalog ORDER BY lang, name")
    suspend fun findAll(): List<ireader.common.models.entities.CatalogRemote>

    @Query("SELECT * FROM catalog ORDER BY lang, name")
    fun subscribeAll(): Flow<List<ireader.common.models.entities.CatalogRemote>>

    @Query("DELETE FROM catalog")
    fun deleteAll()
}
