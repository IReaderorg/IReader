package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryBookDao {

    @Query("SELECT * FROM explore_books_table")
    fun getAllExploreBook(): PagingSource<Int, ExploreBook>

    @Query("SELECT * FROM explore_books_table WHERE id =:id")
    fun getExploreBookById(id:String): Flow<ExploreBook?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllExploredBook(bookEntity: List<ExploreBook>)

    @Query("DELETE FROM explore_books_table")
    fun deleteAllExploredBook()





    @Query("SELECT * FROM book_table")
    fun getAllBooksForPaging(): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    fun getLocalBook(bookId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE id = :bookId Limit 1")
    fun getBookById(bookId: String): Flow<BookEntity>

    @Query("SELECT * FROM book_table WHERE bookName = :bookName Limit 1")
    fun getBookByName(bookName: String): Flow<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Query("SELECT * FROM book_table WHERE bookName LIKE'%' || :query || '%'")
    fun searchBook(query:String): PagingSource<Int,BookEntity>

    @Query("DELETE FROM book_table WHERE id = :bookId ")
    suspend fun deleteBook(bookId: String)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()


}