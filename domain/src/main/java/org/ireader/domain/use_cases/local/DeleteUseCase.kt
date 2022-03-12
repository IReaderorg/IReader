package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBook
import org.ireader.domain.use_cases.local.delete_usecases.chapter.*
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import javax.inject.Inject

data class DeleteUseCase @Inject constructor(
    val deleteNotInLibraryBook: DeleteNotInLibraryBook,
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteNotInLibraryChapters: DeleteNotInLibraryChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)

















