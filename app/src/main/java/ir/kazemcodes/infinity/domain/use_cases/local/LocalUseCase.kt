package ir.kazemcodes.infinity.domain.use_cases.local

import ir.kazemcodes.infinity.domain.use_cases.local.book.*
import ir.kazemcodes.infinity.domain.use_cases.local.chapter.*

data class LocalUseCase(
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val getLocalChaptersByBookNameByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val getAllLocalChaptersUseCase: GetLocalAllChaptersUseCase,
    val getAllLocalBooksUseCase: GetAllLocalBooksUseCase,
    val getInLibraryBooksUseCase: GetInLibraryBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChaptersUseCase: InsertLocalChaptersUseCase,
    val UpdateLocalChapterContentUseCase: UpdateLocalChapterContentUseCase,
    val UpdateLocalChaptersContentUseCase: UpdateLocalChaptersContentUseCase,
    val getLocalChapterReadingContentUseCase: GetLocalChapterReadingContentUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteChaptersUseCase: DeleteLocalChaptersUseCase,
    val deleteAllLocalChaptersUseCase: DeleteAllLocalChaptersUseCase,
    val deleteNotInLibraryLocalChaptersUseCase: DeleteNotInLibraryLocalChaptersUseCase,
    val deleteNotInLibraryBooksUseCase: DeleteNotInLibraryLocalBooksUseCase
)
