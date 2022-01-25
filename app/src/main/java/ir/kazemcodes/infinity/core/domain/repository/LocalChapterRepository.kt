package ir.kazemcodes.infinity.core.domain.repository

import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.domain.models.Chapter
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {


    fun getOneChapterById(
        chapterId: Int
    ): Flow<Chapter?>

    fun getChaptersByBookId(
        bookId: Int,
        isAsc:Boolean
    ): Flow<List<Chapter>?>


    fun getLastReadChapter(bookId: Int): Flow<Chapter?>


    suspend fun insertChapter(chapter: Chapter)

    suspend fun insertChapters(
        chapters: List<Chapter>,
    )


    fun getLocalChaptersByPaging(
        bookId: Int, isAsc: Boolean,
    ): PagingSource<Int, Chapter>


    suspend fun deleteChaptersByBookId(
        bookId: Int,
    )
    suspend fun deleteChapters(chapters: List<Chapter>)

    suspend fun deleteChapterByChapter(
       chapter: Chapter
    )

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()


}