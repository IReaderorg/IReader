package ireader.data.di

import io.github.jan.supabase.SupabaseClient
import ireader.data.remote.MultiSupabaseClientProvider
import ireader.data.remote.RemoteCache
import ireader.data.remote.RetryPolicy
import ireader.data.remote.SupabaseRemoteRepository
import ireader.data.remote.SyncQueue
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.data.repository.SupabaseClientProvider
import ireader.domain.models.remote.SupabaseEndpoint
import ireader.domain.preferences.prefs.SupabasePreferences
import org.koin.dsl.module

/**
 * Remote module for 7-project Supabase setup
 * 
 * This module provides the MultiSupabaseClientProvider that always uses
 * the 7-project database architecture (3.5GB total storage).
 * 
 * Users can configure all 7 projects individually or use the same URL for all.
 */
val remoteModule = module {
    
    // Multi-Project Supabase Client Provider (always enabled, reactive to runtime preference updates)
    single<SupabaseClientProvider> {
        MultiSupabaseClientProvider(preferences = get())
    }
    
    // Sync queue
    single { SyncQueue() }
    
    // Retry policy
    single { RetryPolicy() }
    
    // Remote cache
    single { RemoteCache() }
    
    // Backend Service (routes tables across projects dynamically)
    single<ireader.data.backend.BackendService> {
        val provider = get<SupabaseClientProvider>()
        if (provider is MultiSupabaseClientProvider) {
            ireader.data.backend.MultiProjectBackendService(provider)
        } else {
            ireader.data.backend.NoOpBackendService()
        }
    }
    
    // Auth Service (authentication abstraction)
    single<ireader.data.backend.AuthService> {
        val provider = get<SupabaseClientProvider>()
        if (provider is MultiSupabaseClientProvider) {
            ireader.data.backend.SupabaseAuthService { provider.authClient }
        } else {
            ireader.data.backend.NoOpAuthService()
        }
    }
    
    // Remote repository
    single<RemoteRepository> {
        val provider = get<SupabaseClientProvider>()
        if (provider is MultiSupabaseClientProvider) {
            SupabaseRemoteRepository(
                clientProvider = { provider.authClient },
                backendService = get(),
                syncQueue = get(),
                retryPolicy = get(),
                cache = get()
            )
        } else {
            ireader.data.remote.NoOpRemoteRepository()
        }
    }
    
    // Admin User repository for admin user management
    single<ireader.domain.data.repository.AdminUserRepository> {
        val provider = get<SupabaseClientProvider>()
        if (provider is MultiSupabaseClientProvider) {
            ireader.data.admin.AdminUserRepositoryImpl(
                clientProvider = { provider.authClient },
                backendService = get()
            )
        } else {
            ireader.data.admin.NoOpAdminUserRepository()
        }
    }
}
