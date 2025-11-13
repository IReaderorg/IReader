package ireader.domain.usecases.remote

/**
 * Container for all remote backend use cases related to authentication and sync
 */
data class RemoteBackendUseCases(
    val signUp: SignUpUseCase,
    val signIn: SignInUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val signOut: SignOutUseCase,
    val updateUsername: UpdateUsernameUseCase,
    val updateEthWalletAddress: UpdateEthWalletAddressUseCase,
    val syncReadingProgress: SyncReadingProgressUseCase,
    val getReadingProgress: GetReadingProgressUseCase,
    val observeReadingProgress: ObserveReadingProgressUseCase,
    val observeConnectionStatus: ObserveConnectionStatusUseCase
)
