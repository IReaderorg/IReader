package org.ireader.domain.book.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.ChapterUpdate

interface ChapterRepository {

    fun subscribeForManga(bookId: Long): Flow<List<Chapter>>

    fun subscribe(chapterId: Long): Flow<Chapter?>

    suspend fun findForManga(bookId: Long): List<Chapter>

    suspend fun find(chapterId: Long): Chapter?

    suspend fun find(chapterKey: String, bookId: Long): Chapter?

    suspend fun insert(chapters: List<Chapter>)

    suspend fun update(chapters: List<Chapter>)

    suspend fun updatePartial(updates: List<ChapterUpdate>)

    suspend fun updateOrder(chapters: List<Chapter>)

    suspend fun delete(chapters: List<Chapter>)

}