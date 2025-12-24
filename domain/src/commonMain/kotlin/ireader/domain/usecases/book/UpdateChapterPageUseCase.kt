package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository

/**
 * Use case for updating the current chapter page for a book.
 * This is used for sources that support paginated chapter loading.
 */
class UpdateChapterPageUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Updates the chapter page for a book.
     * 
     * @param bookId The ID of the book
     * @param chapterPage The current chapter page (1-indexed)
     */
    suspend operator fun invoke(bookId: Long, chapterPage: Int) {
        bookRepository.updateChapterPage(bookId, chapterPage)
    }
}
