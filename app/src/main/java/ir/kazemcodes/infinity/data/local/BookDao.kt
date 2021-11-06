package ir.kazemcodes.infinity.data.local

import androidx.room.*
import ir.kazemcodes.infinity.domain.model.book.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM book_table")
    fun getBooks() : Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE bookID = :id")
    suspend fun getBookById(id : Int) : BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Delete
    suspend fun deleteBook(bookEntity: BookEntity)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()




}