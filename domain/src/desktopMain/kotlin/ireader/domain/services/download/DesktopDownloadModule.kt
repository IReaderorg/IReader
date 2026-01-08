package ireader.domain.services.download

import org.koin.dsl.module

/**
 * Desktop-specific Koin module for download dependencies.
 * 
 * Provides platform-specific implementations:
 * - DesktopDownloadProvider: Uses user home directory
 * - DesktopDownloadStore: Uses file-based storage in app config directory
 * - DesktopDownloadCache: Uses ConcurrentHashMap with filesystem scanning
 * - DesktopNetworkStateProvider: Simple connectivity check
 */
val desktopDownloadModule = module {
    
    // DownloadProvider - Desktop implementation using user home directory
    single<DownloadProvider> {
        DesktopDownloadProvider()
    }
    
    // DownloadStore - Desktop implementation using file-based storage
    single<DownloadStore> {
        DesktopDownloadStore()
    }
    
    // DownloadCache - Desktop implementation with filesystem scanning
    single<DownloadCache> {
        DesktopDownloadCache(
            chapterRepository = get()
        )
    }
    
    // NetworkStateProvider - Desktop implementation (simple connectivity check)
    single<NetworkStateProvider> {
        DesktopNetworkStateProvider()
    }
}
