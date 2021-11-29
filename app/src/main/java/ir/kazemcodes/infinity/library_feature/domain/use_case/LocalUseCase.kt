package ir.kazemcodes.infinity.library_feature.domain.use_case

import ir.kazemcodes.infinity.library_feature.domain.use_case.book.*
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.*

data class LocalUseCase(
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val getLocalChaptersByBookNameByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val getLocalBooksUseCase: GetLocalBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChaptersUseCase: InsertLocalChaptersUseCase,
    val insertLocalChapterContentUseCase: InsertLocalChapterContentUseCase,
    val getLocalChapterUseCase: GetLocalChapterUseCase,
    val getLocalChapterReadingContent: GetLocalChapterReadingContent,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase
)
