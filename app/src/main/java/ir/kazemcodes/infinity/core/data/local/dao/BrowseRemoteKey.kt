package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.core.domain.models.BrowseRemoteKey

@Dao
interface BrowseRemoteKey {

    @Query("SELECT * FROM images_key_table")
    fun getRemoteKeys() : PagingSource<Int, BrowseRemoteKey>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImages(images : List<BrowseRemoteKey>)

    @Query("SELECT * FROM images_key_table")
    suspend fun deleteAllImages()
}