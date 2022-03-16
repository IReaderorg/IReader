package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.models.entities.Chapter

class FakeChapterRepository : LocalChapterRepository {

    val chapters = mutableListOf<Chapter>()

    override fun subscribeChapterById(chapterId: Long): Flow<Chapter?> = flow {
        val result = chapters.find { it.id == chapterId }
        emit(result)
    }

    override suspend fun findChapterById(chapterId: Long): Chapter? {
        TODO("Not yet implemented")
    }

    override fun subscribeChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>> =
        flow {
            val result = chapters.filter { it.id == bookId }.sortedBy { it.id }
            if (isAsc) {
                emit(result)
            } else {
                emit(result.reversed())
            }
        }

    override suspend fun findChaptersByBookId(bookId: Long, isAsc: Boolean): List<Chapter> {
        TODO("Not yet implemented")
    }

    override suspend fun findChaptersByKey(key: String): List<Chapter> {
        TODO("Not yet implemented")
    }

    override suspend fun findChapterByKey(key: String): Chapter? {
        TODO("Not yet implemented")
    }

    override fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?> = flow {
        val result = chapters.find { it.lastRead != 0L }
        emit(result)
    }

    override suspend fun findLastReadChapter(bookId: Long): Chapter? {
        TODO("Not yet implemented")
    }

    override fun subscribeFirstChapter(bookId: Long): Flow<Chapter?> = flow {
        val result = chapters.first()
        emit(result)
    }

    override suspend fun findFirstChapter(bookId: Long): Chapter? {
        TODO("Not yet implemented")
    }


    override suspend fun insertChapter(chapter: Chapter): Long {
        chapters.add(chapter)
        return 0
    }

    override suspend fun insertChapters(chapters: List<Chapter>): List<Long> {
        this.chapters.addAll(chapters)
        return emptyList()
    }

    override fun findLocalChaptersByPaging(
        bookId: Long,
        isAsc: Boolean,
        query: String,
    ): PagingSource<Int, Chapter> {
        throw UnsupportedOperationException("unsupported")
    }

    override suspend fun deleteChaptersByBookId(bookId: Long) {
        this.chapters.removeIf { it.bookId == bookId }
    }

    override suspend fun deleteChapters(chapters: List<Chapter>) {
        val ids = chapters.map { it.id }
        this.chapters.removeIf { it.id in ids }
    }

    override suspend fun deleteChapterByChapter(chapter: Chapter) {
        this.chapters.removeIf { it.id == chapter.id }
    }

    override suspend fun deleteNotInLibraryChapters() {
        this.chapters.removeIf { !it.inLibrary }
    }

    override suspend fun deleteAllChapters() {
        this.chapters.clear()
    }

}