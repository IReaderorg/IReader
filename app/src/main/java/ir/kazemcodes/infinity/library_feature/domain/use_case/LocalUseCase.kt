package ir.kazemcodes.infinity.library_feature.domain.use_case

data class LocalUseCase(
    val getLocalBooksUseCase: GetLocalBooksUseCase,
    val GetLocalBookUseCase: GetLocalBookUseCase,
    val deleteAllLocalBooksUseCase: DeleteAllLocalBooksUseCase,
    val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    val insertLocalBookUserCase: InsertLocalBookUserCase
)
