package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys


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
