package ir.kazemcodes.infinity.domain.use_cases.local

import ir.kazemcodes.infinity.domain.use_cases.local.book.*
import ir.kazemcodes.infinity.domain.use_cases.local.chapter.*

data class LocalUseCase(
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val getLocalChaptersByBookNameByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val getLocalBooksUseCase: GetLocalBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChaptersUseCase: InsertLocalChaptersUseCase,
    val UpdateLocalChapterContentUseCase: UpdateLocalChapterContentUseCase,
    val UpdateLocalChaptersContentUseCase: UpdateLocalChaptersContentUseCase,
    val getLocalChapterReadingContentUseCase: GetLocalChapterReadingContentUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteChaptersUseCase: DeleteLocalChaptersUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase,
)
