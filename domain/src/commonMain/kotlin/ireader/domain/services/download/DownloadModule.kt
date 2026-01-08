package ireader.domain.services.download

import org.koin.dsl.module

/**
 * Koin module for download-related dependencies.
 * 
 * This module provides:
 * - DownloadProvider: Platform-specific file system operations for downloads
 * - DownloadStore: Queue persistence across app restarts
 * - DownloadCache: Fast "is downloaded" checks using filesystem cache
 * - NetworkStateProvider: Network connectivity monitoring
 * - Downloader: Core download engine with parallel downloads, retry, etc.
 * - DownloadManager: Central coordinator for all download operations
 */
val downloadModule = module {
    
    // DownloadProvider - Platform-specific, registered in platform modules
    // See: AndroidDownloadProvider, DesktopDownloadProvider
    
    // DownloadStore - Platform-specific, registered in platform modules
    // See: AndroidDownloadStore, DesktopDownloadStore
    
    // DownloadCache - Platform-specific, registered in platform modules
    // See: AndroidDownloadCache, DesktopDownloadCache
    
    // NetworkStateProvider - Platform-specific, registered in platform modules
    // See: AndroidNetworkStateProvider, DesktopNetworkStateProvider
    
    // Downloader - Core download engine
    // Singleton to maintain download state across the app
    single {
        Downloader(
            bookRepository = get(),
            chapterRepository = get(),
            catalogStore = get(),
            remoteUseCases = get(),
            insertUseCases = get(),
            localizeHelper = get(),
            downloadPreferences = get(),
            downloadProvider = get(),
            downloadCache = get(),
            networkStateProvider = get()
        )
    }
    
    // DownloadManager - Central coordinator
    // Singleton to maintain queue state across the app
    single {
        DownloadManager(
            downloader = get(),
            downloadStore = get(),
            downloadCache = get(),
            downloadUseCases = get(),
            bookRepository = get(),
            chapterRepository = get()
        )
    }
}
