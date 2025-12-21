package ireader.domain.di

import okio.Path.Companion.toPath
import ireader.domain.usecases.backup.ScheduleAutomaticBackup
import ireader.domain.usecases.backup.ScheduleAutomaticBackupImpl
import ireader.domain.usecases.backup.DropboxProvider
import ireader.domain.usecases.backup.GoogleDriveProvider
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.usecases.services.StartTTSServicesUseCase
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.usecases.translate.GoogleTranslateML
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.services.ExtensionWatcherService
import ireader.domain.services.tts.AITTSManager
import ireader.domain.js.engine.JSEngine
import ireader.domain.js.update.JSPluginUpdateScheduler
import ireader.domain.js.update.JSPluginUpdateNotifier
import ireader.domain.plugins.PluginClassLoader
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific domain module
 * 
 * Provides iOS implementations of platform-specific services and use cases.
 * 
 * ## Registered Components
 * 
 * ### Backup Services
 * - ScheduleAutomaticBackup: BGTaskScheduler-based automatic backup scheduling
 * - DropboxProvider: Dropbox HTTP API integration (requires OAuth token)
 * - GoogleDriveProvider: Google Drive REST API integration (requires OAuth token)
 * 
 * ### Background Services
 * - StartDownloadServicesUseCase: Background download service
 * - StartLibraryUpdateServicesUseCase: Background library update service
 * - StartTTSServicesUseCase: AVSpeechSynthesizer-based TTS service
 * - StartExtensionManagerService: No-op (JS plugins managed differently)
 * 
 * ### Translation
 * - GoogleTranslateML: Google Translate with rate limiting and retry logic
 * 
 * ### EPUB
 * - EpubCreator: EPUB creation using EpubBuilder
 * - ImportEpub: EPUB parsing with DEFLATE decompression
 * 
 * ### JavaScript Engine
 * - JSEngine: JavaScriptCore-based JS execution
 * - JSPluginUpdateScheduler: BGTaskScheduler-based plugin updates
 * - JSPluginUpdateNotifier: UserNotifications for update alerts
 * 
 * ### Other
 * - AITTSManager: AVSpeechSynthesizer for native iOS voices
 * - ExtensionWatcherService: No-op (not needed for JS plugins)
 * - PluginClassLoader: Throws UnsupportedOperationException (use JS plugins)
 */
actual val DomainModule: Module = module {
    // Include TTS v2 module for new clean TTS architecture
    includes(ireader.domain.services.tts_service.v2.ttsV2Module)
    
    // Process State Manager for handling process death
    single { ireader.domain.services.processstate.ProcessStateManager(get()) }
    
    // Backup scheduling
    single<ScheduleAutomaticBackup> { ScheduleAutomaticBackupImpl() }
    
    // Cloud providers
    // Note: These require OAuth tokens to be set via setAccessToken()
    // OAuth flow should be implemented in the presentation layer
    single { DropboxProvider() }
    single { GoogleDriveProvider() }
    
    // Background services
    // These services use KoinComponent to inject dependencies via Service Locator pattern
    // since expect/actual classes don't support constructor parameters in commonMain.
    // Full functionality is now available including:
    // - StartDownloadServicesUseCase: Uses runDownloadService with all required dependencies
    // - StartLibraryUpdateServicesUseCase: Uses runLibraryUpdateService with all required dependencies
    // - StartTTSServicesUseCase: Full TTS with chapter reading, paragraph navigation, and remote controls
    single { StartDownloadServicesUseCase() }
    single { StartLibraryUpdateServicesUseCase() }
    single { StartTTSServicesUseCase() }
    single { StartExtensionManagerService() }
    
    // Translation
    single { GoogleTranslateML() }
    
    // EPUB
    // EpubCreator uses KoinComponent to inject HttpClient and ChapterRepository automatically
    single { EpubCreator() }
    single { ImportEpub() }
    
    // JavaScript engine
    single { JSEngine() }
    single { JSPluginUpdateScheduler() }
    single { JSPluginUpdateNotifier() }
    
    // TTS
    single { AITTSManager() }
    
    // TTS Download Notification Helper
    single<ireader.domain.services.tts_service.TTSDownloadNotificationHelper> {
        ireader.domain.services.tts_service.TTSDownloadNotificationHelper(
            notificationManager = get()
        )
    }
    
    // TTS Chapter Download Manager with notification support
    single<ireader.domain.services.tts_service.TTSChapterDownloadManager> {
        ireader.domain.services.tts_service.TTSChapterDownloadManager(
            notificationHelper = get()
        )
    }
    
    // TTS Chapter Cache for storing downloaded chapter audio
    single<ireader.domain.services.tts_service.TTSChapterCache> {
        // iOS uses NSCachesDirectory for cache - get via NSSearchPathForDirectoriesInDomains
        val cachePaths = platform.Foundation.NSSearchPathForDirectoriesInDomains(
            platform.Foundation.NSCachesDirectory,
            platform.Foundation.NSUserDomainMask,
            true
        )
        val cachePathStr = (cachePaths.firstOrNull() as? String) ?: "/tmp"
        val fullPath = "$cachePathStr/tts_chapter_cache"
        ireader.domain.services.tts_service.TTSChapterCache(
            fileSystem = okio.FileSystem.SYSTEM,
            cacheDir = fullPath.toPath()
        )
    }
    
    // Extension services (stubs for iOS)
    single { ExtensionWatcherService() }
    single { PluginClassLoader() }
}
