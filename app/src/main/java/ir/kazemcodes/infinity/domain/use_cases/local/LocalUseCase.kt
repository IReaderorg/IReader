package ir.kazemcodes.infinity.domain.use_cases.local

import ir.kazemcodes.infinity.domain.use_cases.local.book.*
import ir.kazemcodes.infinity.domain.use_cases.local.chapter.*

data class LocalUseCase(
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val getAllLocalBooksUseCase: GetAllLocalBooksUseCase,
    val getInLibraryBooksUseCase: GetInLibraryBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val getAllLocalChaptersUseCase: GetLocalAllChaptersUseCase,
    val getLocalChaptersByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val getLocalChapterReadingContentUseCase: GetLocalChapterReadingContentUseCase,
    val getLastReadChapterUseCase: GetLastReadChapterUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChaptersUseCase: InsertLocalChaptersUseCase,
    val updateLocalChapterContentUseCase: UpdateLocalChapterContentUseCase,
    val updateLocalChaptersContentUseCase: UpdateLocalChaptersContentUseCase,
    val setLastReadChaptersUseCase: SetLastReadChaptersUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteChaptersUseCase: DeleteLocalChaptersUseCase,
    val deleteAllLocalChaptersUseCase: DeleteAllLocalChaptersUseCase,
    val deleteNotInLibraryLocalChaptersUseCase: DeleteNotInLibraryLocalChaptersUseCase,
    val deleteNotInLibraryBooksUseCase: DeleteNotInLibraryLocalBooksUseCase,
    val deleteLastReadChapterChaptersUseCase: DeleteLastReadChapterChaptersUseCase,
    val updateLocalBookUserCase: UpdateLocalBookUserCase,
    val updateAddToLibraryChaptersContentUseCase: UpdateAddToLibraryChaptersContentUseCase
)
