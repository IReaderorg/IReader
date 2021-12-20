package ir.kazemcodes.infinity.domain.local_feature.domain.use_case

import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.book.*
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter.*

data class LocalUseCase(
    val getLocalBookByNameUseCase: GetLocalBookByNameUseCase,
    val getLocalChaptersByBookNameByBookNameUseCase: GetLocalChaptersByBookNameUseCase,
    val getLocalBooksUseCase: GetLocalBooksUseCase,
    val getLocalBookByIdByIdUseCase: GetLocalBookByIdUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase,
    val insertLocalChaptersUseCase: InsertLocalChaptersUseCase,
    val UpdateLocalChapterContentUseCase: UpdateLocalChapterContentUseCase,
    val getLocalChapterUseCase: GetLocalChapterUseCase,
    val getLocalChapterReadingContentUseCase: GetLocalChapterReadingContentUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val deleteChaptersUseCase: DeleteLocalChaptersUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase
)
