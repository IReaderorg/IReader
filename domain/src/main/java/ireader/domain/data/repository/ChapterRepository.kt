package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Chapter

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

    suspend fun findChaptersByBookId(
        bookId: Long,
    ): List<Chapter>



    suspend fun findLastReadChapter(bookId: Long): Chapter?

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
    fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>>
}
