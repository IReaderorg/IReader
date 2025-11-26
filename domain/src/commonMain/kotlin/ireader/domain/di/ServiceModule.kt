package ireader.domain.di

import ireader.domain.services.common.*
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

/**
 * Koin module for platform-agnostic services
 */
val ServiceModule = module {
    
    // Core Services
    single<BackgroundTaskService> { 
        ServiceFactory.createBackgroundTaskService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<DownloadService> { 
        ServiceFactory.createDownloadService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<FileService> { 
        ServiceFactory.createFileService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<NotificationService> { 
        ServiceFactory.createNotificationService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<LibraryUpdateService> { 
        ServiceFactory.createLibraryUpdateService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<ExtensionService> { 
        ServiceFactory.createExtensionService().apply {
            runBlocking { initialize() }
        }
    }
    
    // TODO: Implement these services
    // single<BackupService> { ServiceFactory.createBackupService() }
    // single<TTSService> { ServiceFactory.createTTSService() }
    // single<SyncService> { ServiceFactory.createSyncService() }
    // single<CacheService> { ServiceFactory.createCacheService() }
}
