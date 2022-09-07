package ireader.domain.use_cases.local

import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys

data class DeleteUseCase(
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBooks: DeleteBooks,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val unFavoriteBook: UnFavoriteBook,
    val deleteNotInLibraryBooks: DeleteNotInLibraryBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)
