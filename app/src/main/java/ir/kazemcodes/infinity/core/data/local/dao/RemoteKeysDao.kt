package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM page_key_table WHERE id =:id")
    suspend fun getRemoteKeys(id: String): RemoteKeys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page_key_table")
    suspend fun deleteAllRemoteKeys()

    @Query("SELECT * FROM book_table WHERE isExploreMode = 1")
    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    @Query("SELECT * FROM book_table WHERE isExploreMode = 1")
    suspend fun getAllExploreBook(): List<Book?>

    @Query("SELECT * FROM book_table WHERE id =:id AND isExploreMode = 1")
    fun getExploreBookById(id: Int): Flow<Book?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllExploredBook(bookEntity: List<Book>)

    @Query("DELETE FROM book_table WHERE isExploreMode = 1")
    fun deleteAllExploredBook()

}

@Entity(tableName = Constants.PAGE_KET_TABLE)
data class RemoteKeys(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val prevPage: Int?,
    val nextPage: Int?
)