package ir.kazemcodes.infinity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.domain.models.local.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM book_table")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1")
    fun getInLibraryBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE bookId = :bookId Limit 1")
    fun getBookById(bookId: Int): Flow<BookEntity>

    @Query("SELECT * FROM book_table WHERE bookName = :bookName Limit 1")
    fun getBookByName(bookName: String): Flow<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(bookEntity: List<BookEntity>)

    @Query("DELETE FROM book_table WHERE bookName = :bookName ")
    suspend fun deleteBook(bookName: String)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()

    @Query("DELETE FROM book_table WHERE inLibrary = 0")
    suspend fun deleteAllNotInLibraryBooks()

}