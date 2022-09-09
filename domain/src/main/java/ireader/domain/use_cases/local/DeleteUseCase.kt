package ireader.domain.use_cases.local

import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId

data class DeleteUseCase(
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val unFavoriteBook: UnFavoriteBook,
    val deleteNotInLibraryBooks: DeleteNotInLibraryBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)
