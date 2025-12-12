package ireader.domain.di

import ireader.domain.services.SyncManager
import ireader.domain.usecases.sync.GetSyncedDataUseCase
import ireader.domain.usecases.sync.SyncBooksUseCase
import org.koin.dsl.module

/**
 * Dependency injection module for sync-related components
 */
val syncModule = module {
    
    // Remote use cases
    single { ireader.domain.usecases.remote.SignUpUseCase(get()) }
    single { ireader.domain.usecases.remote.SignInUseCase(get()) }
    single { ireader.domain.usecases.remote.GetCurrentUserUseCase(get()) }
    single { ireader.domain.usecases.remote.SignOutUseCase(get()) }
    single { ireader.domain.usecases.remote.UpdateUsernameUseCase(get()) }
    single { ireader.domain.usecases.remote.UpdateEthWalletAddressUseCase(get()) }
    single { ireader.domain.usecases.remote.UpdatePasswordUseCase(get()) }
    single { ireader.domain.usecases.remote.SyncReadingProgressUseCase(get(),get()) }
    single { ireader.domain.usecases.remote.GetReadingProgressUseCase(get()) }
    single { ireader.domain.usecases.remote.ObserveReadingProgressUseCase(get()) }
    single { ireader.domain.usecases.remote.ObserveConnectionStatusUseCase(get()) }
    
    // Admin use cases
    single { ireader.domain.usecases.admin.GetPendingPaymentProofsUseCase(get<ireader.domain.data.repository.BadgeRepository>()) }
    single { ireader.domain.usecases.admin.VerifyPaymentProofUseCase(get<ireader.domain.data.repository.BadgeRepository>()) }
    
    // Remote backend use cases container
    single {
        ireader.domain.usecases.remote.RemoteBackendUseCases(
            signUp = get(),
            signIn = get(),
            getCurrentUser = get(),
            signOut = get(),
            updateUsername = get(),
            updateEthWalletAddress = get(),
            updatePassword = get(),
            syncReadingProgress = get(),
            getReadingProgress = get(),
            observeReadingProgress = get(),
            observeConnectionStatus = get()
        )
    }
    
    // Sync use cases
    single { SyncBooksUseCase(get()) }
    single { GetSyncedDataUseCase(get()) }
    single { ireader.domain.usecases.sync.FetchAndMergeSyncedBooksUseCase(get(), get(), get()) }
    single { ireader.domain.usecases.sync.RefreshLibraryFromRemoteUseCase(get(), get()) }
    single { ireader.domain.usecases.sync.SyncBookToRemoteUseCase(get(), get()) }
    single { ireader.domain.usecases.sync.SyncBooksToRemoteUseCase(get(), get()) }
    single { ireader.domain.usecases.sync.PerformFullSyncUseCase(get(), get(), get()) }
    single { ireader.domain.usecases.sync.ToggleBookInLibraryUseCase(get(), get(), get()) }
    single { ireader.domain.usecases.sync.IsUserAuthenticatedUseCase(get()) }
    
    // Sync use cases container
    single {
        ireader.domain.usecases.sync.SyncUseCases(
            syncBookToRemote = get(),
            syncBooksToRemote = get(),
            performFullSync = get(),
            refreshLibraryFromRemote = get(),
            toggleBookInLibrary = get(),
            fetchAndMergeSyncedBooks = get(),
            isUserAuthenticated = get()
        )
    }
    
    // Sync manager
    single { 
        SyncManager(
            remoteRepository = get(),
            supabasePreferences = get(),
            syncBooksUseCase = get(),
            getSyncedDataUseCase = get()
        )
    }
}
