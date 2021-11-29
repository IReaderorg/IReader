package ir.kazemcodes.infinity.library_feature.domain.repository

import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalChapterRepository {
    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)
    suspend fun insertChapter(chapterEntity: ChapterEntity)

    fun getChapterByName(bookName : String): Flow<List<ChapterEntity>>
    fun getChapterByChapter(chapterTitle : String, bookName: String): Flow<ChapterEntity>

    fun getChapter(bookName : String): Flow<ChapterEntity?>

}