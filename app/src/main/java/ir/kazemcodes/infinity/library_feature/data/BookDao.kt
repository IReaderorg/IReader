package ir.kazemcodes.infinity.library_feature.data

import androidx.room.*
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM book_table")
    fun getBooks() : Flow<List<BookEntity>>

    @Query("SELECT * FROM book_table WHERE title = :name")
    suspend fun getBookByName(name : String) : Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapterEntities: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(bookEntity: List<BookEntity>)

    @Delete
    suspend fun deleteBook(bookEntity: BookEntity)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()




}