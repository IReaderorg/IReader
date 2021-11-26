package ir.kazemcodes.infinity.library_feature.domain.repository

import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity

interface LocalChapterRepository {
    suspend fun insertChapters(chapterEntity: List<ChapterEntity>, bookName : String)

    suspend fun getChapterByName(bookName : String): List<ChapterEntity>

    suspend fun getChapter(bookName : String): ChapterEntity

}