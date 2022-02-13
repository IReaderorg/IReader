package org.ireader.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val daoLibrary: LibraryChapterDao) :
    LocalChapterRepository {


    override fun getLocalChaptersByPaging(
        bookId: Long, isAsc: Boolean,
    ): PagingSource<Int, Chapter> {
        return daoLibrary.getChaptersForPaging(bookId = bookId, isAsc = isAsc)

    }

    override fun getOneChapterById(chapterId: Long): Flow<Chapter?> {
        return daoLibrary.getChapterById(chapterId = chapterId)
    }

    override fun getChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>> {
        return daoLibrary.getChaptersByBookId(bookId = bookId, isAsc = isAsc)
    }


    override fun getLastReadChapter(bookId: Long): Flow<Chapter?> {
        return daoLibrary.getLastReadChapter(bookId)
    }

    /******************************Insert******************************/
    override suspend fun insertChapters(
        chapters: List<Chapter>,
    ) {
        return daoLibrary.insertChapters(chapters = chapters)
    }

    override suspend fun insertChapter(chapter: Chapter) {
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

    override suspend fun deleteNotInLibraryChapters() {
        return daoLibrary.deleteNotInLibraryChapters()
    }

    override suspend fun deleteAllChapters() {
        return daoLibrary.deleteAllChapters()
    }
}