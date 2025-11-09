package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.entities.Chapter


class MarkBookAsReadOrNotUseCase(
    private val localGetChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,

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
    
    /**
     * Mark all chapters as read for given books and return previous state for undo
     */
    suspend fun markAsReadWithUndo(bookIds: List<Long>): MarkResult {
        val previousStates = mutableMapOf<Long, List<Chapter>>()
        var totalChapters = 0
        
        bookIds.forEach { bookId ->
            val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
            previousStates[bookId] = chapters
            totalChapters += chapters.size
            insertUseCases.insertChapters(chapters.map { it.copy(read = true) })
        }
        
        return MarkResult.Success(
            totalChapters = totalChapters,
            totalBooks = bookIds.size,
            previousStates = previousStates
        )
    }
    
    /**
     * Mark all chapters as unread for given books and return previous state for undo
     */
    suspend fun markAsUnreadWithUndo(bookIds: List<Long>): MarkResult {
        val previousStates = mutableMapOf<Long, List<Chapter>>()
        var totalChapters = 0
        
        bookIds.forEach { bookId ->
            val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
            previousStates[bookId] = chapters
            totalChapters += chapters.size
            insertUseCases.insertChapters(chapters.map { it.copy(read = false) })
        }
        
        return MarkResult.Success(
            totalChapters = totalChapters,
            totalBooks = bookIds.size,
            previousStates = previousStates
        )
    }
    
    /**
     * Undo a mark operation by restoring previous chapter states
     */
    suspend fun undoMark(previousStates: Map<Long, List<Chapter>>) {
        previousStates.values.forEach { chapters ->
            insertUseCases.insertChapters(chapters)
        }
    }
}

/**
 * Result of mark as read/unread operation
 */
sealed class MarkResult {
    data class Success(
        val totalChapters: Int,
        val totalBooks: Int,
        val previousStates: Map<Long, List<Chapter>>
    ) : MarkResult()
    
    data class Failure(
        val message: String
    ) : MarkResult()
}