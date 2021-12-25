package ir.kazemcodes.infinity.domain.use_cases.remote

data class RemoteUseCase(
    val getRemoteBookDetailUseCase: GetRemoteBookDetailUseCase,
    val getRemoteBooksUseCase: GetRemoteBooksUseCase,
    val getRemoteChaptersUseCase: GetRemoteChaptersUseCase,
    val getRemoteReadingContentUseCase: GetRemoteReadingContentUseCase,
    val getSearchedBooksUseCase: GetRemoteSearchBookUseCase,
)
