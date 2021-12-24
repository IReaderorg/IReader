package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.domain.models.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {
    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)
    suspend fun updateChapter(readingContent : String ,haveBeenRead: Boolean, bookName: String , chapterTitle: String,lastRead : Boolean)

    suspend fun updateChapters(chapterEntities: List<ChapterEntity>)

    fun getChapterByName(bookName : String): Flow<List<ChapterEntity>>
    fun getChapterByChapter(chapterTitle : String, bookName: String): Flow<ChapterEntity?>

    fun getChapter(bookName : String): Flow<ChapterEntity?>

    fun deleteChapters(bookName: String)

}