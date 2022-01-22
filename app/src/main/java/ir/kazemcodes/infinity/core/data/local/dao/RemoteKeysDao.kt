package ir.kazemcodes.infinity.core.data.local.dao

import androidx.room.*
import ir.kazemcodes.infinity.core.utils.Constants

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM page_key_table WHERE id =:id")
    suspend fun getRemoteKeys(id: String): RemoteKeys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page_key_table")
    suspend fun deleteAllRemoteKeys()

}

@Entity(tableName = Constants.PAGE_KET_TABLE)
data class RemoteKeys(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val prevPage: Int?,
    val nextPage: Int?
)