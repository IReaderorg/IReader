package ireader.domain.services.download

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module for download dependencies.
 * 
 * Provides platform-specific implementations:
 * - AndroidDownloadProvider: Uses Android external storage
 * - AndroidDownloadStore: Uses SharedPreferences for queue persistence
 * - AndroidDownloadCache: Uses ConcurrentHashMap with database queries
 * - AndroidNetworkStateProvider: Uses ConnectivityManager
 */
val androidDownloadModule = module {
    
    // DownloadProvider - Android implementation using external storage
    single<DownloadProvider> {
        AndroidDownloadProvider(
            context = androidContext(),
            downloadPreferences = get()
        )
    }
    
    // DownloadStore - Android implementation using SharedPreferences
    single<DownloadStore> {
        AndroidDownloadStore(
            context = androidContext()
        )
    }
    
    // DownloadCache - Android implementation with database queries
    single<DownloadCache> {
        AndroidDownloadCache(
            chapterRepository = get()
        )
    }
    
    // NetworkStateProvider - Android implementation using ConnectivityManager
    single<NetworkStateProvider> {
        AndroidNetworkStateProvider(
            context = androidContext()
        )
    }
}
