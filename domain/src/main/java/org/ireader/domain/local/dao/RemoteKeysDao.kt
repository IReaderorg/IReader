package org.ireader.domain.local.dao

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

    @Query("SELECT * FROM book_table WHERE id =:id AND isExploreMode = 1")
    fun getExploreBookById(id: Int): Flow<Book?>

    @Query("SELECT * FROM book_table WHERE isExploreMode = 1")
    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    @Query("SELECT * FROM book_table WHERE isExploreMode = 1")
    fun getAllExploreBook(): List<Book>?

    @Query("SELECT * FROM page_key_table WHERE id =:id")
    suspend fun getRemoteKeys(id: String): RemoteKeys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page_key_table")
    suspend fun deleteAllRemoteKeys()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllExploredBook(bookEntity: List<Book>)

    @Query("DELETE FROM book_table WHERE isExploreMode = 1")
    suspend fun deleteAllExploredBook()

    @Query("UPDATE book_table SET isExploreMode = 0 WHERE isExploreMode = 1 AND inLibrary = 1")
    suspend fun turnExploreModeOff()

}

