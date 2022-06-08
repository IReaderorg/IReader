package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.ireader.common_models.entities.Chapter
import org.ireader.data.local.dao.ChapterDao
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(private val daoLibrary: ChapterDao) :
    org.ireader.common_data.repository.ChapterRepository {


    override suspend fun findAllChapters(): List<org.ireader.common_models.entities.Chapter> {
        return daoLibrary.findAllChapters()
    }

    override fun subscribeChapterById(chapterId: Long): Flow<org.ireader.common_models.entities.Chapter?> {
        return daoLibrary.subscribeChapterById(chapterId = chapterId).distinctUntilChanged()
    }

    override suspend fun findChapterById(chapterId: Long): org.ireader.common_models.entities.Chapter? {
        return daoLibrary.findChapterById(chapterId)
    }

    override suspend fun findAllInLibraryChapter(): List<org.ireader.common_models.entities.Chapter> {
        return daoLibrary.findAllInLibraryChapters()
    }

    override fun subscribeChaptersByBookId(bookId: Long, sort: String): Flow<List<Chapter>> {
        return daoLibrary.subscribe(bookId,sort)
    }

    override fun subscribeChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<org.ireader.common_models.entities.Chapter>> {
        return daoLibrary.subscribeChaptersByBookId(bookId = bookId, isAsc = isAsc)
            .distinctUntilChanged()
    }

    override suspend fun findChaptersByBookId(bookId: Long, isAsc: Boolean): List<org.ireader.common_models.entities.Chapter> {
        return daoLibrary.findChaptersByBookId(bookId, isAsc)
    }


    override suspend fun findChaptersByKey(key: String): List<org.ireader.common_models.entities.Chapter> {
        return daoLibrary.findChaptersByKey(key = key)
    }

    override suspend fun findChapterByKey(key: String): org.ireader.common_models.entities.Chapter? {
        return daoLibrary.findChapterByKey(key = key)
    }

    override fun subscribeLastReadChapter(bookId: Long): Flow<org.ireader.common_models.entities.Chapter?> {
        return daoLibrary.subscribeLastReadChapter(bookId).distinctUntilChanged()
    }

    override suspend fun findLastReadChapter(bookId: Long): org.ireader.common_models.entities.Chapter? {
        return daoLibrary.findLastReadChapter(bookId)
    }

    override fun subscribeFirstChapter(bookId: Long): Flow<org.ireader.common_models.entities.Chapter?> {
        return daoLibrary.subscribeFirstChapter(bookId).distinctUntilChanged()
    }

    override suspend fun findFirstChapter(bookId: Long): org.ireader.common_models.entities.Chapter? {
        return daoLibrary.findFirstChapter(bookId)
    }

    /******************************Insert******************************/
    override suspend fun insertChapters(
        chapters: List<org.ireader.common_models.entities.Chapter>,
    ) :List<Long> {
     return   daoLibrary.insertOrUpdate(chapters)
    }

    override suspend fun insertChapter(chapter: org.ireader.common_models.entities.Chapter): Long {
        return daoLibrary.insertOrUpdate(chapter)
    }

    /**************************************************************/

    override suspend fun deleteChaptersByBookId(bookId: Long) {
        return daoLibrary.deleteChaptersByBookId(bookId)
    }

    override suspend fun deleteChapters(chapters: List<org.ireader.common_models.entities.Chapter>) {
        return daoLibrary.deleteChapters(chapters)
    }

    override suspend fun deleteChapter(chapter: org.ireader.common_models.entities.Chapter) {
        return daoLibrary.deleteChapter(chapter)
    }

    override suspend fun deleteAllChapters() {
        return daoLibrary.deleteAllChapters()
    }

}
