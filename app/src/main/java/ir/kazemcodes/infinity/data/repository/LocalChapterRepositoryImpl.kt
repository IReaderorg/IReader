package ir.kazemcodes.infinity.data.repository

import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.domain.models.local.ChapterEntity
import ir.kazemcodes.infinity.domain.repository.LocalChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val dao: ChapterDao) :
    LocalChapterRepository {

    override suspend fun insertChapters(chapterEntity: List<ChapterEntity>) {
        return dao.insertChapters(chapterEntities = chapterEntity)
    }

    override suspend fun updateChapter(
        readingContent: String,
        haveBeenRead: Boolean,
        bookName: String,
        chapterTitle: String,
        lastRead: Boolean,
    ) {
        return dao.updateChapter(readingContent = readingContent,
            bookName = bookName,
            chapterTitle = chapterTitle,
            haveBeenRead = haveBeenRead,
            lastRead = lastRead)
    }

    override suspend fun updateChapter(chapterEntity: ChapterEntity) {
        return dao.updateChapter(chapterEntity)
    }

    override suspend fun updateChapters(chapterEntities: List<ChapterEntity>) {
        return dao.updateChapters(chapterEntities)
    }


    override fun getChapterByName(bookName: String): Flow<List<ChapterEntity>> {
        return dao.getChapters(bookName)
    }

    override fun getAllChapter(): Flow<List<ChapterEntity>> {
        return dao.getAllChapters()
    }

    override fun getChapterByChapter(chapterTitle: String, bookName: String): Flow<ChapterEntity?> {
        return dao.getChapterByChapter(chapterTitle, bookName)
    }


    override suspend fun deleteChapters(bookName: String) {
        return dao.deleteLocalChaptersByName(bookName = bookName)
    }

    override suspend fun deleteNotInLibraryChapters() {
        return dao.deleteAllNotInLibraryChapters()
    }

    override suspend fun deleteAllChapters() {
        return dao.deleteAllChapters()
    }
}