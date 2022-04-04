package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter

interface LocalChapterRepository {


    fun subscribeChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    suspend fun findChapterById(
        chapterId: Long,
    ): Chapter?

    suspend fun findChapterByIdByBatch(
        chapterId: List<Long>,
    ): List<Chapter>

    suspend fun findAllInLibraryChapter(): List<Chapter>

    fun subscribeChaptersByBookId(
        bookId: Long,
        isAsc: Boolean = true,
    ): Flow<List<Chapter>>

    suspend fun findChaptersByBookId(
        bookId: Long,
        isAsc: Boolean = true,
    ): List<Chapter>

    suspend fun findChaptersByKey(key: String): List<Chapter>

    suspend fun findChapterByKey(key: String): Chapter?

    fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?>
    suspend fun findLastReadChapter(bookId: Long): Chapter?

    fun subscribeFirstChapter(bookId: Long): Flow<Chapter?>
    suspend fun findFirstChapter(bookId: Long): Chapter?




    suspend fun insertChapter(chapter: Chapter): Long

    suspend fun insertChapters(
        chapters: List<Chapter>,
    ): List<Long>


    fun findLocalChaptersByPaging(
        bookId: Long, isAsc: Boolean, query: String,
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