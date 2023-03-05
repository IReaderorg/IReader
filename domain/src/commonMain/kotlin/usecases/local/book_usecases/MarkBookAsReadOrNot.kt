package ireader.domain.usecases.local.book_usecases



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
}