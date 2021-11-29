package ir.kazemcodes.infinity.library_feature.data.repository

import ir.kazemcodes.infinity.library_feature.data.ChapterDao
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val dao: ChapterDao) : LocalChapterRepository {

    override suspend fun insertChapters(chapterEntity: List<ChapterEntity>) {
        return dao.insertChapters(chapterEntities = chapterEntity)
    }

    override suspend fun insertChapter(chapterEntity: ChapterEntity) {
        return dao.insertChapter(chapterEntities = chapterEntity)
    }

    override  fun getChapterByName(bookName : String): Flow<List<ChapterEntity>> {
        return dao.getChapters(bookName)
    }
    override  fun getChapterByChapter(chapterTitle : String, bookName: String): Flow<ChapterEntity> {
        return dao.getChapterByChapter(chapterTitle , bookName )
    }

    override fun getChapter(bookName : String): Flow<ChapterEntity?> {
        return dao.getChapter(bookName)
    }
}