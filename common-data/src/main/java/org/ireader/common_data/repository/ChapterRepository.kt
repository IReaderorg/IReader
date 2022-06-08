package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Chapter

interface ChapterRepository {

    fun subscribeChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    suspend fun findChapterById(
        chapterId: Long,
    ): Chapter?


    suspend fun findAllChapters(): List<Chapter>

    suspend fun findAllInLibraryChapter(): List<Chapter>

    fun subscribeChaptersByBookId(
        bookId: Long,
        sort: String,
    ): Flow<List<Chapter>>

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

    suspend fun deleteChaptersByBookId(
        bookId: Long,
    )

    suspend fun deleteChapters(chapters: List<Chapter>)

    suspend fun deleteChapter(
        chapter: Chapter,
    )

    suspend fun deleteAllChapters()

}
