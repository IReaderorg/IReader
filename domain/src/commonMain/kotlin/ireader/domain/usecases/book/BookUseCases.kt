package ireader.domain.usecases.book

/**
 * Aggregate class for all book-related use cases
 * Provides a single point of access for book operations
 */
data class BookUseCases(
    val getBookById: GetBookByIdUseCase,
    val getBooksInLibrary: GetBooksInLibraryUseCase,
    val updateBook: UpdateBookUseCase,
    val deleteBook: DeleteBookUseCase,
    val toggleFavorite: ToggleFavorite,
    val updatePinStatus: UpdateBookPinStatusUseCase,
    val updateArchiveStatus: UpdateBookArchiveStatusUseCase,
    val searchBooks: SearchBooksUseCase,
    val addToLibrary: AddToLibrary,
    val removeFromLibrary: RemoveFromLibrary,
    val updateChapterPage: UpdateChapterPageUseCase,
)
