package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.domain.models.local.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {

    fun getAllChapter(): Flow<List<ChapterEntity>>

    fun getChapterByChapter(chapterTitle: String, bookName: String): Flow<ChapterEntity?>

    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)

    suspend fun updateChapter(
        readingContent: String,
        haveBeenRead: Boolean,
        bookName: String,
        chapterTitle: String,
        lastRead: Boolean,
    )

    suspend fun updateChapter(chapterEntity: ChapterEntity)

    suspend fun updateChapters(chapterEntities: List<ChapterEntity>)

    fun getChapterByName(bookName: String): Flow<List<ChapterEntity>>




    suspend fun deleteChapters(bookName: String)

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()



}