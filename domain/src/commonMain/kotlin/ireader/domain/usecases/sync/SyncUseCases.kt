package ireader.domain.usecases.sync

/**
 * Aggregate class for all sync-related use cases
 * Provides a single point of access for sync operations
 * 
 * Note: This class already exists in the codebase and is configured in DomainModules.
 * The new use cases (SyncLibraryUseCase, CheckSyncAvailabilityUseCase) extend the existing functionality.
 */
data class SyncUseCases(
    val syncBookToRemote: SyncBookToRemoteUseCase,
    val syncBooksToRemote: SyncBooksToRemoteUseCase,
    val performFullSync: PerformFullSyncUseCase,
    val refreshLibraryFromRemote: RefreshLibraryFromRemoteUseCase,
    val toggleBookInLibrary: ToggleBookInLibraryUseCase,
    val fetchAndMergeSyncedBooks: FetchAndMergeSyncedBooksUseCase,
    val isUserAuthenticated: IsUserAuthenticatedUseCase,
    
    // New use cases added for enhanced functionality
    val syncLibrary: SyncLibraryUseCase? = null,
    val checkSyncAvailability: CheckSyncAvailabilityUseCase? = null,
    val getSyncedData: GetSyncedDataUseCase? = null
) {
    /**
     * Check if sync functionality is available
     */
    fun isSyncAvailable(): Boolean {
        return checkSyncAvailability?.invoke() ?: (isUserAuthenticated != null)
    }
}
