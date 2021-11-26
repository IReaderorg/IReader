package ir.kazemcodes.infinity.library_feature.data.repository

import ir.kazemcodes.infinity.library_feature.data.ChapterDao
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalChapterRepository

class LocalChapterRepositoryImpl(private val dao: ChapterDao) : LocalChapterRepository {

    override suspend fun insertChapters(chapterEntity: List<ChapterEntity>, bookName: String) {
        return dao.insertChapters(chapterEntities = chapterEntity.map { it.copy(bookName = bookName) })
    }

    override suspend fun getChapterByName(bookName : String): List<ChapterEntity> {
        return dao.getChapters(bookName)
    }

    override suspend fun getChapter(bookName : String): ChapterEntity {
        return dao.getChapter(bookName)
    }
}