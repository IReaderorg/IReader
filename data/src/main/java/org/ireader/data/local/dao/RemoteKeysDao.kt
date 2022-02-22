package org.ireader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM library WHERE id =:id")
    fun getExploreBookById(id: Int): Flow<Book?>

    @Query("SELECT DISTINCT library.* FROM library JOIN  page_key_table ON library.title = page_key_table.id AND library.sourceId = page_key_table.sourceId GROUP BY  library.title ORDER BY id")
    fun getAllExploreBookByPaging(): PagingSource<Int, Book>


    @Query("SELECT DISTINCT library.* FROM library JOIN  page_key_table ON library.title = page_key_table.id AND library.sourceId = page_key_table.sourceId GROUP BY  library.title ORDER BY id")
    fun getAllExploreBook(): List<Book>?

    @Query("SELECT * FROM page_key_table WHERE id =:id")
    suspend fun getRemoteKeys(id: String): RemoteKeys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page_key_table")
    suspend fun deleteAllRemoteKeys()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExploredBook(bookEntity: List<Book>): List<Long>

    @Query("DELETE FROM library WHERE favorite = 0")
    suspend fun deleteAllExploredBook()

}

