package ir.kazemcodes.infinity.core.domain.use_cases.remote

data class RemoteUseCase(
    val getRemoteBookDetailUseCase: GetRemoteBookDetailUseCase,
    val getRemoteLatestUpdateLatestBooksUseCase: GetRemoteLatestBooksUseCase,
    val getRemoteMostPopularBooksUseCase: GetRemoteMostPopularBooksUseCase,
    val getRemoteChaptersUseCase: GetRemoteChaptersUseCase,
    val getRemoteReadingContentUseCase: GetRemoteReadingContentUseCase,
    val getSearchedBooksUseCase: GetRemoteSearchBookUseCase,
)
