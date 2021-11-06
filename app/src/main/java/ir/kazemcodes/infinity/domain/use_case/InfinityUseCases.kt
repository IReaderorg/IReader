package ir.kazemcodes.infinity.domain.use_case

import ir.kazemcodes.infinity.domain.use_case.local.*

data class InfinityUseCases(
    val getBooksUseCase: GetBooksUseCase,
    val getBookUseCase: GetBookUseCase,
    val deleteAllBooksUseCase: DeleteAllBooksUseCase,
    val deleteBookUseCase: DeleteBookUseCase,
    val addBookUserCase: AddBookUserCase
)
