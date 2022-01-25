package ir.kazemcodes.infinity.core.domain.use_cases.local

import ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.book.*
import ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter.*

data class DeleteUseCase(
    val deleteInLibraryBook: DeleteInLibraryBook,
    val deleteAllExploreBook: DeleteAllExploreBook,
    val deleteBookById: DeleteBookById,
    val setExploreModeOffForInLibraryBooks: SetExploreModeOffForInLibraryBooks,
    val deleteAllBook: DeleteAllBooks,
    val deleteChaptersByBookId: DeleteChaptersByBookId,
    val deleteAllChapters: DeleteAllChapters,
    val deleteNotInLibraryChapters: DeleteNotInLibraryChapters,
    val deleteChapterByChapter: DeleteChapterByChapter,
    val deleteChapters: DeleteChapters
)

















