package ir.kazemcodes.infinity.library_feature.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM book_table WHERE inLibrary = 1")
    fun getInitializedBooks() : Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table")
    fun getBooks() : Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE bookId = :bookId")
    suspend fun getBookById(bookId : Int) : BookEntity

    @Query("SELECT * FROM book_table WHERE bookName = :bookName")
    suspend fun getBookByName(bookName : String) : BookEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(bookEntity: List<BookEntity>)

    @Query("DELETE FROM book_table WHERE bookId = :bookId ")
    suspend fun deleteBook(bookId: Int)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()




}