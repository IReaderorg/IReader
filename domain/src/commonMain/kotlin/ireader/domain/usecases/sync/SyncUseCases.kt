package ireader.domain.usecases.sync

/**
 * Container for all sync-related use cases
 * Provides a clean interface for sync operations following clean architecture
 */
data class SyncUseCases(
    val syncBookToRemote: SyncBookToRemoteUseCase,
    val syncBooksToRemote: SyncBooksToRemoteUseCase,
    val performFullSync: PerformFullSyncUseCase,
    val refreshLibraryFromRemote: RefreshLibraryFromRemoteUseCase,
    val toggleBookInLibrary: ToggleBookInLibraryUseCase,
    val fetchAndMergeSyncedBooks: FetchAndMergeSyncedBooksUseCase,
    val isUserAuthenticated: IsUserAuthenticatedUseCase
) {
    /**
     * Check if sync features are available (user is authenticated)
     */
    suspend fun isSyncAvailable(): Boolean {
        return isUserAuthenticated()
    }
}
