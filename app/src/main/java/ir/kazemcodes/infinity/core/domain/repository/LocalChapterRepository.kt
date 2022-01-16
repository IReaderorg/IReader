package ir.kazemcodes.infinity.core.domain.repository

import ir.kazemcodes.infinity.core.domain.models.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {

    fun getAllChapter(): Flow<List<ChapterEntity>>

    fun getChapterByChapter(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<ChapterEntity?>

    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)

    suspend fun deleteLastReadChapter(
        bookName: String,
        source: String,
    )

    suspend fun setLastReadChapter(
        bookName: String,
        chapterTitle: String,
        source: String,
    )

    fun getLastReadChapter(bookName: String, source: String): Flow<ChapterEntity>

    suspend fun updateChapter(
        readingContent: String,
        haveBeenRead: Boolean,
        bookName: String,
        chapterTitle: String,
        lastRead: Boolean,
        source: String,
    )

    suspend fun updateChapter(chapterEntity: ChapterEntity)

    suspend fun updateChapters(chapterEntities: List<ChapterEntity>)

    suspend fun updateAddToLibraryChapters(
        chapterTitle: String,
        source: String,
        bookName: String,
    )

    fun getChapterByName(bookName: String, source: String): Flow<List<ChapterEntity>>


    suspend fun deleteChapters(bookName: String, source: String)

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()


}