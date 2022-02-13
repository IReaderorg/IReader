package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter

interface LocalChapterRepository {


    fun getOneChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    fun getChaptersByBookId(
        bookId: Long,
        isAsc: Boolean = true,
    ): Flow<List<Chapter>>


    fun getLastReadChapter(bookId: Long): Flow<Chapter?>


    suspend fun insertChapter(chapter: Chapter)

    suspend fun insertChapters(
        chapters: List<Chapter>,
    )


    fun getLocalChaptersByPaging(
        bookId: Long, isAsc: Boolean,
    ): PagingSource<Int, Chapter>


    suspend fun deleteChaptersByBookId(
        bookId: Long,
    )

    suspend fun deleteChapters(chapters: List<Chapter>)

    suspend fun deleteChapterByChapter(
        chapter: Chapter,
    )

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()


}