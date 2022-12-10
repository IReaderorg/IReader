package ireader.domain.usecases.local.book_usecases

import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import org.koin.core.annotation.Factory

@Factory
class MarkBookAsReadOrNotUseCase(
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