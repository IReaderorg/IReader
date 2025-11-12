package ireader.data.di

import ireader.data.remote.AutoSyncService
import ireader.data.remote.DebouncedProgressSync
import ireader.data.remote.NetworkConnectivityMonitor
import ireader.data.remote.RemoteCache
import ireader.data.remote.RetryPolicy
import ireader.data.remote.SupabaseConfig
import ireader.data.remote.SupabaseRemoteRepository
import ireader.data.remote.SyncQueue
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.loadRemoteConfig
import ireader.domain.usecases.remote.AuthenticateWithWalletUseCase
import ireader.domain.usecases.remote.GetCurrentUserUseCase
import ireader.domain.usecases.remote.GetReadingProgressUseCase
import ireader.domain.usecases.remote.ObserveConnectionStatusUseCase
import ireader.domain.usecases.remote.ObserveReadingProgressUseCase
import ireader.domain.usecases.remote.RemoteBackendUseCases
import ireader.domain.usecases.remote.SignOutUseCase
import ireader.domain.usecases.remote.SyncReadingProgressUseCase
import ireader.domain.usecases.remote.UpdateUsernameUseCase
import org.koin.dsl.module

/**
 * Koin module for remote backend dependencies
 * Provides Supabase client, repository implementation, and related services
 * 
 * Requirements: 1.5, 8.1, 8.2, 10.1, 10.3, 10.4, 11.3
 */
val remoteModule = module {
    
    // Remote configuration - keep as single since it's always available
    single {
        loadRemoteConfig()
    }
    
    // Supabase client (only created if config is available)
    // Using factory to avoid caching null when config is missing
    factory<io.github.jan.supabase.SupabaseClient?> {
        try {
            val config = getOrNull<ireader.domain.models.remote.RemoteConfig>()
            if (config != null) {
                SupabaseConfig.createClient(config.supabaseUrl, config.supabaseAnonKey)
            } else {
                println("⚠️ Supabase config not found - remote features will be disabled")
                null
            }
        } catch (e: Exception) {
            println("⚠️ Failed to create Supabase client: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    // Core components - keep as singletons since they're always created
    single { RetryPolicy() }
    single { SyncQueue(get()) }
    single { RemoteCache() }
    
    // Remote repository implementation (only if Supabase client is available)
    // Using factory instead of single to avoid caching null values
    factory<SupabaseRemoteRepository?> {
        val supabaseClient = getOrNull<io.github.jan.supabase.SupabaseClient>()
        supabaseClient?.let {
            SupabaseRemoteRepository(
                supabaseClient = it,
                syncQueue = get(),
                retryPolicy = get(),
                cache = get()
            )
        }
    }
    
    // Expose as RemoteRepository interface - use factory to avoid singleton null issue
    factory<RemoteRepository?> {
        getOrNull<SupabaseRemoteRepository>()
    }
    
    // Auto-sync service
    factory<AutoSyncService?> {
        val repository = getOrNull<SupabaseRemoteRepository>()
        val monitor = getOrNull<NetworkConnectivityMonitor>()
        if (repository != null && monitor != null) {
            AutoSyncService(monitor, repository)
        } else {
            null
        }
    }
    
    // Debounced progress sync
    factory<DebouncedProgressSync?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let {
            DebouncedProgressSync(
                syncOperation = { progress -> it.syncReadingProgress(progress) },
                delayMs = 2000
            )
        }
    }
    
    // Use cases (only created if repository is available)
    factory<AuthenticateWithWalletUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { AuthenticateWithWalletUseCase(it, get()) }
    }
    
    factory<GetCurrentUserUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { GetCurrentUserUseCase(it) }
    }
    
    factory<SignOutUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { SignOutUseCase(it) }
    }
    
    factory<UpdateUsernameUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { UpdateUsernameUseCase(it) }
    }
    
    factory<SyncReadingProgressUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { SyncReadingProgressUseCase(it, get()) }
    }
    
    factory<GetReadingProgressUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { GetReadingProgressUseCase(it) }
    }
    
    factory<ObserveReadingProgressUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { ObserveReadingProgressUseCase(it) }
    }
    
    factory<ObserveConnectionStatusUseCase?> {
        val repository = getOrNull<RemoteRepository>()
        repository?.let { ObserveConnectionStatusUseCase(it) }
    }
    
    // Use cases container (only if all use cases are available)
    factory<RemoteBackendUseCases?> {
        val auth = getOrNull<AuthenticateWithWalletUseCase>()
        val getCurrentUser = getOrNull<GetCurrentUserUseCase>()
        val signOut = getOrNull<SignOutUseCase>()
        val updateUsername = getOrNull<UpdateUsernameUseCase>()
        val syncProgress = getOrNull<SyncReadingProgressUseCase>()
        val getProgress = getOrNull<GetReadingProgressUseCase>()
        val observeProgress = getOrNull<ObserveReadingProgressUseCase>()
        val observeConnection = getOrNull<ObserveConnectionStatusUseCase>()
        
        if (auth != null && getCurrentUser != null && signOut != null && 
            updateUsername != null && syncProgress != null && getProgress != null &&
            observeProgress != null && observeConnection != null) {
            RemoteBackendUseCases(
                authenticateWithWallet = auth,
                getCurrentUser = getCurrentUser,
                signOut = signOut,
                updateUsername = updateUsername,
                syncReadingProgress = syncProgress,
                getReadingProgress = getProgress,
                observeReadingProgress = observeProgress,
                observeConnectionStatus = observeConnection
            )
        } else {
            null
        }
    }
}
