package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBook
import org.ireader.domain.use_cases.local.delete_usecases.chapter.*

data class DeleteUseCase(
    val deleteNotInLibraryBook: DeleteNotInLibraryBook,
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteNotInLibraryChapters: DeleteNotInLibraryChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)

















