package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.delete_usecases.book.*
import org.ireader.domain.use_cases.local.delete_usecases.chapter.*

data class DeleteUseCase(
    val deleteNotInLibraryBook: DeleteNotInLibraryBook,
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBookById: DeleteBookById,
    val deleteAllBook: DeleteAllBooks,
    val setExploreModeOffForInLibraryBooks: SetOffExploreMode,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteNotInLibraryChapters: DeleteNotInLibraryChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)

















