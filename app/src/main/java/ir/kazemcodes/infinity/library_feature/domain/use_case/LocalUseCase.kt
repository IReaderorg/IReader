package ir.kazemcodes.infinity.library_feature.domain.use_case

import ir.kazemcodes.infinity.library_feature.domain.use_case.book.*
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.GetLocalChapterUseCase
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.GetLocalChaptersByBookNameUseCase
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.InsertLocalChapterUseCase

data class LocalUseCase(
    val getLocalBooksUseCase: GetLocalBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChapterUseCase: InsertLocalChapterUseCase,
    val getLocalChapterUseCase: GetLocalChapterUseCase,
    val getLocalChaptersByBookNameByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase
)
