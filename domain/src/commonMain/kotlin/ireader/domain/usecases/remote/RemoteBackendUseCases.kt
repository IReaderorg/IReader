package ireader.domain.usecases.remote

/**
 * Container for all remote backend use cases related to Web3 authentication and sync
 */
data class RemoteBackendUseCases(
    val authenticateWithWallet: AuthenticateWithWalletUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val signOut: SignOutUseCase,
    val updateUsername: UpdateUsernameUseCase,
    val syncReadingProgress: SyncReadingProgressUseCase,
    val getReadingProgress: GetReadingProgressUseCase,
    val observeReadingProgress: ObserveReadingProgressUseCase,
    val observeConnectionStatus: ObserveConnectionStatusUseCase
)
