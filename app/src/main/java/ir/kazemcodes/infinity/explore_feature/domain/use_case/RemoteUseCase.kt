package ir.kazemcodes.infinity.explore_feature.domain.use_case

data class RemoteUseCase(
    val getRemoteBookDetailUseCase: GetRemoteBookDetailUseCase,
    val getRemoteBooksUseCase: GetRemoteBooksUseCase,
    val getRemoteChaptersUseCase: GetRemoteChaptersUseCase,
    val getRemoteReadingContentUseCase: GetRemoteReadingContentUseCase
)
