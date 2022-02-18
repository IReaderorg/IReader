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

    override fun subscribeChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>> =
        flow {
            val result = chapters.filter { it.id == bookId }.sortedBy { it.id }
            if (isAsc) {
                emit(result)
            } else {
                emit(result.reversed())
            }
        }

    override fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?> = flow {
        val result = chapters.find { it.lastRead }
        emit(result)
    }

    override fun subscribeFirstChapter(bookId: Long): Flow<Chapter?> = flow {
        val result = chapters.first()
        emit(result)
    }

    override suspend fun setLastReadToFalse(bookId: Long) {
        chapters.forEach {
            it.lastRead = false
        }
    }

    override suspend fun insertChapter(chapter: Chapter) {
        chapters.add(chapter)
    }

    override suspend fun insertChapters(chapters: List<Chapter>) {
        this.chapters.addAll(chapters)
    }

    override fun findLocalChaptersByPaging(
        bookId: Long,
        isAsc: Boolean,
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