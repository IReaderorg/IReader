package org.ireader.domain.use_cases.local.book_usecases

import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import javax.inject.Inject

class MarkBookAsReadOrNotUseCase @Inject constructor(
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,

    ) {

    suspend fun markAsRead(bookIds: List<Long>) {
        bookIds.forEach { bookId ->
            val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
            insertUseCases.insertChapters(chapters.map { it.copy(read = true) })
        }
    }
    suspend fun markAsNotRead(bookIds: List<Long>) {
        bookIds.forEach { bookId ->
            val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
            insertUseCases.insertChapters(chapters.map { it.copy(read = false) })
        }
    }
}