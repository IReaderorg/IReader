package org.ireader.infinity.core.domain.use_cases.local

import org.ireader.infinity.core.domain.use_cases.local.delete_usecases.book.*
import org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter.*

data class DeleteUseCase(
    val deleteNotInLibraryBook: DeleteNotInLibraryBook,
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBookById: DeleteBookById,
    val setExploreModeOffForInLibraryBooks: SetExploreModeOffForInLibraryBooks,
    val deleteAllBook: DeleteAllBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteNotInLibraryChapters: DeleteNotInLibraryChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters,
)

















