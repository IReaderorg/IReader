package ireader.domain.usecases.remote

data class RemoteUseCases(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteChapters: GetRemoteChapters,
    val getRemoteBooks: GetRemoteBooksUseCase,
)
