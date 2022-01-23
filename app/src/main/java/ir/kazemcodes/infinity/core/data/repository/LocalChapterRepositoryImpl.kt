package ir.kazemcodes.infinity.core.data.repository

import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalChapterRepositoryImpl @Inject constructor(private val daoLibrary: LibraryChapterDao) :
    LocalChapterRepository {



    override fun getLocalChaptersByPaging(
        bookId: Int, isAsc: Boolean,
    ): PagingSource<Int, Chapter> {
        return daoLibrary.getChaptersForPaging(bookId = bookId, isAsc = isAsc)

    }

    override fun getOneChapterById(chapterId: Int): Flow<Chapter?> {
       return daoLibrary.getChapterById(chapterId = chapterId)
    }

    override fun getChaptersByBookId(bookId: Int): Flow<List<Chapter>?> {
      return  daoLibrary.getChaptersByBookId(bookId = bookId)
    }



    override fun getLastReadChapter(bookId: Int): Flow<Chapter?> {
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

    override suspend fun deleteChaptersByBookId(bookId: Int) {
        return daoLibrary.deleteChaptersById(bookId)
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