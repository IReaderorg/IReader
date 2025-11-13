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
    single { ireader.domain.usecases.remote.SyncReadingProgressUseCase(get(),get()) }
    single { ireader.domain.usecases.remote.GetReadingProgressUseCase(get()) }
    single { ireader.domain.usecases.remote.ObserveReadingProgressUseCase(get()) }
    single { ireader.domain.usecases.remote.ObserveConnectionStatusUseCase(get()) }
    
    // Remote backend use cases container
    single {
        ireader.domain.usecases.remote.RemoteBackendUseCases(
            signUp = get(),
            signIn = get(),
            getCurrentUser = get(),
            signOut = get(),
            updateUsername = get(),
            updateEthWalletAddress = get(),
            syncReadingProgress = get(),
            getReadingProgress = get(),
            observeReadingProgress = get(),
            observeConnectionStatus = get()
        )
    }
    
    // Sync use cases
    single { SyncBooksUseCase(get()) }
    single { GetSyncedDataUseCase(get()) }
    
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
