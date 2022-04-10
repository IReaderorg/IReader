package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.ireader.data.local.dao.chapterDao
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val daoLibrary: chapterDao) :
    LocalChapterRepository {


    override suspend fun findChapterByIdByBatch(
        chapterId: List<Long>,
    ): List<Chapter> {
        return daoLibrary.findChapterByIdByBatch(chapterId = chapterId)
    }

    override suspend fun findAllChapters(): List<Chapter> {
        return daoLibrary.findAllChapters()
    }

    override fun subscribeChapterById(chapterId: Long): Flow<Chapter?> {
        return daoLibrary.subscribeChapterById(chapterId = chapterId).distinctUntilChanged()
    }

    override suspend fun findChapterById(chapterId: Long): Chapter? {
        return daoLibrary.findChapterById(chapterId)
    }

    override suspend fun findAllInLibraryChapter(): List<Chapter> {
        return daoLibrary.findAllInLibraryChapters()
    }

    override fun subscribeChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>> {
        return daoLibrary.subscribeChaptersByBookId(bookId = bookId, isAsc = isAsc)
            .distinctUntilChanged()
    }

    override suspend fun findChaptersByBookId(bookId: Long, isAsc: Boolean): List<Chapter> {
        return daoLibrary.findChaptersByBookId(bookId, isAsc)
    }

    override suspend fun findChaptersByKey(key: String): List<Chapter> {
        return daoLibrary.findChaptersByKey(key = key)
    }

    override suspend fun findChapterByKey(key: String): Chapter? {
        return daoLibrary.findChapterByKey(key = key)
    }


    override fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?> {
        return daoLibrary.subscribeLastReadChapter(bookId).distinctUntilChanged()
    }

    override suspend fun findLastReadChapter(bookId: Long): Chapter? {
        return daoLibrary.findLastReadChapter(bookId)
    }

    override fun subscribeFirstChapter(bookId: Long): Flow<Chapter?> {
        return daoLibrary.subscribeFirstChapter(bookId).distinctUntilChanged()
    }

    override suspend fun findFirstChapter(bookId: Long): Chapter? {
        return daoLibrary.findFirstChapter(bookId)
    }


    /******************************Insert******************************/
    override suspend fun insertChapters(
        chapters: List<Chapter>,
    ): List<Long> {
        return daoLibrary.insertChapters(chapters = chapters)

    }

    override suspend fun insertChapter(chapter: Chapter): Long {
        return daoLibrary.insertChapter(chapter)
    }

    /**************************************************************/

    override suspend fun deleteChaptersByBookId(bookId: Long) {
        return daoLibrary.deleteChaptersById(bookId)
    }


    override suspend fun deleteChapters(chapters: List<Chapter>) {
        return daoLibrary.deleteChaptersById(chapters)
    }

    override suspend fun deleteChapterByChapter(chapter: Chapter) {
        return daoLibrary.deleteChapter(chapter)
    }


    override suspend fun deleteAllChapters() {
        return daoLibrary.deleteAllChapters()
    }
}