package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryBookDao {

    @Query("SELECT * FROM explore_books_table")
    fun getAllExploreBookByPaging(): PagingSource<Int, ExploreBook>

    @Query("SELECT * FROM explore_books_table")
    suspend fun getAllExploreBook(): List<ExploreBook?>

    @Query("SELECT * FROM explore_books_table WHERE id =:id")
    fun getExploreBookById(id:String): Flow<ExploreBook?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllExploredBook(bookEntity: List<ExploreBook?>)

    @Query("DELETE FROM explore_books_table")
    fun deleteAllExploredBook()



    @Query("SELECT * FROM book_table WHERE inLibrary = 1 ORDER BY CASE WHEN :isAsc = 1 THEN bookName END ASC, CASE WHEN :isAsc = 0 THEN bookName END DESC")
    fun getAllLocalBooksForPagingSortedByAlphabetically(isAsc:Boolean): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1 ORDER BY CASE WHEN :isAsc = 1 THEN dataAdded END ASC , CASE WHEN :isAsc = 0 THEN dataAdded END DESC")
    fun getAllLocalBooksForPagingSortedByDateAdded(isAsc:Boolean): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1 ORDER BY CASE WHEN :isAsc = 1 THEN lastRead END ASC , CASE WHEN :isAsc = 0 THEN lastRead END DESC")
    fun getAllLocalBooksForPagingSortedByLastRead(isAsc:Boolean): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1 ORDER BY CASE WHEN :isAsc = 1 THEN download END ASC , CASE WHEN :isAsc = 0 THEN download END DESC")
    fun getAllLocalBooksForPagingSortedByDownloads(isAsc:Boolean): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1 ORDER BY CASE WHEN :isAsc = 1 THEN totalChapters END ASC,CASE WHEN :isAsc = 0 THEN totalChapters END DESC")
    fun getAllLocalBooksForPagingSortedByTotalChapter(isAsc:Boolean): PagingSource<Int,BookEntity>

    @Query("SELECT * FROM book_table")
    fun getAllBooks(): Flow<List<BookEntity>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    fun getLocalBook(bookId: String): Flow<List<BookEntity>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId Limit 1")
    fun getBookById(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM book_table WHERE bookName = :bookName Limit 1")
    fun getBookByName(bookName: String): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(bookEntity: BookEntity)

    @Query("SELECT * FROM book_table WHERE bookName LIKE'%' || :query || '%'")
    fun searchBook(query:String): PagingSource<Int,BookEntity>

    @Query("DELETE FROM book_table WHERE id = :bookId ")
    suspend fun deleteBook(bookId: String)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()

    @Update(entity = BookEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBook(inLibraryUpdate: InLibraryUpdate)


}

data class InLibraryUpdate(val id: String, val inLibrary:Boolean, val totalChapters:Int)
